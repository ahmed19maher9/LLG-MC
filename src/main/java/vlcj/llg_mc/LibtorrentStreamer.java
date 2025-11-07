package vlcj.llg_mc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.frostwire.jlibtorrent.StorageMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.List;
import java.util.Arrays;
import java.util.function.Consumer;

import com.frostwire.jlibtorrent.AddTorrentParams;
import com.frostwire.jlibtorrent.AlertListener;
import com.frostwire.jlibtorrent.Priority;
import com.frostwire.jlibtorrent.SessionManager;
import com.frostwire.jlibtorrent.SessionParams;
import com.frostwire.jlibtorrent.SettingsPack;
import com.frostwire.jlibtorrent.TorrentHandle;
import com.frostwire.jlibtorrent.TorrentInfo;
import com.frostwire.jlibtorrent.TorrentStatus;
import com.frostwire.jlibtorrent.alerts.*;
import com.frostwire.jlibtorrent.swig.settings_pack.bool_types;
import com.frostwire.jlibtorrent.swig.settings_pack.int_types;

import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventListener;

public class LibtorrentStreamer implements AlertListener, AutoCloseable {
    @Override
    public int[] types() {
        // Return specific alert types we're interested in (without duplicates)
        return new int[] {
                // Torrent state alerts
                AlertType.ADD_TORRENT.swig(),
                AlertType.TORRENT_FINISHED.swig(),
                AlertType.TORRENT_REMOVED.swig(),
                AlertType.TORRENT_PAUSED.swig(),
                AlertType.TORRENT_RESUMED.swig(),
                AlertType.TORRENT_CHECKED.swig(),
                AlertType.TORRENT_ERROR.swig(),
                AlertType.FILE_ERROR.swig(),
                AlertType.METADATA_RECEIVED.swig(),
                AlertType.STATE_CHANGED.swig(),
                AlertType.STATE_UPDATE.swig(),

                // Tracker alerts
                AlertType.TRACKER_ANNOUNCE.swig(),
                AlertType.TRACKER_ERROR.swig(),
                AlertType.TRACKER_REPLY.swig(),
                AlertType.TRACKER_WARNING.swig(),
                AlertType.SCRAPE_REPLY.swig(),
                AlertType.SCRAPE_FAILED.swig(),

                // Peer alerts
                AlertType.PEER_BLOCKED.swig(),
                AlertType.PEER_BAN.swig(),
                AlertType.PEER_ERROR.swig(),
                AlertType.PEER_CONNECT.swig(),
                AlertType.PEER_DISCONNECTED.swig(),
                AlertType.INCOMING_CONNECTION.swig(),

                // Network alerts
                AlertType.LISTEN_SUCCEEDED.swig(),
                AlertType.LISTEN_FAILED.swig(),
                AlertType.PORTMAP.swig(),
                AlertType.PORTMAP_ERROR.swig(),
                AlertType.PORTMAP_LOG.swig(),
                AlertType.FASTRESUME_REJECTED.swig(),

                // DHT alerts
                AlertType.DHT_REPLY.swig(),
                AlertType.DHT_BOOTSTRAP.swig(),
                AlertType.DHT_ERROR.swig(),
                AlertType.DHT_IMMUTABLE_ITEM.swig(),
                AlertType.DHT_MUTABLE_ITEM.swig(),
                AlertType.DHT_PUT.swig(),
                AlertType.DHT_OUTGOING_GET_PEERS.swig(),
                AlertType.DHT_LOG.swig(),
                AlertType.DHT_PKT.swig(),
                AlertType.DHT_GET_PEERS.swig(),
                AlertType.DHT_ANNOUNCE.swig(),
                AlertType.DHT_SAMPLE_INFOHASHES.swig(),

                // Logging alerts
                AlertType.LOG.swig(),
                AlertType.TORRENT_LOG.swig(),
                AlertType.PEER_LOG.swig(),
                AlertType.LSD_PEER.swig(),
                AlertType.LSD_ERROR.swig()
        };
    }

    private final JFrame frame;
    private final EmbeddedMediaPlayerComponent mediaPlayerComponent;
    private static final Logger LOGGER = Logger.getLogger(LibtorrentStreamer.class.getName());
    private SessionManager sessionManager;
    private final Object handleLock = new Object();
    private volatile TorrentHandle torrentHandle;
    private final DefaultListModel<String> contentsModel = new DefaultListModel<>();
    private final JList<String> contentsList;
    private volatile boolean metadataReceived = false;
    private TorrentHttpServer httpServer;
    private final boolean streamingMode = true; // Always use streaming mode when embedded
    private File torrentFile;
    private final JPanel torrentContentPanel; // Panel to display torrent contents
    private final JPanel torrentMediaPanel; // Panel to display media player
    private Consumer<List<vlcj.llg_mc.App.TorrentFileEntry>> metadataCallback;

    public LibtorrentStreamer(JFrame frame, JPanel torrentContentPanel, JPanel torrentMediaPanel) {
        this.frame = frame;
        this.torrentContentPanel = torrentContentPanel;
        this.torrentMediaPanel = torrentMediaPanel;

        // Initialize HTTP server for streaming
        try {
            this.httpServer = new TorrentHttpServer();
            this.httpServer.start();
            LOGGER.info("HTTP server started for torrent streaming");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to start HTTP server: " + e.getMessage(), e);
            JOptionPane.showMessageDialog(frame,
                    "Warning: HTTP server failed to start. Streaming may not work properly.",
                    "Server Warning",
                    JOptionPane.WARNING_MESSAGE);
        }

        // Initialize media player component
        this.mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        this.mediaPlayerComponent.mediaPlayer().events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void finished(MediaPlayer mediaPlayer) {
                // Handle playback finished
            }

            @Override
            public void error(MediaPlayer mediaPlayer) {
                LOGGER.log(Level.SEVERE, "Media player error");
                JOptionPane.showMessageDialog(frame,
                        "Error playing media",
                        "Playback Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // Initialize contents list
        this.contentsList = new JList<>(contentsModel);
        this.contentsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.contentsList.setOpaque(false);
        this.contentsList.setForeground(Color.WHITE);

        // Add double-click handler for streaming
        this.contentsList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int index = contentsList.locationToIndex(evt.getPoint());
                    if (index >= 0) {
                        playSelectedFile(index);
                    }
                }
            }
        });

        // Initialize UI
        initializeUI();
        initializeSession();
    }

    /**
     * Sets a callback to be invoked when torrent metadata is received.
     *
     * @param callback The callback to invoke with the list of torrent file entries
     */
    public void setMetadataCallback(Consumer<List<vlcj.llg_mc.App.TorrentFileEntry>> callback) {
        this.metadataCallback = callback;
    }

    private void initializeUI() {
        // Set up the content panel
        torrentContentPanel.setLayout(new BorderLayout());

        // Add the contents list to the provided panel
        JScrollPane contentsScroll = new JScrollPane(contentsList);
        torrentContentPanel.add(contentsScroll, BorderLayout.CENTER);

        // Set up the media panel
        torrentMediaPanel.setLayout(new BorderLayout());
        torrentMediaPanel.add(mediaPlayerComponent, BorderLayout.CENTER);
    }

    private synchronized void initializeSession() {
        if (sessionManager != null && sessionManager.isRunning()) {
            return; // Already initialized
        }

        // Enable detailed logging
        System.setProperty("jlibtorrent.log.mask", "0xffffffff");
        System.setProperty("jlibtorrent.log.dht", "true");
        System.setProperty("jlibtorrent.log.tracker", "true");
        System.setProperty("jlibtorrent.log.peer", "true");
        System.setProperty("jlibtorrent.log.torrent", "true");

        try {
            // Create and configure settings
            SettingsPack settings = new SettingsPack();

            // Enable DHT, LSD, UPnP for better peer discovery
            settings.setBoolean(bool_types.enable_dht.swigValue(), true);
            settings.setBoolean(bool_types.enable_lsd.swigValue(), true);
            settings.setBoolean(bool_types.enable_upnp.swigValue(), true);

            // Optimize for streaming
            settings.setInteger(int_types.connection_speed.swigValue(), 100);
            settings.setInteger(int_types.active_downloads.swigValue(), 3);
            settings.setInteger(int_types.active_seeds.swigValue(), 3);
            settings.setInteger(int_types.active_limit.swigValue(), 15);
            settings.setInteger(int_types.download_rate_limit.swigValue(), 0); // 0 means unlimited
            settings.setInteger(int_types.upload_rate_limit.swigValue(), 0); // 0 means unlimited

            // Configure peer and connection settings
            settings.setInteger(int_types.max_failcount.swigValue(), 10);
            settings.setInteger(int_types.request_timeout.swigValue(), 30);
            settings.setInteger(int_types.peer_connect_timeout.swigValue(), 30);
            settings.setInteger(int_types.connection_speed.swigValue(), 50); // Number of connection attempts per second

            // Create session params and apply settings
            SessionParams params = new SessionParams();
            params.setSettings(settings);

            // Create and start the session
            sessionManager = new SessionManager();
            sessionManager.start(params);

            // Start the DHT
            sessionManager.startDht();

            // Add this class as an alert listener
            sessionManager.addListener(this);

            LOGGER.info("Libtorrent session started successfully with DHT enabled");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize libtorrent session: " + e.getMessage(), e);
            JOptionPane.showMessageDialog(frame,
                    "Failed to initialize torrent session: " + e.getMessage(),
                    "Torrent Error",
                    JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException("Failed to initialize torrent session", e);
        }

        // No need to set up UI components here as they're now handled in initializeUI()
    }

    @Override
    public synchronized void close() {
        cleanup();
    }

    public synchronized void cleanup() {
        LOGGER.info("Cleaning up resources...");

        // Stop the HTTP server if running
        if (httpServer != null) {
            try {
                httpServer.stop();
                LOGGER.info("HTTP server stopped");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error stopping HTTP server: " + e.getMessage(), e);
            }
        }

        // Clean up the torrent session
        if (sessionManager != null) {
            try {
                // Remove the torrent if it exists
                if (torrentHandle != null && isHandleValid(torrentHandle)) {
                    try {
                        sessionManager.remove(torrentHandle);
                        LOGGER.info("Removed torrent handle");
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Error removing torrent handle: " + e.getMessage(), e);
                    } finally {
                        torrentHandle = null;
                    }
                }

                // Stop the session
                if (sessionManager.isRunning()) {
                    sessionManager.pause();
                    sessionManager.stop();
                    LOGGER.info("Libtorrent session stopped");
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error cleaning up session manager: " + e.getMessage(), e);
            } finally {
                sessionManager = null;
            }
        }

        return;
    }

    /**
     * Plays the selected file from the torrent.
     * 
     * @param index The index of the file to play
     */
    private void playSelectedFile(int index) {
        TorrentHandle handle = getValidTorrentHandle();
        if (handle == null) {
            LOGGER.warning("Cannot play file: No valid torrent handle");
            return;
        }

        // Get the torrent info safely
        TorrentInfo info = handle.torrentFile();
        if (info == null) {
            LOGGER.warning("Cannot play file: Failed to get torrent info");
            return;
        }

        if (index < 0 || index >= info.numFiles()) {
            LOGGER.warning(String.format("Cannot play file: Invalid file index %d (valid range: 0-%d)",
                    index, info.numFiles() - 1));
            return;
        }

        // Get file information
        String fileName = info.files().filePath(index);
        File saveDir = new File("downloads");
        File mediaFile = new File(saveDir, fileName);

        // Ensure the file's directory exists
        if (!mediaFile.getParentFile().exists()) {
            if (!mediaFile.getParentFile().mkdirs()) {
                LOGGER.warning("Failed to create directory: " + mediaFile.getParentFile().getAbsolutePath());
                return;
            }
        }

        try {
            // Set priority to download only the selected file
            for (int i = 0; i < info.numFiles(); i++) {
                try {
                    if (i == index) {
                        handle.filePriority(i, Priority.SEVEN); // Download selected file with highest priority
                        LOGGER.info("Setting high priority for file: " + info.files().filePath(i));
                    } else {
                        handle.filePriority(i, Priority.IGNORE); // Ignore all other files
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error setting priority for file " + i +
                            " (" + info.files().filePath(i) + "): " + e.getMessage());
                    // Continue with next file even if one fails
                }
            }

            // Force recheck to apply priority changes
            try {
                handle.forceRecheck();
                LOGGER.info("Forced recheck of torrent files");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error forcing recheck: " + e.getMessage(), e);
                // Continue anyway as the priority might still be set
            }

            // Wait for the file to be available with timeout
            int retries = 0;
            final int MAX_RETRIES = 10; // 5 seconds total (500ms * 10)
            while (!mediaFile.exists() && retries < MAX_RETRIES) {
                try {
                    Thread.sleep(500);
                    retries++;

                    // Check if handle is still valid
                    if (!isHandleValid(handle)) {
                        LOGGER.warning("Torrent handle became invalid while waiting for file");
                        return;
                    }

                    LOGGER.fine(String.format("Waiting for file %s (attempt %d/%d)",
                            mediaFile.getName(), retries, MAX_RETRIES));

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.warning("Interrupted while waiting for file to be available");
                    return;
                }
            }

            if (!mediaFile.exists()) {
                LOGGER.warning("File not available for playback: " + mediaFile.getAbsolutePath());
                return;
            }

            // Start playback of the media file
            startPlayback(handle);

            if (streamingMode) {
                // In streaming mode, use HTTP server
                if (httpServer != null) {
                    try {
                        httpServer.setFile(mediaFile);
                        String streamUrl = httpServer.getStreamUrl();
                        LOGGER.info("Starting playback from URL: " + streamUrl);
                        mediaPlayerComponent.mediaPlayer().media().play(streamUrl);
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Error starting HTTP server or playback", e);
                    }
                } else {
                    LOGGER.warning("HTTP server not initialized");
                }
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in playSelectedFile: " + e.getMessage(), e);
            JOptionPane.showMessageDialog(frame,
                    "Error playing file: " + e.getMessage(),
                    "Playback Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadTorrentFile(File torrentFile) {
        if (torrentFile == null || !torrentFile.exists()) {
            JOptionPane.showMessageDialog(frame, "Torrent file does not exist", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            LOGGER.info("Loading torrent file: " + torrentFile.getAbsolutePath());
            this.torrentFile = torrentFile;

            // Clear previous torrent data
            contentsModel.clear();
            metadataReceived = false;

            // Clear previous torrent if any
            if (torrentHandle != null && torrentHandle.isValid()) {
                try {
                    sessionManager.remove(torrentHandle);
                    LOGGER.info("Removed previous torrent handle");
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error removing previous torrent handle: " + e.getMessage(), e);
                } finally {
                    torrentHandle = null;
                }
            }

            // Parse the torrent file
            TorrentInfo ti;
            try {
                ti = new TorrentInfo(torrentFile);
            } catch (Exception e) {
                String errorMsg = "Failed to parse torrent file: " + e.getMessage();
                LOGGER.severe(errorMsg);
                JOptionPane.showMessageDialog(frame, "Invalid torrent file: " + e.getMessage(), "Torrent Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            LOGGER.info(String.format("Torrent Info - Name: %s, Size: %s, Files: %d",
                    ti.name(), humanSize(ti.totalSize()), ti.numFiles()));

            // Create download directory if it doesn't exist
            File downloadDir = new File("./downloads");
            if (!downloadDir.exists() && !downloadDir.mkdirs()) {
                LOGGER.warning("Failed to create download directory: " + downloadDir.getAbsolutePath());
            }

            LOGGER.info("Starting torrent download to: " + downloadDir.getAbsolutePath());

            // Start the download
            sessionManager.download(ti, downloadDir);

            // Try to get the torrent handle with retries
            int maxRetries = 5;
            int retryDelayMs = 200; // 200ms between retries

            for (int i = 0; i < maxRetries; i++) {
                try {
                    // Try to get the handle using the info hash
                    torrentHandle = sessionManager.find(ti.infoHashV1());

                    if (torrentHandle != null && torrentHandle.isValid()) {
                        break;
                    }

                    // If not found, wait a bit and try again
                    if (i < maxRetries - 1) {
                        Thread.sleep(retryDelayMs);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING,
                            "Error while getting torrent handle (attempt " + (i + 1) + "): " + e.getMessage());
                    if (i == maxRetries - 1) {
                        String errorMsg = "Failed to get torrent handle after " + maxRetries + " attempts: "
                                + e.getMessage();
                        LOGGER.severe(errorMsg);
                        JOptionPane.showMessageDialog(frame, errorMsg, "Torrent Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    Thread.sleep(retryDelayMs);
                }
            }

            if (torrentHandle == null || !torrentHandle.isValid()) {
                String errorMsg = "Failed to get torrent handle. The torrent file might be invalid or corrupted.";
                LOGGER.severe(errorMsg);
                JOptionPane.showMessageDialog(frame, errorMsg, "Torrent Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            LOGGER.info("Successfully created torrent handle for: " + ti.name());

            // Set the download priority for all files to normal priority
            for (int i = 0; i < ti.numFiles(); i++) {
                torrentHandle.filePriority(i, Priority.NORMAL);
            }

            // Wait for metadata if needed (with a timeout)
            TorrentStatus status = getSafeStatus(torrentHandle);
            if (status == null || !status.hasMetadata()) {
                LOGGER.info("Waiting for metadata...");
                int maxWaitTime = 15; // seconds
                long startTime = System.currentTimeMillis();

                while (torrentHandle != null && (status = getSafeStatus(torrentHandle)) != null &&
                        !status.hasMetadata() &&
                        (System.currentTimeMillis() - startTime) < (maxWaitTime * 1000)) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }

                if (torrentHandle == null || status == null || !status.hasMetadata()) {
                    String errorMsg = "Failed to load torrent metadata. Possible reasons:\n" +
                            "1. No peers found with the metadata\n" +
                            "2. Network connectivity issues\n" +
                            "3. Firewall blocking BitTorrent traffic";
                    LOGGER.warning(errorMsg);
                    JOptionPane.showMessageDialog(frame, errorMsg, "Torrent Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // Update UI with file list on the EDT
            SwingUtilities.invokeLater(() -> {
                TorrentInfo torrentInfo = torrentHandle.torrentFile();
                if (torrentInfo != null) {
                    contentsModel.clear();
                    for (int i = 0; i < torrentInfo.numFiles(); i++) {
                        contentsModel.addElement(torrentInfo.files().filePath(i));
                    }
                }
            });

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading torrent: " + e.getMessage(), e);
            JOptionPane.showMessageDialog(frame,
                    "Error loading torrent: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Returns a list of TorrentFileEntry objects representing the files in the
     * current torrent.
     * 
     * @return List of TorrentFileEntry objects, or null if no torrent is loaded
     */
    public java.util.List<vlcj.llg_mc.App.TorrentFileEntry> getTorrentFiles() {
        if (torrentHandle == null || !torrentHandle.isValid()) {
            return null;
        }

        TorrentStatus status = getSafeStatus(torrentHandle);
        if (status == null || !status.hasMetadata()) {
            return null;
        }

        TorrentInfo torrentInfo = torrentHandle.torrentFile();
        if (torrentInfo == null) {
            return null;
        }

        java.util.List<vlcj.llg_mc.App.TorrentFileEntry> entries = new java.util.ArrayList<>();
        int numFiles = torrentInfo.numFiles();

        for (int i = 0; i < numFiles; i++) {
            com.frostwire.jlibtorrent.FileStorage fs = torrentInfo.files();
            String fileName = fs.fileName(i);
            long fileSize = fs.fileSize(i);
            String filePath = fs.filePath(i);
            entries.add(new vlcj.llg_mc.App.TorrentFileEntry(fileName, fileSize, filePath));
        }

        return entries;
    }

    public void loadMagnetLink(String magnetLink) {
        if (magnetLink == null || magnetLink.trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please provide a valid magnet link", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Ensure the session is initialized
        initializeSession();

        if (sessionManager == null || !sessionManager.isRunning()) {
            JOptionPane.showMessageDialog(frame, "Failed to initialize torrent session", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Clear previous torrent if any
            if (torrentHandle != null && torrentHandle.isValid()) {
                try {
                    sessionManager.remove(torrentHandle);
                    LOGGER.info("Removed previous torrent handle");
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error removing previous torrent handle: " + e.getMessage(), e);
                } finally {
                    torrentHandle = null;
                    metadataReceived = false;
                }
            }

            // Clear the contents list on the EDT
            SwingUtilities.invokeLater(() -> contentsModel.clear());

            LOGGER.info("Loading magnet link: " + magnetLink);

            // Create download directory if it doesn't exist
            File downloadDir = new File("./downloads");
            if (!downloadDir.exists() && !downloadDir.mkdirs()) {
                LOGGER.warning("Failed to create download directory: " + downloadDir.getAbsolutePath());
            }

            LOGGER.info("Starting magnet download to: " + downloadDir.getAbsolutePath());

            // For magnet links, we need to create a TorrentInfo from the magnet URI
            // This is a simplified approach - in practice, magnet links need metadata
            // which is downloaded from peers. For now, we'll use the existing approach
            // where metadata is fetched via HTTP endpoints.

            // Since the App.java already handles magnet link metadata fetching,
            // we'll just set a flag indicating magnet support is not fully implemented
            // in this version of jlibtorrent
            JOptionPane.showMessageDialog(frame,
                    "Magnet link support requires metadata fetching.\n" +
                            "Please use the 'Load Magnet' button in the main torrent panel instead.",
                    "Feature Not Available",
                    JOptionPane.INFORMATION_MESSAGE);
            LOGGER.info("Magnet link requested but delegated to App.java implementation: " + magnetLink);
            return;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading magnet link: " + e.getMessage(), e);
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame,
                    "Error loading magnet link: " + e.getMessage(),
                    "Magnet Error",
                    JOptionPane.ERROR_MESSAGE));
        }
    }

    public void chooseAndLoadTorrent() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Torrent Files", "torrent"));

        int returnValue = fileChooser.showOpenDialog(frame);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                // Clear previous torrent if any
                if (torrentHandle != null) {
                    sessionManager.remove(torrentHandle);
                    torrentHandle = null;
                }

                // Clear the contents list
                contentsModel.clear();

                // Load the new torrent file
                try {
                    LOGGER.info("Loading torrent file: " + selectedFile.getAbsolutePath());
                    byte[] torrentBytes = Files.readAllBytes(selectedFile.toPath());
                    TorrentInfo ti = new TorrentInfo(torrentBytes);

                    LOGGER.info("Torrent name: " + ti.name());
                    LOGGER.info("Number of files: " + ti.numFiles());
                    LOGGER.info("Total size: " + humanSize(ti.totalSize()));

                    // Store the torrent info for later use
                    torrentFile = selectedFile;

                    // Start the download
                    LOGGER.info("Starting torrent download...");
                    sessionManager.download(ti, selectedFile.getParentFile());

                    // Get the torrent handle from the alert handler
                    int waitCount = 0;
                    while ((torrentHandle == null || !torrentHandle.isValid()) && waitCount < 30) {
                        try {
                            Thread.sleep(100);
                            waitCount++;
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }

                    if (torrentHandle == null || !torrentHandle.isValid()) {
                        throw new RuntimeException(
                                "Failed to get valid torrent handle after " + waitCount / 10 + " seconds");
                    }

                    // The TorrentHandle will be set in the alert handler when the torrent is added
                    metadataReceived = false;

                    // Wait for metadata with longer timeout (30 seconds)
                    int attempts = 0;
                    final int MAX_ATTEMPTS = 30; // 30 seconds total wait time

                    LOGGER.info("Waiting for metadata (max " + MAX_ATTEMPTS + " seconds)...");
                    while (!metadataReceived && attempts < MAX_ATTEMPTS) {
                        try {
                            Thread.sleep(1000); // Check every second
                            attempts++;
                            if (attempts % 2 == 0) { // Log every 2 seconds
                                LOGGER.info("Waiting for metadata... (" + attempts + "s)");
                            }

                            // Check if we have a valid torrent handle
                            if (torrentHandle != null && torrentHandle.isValid()) {
                                TorrentStatus status = getSafeStatus(torrentHandle);
                                if (status != null) {
                                    LOGGER.fine("Torrent status - " +
                                            "State: " + status.state() +
                                            ", Peers: " + status.numPeers() +
                                            ", Seeds: " + status.numSeeds() +
                                            ", Progress: " + (status.progressPpm() / 10000.0) + "%");
                                }
                            }

                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            LOGGER.warning("Metadata wait interrupted");
                            break;
                        }
                    }

                    if (!metadataReceived) {
                        String errorMsg = "Timed out waiting for torrent metadata after " + MAX_ATTEMPTS + " seconds\n"
                                +
                                "No valid torrent handle available. Possible reasons:\n" +
                                "1. Torrent file is invalid or corrupted\n" +
                                "2. Network connectivity issues\n" +
                                "3. Firewall blocking BitTorrent traffic";
                        LOGGER.warning(errorMsg);
                        JOptionPane.showMessageDialog(frame,
                                errorMsg,
                                "Torrent Error",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Error reading torrent file: " + selectedFile, e);
                    return;
                }

                // Update UI with file list
                TorrentInfo ti = torrentHandle.torrentFile();
                if (ti != null) {
                    for (int i = 0; i < ti.numFiles(); i++) {
                        contentsModel.addElement(ti.files().filePath(i));
                    }
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame,
                        "Error loading torrent: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                LOGGER.log(Level.SEVERE, "Error loading torrent", e);
            }
        }
    }

    public void start() {
        initializeSession();
        LOGGER.info("Libtorrent streamer started with alert listener");
    }

    /**
     * Loads a torrent file and starts downloading it.
     * 
     * @param torrentFile The .torrent file to load
     * @param fileToPlay  The specific file within the torrent to play (can be null
     *                    to play first media file)
     */
    public void loadTorrent(File torrentFile, String fileToPlay) {
        if (torrentFile == null || !torrentFile.exists()) {
            String errorMsg = "Torrent file does not exist: " +
                    (torrentFile != null ? torrentFile.getAbsolutePath() : "null");
            LOGGER.warning(errorMsg);
            SwingUtilities.invokeLater(
                    () -> JOptionPane.showMessageDialog(frame, errorMsg, "Torrent Error", JOptionPane.ERROR_MESSAGE));
            return;
        }

        // Ensure the session is initialized
        initializeSession();

        if (sessionManager == null || !sessionManager.isRunning()) {
            String errorMsg = "Failed to initialize torrent session";
            LOGGER.severe(errorMsg);
            SwingUtilities.invokeLater(
                    () -> JOptionPane.showMessageDialog(frame, errorMsg, "Torrent Error", JOptionPane.ERROR_MESSAGE));
            return;
        }

        try {
            // Clear previous torrent if any
            if (torrentHandle != null && torrentHandle.isValid()) {
                try {
                    sessionManager.remove(torrentHandle);
                    LOGGER.info("Removed previous torrent handle");
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error removing previous torrent handle: " + e.getMessage(), e);
                } finally {
                    torrentHandle = null;
                    metadataReceived = false;
                }
            }

            // Clear the contents list on the EDT
            SwingUtilities.invokeLater(() -> contentsModel.clear());

            // Load the torrent file
            LOGGER.info("Loading torrent file: " + torrentFile.getAbsolutePath());
            byte[] torrentBytes = Files.readAllBytes(torrentFile.toPath());
            TorrentInfo ti = new TorrentInfo(torrentBytes);

            if (ti == null) {
                String errorMsg = "Failed to parse torrent file: Invalid or corrupted torrent file";
                LOGGER.severe(errorMsg);
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, errorMsg, "Torrent Error",
                        JOptionPane.ERROR_MESSAGE));
                return;
            }

            LOGGER.info(String.format("Torrent Info - Name: %s, Size: %s, Files: %d",
                    ti.name(), humanSize(ti.totalSize()), ti.numFiles()));

            // Store the torrent file reference
            this.torrentFile = torrentFile;

            // Add public trackers for better peer discovery
            List<String> trackers = Arrays.asList(
                    "udp://tracker.opentrackr.org:1337/announce",
                    "udp://tracker.openbittorrent.com:6969/announce",
                    "udp://tracker.coppersurfer.tk:6969/announce",
                    "udp://tracker.leechers-paradise.org:6969/announce",
                    "http://tracker1.itzmx.com:8080/announce",
                    "udp://open.stealth.si:80/announce",
                    "udp://tracker.cyberia.is:6969/announce");

            // Add each tracker to the TorrentInfo
            for (String tracker : trackers) {
                try {
                    ti.addTracker(tracker);
                    LOGGER.fine("Added tracker: " + tracker);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Failed to add tracker " + tracker + ": " + e.getMessage());
                }
            }

            // Create download directory if it doesn't exist
            File downloadDir = new File("./downloads");
            if (!downloadDir.exists() && !downloadDir.mkdirs()) {
                LOGGER.warning("Failed to create download directory: " + downloadDir.getAbsolutePath());
            }

            LOGGER.info("Starting torrent download to: " + downloadDir.getAbsolutePath());

            // Start the download
            sessionManager.download(ti, downloadDir);

            // Try to get the torrent handle with retries
            int maxRetries = 5;
            int retryDelayMs = 200; // 200ms between retries

            for (int i = 0; i < maxRetries; i++) {
                try {
                    // Try to get the handle using the info hash
                    torrentHandle = sessionManager.find(ti.infoHashV1());

                    if (torrentHandle != null && torrentHandle.isValid()) {
                        break;
                    }

                    // If not found, wait a bit and try again
                    if (i < maxRetries - 1) {
                        Thread.sleep(retryDelayMs);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING,
                            "Error while getting torrent handle (attempt " + (i + 1) + "): " + e.getMessage());
                    if (i == maxRetries - 1) {
                        String errorMsg = "Failed to get torrent handle after " + maxRetries + " attempts: "
                                + e.getMessage();
                        LOGGER.severe(errorMsg);
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, errorMsg, "Torrent Error",
                                JOptionPane.ERROR_MESSAGE));
                        return;
                    }
                    Thread.sleep(retryDelayMs);
                }
            }

            if (torrentHandle == null || !torrentHandle.isValid()) {
                String errorMsg = "Failed to get torrent handle. The torrent file might be invalid or corrupted.";
                LOGGER.severe(errorMsg);
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, errorMsg, "Torrent Error",
                        JOptionPane.ERROR_MESSAGE));
                return;
            }

            LOGGER.info("Successfully created torrent handle for: " + ti.name());

            // Set the download priority for all files to normal priority
            // This ensures we don't skip any files in the torrent
            for (int i = 0; i < ti.numFiles(); i++) {
                torrentHandle.filePriority(i, Priority.NORMAL);
            }

            // Wait for metadata if needed (with a timeout)
            TorrentStatus status = getSafeStatus(torrentHandle);
            if (torrentHandle == null || status == null) {
                LOGGER.severe("Failed to load torrent: Torrent handle or status is null");
                throw new IllegalStateException("Failed to initialize torrent handle");
            }

            if (!status.hasMetadata()) {
                LOGGER.info("Waiting for metadata...");
                int maxWaitTime = 15; // seconds
                long startTime = System.currentTimeMillis();

                while (torrentHandle != null && (status = getSafeStatus(torrentHandle)) != null &&
                        !status.hasMetadata() &&
                        (System.currentTimeMillis() - startTime) < (maxWaitTime * 1000)) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }

                if (torrentHandle == null || status == null || !status.hasMetadata()) {
                    String errorMsg = "Failed to load torrent metadata. Possible reasons:\n" +
                            "1. No peers found with the metadata\n" +
                            "2. Network connectivity issues\n" +
                            "3. Firewall blocking BitTorrent traffic";
                    LOGGER.warning(errorMsg);
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, errorMsg, "Torrent Error",
                            JOptionPane.ERROR_MESSAGE));
                    return;
                }
            }

            // Update UI with file list on the EDT
            SwingUtilities.invokeLater(() -> {
                TorrentInfo torrentInfo = torrentHandle.torrentFile();
                if (torrentInfo != null) {
                    contentsModel.clear();
                    for (int i = 0; i < torrentInfo.numFiles(); i++) {
                        contentsModel.addElement(torrentInfo.files().filePath(i));
                    }
                }

                // If a specific file was requested to play, find and play it
                if (fileToPlay != null && !fileToPlay.isEmpty()) {
                    for (int i = 0; i < contentsModel.size(); i++) {
                        if (contentsModel.get(i).equals(fileToPlay)) {
                            playSelectedFile(i);
                            break;
                        }
                    }
                }
            });

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading torrent: " + e.getMessage(), e);
            JOptionPane.showMessageDialog(frame,
                    "Error loading torrent: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // This constructor is kept for backward compatibility but should be avoided
    public LibtorrentStreamer() {
        this(new JFrame("vlcj Torrent Streamer"), new JPanel(), new JPanel());
        frame.setBounds(100, 100, 1200, 720);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    /**
     * Safely gets the current torrent handle with validation.
     *
     * @return The current torrent handle if valid, or null if not available or
     *         invalid
     */
    TorrentHandle getValidTorrentHandle() {
        TorrentHandle handle = this.torrentHandle;
        if (handle == null) {
            LOGGER.warning("Torrent handle is null");
            return null;
        }

        try {
            if (!handle.isValid()) {
                LOGGER.warning("Torrent handle is no longer valid");
                return null;
            }

            // Check if we can get the status without throwing an exception
            TorrentStatus status = handle.status();
            if (status == null) {
                LOGGER.warning("Failed to get torrent status");
                return null;
            }

            return handle;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error validating torrent handle: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Safely checks if a torrent handle is valid, catching any exceptions that
     * might occur during validation.
     * This prevents crashes when the handle becomes corrupted or invalid during
     * torrent completion.
     *
     * @param handle The TorrentHandle to validate
     * @return true if the handle is valid and safe to use, false otherwise
     */
    private boolean isHandleValid(TorrentHandle handle) {
        if (handle == null) {
            return false;
        }
        try {
            return handle.isValid();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Exception during handle validation: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Safely get torrent status with proper error handling.
     * This method prevents crashes by catching native exceptions.
     * 
     * @param handle The torrent handle to get status from
     * @return TorrentStatus if successful, null if handle is invalid or error
     *         occurs
     */
    private TorrentStatus getSafeStatus(TorrentHandle handle) {
        if (handle == null) {
            return null;
        }
        try {
            // Double-check validity before calling status()
            if (!handle.isValid()) {
                LOGGER.fine("Torrent handle is invalid, cannot get status");
                return null;
            }

            // Get status - this is where crashes can occur
            TorrentStatus status = handle.status();
            return status;
        } catch (Exception e) {
            // Catch all exceptions including native crashes
            LOGGER.log(Level.WARNING, "Error getting torrent status (handle may be invalid): " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Safely gets the current torrent handle with validation.
     * 
     * @return The current torrent handle if valid, or null if not available or
     *         invalid
     */
    private TorrentHandle getTorrentHandle() {
        synchronized (handleLock) {
            if (torrentHandle == null || !torrentHandle.isValid()) {
                return null;
            }
            return torrentHandle;
        }
    }

    @Override
    public void alert(Alert<?> alert) {
        try {
            // Log all alerts at FINE level
            LOGGER.fine("Received alert: " + alert.getClass().getSimpleName() +
                    " - " + alert.message());

            // Handle different types of alerts with comprehensive error handling
            if (alert instanceof TorrentCheckedAlert) {
                TorrentCheckedAlert taa = (TorrentCheckedAlert) alert;
                TorrentHandle handle = taa.handle();
                if (handle != null) {
                    synchronized (handleLock) {
                        // Only update if we don't have a valid handle or this is a different torrent
                        if (torrentHandle == null || !isHandleValid(torrentHandle) ||
                                !handle.infoHash().equals(torrentHandle.infoHash())) {
                            torrentHandle = handle;
                            LOGGER.info("Torrent handle updated from TorrentCheckedAlert");
                        }
                    }
                }
            } else if (alert instanceof TorrentAlert) {
                TorrentAlert taa = (TorrentAlert) alert;
                TorrentHandle alertHandle = taa.handle();

                // Safely get status with comprehensive error handling
                TorrentStatus status = null;
                synchronized (handleLock) {
                    try {
                        if (alertHandle != null && isHandleValid(alertHandle)) {
                            // Update our handle reference if needed
                            if (torrentHandle == null || !isHandleValid(torrentHandle)) {
                                torrentHandle = alertHandle;
                            }

                            // Only proceed if we have a valid handle
                            if (torrentHandle != null && isHandleValid(torrentHandle)) {
                                status = getSafeStatus(torrentHandle);
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Error accessing torrent handle in alert: " + e.getMessage(), e);
                        // Reset handle if it's corrupted
                        torrentHandle = null;
                        return;
                    }
                }

                // Process status information safely
                if (status != null) {
                    try {
                        String statusMsg = "Torrent alert: " +
                                "State: " + status.state() +
                                ", Has metadata: " + status.hasMetadata() +
                                ", Progress: " + String.format("%.2f", status.progress() * 100) + "%" +
                                ", Peers: " + status.numPeers() +
                                ", Seeds: " + status.numSeeds();
                        LOGGER.info(statusMsg);

                        // Set metadata received if we have it
                        if (status.hasMetadata()) {
                            LOGGER.info("Metadata received for torrent");
                            metadataReceived = true;

                            // Update UI with file list - do this safely
                            SwingUtilities.invokeLater(() -> {
                                try {
                                    synchronized (handleLock) {
                                        if (torrentHandle != null && isHandleValid(torrentHandle)) {
                                            TorrentInfo info = torrentHandle.torrentFile();
                                            if (info != null) {
                                                contentsModel.clear();
                                                for (int i = 0; i < info.numFiles(); i++) {
                                                    try {
                                                        contentsModel.addElement(info.files().filePath(i));
                                                    } catch (Exception e) {
                                                        LOGGER.log(Level.WARNING,
                                                                "Error adding file to model: " + e.getMessage(), e);
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Call the metadata callback if set
                                    if (metadataCallback != null) {
                                        List<vlcj.llg_mc.App.TorrentFileEntry> entries = getTorrentFiles();
                                        if (entries != null && !entries.isEmpty()) {
                                            metadataCallback.accept(entries);
                                        }
                                    }
                                } catch (Exception e) {
                                    LOGGER.log(Level.WARNING, "Error updating UI from alert: " + e.getMessage(), e);
                                }
                            });
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Error processing torrent status: " + e.getMessage(), e);
                    }
                }
            } else if (alert instanceof TorrentFinishedAlert) {
                TorrentFinishedAlert tfa = (TorrentFinishedAlert) alert;
                TorrentHandle th = tfa.handle();
                if (th != null && isHandleValid(th)) {
                    try {
                        LOGGER.info("Torrent download finished");

                        // Start playing the media file once it's finished
                        if (!metadataReceived) {
                            metadataReceived = true;
                            SwingUtilities.invokeLater(() -> startPlayback(th));
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Error handling torrent finished alert: " + e.getMessage(), e);
                    }
                }
            } else if (alert instanceof TorrentRemovedAlert) {
                LOGGER.info("Torrent removed: " + alert.message());
                synchronized (handleLock) {
                    torrentHandle = null;
                    metadataReceived = false;
                }
            } else if (alert instanceof TrackerErrorAlert) {
                TrackerErrorAlert tea = (TrackerErrorAlert) alert;
                LOGGER.warning("Tracker error: " + tea.errorMessage() +
                        " for URL: " + tea.trackerUrl());
            } else if (alert instanceof ListenSucceededAlert) {
                ListenSucceededAlert lsa = (ListenSucceededAlert) alert;
                LOGGER.info("Successfully listening on " + lsa.address() +
                        ":" + lsa.port());
            } else if (alert instanceof ListenFailedAlert) {
                ListenFailedAlert lfa = (ListenFailedAlert) alert;
                LOGGER.warning("Failed to listen on " + lfa.address() +
                        ":" + lfa.port() + " - " + lfa.error().message());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Critical error in alert handler: " + e.getMessage(), e);
            // Don't rethrow - we need to keep the alert listener alive
        }
    }

    private void startPlayback(TorrentHandle th) {
        if (th == null || !isHandleValid(th)) {
            LOGGER.warning("Invalid torrent handle");
            return;
        }

        // Use safe status getter
        TorrentStatus status = getSafeStatus(th);
        if (status == null) {
            LOGGER.warning("Unable to get torrent status, handle may be invalid");
            return;
        }

        try {
            if (!status.hasMetadata()) {
                LOGGER.warning("No metadata available");
                return;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error checking metadata status: " + e.getMessage(), e);
            return;
        }

        TorrentInfo torrentInfo = th.torrentFile();
        if (torrentInfo == null) {
            LOGGER.warning("No torrent info available");
            return;
        }

        // Create download directory if it doesn't exist
        File saveDir = new File("downloads");
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }

        // Find the first media file to play
        int fileIndex = -1;
        for (int i = 0; i < torrentInfo.numFiles(); i++) {
            String path = torrentInfo.files().filePath(i).toLowerCase();
            if (path.endsWith(".mp4") || path.endsWith(".mkv") || path.endsWith(".avi") ||
                    path.endsWith(".mov") || path.endsWith(".mp3")) {
                fileIndex = i;
                break;
            }
        }

        if (fileIndex == -1 && torrentInfo.numFiles() > 0) {
            fileIndex = 0; // Fallback to first file if no media files found
        }

        if (fileIndex == -1) {
            LOGGER.warning("No files found in torrent");
            return;
        }

        // Get the file path
        String relativePath = torrentInfo.files().filePath(fileIndex);
        File mediaFile = new File(saveDir, relativePath).getAbsoluteFile();

        // Make sure parent directories exist
        mediaFile.getParentFile().mkdirs();

        // Start playback in the UI thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Wait for the file to have some data before starting playback
                int attempts = 0;
                while (attempts < 30 && (!mediaFile.exists() || mediaFile.length() < 1024 * 1024)) { // Wait for at
                                                                                                     // least 1MB
                    try {
                        Thread.sleep(1000);
                        attempts++;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }

                if (!mediaFile.exists()) {
                    LOGGER.warning("Media file not found: " + mediaFile.getAbsolutePath());
                    return;
                }

                LOGGER.info("Starting playback of: " + mediaFile.getAbsolutePath());
                mediaPlayerComponent.mediaPlayer().media().play(mediaFile.getAbsolutePath());

                // Show success message
                JOptionPane.showMessageDialog(frame,
                        "Starting playback of: " + mediaFile.getName() + "\n" +
                                "File size: " + humanSize(mediaFile.length()),
                        "Starting Playback",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error playing media: " + e.getMessage(), e);
                JOptionPane.showMessageDialog(frame,
                        "Error starting playback: " + e.getMessage(),
                        "Playback Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Converts a size in bytes to a human-readable string.
     *
     * @param bytes the size in bytes
     * @return a human-readable string representation of the size
     */
    private static String humanSize(long bytes) {
        if (bytes < 1024)
            return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
