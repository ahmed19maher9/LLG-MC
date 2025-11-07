package vlcj.llg_mc;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.scene.Scene;
import javafx.concurrent.Worker;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import netscape.javascript.JSObject;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.WindowConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import com.frostwire.jlibtorrent.TorrentHandle;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import okhttp3.Response;
import uk.co.caprica.vlcj.media.InfoApi;
import uk.co.caprica.vlcj.media.Media;
import uk.co.caprica.vlcj.media.MediaRef;
import uk.co.caprica.vlcj.medialist.MediaList;
import uk.co.caprica.vlcj.medialist.MediaListEventAdapter;
import uk.co.caprica.vlcj.player.base.MediaApi;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaListPlayerComponent;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.fullscreen.adaptive.AdaptiveFullScreenStrategy;
import java.util.logging.Level;
import uk.co.caprica.vlcj.player.list.ListApi;
import uk.co.caprica.vlcj.player.list.MediaListPlayer;
import uk.co.caprica.vlcj.player.list.MediaListPlayerEventAdapter;
import uk.co.caprica.vlcj.player.list.PlaybackMode;
import vlcj.llg_mc.util.PdfUtils;

/**
 * Hello world!
 */

public class App extends JFrame {

	private static final Logger LOGGER = Logger.getLogger(App.class.getName());

	private static EmbeddedMediaListPlayerComponent mediaPlayerListComponent;
	static JFrame frame;
	private static JFrame mediaPlayerFrame;
	static JTextPane editorPane;
	private static String currentSubtitleText = "";
	static String finalSubtitleToShow = "";
	static JTextArea myTextArea;
	static javax.swing.JLabel subtitleLabel;
	private static JLabel statusLabel;
	static DefaultListModel<String> playlistModel;
	static JList<String> playlistView;
	static int currentMediaIndex = 0;
	private static int loopItemIndex = -1;
	private static boolean suppressSelectionPlay = false;
	public static JButton subtitleButton;
	private static MediaPlayerEventAdapter activeSubtitleListener;
	private static EmbeddedMediaPlayerComponent subtitleListenerComponent;
	private static ImageIcon applicationLogo;
	private static final Color BRAND_PRIMARY = new Color(32, 78, 219);
	private static final Color BRAND_SECONDARY = new Color(103, 232, 249);
	private static final Color BRAND_ACCENT = new Color(249, 115, 22);
	private static final Color TEXT_PRIMARY = new Color(241, 245, 249);
	private static final Color TEXT_SECONDARY = new Color(203, 213, 225); // Slightly muted version of TEXT_PRIMARY
	private static final Color PANEL_OVERLAY = new Color(15, 23, 42, 140);
	private static Image backgroundImage;
	private static Image scaledBackgroundImage;
	private static int scaledBackgroundWidth;
	private static int scaledBackgroundHeight;
	private static double ambientPhase;
	private static Timer ambientTimer;
	private static JButton readAloudButton;
	private static JPanel taskbarButtonsPanel;
	private static JProgressBar pdfConversionProgressBar;
	private static JLabel pdfConversionStatusLabel;
	private static JPanel bookReaderHeader;
	private static Color originalMainViewBackground;
	private static Color originalBookReaderPanelBackground;
	private static Rectangle savedFrameBounds;
	private static int savedFrameExtendedState = Frame.NORMAL;
	private static Color savedContentBackground;
	private static boolean bookReaderFullscreenActive;
	private static GraphicsDevice fullscreenDevice;
	private static boolean savedFrameResizable;
	private static boolean savedFrameUndecorated;
	private static boolean savedFrameAlwaysOnTop;
	private static File pendingMediaFile;

	// PDF related variables
	private static final int PDF_IMAGE_DPI = 150; // DPI for PDF to image conversion
	private static Path BOOKS_BASE_DIR;
	private static Path RUNTIME_BOOKS_DIR;
	private static final Set<String> BOOK_IMAGE_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp", ".gif");

	// Card layout for managing different views
	private static JPanel cards;
	private static CardLayout cardLayout;

	private static final Map<String, JButton> viewButtons = new LinkedHashMap<>();
	private static final Set<String> openedViews = new LinkedHashSet<>();
	private static WebEngine bookReaderEngine;
	private static volatile boolean bookReaderReady;
	private static String activeTaskName = "Hub";
	private static final java.util.concurrent.ConcurrentMap<String, SubtitleAssociation> subtitleAssociations = new java.util.concurrent.ConcurrentHashMap<>();
	private static CardLayout mainViewLayout;
	private static JPanel mainViewPanel;
	private static JPanel mediaPlayerView;
	private static JPanel bookReaderView;
	private static JPanel youtubeView;
	private static JPanel vimeoView;
	private static JPanel goMoviesView;
	private static final BookReaderBridge BOOK_READER_BRIDGE = new BookReaderBridge();
	private static JPanel torrentView;
	private static JFXPanel bookReaderPanel;
	private static JFXPanel youtubePanel;
	private static JFXPanel vimeoPanel;
	private static JFXPanel goMoviesPanel;
	private static JPanel torrentMediaPanel;
	private static JPanel taskbarPanel;
	private static JButton youtubeLyricsButton;
	private static JButton vimeoLyricsButton;
	private static JButton goMoviesLyricsButton;
	private static EmbeddedMediaPlayerComponent torrentMediaPlayer;
	private static JTextField torrentMagnetField;
	private static JTextField torrentFileField;
	private static JButton torrentBrowseButton;
	private static DefaultListModel<TorrentFileEntry> torrentFileListModel;
	private static JList<TorrentFileEntry> torrentFileList;
	private static String selectedTorrentEntryPath;
	private static volatile TorrentFileEntry currentlyDownloadingEntry = null;

	// WebView related fields
	private static WebEngine youtubeEngine;
	private static volatile boolean youtubeReady = false;
	private static WebEngine vimeoEngine;
	private static volatile boolean vimeoReady = false;
	private static WebEngine goMoviesEngine;
	private static volatile boolean goMoviesReady = false;
	private static final AtomicBoolean javafxRuntimeInitialized = new AtomicBoolean(false);
	private static boolean torrentLandingAutoStarted = false;
	private static final String DEFAULT_DEMO_MAGNET = "magnet:?xt=urn:btih:08ada5a7a6183aae1e09d831df6748d566095a10&dn=Big%20Buck%20Bunny";
	private static WatchService booksWatchService;
	private static Thread booksWatcherThread;
	private static final AtomicBoolean booksWatcherRunning = new AtomicBoolean(false);
	// Remember user's preferred translation language between selections
	public static String savedLangCode = "eng";
	// If true, skip prompting and use savedLangCode automatically
	public static boolean rememberTranslateLang = false;
	// Saved selection/translation items with media context
	private static java.util.List<SavedItem> savedSelections = new java.util.ArrayList<>();
	// Persistence file for saved selections
	private static final java.nio.file.Path SAVED_FILE = java.nio.file.Paths.get(System.getProperty("user.home"),
			".llg-mc-saved.csv");

	private static void showPdfConversionProgress(String message, int totalPages) {
		if (pdfConversionProgressBar == null || pdfConversionStatusLabel == null) {
			return;
		}
		pdfConversionStatusLabel.setText(message);
		pdfConversionStatusLabel.setVisible(true);
		pdfConversionProgressBar.setVisible(true);
		if (totalPages > 0) {
			pdfConversionProgressBar.setIndeterminate(false);
			pdfConversionProgressBar.setMaximum(totalPages);
			pdfConversionProgressBar.setValue(0);
		} else {
			pdfConversionProgressBar.setIndeterminate(true);
		}
	}

	private static void updatePdfConversionProgress(int completedPages, int currentPage, long elapsedMillis) {
		if (pdfConversionProgressBar == null || pdfConversionStatusLabel == null) {
			return;
		}
		if (completedPages >= 0 && !pdfConversionProgressBar.isIndeterminate()) {
			pdfConversionProgressBar.setValue(Math.min(completedPages, pdfConversionProgressBar.getMaximum()));
		}
		String status;
		if (currentPage > 0) {
			status = String.format("Converted page %d in %.2f s", currentPage, elapsedMillis / 1000.0);
		} else {
			status = String.format("Conversion finished in %.2f s", elapsedMillis / 1000.0);
		}
		pdfConversionStatusLabel.setText(status);
	}

	private static void hidePdfConversionProgress() {
		if (pdfConversionProgressBar == null || pdfConversionStatusLabel == null) {
			return;
		}
		pdfConversionProgressBar.setVisible(false);
		pdfConversionStatusLabel.setVisible(false);
	}

	private static final Map<String, List<TorrentFileEntry>> torrentMetadataCache = new java.util.concurrent.ConcurrentHashMap<>();
	private static final List<String> TORRENT_METADATA_ENDPOINTS = List.of("https://itorrents.org/torrent/%s.torrent",
			"https://torrage.info/torrent/%s.torrent", "https://btcache.me/torrent/%s.torrent");
	private static final String[] SIZE_UNITS = { "B", "KB", "MB", "GB", "TB", "PB", "EB" };
	private static final int[] BASE32_DECODE_TABLE = new int[128];

	static {
		Arrays.fill(BASE32_DECODE_TABLE, -1);
		String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
		for (int i = 0; i < alphabet.length(); i++) {
			BASE32_DECODE_TABLE[alphabet.charAt(i)] = i;
		}
	}

	static {
		loadSavedSelections();
	}

	private static class TorrentFileListCellRenderer implements ListCellRenderer<TorrentFileEntry> {
		private final JPanel panel;
		private final JLabel nameLabel;
		private final JLabel sizeLabel;
		private final JProgressBar progressBar;

		public TorrentFileListCellRenderer() {
			panel = new JPanel(new BorderLayout(8, 0));
			panel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

			nameLabel = new JLabel();
			nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 12f));
			nameLabel.setForeground(TEXT_PRIMARY);

			sizeLabel = new JLabel();
			sizeLabel.setForeground(TEXT_SECONDARY);
			sizeLabel.setFont(sizeLabel.getFont().deriveFont(10f));

			JPanel infoPanel = new JPanel(new BorderLayout());
			infoPanel.setOpaque(false);
			infoPanel.add(nameLabel, BorderLayout.NORTH);
			infoPanel.add(sizeLabel, BorderLayout.SOUTH);

			progressBar = new JProgressBar(0, 100);
			progressBar.setStringPainted(true);
			progressBar.setBorderPainted(false);
			progressBar.setBackground(new Color(0, 0, 0, 30));
			progressBar.setForeground(BRAND_PRIMARY);

			panel.add(infoPanel, BorderLayout.CENTER);
			panel.add(progressBar, BorderLayout.SOUTH);
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends TorrentFileEntry> list, TorrentFileEntry value,
				int index, boolean isSelected, boolean cellHasFocus) {

			nameLabel.setText(value.toString());
			sizeLabel.setText(value.getFormattedSize());

			int progress = (int) (value.getProgress() * 100);
			progressBar.setValue(progress);
			progressBar.setString(String.format("%d%%", progress));

			// Highlight the currently downloading file
			boolean isDownloading = value == currentlyDownloadingEntry;

			if (isDownloading) {
				// Highlight the currently downloading file
				Color highlight = new Color(BRAND_PRIMARY.getRed(), BRAND_PRIMARY.getGreen(), BRAND_PRIMARY.getBlue(),
						100);
				panel.setBackground(highlight);
				panel.setBorder(BorderFactory.createMatteBorder(1, 2, 1, 1, BRAND_PRIMARY.brighter()));
				nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
			} else if (isSelected) {
				// Normal selection highlight
				panel.setBackground(
						new Color(BRAND_PRIMARY.getRed(), BRAND_PRIMARY.getGreen(), BRAND_PRIMARY.getBlue(), 60));
				panel.setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 1));
				nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
			} else {
				// Default background based on row
				panel.setBackground(index % 2 == 0 ? new Color(0, 0, 0, 10) : new Color(0, 0, 0, 5));
				panel.setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 1));
				nameLabel.setFont(nameLabel.getFont().deriveFont(Font.PLAIN));
			}

			// Make progress bar more prominent for the currently downloading file
			if (isDownloading) {
				progressBar.setForeground(BRAND_PRIMARY.brighter());
				progressBar.setBackground(new Color(255, 255, 255, 50));
				progressBar.setStringPainted(true);
			} else {
				progressBar.setForeground(BRAND_PRIMARY.darker());
				progressBar.setBackground(new Color(0, 0, 0, 30));
				// Only show percentage for non-downloading files if they have some progress
				progressBar.setStringPainted(progress > 0);
			}

			return panel;
		}
	}

	static class TorrentFileEntry {
		private final String name;
		private final long length;
		private final String path;
		private double progress;

		public TorrentFileEntry(String path, long size) {
			this(path, size, path);
		}

		public TorrentFileEntry(String name, long length, String path) {
			this.name = name;
			this.length = length;
			this.path = path;
			this.progress = 0.0;
		}

		@Override
		public String toString() {
			String displayName = name != null && !name.isBlank() ? name : path;
			if (displayName == null || displayName.isBlank())
				displayName = "(unnamed file)";
			return displayName;
		}

		public String getPath() {
			return path;
		}

		public long getSize() {
			return length;
		}

		public double getProgress() {
			return progress;
		}

		public void setProgress(double progress) {
			this.progress = Math.max(0, Math.min(1.0, progress));
		}

		public String getFormattedSize() {
			return formatBytes(length);
		}
	}

	private static String formatBytes(long bytes) {
		if (bytes < 0)
			return "?";
		double value = bytes;
		int unitIndex = 0;
		while (value >= 1024 && unitIndex < SIZE_UNITS.length - 1) {
			value /= 1024;
			unitIndex++;
		}
		if (unitIndex == 0)
			return String.format(Locale.ENGLISH, "%d %s", bytes, SIZE_UNITS[unitIndex]);
		return String.format(Locale.ENGLISH, "%.1f %s", value, SIZE_UNITS[unitIndex]);
	}

	private static void styleToolbarButton(JButton button) {
		button.setFocusPainted(false);
		button.setContentAreaFilled(false);
		button.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
		button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		button.setFont(button.getFont().deriveFont(Font.BOLD, 14f));
	}

	private static void styleTorrentButton(JButton button) {
		button.setFocusPainted(false);
		button.setContentAreaFilled(false);
		button.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
		button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		button.setFont(button.getFont().deriveFont(Font.BOLD, 12f));
		button.setBackground(new Color(50, 120, 200, 50));
		button.setOpaque(true);
	}

	private static void refreshBookshelfView(String statusMessage) {
		swingInvoke(() -> {
			if (statusMessage != null && !statusMessage.isBlank()) {
				updateStatus(statusMessage);
			}
			if (readAloudButton != null) {
				readAloudButton.setEnabled(false);
			}
		});
		Platform.runLater(() -> {
			if (bookReaderEngine != null) {
				bookReaderReady = false;
				bookReaderEngine.loadContent(generateBookshelfLandingHtml(), "text/html");
			}
		});
	}

	private static void startBooksDirectoryWatcher() {
		if (!booksWatcherRunning.compareAndSet(false, true)) {
			return;
		}
		try {
			Files.createDirectories(BOOKS_BASE_DIR);
			booksWatchService = FileSystems.getDefault().newWatchService();
			BOOKS_BASE_DIR.register(booksWatchService, StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
		} catch (IOException e) {
			booksWatcherRunning.set(false);
			swingInvoke(() -> updateStatus("Unable to watch bookshelf: " + e.getMessage()));
			return;
		}
		booksWatcherThread = new Thread(() -> {
			try {
				while (booksWatcherRunning.get()) {
					WatchKey key;
					try {
						key = booksWatchService.take();
					} catch (InterruptedException ex) {
						Thread.currentThread().interrupt();
						break;
					} catch (ClosedWatchServiceException closed) {
						break;
					}
					boolean refresh = false;
					for (WatchEvent<?> event : key.pollEvents()) {
						WatchEvent.Kind<?> kind = event.kind();
						if (kind == StandardWatchEventKinds.OVERFLOW) {
							continue;
						}
						if (kind == StandardWatchEventKinds.ENTRY_DELETE
								|| kind == StandardWatchEventKinds.ENTRY_CREATE) {
							refresh = true;
						}
					}
					boolean valid = key.reset();
					if (!valid) {
						break;
					}
					if (refresh) {
						refreshBookshelfView(null);
					}
				}
			} finally {
				booksWatcherRunning.set(false);
			}
		}, "BookReader-BooksWatcher");
		booksWatcherThread.setDaemon(true);
		booksWatcherThread.start();
	}

	private static void stopBooksDirectoryWatcher() {
		booksWatcherRunning.set(false);
		if (booksWatchService != null) {
			try {
				booksWatchService.close();
			} catch (IOException ignored) {
			}
			booksWatchService = null;
		}
		if (booksWatcherThread != null) {
			booksWatcherThread.interrupt();
			booksWatcherThread = null;
		}
	}

	private static List<TorrentFileEntry> fetchTorrentMetadata(String magnet) throws IOException {
		if (magnet == null)
			return List.of();
		String trimmed = magnet.trim();
		if (!trimmed.startsWith("magnet:"))
			return List.of();
		String infoHash = extractInfoHash(trimmed);
		if (infoHash == null)
			return List.of();
		List<TorrentFileEntry> cached = torrentMetadataCache.get(infoHash);
		if (cached != null)
			return cached;
		List<TorrentFileEntry> result = new ArrayList<>();
		for (String endpoint : TORRENT_METADATA_ENDPOINTS) {
			String url = String.format(endpoint, infoHash);
			byte[] torrentBytes = downloadBytes(url);
			if (torrentBytes == null || torrentBytes.length == 0)
				continue;
			List<TorrentFileEntry> parsed = parseTorrentFiles(torrentBytes);
			if (!parsed.isEmpty()) {
				result = parsed;
				break;
			}
		}
		result = result.isEmpty() ? List.of() : List.copyOf(result);
		torrentMetadataCache.put(infoHash, result);
		return result;
	}

	private static String extractInfoHash(String magnet) {
		int xtIndex = magnet.indexOf("xt=urn:btih:");
		if (xtIndex < 0)
			return null;
		int start = xtIndex + "xt=urn:btih:".length();
		int end = magnet.indexOf('&', start);
		String raw = end >= 0 ? magnet.substring(start, end) : magnet.substring(start);
		if (raw.isEmpty())
			return null;
		String normalized = normalizeInfoHash(raw);
		return normalized != null && !normalized.isBlank() ? normalized : null;
	}

	private static String normalizeInfoHash(String raw) {
		if (raw == null)
			return null;
		String cleaned = raw.trim();
		if (cleaned.isEmpty())
			return null;
		try {
			cleaned = URLDecoder.decode(cleaned, StandardCharsets.UTF_8);
		} catch (IllegalArgumentException ignored) {
		}
		cleaned = cleaned.trim();
		if (cleaned.length() == 40 && isHex(cleaned))
			return cleaned.toLowerCase(Locale.getDefault());
		String upper = cleaned.toUpperCase(Locale.getDefault());
		if (upper.length() == 32 && isBase32(upper)) {
			byte[] decoded = decodeBase32(upper);
			if (decoded != null && decoded.length == 20)
				return toHex(decoded);
		}
		return null;
	}

	private static boolean isHex(String value) {
		for (int i = 0; i < value.length(); i++) {
			char ch = value.charAt(i);
			if (Character.digit(ch, 16) < 0)
				return false;
		}
		return true;
	}

	private static boolean isBase32(String value) {
		for (int i = 0; i < value.length(); i++) {
			char ch = value.charAt(i);
			if (ch >= BASE32_DECODE_TABLE.length || BASE32_DECODE_TABLE[ch] < 0)
				return false;
		}
		return true;
	}

	private static byte[] decodeBase32(String value) {
		int buffer = 0;
		int bitsLeft = 0;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		for (int i = 0; i < value.length(); i++) {
			char ch = value.charAt(i);
			if (ch >= BASE32_DECODE_TABLE.length)
				return null;
			int val = BASE32_DECODE_TABLE[ch];
			if (val < 0)
				return null;
			buffer = (buffer << 5) | val;
			bitsLeft += 5;
			if (bitsLeft >= 8) {
				bitsLeft -= 8;
				out.write((buffer >> bitsLeft) & 0xFF);
			}
		}
		if (bitsLeft > 0 && (buffer & ((1 << bitsLeft) - 1)) != 0)
			return null;
		return out.toByteArray();
	}

	private static String toHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder(bytes.length * 2);
		for (byte b : bytes) {
			sb.append(Character.forDigit((b >>> 4) & 0xF, 16));
			sb.append(Character.forDigit(b & 0xF, 16));
		}
		return sb.toString();
	}

	private static byte[] downloadBytes(String url) throws IOException {
		OkHttpClient client = new OkHttpClient.Builder().followRedirects(true).followSslRedirects(true).build();
		Request request = new Request.Builder().url(url).get().header("User-Agent", "llg-mc/1.0").build();
		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful() || response.body() == null)
				return null;
			return response.body().bytes();
		}
	}

	private static List<TorrentFileEntry> parseTorrentFiles(byte[] data) throws IOException {
		Map<String, Object> root = decodeBencodeToMap(data);
		if (root.isEmpty())
			return List.of();
		Object infoObj = root.get("info");
		if (!(infoObj instanceof Map<?, ?> infoMapRaw))
			return List.of();
		Map<String, Object> infoMap = toStringObjectMap(infoMapRaw);
		Object filesObj = infoMap.get("files");
		Object lengthObj = infoMap.get("length");
		String name = valueAsString(infoMap.get("name"));
		if (filesObj instanceof List<?> filesList) {
			List<TorrentFileEntry> entries = new ArrayList<>();
			for (Object fileObj : filesList) {
				if (!(fileObj instanceof Map<?, ?> fileMapRaw))
					continue;
				Map<String, Object> fileMap = toStringObjectMap(fileMapRaw);
				long length = valueAsLong(fileMap.get("length"), -1);
				List<String> pathSegments = new ArrayList<>();
				Object pathValue = fileMap.get("path");
				if (pathValue instanceof List<?> pathList) {
					for (Object segment : pathList) {
						String s = valueAsString(segment);
						if (s != null && !s.isBlank())
							pathSegments.add(s);
					}
				}
				String path = String.join("/", pathSegments);
				String displayName = path.isBlank() ? name : path;
				if (displayName == null || displayName.isBlank())
					displayName = "(unnamed file)";
				entries.add(new TorrentFileEntry(displayName, length, path.isBlank() ? displayName : path));
			}
			return entries;
		} else if (name != null) {
			long length = valueAsLong(lengthObj, -1);
			return List.of(new TorrentFileEntry(name, length, name));
		}
		return List.of();
	}

	private static Map<String, Object> decodeBencodeToMap(byte[] data) throws IOException {
		int[] indexHolder = new int[] { 0 };
		Object value = decodeBencodeValue(data, indexHolder);
		if (value instanceof Map<?, ?> raw)
			return toStringObjectMap(raw);
		return Map.of();
	}

	private static Object decodeBencodeValue(byte[] data, int[] indexHolder) throws IOException {
		if (indexHolder[0] >= data.length)
			throw new IOException("Unexpected end of bencode data");
		int start = indexHolder[0];
		byte marker = data[start];
		if (marker == 'i') {
			int end = indexOf(data, (byte) 'e', start + 1);
			if (end < 0)
				throw new IOException("Invalid bencode integer");
			String value = new String(data, start + 1, end - (start + 1), StandardCharsets.US_ASCII);
			indexHolder[0] = end + 1;
			return Long.parseLong(value.trim());
		} else if (marker == 'l') {
			indexHolder[0] = start + 1;
			List<Object> list = new ArrayList<>();
			while (indexHolder[0] < data.length && data[indexHolder[0]] != 'e') {
				list.add(decodeBencodeValue(data, indexHolder));
			}
			if (indexHolder[0] >= data.length)
				throw new IOException("List not terminated");
			indexHolder[0]++;
			return list;
		} else if (marker == 'd') {
			indexHolder[0] = start + 1;
			Map<String, Object> map = new LinkedHashMap<>();
			while (indexHolder[0] < data.length && data[indexHolder[0]] != 'e') {
				Object keyObj = decodeBencodeValue(data, indexHolder);
				if (!(keyObj instanceof String key))
					throw new IOException("Dictionary key must be a string");
				Object value = decodeBencodeValue(data, indexHolder);
				map.put(key, value);
			}
			if (indexHolder[0] >= data.length)
				throw new IOException("Dictionary not terminated");
			indexHolder[0]++;
			return map;
		} else if (marker >= '0' && marker <= '9') {
			int colon = indexOf(data, (byte) ':', start);
			if (colon < 0)
				throw new IOException("Invalid bencode string length");
			String lengthStr = new String(data, start, colon - start, StandardCharsets.US_ASCII);
			int length = Integer.parseInt(lengthStr.trim());
			int stringStart = colon + 1;
			if (stringStart + length > data.length)
				throw new IOException("Bencode string exceeds buffer");
			String value = new String(data, stringStart, length, StandardCharsets.UTF_8);
			indexHolder[0] = stringStart + length;
			return value;
		}
		throw new IOException("Unknown bencode marker: " + (char) marker);
	}

	private static int indexOf(byte[] data, byte target, int start) {
		for (int i = start; i < data.length; i++) {
			if (data[i] == target)
				return i;
		}
		return -1;
	}

	private static Map<String, Object> toStringObjectMap(Map<?, ?> raw) {
		Map<String, Object> map = new LinkedHashMap<>();
		for (Map.Entry<?, ?> entry : raw.entrySet()) {
			map.put(String.valueOf(entry.getKey()), entry.getValue());
		}
		return map;
	}

	private static String valueAsString(Object value) {
		if (value == null)
			return null;
		if (value instanceof String s)
			return s;
		if (value instanceof Number n)
			return Long.toString(n.longValue());
		return value.toString();
	}

	private static long valueAsLong(Object value, long defaultValue) {
		if (value instanceof Number n)
			return n.longValue();
		if (value instanceof String s) {
			try {
				return Long.parseLong(s.trim());
			} catch (NumberFormatException ignored) {
			}
		}
		return defaultValue;
	}

	private static JPanel buildTorrentView() {
		JPanel container = new JPanel(new BorderLayout());
		container.setOpaque(false);

		// Note: LibtorrentStreamer initialization is handled in openTorrentView()

		// Create main controls panel with GridBagLayout
		JPanel controls = createGlassPanel(new GridBagLayout());
		controls.setOpaque(false);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(6, 8, 6, 8);
		gbc.anchor = GridBagConstraints.WEST;

		// Torrent File Input
		gbc.gridx = 0;
		gbc.gridy = 0;
		JLabel fileLabel = new JLabel("Torrent File:");
		fileLabel.setForeground(TEXT_PRIMARY);
		controls.add(fileLabel, gbc);

		torrentFileField = new JTextField(30);
		torrentFileField.setOpaque(false);
		torrentFileField.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BRAND_PRIMARY));
		gbc.gridx = 1;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		controls.add(torrentFileField, gbc);

		torrentBrowseButton = new JButton("Browse");
		torrentBrowseButton.addActionListener(e -> browseTorrentFile());
		styleTorrentButton(torrentBrowseButton);
		gbc.gridx = 2;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		controls.add(torrentBrowseButton, gbc);

		// Magnet Link Input
		JLabel magnetLabel = new JLabel("Or Magnet Link:");
		magnetLabel.setForeground(TEXT_PRIMARY);
		gbc.gridx = 0;
		gbc.gridy = 1;
		controls.add(magnetLabel, gbc);

		torrentMagnetField = new JTextField(30);
		torrentMagnetField.setOpaque(false);
		torrentMagnetField.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BRAND_PRIMARY));
		torrentMagnetField.setToolTipText("Paste magnet link here");
		gbc.gridx = 1;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		controls.add(torrentMagnetField, gbc);

		JButton loadMagnetButton = new JButton("Load");
		loadMagnetButton.addActionListener(e -> {
			String magnetLink = torrentMagnetField.getText().trim();
			if (!magnetLink.isEmpty()) {
				// Ensure libtorrentStreamer is initialized before loading
				if (libtorrentStreamer == null) {
					// Initialize torrent view components if not already done
					if (torrentView == null) {
						torrentView = buildTorrentView();
					}
					// Initialize the streamer with proper UI components
					JSplitPane splitPane = (JSplitPane) torrentView.getComponent(1);
					JPanel contentPanel = (JPanel) splitPane.getLeftComponent();
					libtorrentStreamer = new LibtorrentStreamer(frame, contentPanel, torrentMediaPanel);
				}
				loadTorrentFromMagnet(magnetLink);
			} else {
				JOptionPane.showMessageDialog(frame, "Please enter a magnet link", "Error",
						JOptionPane.WARNING_MESSAGE);
			}
		});
		styleTorrentButton(loadMagnetButton);
		gbc.gridx = 2;
		controls.add(loadMagnetButton, gbc);

		// Add demo magnet button
		JButton demoMagnetButton = new JButton("Load Demo");
		demoMagnetButton.addActionListener(e -> {
			torrentMagnetField.setText(DEFAULT_DEMO_MAGNET);
			// Ensure libtorrentStreamer is initialized before loading
			if (libtorrentStreamer == null) {
				// Initialize torrent view components if not already done
				if (torrentView == null) {
					torrentView = buildTorrentView();
				}
				// Initialize the streamer with proper UI components
				JSplitPane splitPane = (JSplitPane) torrentView.getComponent(1);
				JPanel contentPanel = (JPanel) splitPane.getLeftComponent();
				libtorrentStreamer = new LibtorrentStreamer(frame, contentPanel, torrentMediaPanel);
			}
			loadTorrentFromMagnet(DEFAULT_DEMO_MAGNET);
		});
		styleTorrentButton(demoMagnetButton);
		gbc.gridx = 3;
		controls.add(demoMagnetButton, gbc);

		// Add controls to container
		container.add(controls, BorderLayout.NORTH);

		// Create torrent selection panel (left side)
		torrentFileListModel = new DefaultListModel<>();
		torrentFileList = new JList<>(torrentFileListModel) {
			@Override
			public int getFixedCellHeight() {
				return 60; // Increased height to accommodate progress bar
			}
		};
		torrentFileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		torrentFileList.setOpaque(false);
		torrentFileList.setForeground(TEXT_PRIMARY);
		torrentFileList.setCellRenderer(new TorrentFileListCellRenderer());
		torrentFileList.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				TorrentFileEntry selected = torrentFileList.getSelectedValue();
				selectedTorrentEntryPath = selected == null ? null : selected.getPath();
			}
		});
		torrentFileList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					int index = torrentFileList.locationToIndex(e.getPoint());
					if (index >= 0) {
						torrentFileList.setSelectedIndex(index);
						TorrentFileEntry selected = torrentFileList.getSelectedValue();
						selectedTorrentEntryPath = selected == null ? null : selected.getPath();

						// On double-click, check if we have a torrent loaded before playing
						if (e.getClickCount() == 2 && selected != null) {
							// Check if torrent is loaded and has metadata
							if (libtorrentStreamer == null) {
								JOptionPane.showMessageDialog(frame,
										"Torrent system not initialized. Please restart the application.",
										"Initialization Error",
										JOptionPane.ERROR_MESSAGE);
								return;
							}

							TorrentHandle handle = libtorrentStreamer.getValidTorrentHandle();
							if (handle == null) {
								// Check if this is magnet link metadata (no actual torrent session)
								if (isMagnetLink && !torrentFileListModel.isEmpty()) {
									JOptionPane.showMessageDialog(frame,
											"This is magnet link metadata preview.\n\n" +
													"To actually download and stream torrent files, you need to:\n" +
													"1. Find the .torrent file from a torrent site\n" +
													"2. Use the 'Browse' button to load the .torrent file\n" +
													"3. Then double-click files to stream them\n\n" +
													"Magnet links are currently used only for previewing torrent contents.",
											"Magnet Link Limitation",
											JOptionPane.INFORMATION_MESSAGE);
								} else {
									JOptionPane.showMessageDialog(frame,
											"Please load a torrent file or magnet link first.",
											"No Torrent Loaded",
											JOptionPane.INFORMATION_MESSAGE);
								}
								return;
							}

							// Check if torrent has metadata (needed for file access)
							if (!handle.status().hasMetadata()) {
								JOptionPane.showMessageDialog(frame,
										"Torrent metadata is still loading. Please wait a moment and try again.",
										"Metadata Loading",
										JOptionPane.INFORMATION_MESSAGE);
								return;
							}

							playSelectedTorrentFile(selected);
						}
					}
				}
			}
		});
		JScrollPane torrentFileScroll = new JScrollPane(torrentFileList);
		torrentFileScroll.setOpaque(false);
		if (torrentFileScroll.getViewport() != null)
			torrentFileScroll.getViewport().setOpaque(false);
		torrentFileScroll.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));
		JPanel torrentSelectionPanel = createGlassPanel(new BorderLayout());
		torrentSelectionPanel.setOpaque(false);
		JLabel torrentContentsLabel = new JLabel("Torrent Contents:");
		torrentContentsLabel.setForeground(TEXT_PRIMARY);
		torrentContentsLabel.setBorder(BorderFactory.createEmptyBorder(8, 12, 4, 12));
		torrentSelectionPanel.add(torrentContentsLabel, BorderLayout.NORTH);
		torrentSelectionPanel.add(torrentFileScroll, BorderLayout.CENTER);

		// Create media panel (right side)
		torrentMediaPlayer = new EmbeddedMediaPlayerComponent();
		torrentMediaPlayer.mediaPlayer().events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
			@Override
			public void mediaChanged(MediaPlayer mediaPlayer, MediaRef media) {
				// Stop default video when torrent media starts playing
				if (media != null) {
					try {
						uk.co.caprica.vlcj.media.Media mediaInstance = media.newMedia();
						String mrl = mediaInstance.info().mrl();
						mediaInstance.release();

						// If this is not the default video, stop the default video
						String defaultVideoUrl = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";
						if (mrl != null && !mrl.equals(defaultVideoUrl)) {
							// This is torrent media, stop the default video
							SwingUtilities.invokeLater(() -> updateStatus("Torrent streaming started"));
						}
					} catch (Exception e) {
						LOGGER.log(Level.WARNING, "Error checking media change: " + e.getMessage(), e);
					}
				}
			}

			@Override
			public void positionChanged(MediaPlayer mediaPlayer, float newPosition) {
				if (currentlyDownloadingEntry != null) {
					currentlyDownloadingEntry.setProgress(newPosition);
					if (torrentFileList != null) {
						SwingUtilities.invokeLater(() -> torrentFileList.repaint());
					}
				}
			}

			@Override
			public void finished(MediaPlayer mediaPlayer) {
				SwingUtilities.invokeLater(() -> {
					updateStatus("Torrent playback finished");
					// When torrent playback finishes, open streaming link if no torrent is loaded
					openStreamingLinkIfNoTorrent();
				});
			}

			@Override
			public void error(MediaPlayer mediaPlayer) {
				SwingUtilities.invokeLater(() -> {
					updateStatus("Torrent playback error");
					// On error, open streaming link if no torrent is loaded
					openStreamingLinkIfNoTorrent();
				});
			}
		});
		// Create media controls panel
		JPanel torrentControlsPanel = createGlassPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));
		torrentControlsPanel.setOpaque(false);
		torrentControlsPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		JButton torrentPlayButton = new JButton("▶");
		JButton torrentPauseButton = new JButton("⏸");
		JButton torrentStopButton = new JButton("⏹");
		JButton torrentSkipForwardButton = new JButton("⏭");
		JButton torrentSkipBackButton = new JButton("⏮");

		// Style the buttons
		JButton[] controlButtons = { torrentPlayButton, torrentPauseButton, torrentStopButton, torrentSkipForwardButton,
				torrentSkipBackButton };
		for (JButton btn : controlButtons) {
			btn.setFocusPainted(false);
			btn.setContentAreaFilled(false);
			btn.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
			btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			btn.setFont(btn.getFont().deriveFont(Font.BOLD, 12f));
			btn.setForeground(TEXT_PRIMARY);
		}

		// Add button actions
		torrentPlayButton.addActionListener(e -> {
			try {
				if (torrentMediaPlayer != null && torrentMediaPlayer.mediaPlayer() != null) {
					torrentMediaPlayer.mediaPlayer().controls().play();
					updateStatus("Playing torrent media");
				}
			} catch (Exception ex) {
				LOGGER.log(Level.WARNING, "Error playing torrent media: " + ex.getMessage(), ex);
			}
		});

		torrentPauseButton.addActionListener(e -> {
			try {
				if (torrentMediaPlayer != null && torrentMediaPlayer.mediaPlayer() != null) {
					if (torrentMediaPlayer.mediaPlayer().status().isPlaying()) {
						torrentMediaPlayer.mediaPlayer().controls().pause();
						torrentPauseButton.setText("▶");
					} else {
						torrentMediaPlayer.mediaPlayer().controls().play();
						torrentPauseButton.setText("⏸");
					}
				}
			} catch (Exception ex) {
				LOGGER.log(Level.WARNING, "Error controlling torrent playback: " + ex.getMessage(), ex);
			}
		});

		torrentStopButton.addActionListener(e -> {
			try {
				if (torrentMediaPlayer != null && torrentMediaPlayer.mediaPlayer() != null) {
					torrentMediaPlayer.mediaPlayer().controls().stop();
					updateStatus("Torrent playback stopped");
				}
			} catch (Exception ex) {
				LOGGER.log(Level.WARNING, "Error stopping torrent media: " + ex.getMessage(), ex);
			}
		});

		torrentSkipForwardButton.addActionListener(e -> {
			try {
				if (torrentMediaPlayer != null && torrentMediaPlayer.mediaPlayer() != null) {
					torrentMediaPlayer.mediaPlayer().controls().skipTime(SKIP_MS);
					updateStatus("Skipped forward " + (SKIP_MS / 1000) + " seconds");
				}
			} catch (Exception ex) {
				LOGGER.log(Level.WARNING, "Error skipping forward: " + ex.getMessage(), ex);
			}
		});

		torrentSkipBackButton.addActionListener(e -> {
			try {
				if (torrentMediaPlayer != null && torrentMediaPlayer.mediaPlayer() != null) {
					torrentMediaPlayer.mediaPlayer().controls().skipTime(-SKIP_MS);
					updateStatus("Skipped back " + (SKIP_MS / 1000) + " seconds");
				}
			} catch (Exception ex) {
				LOGGER.log(Level.WARNING, "Error skipping back: " + ex.getMessage(), ex);
			}
		});

		// Add buttons to controls panel
		torrentControlsPanel.add(torrentSkipBackButton);
		torrentControlsPanel.add(torrentPlayButton);
		torrentControlsPanel.add(torrentPauseButton);
		torrentControlsPanel.add(torrentStopButton);
		torrentControlsPanel.add(torrentSkipForwardButton);

		torrentMediaPanel = new JPanel(new BorderLayout());
		torrentMediaPanel.setOpaque(false);
		torrentMediaPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		torrentMediaPanel.add(torrentMediaPlayer, BorderLayout.CENTER);
		torrentMediaPanel.add(torrentControlsPanel, BorderLayout.SOUTH);

		// Create TV-like frame around the media panel
		JPanel tvFramePanel = new JPanel(new BorderLayout()) {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				int width = getWidth();
				int height = getHeight();
				int borderWidth = 8;
				int cornerRadius = 20;

				// Draw outer dark border (TV bezel)
				g2.setColor(new Color(20, 20, 20));
				g2.fillRoundRect(0, 0, width, height, cornerRadius, cornerRadius);

				// Draw inner shadow/border
				g2.setColor(new Color(40, 40, 40));
				g2.fillRoundRect(borderWidth / 2, borderWidth / 2, width - borderWidth, height - borderWidth,
						cornerRadius - 5, cornerRadius - 5);

				// Draw screen area (slightly inset)
				g2.setColor(new Color(10, 10, 10));
				g2.fillRoundRect(borderWidth, borderWidth, width - 2 * borderWidth, height - 2 * borderWidth,
						cornerRadius - 10, cornerRadius - 10);

				// Draw subtle inner highlight
				g2.setColor(new Color(30, 30, 30));
				g2.drawRoundRect(borderWidth + 2, borderWidth + 2, width - 2 * borderWidth - 4,
						height - 2 * borderWidth - 4, cornerRadius - 12, cornerRadius - 12);

				g2.dispose();
			}
		};
		tvFramePanel.setOpaque(false);
		tvFramePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		tvFramePanel.add(torrentMediaPanel, BorderLayout.CENTER);

		// Create JSplitPane for resizable panels (like media player view)
		final JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, torrentSelectionPanel, tvFramePanel);
		// Allow either pane to fully expand/collapse by setting minimum sizes to 0
		torrentSelectionPanel.setMinimumSize(new Dimension(0, 0));
		tvFramePanel.setMinimumSize(new Dimension(0, 0));
		mainSplit.setResizeWeight(0.0); // Give more space to the media panel (right side)
		mainSplit.setContinuousLayout(true);
		mainSplit.setOneTouchExpandable(true);
		int panelWidth = 400; // Define the desired width for the torrent selection panel
		int initialDivider = Math.max(320, FRAME_WIDTH - panelWidth); // Initial position similar to media player
		mainSplit.setDividerLocation(initialDivider);

		container.add(mainSplit, BorderLayout.CENTER);
		return container;
	}

	private static void browseTorrentFile() {
		SwingUtilities.invokeLater(() -> {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int result = chooser.showOpenDialog(frame);
			if (result == JFileChooser.APPROVE_OPTION) {
				File selected = chooser.getSelectedFile();
				if (selected != null) {
					torrentFileField.setText(selected.getAbsolutePath());
					loadTorrentFileEntries(selected.toPath());
				}
			}
		});
	}

	private static void loadTorrentFileEntries(Path torrentPath) {
		if (torrentFileListModel == null)
			return;
		torrentFileListModel.clear();
		selectedTorrentEntryPath = null;
		isMagnetLink = false; // This is actual torrent file data, not magnet metadata
		if (torrentPath == null)
			return;
		updateStatus("Loading torrent contents...");

		// Load the torrent file in a background thread
		new Thread(() -> {
			try {
				// Ensure libtorrentStreamer is initialized with proper UI components
				if (libtorrentStreamer == null) {
					// Initialize torrent view components if not already done
					if (torrentView == null) {
						torrentView = buildTorrentView();
					}
					// Initialize the streamer with proper UI components
					JSplitPane splitPane = (JSplitPane) torrentView.getComponent(1);
					JPanel contentPanel = (JPanel) splitPane.getLeftComponent();
					libtorrentStreamer = new LibtorrentStreamer(frame, contentPanel, torrentMediaPanel);
				}

				// Load the torrent file - this will load it into the session
				libtorrentStreamer.loadTorrentFile(torrentPath.toFile());

				// Wait for metadata to be available (up to 30 seconds)
				int attempts = 0;
				final int MAX_ATTEMPTS = 30; // 30 seconds total wait time

				while (attempts < MAX_ATTEMPTS) {
					try {
						Thread.sleep(1000);
						attempts++;

						// Check if we have a valid torrent handle with metadata
						TorrentHandle handle = libtorrentStreamer.getValidTorrentHandle();
						if (handle != null && handle.status().hasMetadata()) {
							break; // Metadata is ready
						}
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						break;
					}
				}

				// Get the list of files from the torrent
				List<TorrentFileEntry> entries = parseTorrentFiles(Files.readAllBytes(torrentPath));

				// Update UI on the EDT
				SwingUtilities.invokeLater(() -> {
					torrentFileListModel.clear();
					selectedTorrentEntryPath = null;

					if (entries == null || entries.isEmpty()) {
						updateStatus("Torrent contains no files");
						return;
					}

					for (TorrentFileEntry entry : entries) {
						torrentFileListModel.addElement(entry);
					}

					torrentFileList.setSelectedIndex(0);
					TorrentFileEntry selected = torrentFileList.getSelectedValue();
					selectedTorrentEntryPath = selected == null ? null : selected.getPath();

					// Check if torrent is actually loaded
					TorrentHandle handle = libtorrentStreamer.getValidTorrentHandle();
					if (handle != null && handle.status().hasMetadata()) {
						updateStatus("Torrent loaded successfully - double-click files to stream");
					} else {
						updateStatus("Torrent loaded but metadata still loading - please wait...");
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
				SwingUtilities.invokeLater(() -> {
					String errorMsg = "Error loading torrent: " + e.getMessage();
					if (e.getCause() != null) {
						errorMsg += "\nCause: " + e.getCause().getMessage();
					}
					updateStatus(errorMsg);
					JOptionPane.showMessageDialog(frame, errorMsg, "Torrent Error", JOptionPane.ERROR_MESSAGE);
				});
			}
		}, "TorrentLoader").start();
	}

	private static JPanel buildGoMoviesView() {
		JPanel container = new JPanel(new BorderLayout());
		container.setOpaque(false);
		JPanel toolbar = createGlassPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
		toolbar.setOpaque(false);
		JButton reloadButton = new JButton("Reload");
		reloadButton.addActionListener(e -> initializeGoMoviesScene());
		reloadButton.setFocusPainted(false);
		reloadButton.setContentAreaFilled(false);
		reloadButton.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
		reloadButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		reloadButton.setFont(reloadButton.getFont().deriveFont(Font.BOLD, 14f));
		toolbar.add(reloadButton);
		JButton subtitlesButton = new JButton("Subtitles / Lyrics");
		subtitlesButton.addActionListener(e -> showGoMoviesSubtitlesDialog());
		subtitlesButton.setFocusPainted(false);
		subtitlesButton.setContentAreaFilled(false);
		subtitlesButton.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
		subtitlesButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		subtitlesButton.setFont(subtitlesButton.getFont().deriveFont(Font.BOLD, 14f));
		subtitlesButton.setEnabled(false);
		toolbar.add(subtitlesButton);
		goMoviesLyricsButton = subtitlesButton;
		container.add(toolbar, BorderLayout.NORTH);
		goMoviesPanel = new JFXPanel();
		container.add(goMoviesPanel, BorderLayout.CENTER);
		initializeGoMoviesScene();
		return container;
	}

	private static JPanel buildVimeoView() {
		JPanel container = new JPanel(new BorderLayout());
		container.setOpaque(false);
		JPanel toolbar = createGlassPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
		toolbar.setOpaque(false);
		JButton reloadButton = new JButton("Reload");
		reloadButton.addActionListener(e -> initializeVimeoScene());
		reloadButton.setFocusPainted(false);
		reloadButton.setContentAreaFilled(false);
		reloadButton.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
		reloadButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		reloadButton.setFont(reloadButton.getFont().deriveFont(Font.BOLD, 14f));
		toolbar.add(reloadButton);
		JButton subtitlesButton = new JButton("Subtitles / Lyrics");
		subtitlesButton.addActionListener(e -> showVimeoSubtitlesDialog());
		subtitlesButton.setFocusPainted(false);
		subtitlesButton.setContentAreaFilled(false);
		subtitlesButton.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
		subtitlesButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		subtitlesButton.setFont(subtitlesButton.getFont().deriveFont(Font.BOLD, 14f));
		subtitlesButton.setEnabled(false);
		toolbar.add(subtitlesButton);
		vimeoLyricsButton = subtitlesButton;
		container.add(toolbar, BorderLayout.NORTH);
		vimeoPanel = new JFXPanel();
		container.add(vimeoPanel, BorderLayout.CENTER);
		initializeVimeoScene();
		return container;
	}

	// Model for saved items
	private static class SavedItem {
		final String selection;
		final String translation;
		final String mrl;
		final long timeMs;

		SavedItem(String selection, String translation, String mrl, long timeMs) {
			this.selection = selection;
			this.translation = translation;
			this.mrl = mrl;
			this.timeMs = timeMs;
		}
	}

	private static final class SubtitleAssociation {
		final String subtitlePath;
		final List<SrtParser.Subtitle> subtitles;

		SubtitleAssociation(String subtitlePath, List<SrtParser.Subtitle> subtitles) {
			this.subtitlePath = subtitlePath;
			this.subtitles = subtitles;
		}
	}

	private static void saveSavedSelections() {
		java.util.List<String> lines = new java.util.ArrayList<>();
		for (SavedItem it : savedSelections) {
			String sel = sanitizeTSV(it.selection);
			String tr = sanitizeTSV(it.translation);
			String m = it.mrl == null ? "" : sanitizeTSV(it.mrl);
			lines.add(sel + "\t" + tr + "\t" + m + "\t" + it.timeMs);
		}
		try {
			java.nio.file.Files.write(SAVED_FILE, lines, java.nio.charset.StandardCharsets.UTF_8,
					java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.TRUNCATE_EXISTING);
		} catch (java.io.IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void loadSavedSelections() {
		savedSelections.clear();
		try {
			if (java.nio.file.Files.exists(SAVED_FILE)) {
				for (String line : java.nio.file.Files.readAllLines(SAVED_FILE,
						java.nio.charset.StandardCharsets.UTF_8)) {
					String[] parts = line.split("\t", -1);
					if (parts.length >= 4) {
						String sel = sanitizeTSV(parts[0]);
						String tr = sanitizeTSV(parts[1]);
						String m = sanitizeTSV(parts[2]);
						long t;
						try {
							t = Long.parseLong(parts[3]);
						} catch (NumberFormatException nfe) {
							t = 0L;
						}
						savedSelections.add(new SavedItem(sel, tr, m, Math.max(0, t)));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// CSV/TSV helpers
	private static String sanitizeTSV(String s) {
		if (s == null)
			return "";
		return s.replace('\t', ' ').replace('\n', ' ').replace('\r', ' ').trim();
	}

	private static String toCsv(String s) {
		if (s == null)
			return "";
		String v = s.replace("\r", " ").replace("\n", " ");
		boolean needQuotes = v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r");
		v = v.replace("\"", "\"\"");
		return needQuotes ? "\"" + v + "\"" : v;
	}

	private static java.util.List<String> parseCsvLine(String line) {
		java.util.List<String> out = new java.util.ArrayList<>();
		if (line == null)
			return out;
		int i = 0, n = line.length();
		while (i < n) {
			if (line.charAt(i) == '"') {
				i++;
				StringBuilder sb = new StringBuilder();
				while (i < n) {
					char c = line.charAt(i++);
					if (c == '"') {
						if (i < n && line.charAt(i) == '"') {
							sb.append('"');
							i++;
						} else
							break;
					} else
						sb.append(c);
				}
				out.add(sb.toString());
				if (i < n && line.charAt(i) == ',')
					i++;
			} else {
				int j = line.indexOf(',', i);
				if (j == -1)
					j = n;
				out.add(line.substring(i, j).trim());
				i = j + 1;
			}
		}
		return out;
	}

	public static void promptTranslationForSelection(Component parentComponent, String selectedText) {
		if (selectedText == null)
			return;
		String trimmedSelection = selectedText.trim();
		if (trimmedSelection.isEmpty())
			return;
		Component parent = parentComponent != null ? parentComponent : frame;
		String translatedText = "";
		try {
			String targetLang;
			if (rememberTranslateLang && savedLangCode != null && !savedLangCode.isEmpty()) {
				targetLang = savedLangCode;
			} else {
				String[] codes = java.util.Locale.getISOLanguages();
				java.util.List<String> choicesList = new java.util.ArrayList<>();
				for (String code : codes) {
					java.util.Locale loc = new java.util.Locale.Builder().setLanguage(code).build();
					String name = loc.getDisplayLanguage(java.util.Locale.ENGLISH);
					if (name == null || name.isEmpty())
						name = code.toUpperCase();
					choicesList.add(code + " - " + name);
				}
				java.util.Collections.sort(choicesList, String.CASE_INSENSITIVE_ORDER);
				javax.swing.JComboBox<String> combo = new javax.swing.JComboBox<>(choicesList.toArray(new String[0]));
				if (savedLangCode != null) {
					for (int i = 0; i < combo.getItemCount(); i++) {
						String item = combo.getItemAt(i);
						if (item.startsWith(savedLangCode + " ")) {
							combo.setSelectedIndex(i);
							break;
						}
					}
				}
				javax.swing.JCheckBox rememberBox = new javax.swing.JCheckBox("Remember this language", true);
				javax.swing.JPanel panel = new javax.swing.JPanel(new java.awt.BorderLayout(8, 8));
				panel.add(new javax.swing.JLabel("Translate to which language?"), java.awt.BorderLayout.NORTH);
				panel.add(combo, java.awt.BorderLayout.CENTER);
				panel.add(rememberBox, java.awt.BorderLayout.SOUTH);
				int res = JOptionPane.showConfirmDialog(parent, panel, "Choose Language", JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				if (res != JOptionPane.OK_OPTION)
					return;
				String selStr = (String) combo.getSelectedItem();
				if (selStr == null || selStr.isBlank())
					return;
				targetLang = selStr.split(" ")[0];
				if (rememberBox.isSelected()) {
					savedLangCode = targetLang;
					rememberTranslateLang = true;
				} else {
					rememberTranslateLang = false;
				}
			}
			translatedText = SubtitleService.translate(trimmedSelection, "en", targetLang);
			System.out.println("Translated text: " + translatedText);
			JOptionPane.showMessageDialog(parent,
					"Selection: " + trimmedSelection + "\n" + "Translation: " + translatedText, "Translation Result",
					JOptionPane.INFORMATION_MESSAGE);
			int save = JOptionPane.showConfirmDialog(parent,
					"Save this selection and translation for future reference?", "Save Translation",
					JOptionPane.YES_NO_OPTION);
			if (save == JOptionPane.YES_OPTION) {
				String mrl = FileDropTargetListener.filePath;
				mrl = App.normalizeMrl(mrl);
				long tMs = 0L;
				if (mediaPlayerListComponent != null && mediaPlayerListComponent.mediaPlayer() != null) {
					tMs = mediaPlayerListComponent.mediaPlayer().status().time();
				}
				savedSelections.add(new SavedItem(trimmedSelection, translatedText, mrl, Math.max(0, tMs)));
				saveSavedSelections();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Normalize an MRL/path to an absolute local path when possible
	public static String normalizeMrl(String mrl) {
		if (mrl == null || mrl.isEmpty())
			return mrl;
		try {
			if (mrl.startsWith("file:/")) {
				java.net.URI uri = java.net.URI.create(mrl);
				java.io.File f = new java.io.File(uri);
				return f.getAbsolutePath();
			}
			java.io.File f = new java.io.File(mrl);
			if (f.exists()) {
				return f.getAbsolutePath();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mrl; // maybe a network URL or unsupported scheme
	}

	private static ImageIcon loadApplicationLogo() {
		if (applicationLogo != null && applicationLogo.getIconWidth() > 0 && applicationLogo.getIconHeight() > 0) {
			return applicationLogo;
		}
		String[] candidates = { "/icons/llg-logo.png", "/icons/llg-logo.jpg", "/icons/llg-logo.jpeg",
				"/icons/llg-logo.ico" };
		for (String candidate : candidates) {
			URL resource = App.class.getResource(candidate);
			if (resource != null) {
				applicationLogo = new ImageIcon(resource);
				break;
			}
		}
		if (applicationLogo == null) {
			applicationLogo = new ImageIcon();
		}
		return applicationLogo;
	}

	private static ImageIcon getScaledApplicationLogo(int maxSize) {
		ImageIcon base = loadApplicationLogo();
		if (base.getIconWidth() <= 0 || base.getIconHeight() <= 0) {
			return null;
		}
		int width = base.getIconWidth();
		int height = base.getIconHeight();
		if (maxSize <= 0) {
			return base;
		}
		int maxDimension = Math.max(width, height);
		if (maxDimension <= maxSize) {
			return base;
		}
		double scale = (double) maxSize / (double) maxDimension;
		int scaledWidth = Math.max(1, (int) Math.round(width * scale));
		int scaledHeight = Math.max(1, (int) Math.round(height * scale));
		Image scaled = base.getImage().getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
		return new ImageIcon(scaled);
	}

	private static void ensureBackgroundImageLoaded() {
		if (backgroundImage != null) {
			return;
		}
		String[] candidates = { "/icons/llg-background.jpg", "/icons/llg-background.png", "/icons/llg-logo.png",
				"/icons/llg-logo.jpg" };
		for (String candidate : candidates) {
			URL resource = App.class.getResource(candidate);
			if (resource != null) {
				backgroundImage = new ImageIcon(resource).getImage();
				break;
			}
		}
		if (backgroundImage == null) {
			int w = 1920;
			int h = 1080;
			java.awt.image.BufferedImage generated = new java.awt.image.BufferedImage(w, h,
					java.awt.image.BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2 = generated.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			GradientPaint g1 = new GradientPaint(0, 0, BRAND_PRIMARY, w, h / 2f, BRAND_SECONDARY);
			GradientPaint g2paint = new GradientPaint(0, h / 2f, new Color(15, 23, 42), w, h, BRAND_ACCENT);
			g2.setPaint(g1);
			g2.fillRect(0, 0, w, h);
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
			g2.setPaint(g2paint);
			g2.fillRect(0, 0, w, h);
			g2.dispose();
			backgroundImage = generated;
		}
	}

	private static void updateBackgroundScale(int width, int height) {
		if (backgroundImage == null || width <= 0 || height <= 0) {
			return;
		}
		if (scaledBackgroundImage != null && scaledBackgroundWidth == width && scaledBackgroundHeight == height) {
			return;
		}
		double scale = Math.max((double) width / backgroundImage.getWidth(null),
				(double) height / backgroundImage.getHeight(null));
		int scaledW = Math.max(1, (int) Math.round(backgroundImage.getWidth(null) * scale));
		int scaledH = Math.max(1, (int) Math.round(backgroundImage.getHeight(null) * scale));
		scaledBackgroundImage = backgroundImage.getScaledInstance(scaledW, scaledH, Image.SCALE_SMOOTH);
		scaledBackgroundWidth = width;
		scaledBackgroundHeight = height;
	}

	private static final class BackgroundPanel extends JPanel {
		BackgroundPanel() {
			setOpaque(false);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (bookReaderFullscreenActive) {
				return;
			}
			ensureBackgroundImageLoaded();
			paintAmbientBackground(g);
		}

		private void paintAmbientBackground(Graphics g) {
			updateBackgroundScale(getWidth(), getHeight());
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			if (scaledBackgroundImage != null) {
				int imgW = scaledBackgroundImage.getWidth(null);
				int imgH = scaledBackgroundImage.getHeight(null);
				if (imgW > 0 && imgH > 0) {
					int x = (getWidth() - imgW) / 2;
					int y = (getHeight() - imgH) / 2;
					g2.drawImage(scaledBackgroundImage, x, y, null);
				} else {
					g2.drawImage(scaledBackgroundImage, 0, 0, getWidth(), getHeight(), null);
				}
			}
			float overlayAlpha = 0.35f + 0.2f * (float) Math.sin(ambientPhase);
			if (overlayAlpha < 0f) {
				overlayAlpha = 0f;
			}
			if (overlayAlpha > 1f) {
				overlayAlpha = 1f;
			}
			g2.setComposite(AlphaComposite.SrcOver.derive(overlayAlpha));
			g2.setColor(PANEL_OVERLAY);
			g2.fillRect(0, 0, getWidth(), getHeight());
			g2.dispose();
		}
	}

	private static void startAmbientAnimation() {
		if (ambientTimer != null) {
			return;
		}
		ambientTimer = new Timer(40, e -> {
			ambientPhase += 0.02;
			if (ambientPhase > Math.PI * 2) {
				ambientPhase -= Math.PI * 2;
			}
			if (frame != null) {
				frame.repaint();
			}
		});
		ambientTimer.setRepeats(true);
		ambientTimer.start();
	}

	private static void addViewButton(String viewName) {
		if (viewButtons.containsKey(viewName) || "hub".equals(viewName))
			return;

		String displayName = Arrays.stream(viewName.split("\\s+"))
				.map(word -> word.substring(0, 1).toUpperCase() + word.substring(1)).collect(Collectors.joining(" "));

		JButton button = new JButton(displayName);
		styleTaskbarButton(button, viewName.equals(activeTaskName.toLowerCase().replace(" ", "")));

		// Create a panel with the button and close button
		final JPanel[] buttonPanelHolder = new JPanel[1];
		buttonPanelHolder[0] = createTaskbarButtonPanel(viewName, button, () -> {
			// Close button action
			viewButtons.remove(viewName);
			openedViews.remove(viewName);
			taskbarButtonsPanel.remove(buttonPanelHolder[0]);
			taskbarButtonsPanel.revalidate();
			taskbarButtonsPanel.repaint();

			// If we're closing the active view, switch to Hub
			if (viewName.equals(activeTaskName.toLowerCase().replace(" ", ""))) {
				mainViewLayout.show(mainViewPanel, "hub");
				setActiveTask("Hub");
			}
		});

		button.addActionListener(e -> {
			if (viewName.startsWith("PDF: ")) {
				// Handle PDF view switching
				if (cardLayout != null && cards != null) {
					// Show the PDF card
					cardLayout.show(cards, viewName);
					setActiveTask(viewName);
					// Make sure the book reader view is active
					if (mainViewLayout != null && mainViewPanel != null) {
						mainViewLayout.show(mainViewPanel, "bookreader");
					}
				}
			} else {
				switch (viewName) {
					case "mediaplayer":
						showMediaPlayerView();
						break;
					case "bookreader":
						openBookReaderView();
						break;
					case "youtube":
						openYouTubeView();
						break;
					case "vimeo":
						openVimeoView();
						break;
					case "gomovies":
						openGoMoviesView();
						break;
					case "torrent":
						openTorrentView();
						break;
					default:
						if (mainViewLayout != null && mainViewPanel != null) {
							mainViewLayout.show(mainViewPanel, viewName);
							setActiveTask(viewName);
						}
						break;
				}
			}
		});

		viewButtons.put(viewName, button);
		taskbarButtonsPanel.add(buttonPanelHolder[0]);
		taskbarButtonsPanel.revalidate();
		taskbarButtonsPanel.repaint();
	}

	private static void setActiveTask(String taskName) {
		String normalized = taskName == null || taskName.isBlank() ? "Hub" : taskName;
		String viewKey = normalized.toLowerCase().replace(" ", "");

		// Add to opened views if not the Hub
		if (!"hub".equals(viewKey)) {
			openedViews.add(viewKey);
		}

		activeTaskName = normalized;
		updateTaskbarButtons();

		// Request focus on the corresponding button if it exists
		SwingUtilities.invokeLater(() -> {
			JButton button = viewButtons.get(viewKey);
			if (button != null && button.isDisplayable()) {
				button.requestFocusInWindow();
				// Also ensure the button is visible in the scrollable panel
				if (button.getParent() != null && button.getParent().getParent() instanceof JViewport) {
					JViewport viewport = (JViewport) button.getParent().getParent();
					Rectangle viewRect = viewport.getViewRect();
					Rectangle buttonRect = button.getBounds();

					// If button is not fully visible, scroll to make it visible
					if (!viewRect.contains(buttonRect)) {
						// Center the button in the viewport
						Rectangle targetRect = new Rectangle(buttonRect.x - viewRect.width / 2 + buttonRect.width / 2,
								buttonRect.y, buttonRect.width, buttonRect.height);
						button.scrollRectToVisible(targetRect);
					}
				}
			}
		});
	}

	private static void updateStatus(String text) {
		if (statusLabel == null) {
			return;
		}
		statusLabel.setText(text == null || text.isBlank() ? "Ready" : text);
	}

	private static void initializeYouTubeScene() {
		if (youtubePanel == null) {
			return;
		}
		youtubeReady = false;
		youtubeEngine = null;

		SwingUtilities.invokeLater(() -> {
			updateStatus("Loading YouTube...");
			if (youtubeLyricsButton != null) {
				youtubeLyricsButton.setEnabled(false);
			}
		});

		ensureJavaFxRuntime();
		Platform.runLater(() -> {
			WebView webView = new WebView();
			WebEngine engine = WebViewLogger.configureWebViewLogging(webView, WebViewLogger.LogSource.YOUTUBE);
			youtubeEngine = engine;
			javafx.scene.Scene scene = new javafx.scene.Scene(webView);
			youtubePanel.setScene(scene);

			engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
				if (newState == Worker.State.SUCCEEDED) {
					youtubeReady = true;
					SwingUtilities.invokeLater(() -> {
						if (youtubeLyricsButton != null) {
							youtubeLyricsButton.setEnabled(true);
						}
						updateStatus("YouTube ready");
					});
				} else if (newState == Worker.State.FAILED || newState == Worker.State.CANCELLED) {
					youtubeReady = false;
					SwingUtilities.invokeLater(() -> {
						if (youtubeLyricsButton != null) {
							youtubeLyricsButton.setEnabled(false);
						}
						updateStatus("Unable to load YouTube");
					});
				}
			});

			try {
				engine.load(YOUTUBE_URL);
			} catch (Exception e) {
				System.err.println("Error loading YouTube: " + e.getMessage());
				e.printStackTrace();
				youtubeReady = false;
				SwingUtilities.invokeLater(() -> {
					if (youtubeLyricsButton != null) {
						youtubeLyricsButton.setEnabled(false);
					}
					updateStatus("Error loading YouTube");
				});
			}
		});
	}

	private static void initializeVimeoScene() {
		if (vimeoPanel == null) {
			return;
		}
		vimeoReady = false;
		vimeoEngine = null;

		SwingUtilities.invokeLater(() -> {
			updateStatus("Loading Vimeo...");
			if (vimeoLyricsButton != null) {
				vimeoLyricsButton.setEnabled(false);
			}
		});

		ensureJavaFxRuntime();
		Platform.runLater(() -> {
			try {
				WebView webView = new WebView();
				WebEngine engine = WebViewLogger.configureWebViewLogging(webView, WebViewLogger.LogSource.VIMEO);
				vimeoEngine = engine;
				javafx.scene.Scene scene = new javafx.scene.Scene(webView);
				vimeoPanel.setScene(scene);

				engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
					if (newState == Worker.State.SUCCEEDED) {
						vimeoReady = true;
						SwingUtilities.invokeLater(() -> {
							if (vimeoLyricsButton != null) {
								vimeoLyricsButton.setEnabled(true);
							}
							updateStatus("Vimeo ready");
						});
					} else if (newState == Worker.State.FAILED || newState == Worker.State.CANCELLED) {
						vimeoReady = false;
						SwingUtilities.invokeLater(() -> {
							if (vimeoLyricsButton != null) {
								vimeoLyricsButton.setEnabled(false);
							}
							updateStatus("Unable to load Vimeo");
						});
					}
				});

				try {
					engine.load(VIMEO_URL);
				} catch (Exception e) {
					System.err.println("Error loading Vimeo: " + e.getMessage());
					e.printStackTrace();
					vimeoReady = false;
					SwingUtilities.invokeLater(() -> {
						if (vimeoLyricsButton != null) {
							vimeoLyricsButton.setEnabled(false);
						}
						updateStatus("Error loading Vimeo");
					});
				}
			} catch (Exception e) {
				System.err.println("Error initializing Vimeo: " + e.getMessage());
				e.printStackTrace();
			}
		});
	}

	private static void showVimeoSubtitlesDialog() {
		if (frame == null) {
			return;
		}
		SubtitleSettingsDialog.showDialog(frame, mediaPlayerListComponent);
	}

	private static void showYouTubeSubtitlesDialog() {
		if (frame == null) {
			return;
		}
		SubtitleSettingsDialog.showDialog(frame, mediaPlayerListComponent);
	}

	private static void initializeGoMoviesScene() {
		if (goMoviesPanel == null) {
			return;
		}
		goMoviesReady = false;
		goMoviesEngine = null;

		SwingUtilities.invokeLater(() -> {
			updateStatus("Loading GoMovies...");
			if (goMoviesLyricsButton != null) {
				goMoviesLyricsButton.setEnabled(false);
			}
		});

		ensureJavaFxRuntime();
		Platform.runLater(() -> {
			try {
				WebView webView = new WebView();
				WebEngine engine = WebViewLogger.configureWebViewLogging(webView, WebViewLogger.LogSource.GOMOVIES);
				goMoviesEngine = engine;
				javafx.scene.Scene scene = new javafx.scene.Scene(webView);
				goMoviesPanel.setScene(scene);

				engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
					if (newState == Worker.State.SUCCEEDED) {
						goMoviesReady = true;
						SwingUtilities.invokeLater(() -> {
							if (goMoviesLyricsButton != null) {
								goMoviesLyricsButton.setEnabled(true);
							}
							updateStatus("GoMovies ready");
						});
					} else if (newState == Worker.State.FAILED || newState == Worker.State.CANCELLED) {
						goMoviesReady = false;
						SwingUtilities.invokeLater(() -> {
							if (goMoviesLyricsButton != null) {
								goMoviesLyricsButton.setEnabled(false);
							}
							updateStatus("Unable to load GoMovies");
						});
					}
				});

				try {
					engine.load(GO_MOVIES_URL);
				} catch (Exception e) {
					System.err.println("Error loading GoMovies: " + e.getMessage());
					e.printStackTrace();
					goMoviesReady = false;
					SwingUtilities.invokeLater(() -> {
						if (goMoviesLyricsButton != null) {
							goMoviesLyricsButton.setEnabled(false);
						}
						updateStatus("Error loading GoMovies");
					});
				}
			} catch (Exception e) {
				System.err.println("Error initializing GoMovies: " + e.getMessage());
				e.printStackTrace();
			}
		});
	}

	private static void showGoMoviesSubtitlesDialog() {
		if (frame == null) {
			return;
		}
		SubtitleSettingsDialog.showDialog(frame, mediaPlayerListComponent);
	}

	private static boolean isSubtitlePath(String path) {
		if (path == null) {
			return false;
		}
		String lower = path.toLowerCase();
		return lower.endsWith(".srt") || lower.endsWith(".ass") || lower.endsWith(".ssa") ||
				lower.endsWith(".vtt") || lower.endsWith(".lrc");
	}

	private static boolean isUsableMediaKey(String key) {
		if (key == null || key.isBlank())
			return false;
		if (key.contains("://"))
			return true;
		java.io.File f = new java.io.File(key);
		return f.isAbsolute();
	}

	private static String resolveActiveMediaMrl() {
		String candidate = null;
		try {
			uk.co.caprica.vlcj.media.InfoApi info = mediaPlayerListComponent == null ? null
					: mediaPlayerListComponent.mediaPlayer().media().info();
			if (info != null) {
				candidate = info.mrl();
			}
		} catch (Exception ignore) {
		}
		if (candidate != null && !candidate.isBlank() && !isSubtitlePath(candidate)) {
			return normalizeMrl(candidate);
		}
		javax.swing.ListModel<?> model = playlistView == null ? null : playlistView.getModel();
		int selIdx = playlistView == null ? -1 : playlistView.getSelectedIndex();
		if (model != null && model.getSize() > 0) {
			int n = model.getSize();
			if (selIdx < 0 || selIdx >= n)
				selIdx = 0;
			int left = selIdx;
			int right = selIdx + 1;
			while (left >= 0 || right < n) {
				if (left >= 0) {
					Object v = model.getElementAt(left);
					if (v != null) {
						String s = v.toString();
						if (!isSubtitlePath(s)) {
							candidate = s;
							break;
						}
					}
					left--;
				}
				if (right < n) {
					Object v = model.getElementAt(right);
					if (v != null) {
						String s = v.toString();
						if (!isSubtitlePath(s)) {
							candidate = s;
							break;
						}
					}
					right++;
				}
			}
			if ((candidate == null || candidate.isBlank()) && currentMediaIndex >= 0 && currentMediaIndex < n) {
				Object v = model.getElementAt(currentMediaIndex);
				if (v != null) {
					String s = v.toString();
					if (!isSubtitlePath(s)) {
						candidate = s;
					}
				}
			}
		}
		if ((candidate == null || candidate.isBlank()) && FileDropTargetListener.filePath != null
				&& !FileDropTargetListener.filePath.isBlank() && !isSubtitlePath(FileDropTargetListener.filePath)) {
			candidate = FileDropTargetListener.filePath;
		}
		if (candidate == null || candidate.isBlank()) {
			return candidate;
		}
		return normalizeMrl(candidate);
	}

	static void registerSubtitleForCurrentMedia(String subtitlePath, List<SrtParser.Subtitle> subtitles,
			EmbeddedMediaPlayerComponent mediaComponent) {
		if (subtitles == null || mediaComponent == null)
			return;

		addSubtitleListener(mediaComponent, subtitles);
		if (subtitleButton != null)
			subtitleButton.setVisible(true);
		String mediaMrl = resolveActiveMediaMrl();
		if ((mediaMrl == null || mediaMrl.isBlank()) && FileDropTargetListener.filePath != null
				&& !FileDropTargetListener.filePath.isBlank()) {
			mediaMrl = normalizeMrl(FileDropTargetListener.filePath);
		}
		if (mediaMrl == null || mediaMrl.isBlank())
			return;
		String normalizedMediaMrl = normalizeMrl(mediaMrl);
		if (!isUsableMediaKey(normalizedMediaMrl) && FileDropTargetListener.filePath != null
				&& !FileDropTargetListener.filePath.isBlank()) {
			normalizedMediaMrl = normalizeMrl(FileDropTargetListener.filePath);
		}
		if (!isUsableMediaKey(normalizedMediaMrl))
			return;
		SubtitleAssociation association = new SubtitleAssociation(normalizeMrl(subtitlePath), subtitles);
		subtitleAssociations.put(normalizedMediaMrl, association);
	}

	private static void applyAssociatedSubtitle(String mediaMrl) {
		if (mediaMrl == null || mediaMrl.isBlank())
			return;
		String normalized = normalizeMrl(mediaMrl);
		if (normalized == null || normalized.isBlank())
			return;
		SubtitleAssociation association = subtitleAssociations.get(normalized);
		if (association == null && playlistView != null) {
			int index = playlistView.getSelectedIndex();
			if (index >= 0) {
				javax.swing.ListModel<?> model = playlistView.getModel();
				Object element = index < model.getSize() ? model.getElementAt(index) : null;
				if (element != null) {
					String candidate = normalizeMrl(element.toString());
					association = subtitleAssociations.get(candidate);
				}
			}
		}
		if (association == null || mediaPlayerListComponent == null)
			return;
		addSubtitleListener(mediaPlayerListComponent, association.subtitles);
		if (subtitleButton != null)
			subtitleButton.setVisible(true);
	}

	private static final int FRAME_WIDTH = 1000;
	private static final int FRAME_HEIGHT = 600;
	private static final int VOLUME_MAX = 200;
	private static final int SKIP_MS = 10_000;
	private static final int TIMER_DELAY_MS = 100;
	private static final int PERCENT_SCALE = 100;

	// Subtitle sync offset (ms). Positive delays the subtitle, negative advances
	// it.
	private static volatile long subtitleOffsetMs = 0L;

	public static long getSubtitleOffsetMs() {
		return subtitleOffsetMs;
	}

	public static void adjustSubtitleOffset(long deltaMs) {
		subtitleOffsetMs += deltaMs;
	}

	public static void resetSubtitleOffset() {
		subtitleOffsetMs = 0L;
	}

	private static void applyPlaybackMode() {
		if (mediaPlayerListComponent == null) {
			return;
		}
		javax.swing.ListModel<?> model = playlistView == null ? null : playlistView.getModel();
		int count = model == null ? 0 : model.getSize();
		if (loopItemIndex < 0 || count == 0) {
			loopItemIndex = -1;
			mediaPlayerListComponent.mediaListPlayer().controls().setMode(PlaybackMode.LOOP);
			return;
		}
		if (loopItemIndex >= count) {
			loopItemIndex = count - 1;
		}
		mediaPlayerListComponent.mediaListPlayer().controls().setMode(PlaybackMode.REPEAT);
	}

	public App() {
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				cleanupResources();
			}
		});

	}

	/**
	 * Comprehensive cleanup of all resources to prevent memory leaks and crashes
	 */
	private static void cleanupResources() {
		LOGGER.info("Starting comprehensive resource cleanup...");

		try {
			// Stop ambient animation timer
			if (ambientTimer != null) {
				ambientTimer.stop();
				ambientTimer = null;
			}

			// Stop books directory watcher
			stopBooksDirectoryWatcher();

			// Clean up media player components
			cleanupMediaPlayerComponents();

			// Clean up torrent streamer
			cleanupTorrentStreamer();

			// Clean up JavaFX resources
			cleanupJavaFxResources();

			LOGGER.info("Resource cleanup completed");
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error during resource cleanup: " + e.getMessage(), e);
		}
	}

	/**
	 * Clean up all media player components safely
	 */
	private static void cleanupMediaPlayerComponents() {
		try {
			// Clean up main media player list component
			if (mediaPlayerListComponent != null) {
				try {
					// Stop playback first
					if (mediaPlayerListComponent.mediaPlayer() != null) {
						mediaPlayerListComponent.mediaPlayer().controls().stop();
						// Give it a moment to stop
						Thread.sleep(100);
					}
					mediaPlayerListComponent.release();
				} catch (Exception e) {
					LOGGER.log(Level.WARNING, "Error releasing media player list component: " + e.getMessage(), e);
				} finally {
					mediaPlayerListComponent = null;
				}
			}

			// Clean up torrent media player
			if (torrentMediaPlayer != null) {
				try {
					// Stop playback first
					if (torrentMediaPlayer.mediaPlayer() != null) {
						torrentMediaPlayer.mediaPlayer().controls().stop();
						Thread.sleep(100);
					}
					torrentMediaPlayer.release();
				} catch (Exception e) {
					LOGGER.log(Level.WARNING, "Error releasing torrent media player: " + e.getMessage(), e);
				} finally {
					torrentMediaPlayer = null;
				}
			}

			// Clean up subtitle listener component
			if (subtitleListenerComponent != null) {
				try {
					detachSubtitleListener();
				} catch (Exception e) {
					LOGGER.log(Level.WARNING, "Error detaching subtitle listener: " + e.getMessage(), e);
				}
			}

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error during media player cleanup: " + e.getMessage(), e);
		}
	}

	/**
	 * Clean up torrent streamer resources
	 */
	private static void cleanupTorrentStreamer() {
		if (libtorrentStreamer != null) {
			try {
				libtorrentStreamer.close();
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, "Error closing torrent streamer: " + e.getMessage(), e);
			} finally {
				libtorrentStreamer = null;
			}
		}
	}

	/**
	 * Clean up JavaFX resources
	 */
	private static void cleanupJavaFxResources() {
		try {
			// Clean up WebEngine references
			bookReaderEngine = null;
			youtubeEngine = null;
			vimeoEngine = null;
			goMoviesEngine = null;

			// Reset JavaFX state flags
			bookReaderReady = false;
			youtubeReady = false;
			vimeoReady = false;
			goMoviesReady = false;
			javafxRuntimeInitialized.set(false);

		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Error during JavaFX cleanup: " + e.getMessage(), e);
		}
	}

	// Decompress a .gz file to the same directory
	public static void decompressGzipFileHere(String gzipFilePath) {
		Path inputPath = Paths.get(gzipFilePath);
		String fileName = inputPath.getFileName().toString();
		if (!fileName.endsWith(".gz")) {
			System.err.println("Input file is not a .gz file.");
			return;
		}
		String outputFileName = fileName.substring(0, fileName.length() - 3);
		Path outputPath = inputPath.getParent().resolve(outputFileName);
		System.out.println("Decompressing " + inputPath + " to " + outputPath);
		try (GZIPInputStream gis = new GZIPInputStream(new FileInputStream(inputPath.toFile()));
				FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
			byte[] buffer = new byte[1024];
			int len;
			while ((len = gis.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
			}
			System.out.println("Decompression successful.");
		} catch (IOException e) {
			System.err.println("An error occurred during decompression: " + e.getMessage());
			e.printStackTrace();
		}
	}

	// Helper to reduce GridBagConstraints boilerplate
	private static GridBagConstraints gbc(int x, int y) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		return gbc;
	}

	private static String formatTime(long millis) {
		if (millis < 0) {
			return "00:00:00";
		}
		long seconds = millis / 1000;
		long hours = seconds / 3600;
		long minutes = (seconds % 3600) / 60;
		long remainingSeconds = seconds % 60;
		return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds);
	}

	// Simple HTML escaper for subtitle text
	private static String htmlEscape(String s) {
		if (s == null)
			return "";
		StringBuilder sb = new StringBuilder(s.length() + 16);
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
				case '&':
					sb.append("&amp;");
					break;
				case '<':
					sb.append("&lt;");
					break;
				case '>':
					sb.append("&gt;");
					break;
				case '"':
					sb.append("&quot;");
					break;
				case '\'':
					sb.append("&#39;");
					break;
				default:
					sb.append(c);
			}
		}
		return sb.toString();
	}

	static void addSubtitleListener(EmbeddedMediaPlayerComponent mediaComponent, List<SrtParser.Subtitle> subtitles) {
		detachSubtitleListener();
		clearSubtitleDisplay();
		subtitleListenerComponent = mediaComponent;

		MediaPlayerEventAdapter listener = new MediaPlayerEventAdapter() {
			@Override
			public void timeChanged(uk.co.caprica.vlcj.player.base.MediaPlayer mediaPlayer, long newTime) {

				long effectiveTime = newTime + subtitleOffsetMs;
				String subtitleToShow = "";
				for (SrtParser.Subtitle sub : subtitles) {
					if (effectiveTime >= sub.startTime && effectiveTime <= sub.endTime) {
						subtitleToShow = sub.text;
						break;
					}
				}

				String sanitized = subtitleToShow.replaceAll("\\r?\\n", " ").replaceAll("\\s+", " ");
				finalSubtitleToShow = sanitized;

				SwingUtilities.invokeLater(() -> {
					if (subtitleLabel != null) {
						if (finalSubtitleToShow.isEmpty()) {
							subtitleLabel.setText("");
							subtitleLabel.setVisible(false);
						} else {
							String html = "<html><div style='text-align:center;white-space:nowrap;overflow:hidden;'>"
									+ htmlEscape(finalSubtitleToShow) + "</div></html>";
							subtitleLabel.setText(html);
							subtitleLabel.setVisible(true);
							subtitleLabel.revalidate();
						}
					}
					if (!finalSubtitleToShow.equals(currentSubtitleText)) {
						currentSubtitleText = finalSubtitleToShow;
						myTextArea.setText(finalSubtitleToShow);
					}
					if (finalSubtitleToShow.isEmpty()) {
						myTextArea.setText("");
					}
				});
			}
		};
		activeSubtitleListener = listener;
		mediaComponent.mediaPlayer().events().addMediaPlayerEventListener(listener);
		if (subtitleButton != null)
			subtitleButton.setVisible(true);
	}

	private static synchronized void detachSubtitleListener() {
		if (subtitleListenerComponent != null && activeSubtitleListener != null) {
			subtitleListenerComponent.mediaPlayer().events().removeMediaPlayerEventListener(activeSubtitleListener);
		}
		subtitleListenerComponent = null;
		activeSubtitleListener = null;
	}

	private static void clearSubtitleDisplay() {
		currentSubtitleText = "";
		finalSubtitleToShow = "";
		SwingUtilities.invokeLater(() -> {
			if (myTextArea != null) {
				myTextArea.setText("");
			}
			if (subtitleLabel != null) {
				subtitleLabel.setText("");
				subtitleLabel.setVisible(false);
			}
		});
	}

	private static synchronized void resetSubtitleState() {
		detachSubtitleListener();
		clearSubtitleDisplay();
	}

	public static void downloadFileWithChooser(String urlString, String fileName) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setSelectedFile(new java.io.File(fileName + ".gz"));

		int result = fileChooser.showSaveDialog(null);

		if (result == JFileChooser.APPROVE_OPTION) {
			Path savePath = fileChooser.getSelectedFile().toPath();
			try {
				URL url = URI.create(urlString).toURL();
				try (InputStream in = url.openStream()) {
					Files.copy(in, savePath);
					System.out.println(fileChooser.getSelectedFile().getPath());
					String inputGzipFile = fileChooser.getSelectedFile().getPath();
					decompressGzipFileHere(inputGzipFile);
					try {
						boolean deleted = Files.deleteIfExists(Paths.get(inputGzipFile));
						if (deleted) {
							System.out.println("File deleted successfully: " + inputGzipFile.toString());
						} else {
							System.out.println("File not found or could not be deleted: " + inputGzipFile);
						}
					} catch (IOException e) {
						System.err.println("Failed to delete the file due to an I/O error: " + e.getMessage());
						e.printStackTrace();
					}
					JOptionPane.showMessageDialog(null, "Download of " + fileName + " complete!", "Download Complete",
							JOptionPane.INFORMATION_MESSAGE);
				}
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Download failed: " + e.getMessage(), "Download Error",
						JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}
	}

	public static String translate(String text, String sourceLang, String targetLang) throws IOException {
		// OkHttpClient automatically handles redirects by default.
		OkHttpClient httpClient = new OkHttpClient();

		String fullUrl = String.format("https://api.mymemory.translated.net/get?q=%s&langpair=%s|%s",
				URLEncoder.encode(text, StandardCharsets.UTF_8.toString()), sourceLang, targetLang);

		Request request = new Request.Builder().url(fullUrl).build();

		// The execute() method will follow the redirect before returning the final
		// response.
		try (Response response = httpClient.newCall(request).execute()) {
			if (!response.isSuccessful()) {
				// This code will execute only if the *final* request was unsuccessful.
				throw new IOException("Unexpected code " + response);
			}

			String responseBody = response.body().string();
			Gson gson = new Gson();
			JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
			JsonObject responseData = jsonObject.getAsJsonObject("responseData");
			// The string returned by getAsString() will be correctly decoded.
			return responseData.get("translatedText").getAsString();
		}
	}

	/**
	 * Opens a PDF file in the BookReader view.
	 * 
	 * @param filePath The absolute path to the PDF file
	 */
	private static void openPdfInBookReader(String filePath) {
		File pdfFile = new File(filePath);
		if (!pdfFile.exists() || !pdfFile.canRead()) {
			JOptionPane.showMessageDialog(frame, "Cannot read PDF file: " + filePath, "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		swingInvoke(() -> updateStatus("Processing PDF: " + pdfFile.getName()));
		swingInvoke(App::openBookReaderView);
		new Thread(() -> {
			try {
				String baseName = pdfFile.getName().replaceFirst("(?i)\\.pdf$", "");
				String folderName = sanitizeBookFolderName(baseName);
				Path bookFolder = BOOKS_BASE_DIR.resolve(folderName);
				File bookDirFile = bookFolder.toFile();
				boolean reuseExisting;
				List<String> imagePaths;
				if (isBookAlreadyConverted(bookFolder)) {
					List<String> cachedImages;
					try {
						cachedImages = collectExistingBookImages(bookFolder);
					} catch (IOException e) {
						cachedImages = List.of();
					}
					if (!cachedImages.isEmpty()) {
						reuseExisting = true;
						imagePaths = cachedImages;
					} else {
						reuseExisting = false;
						imagePaths = null;
					}
				} else {
					reuseExisting = false;
					imagePaths = null;
				}
				if (!reuseExisting) {
					if (!bookDirFile.exists()) {
						bookDirFile.mkdirs();
					} else {
						purgeDirectory(bookDirFile);
					}
					AtomicInteger pageCounter = new AtomicInteger();
					swingInvoke(() -> showPdfConversionProgress("Preparing conversion...", -1));
					imagePaths = PdfUtils.convertPdfToImages(pdfFile.getAbsolutePath(), bookDirFile.getAbsolutePath(),
							PDF_IMAGE_DPI, new PdfUtils.PdfConversionProgressListener() {
								@Override
								public void onStart(int totalPages) {
									SwingUtilities.invokeLater(
											() -> showPdfConversionProgress("Converting PDF pages...", totalPages));
								}

								@Override
								public void onPageComplete(int pageNumber, long elapsedMillis) {
									int completed = pageCounter.incrementAndGet();
									SwingUtilities.invokeLater(
											() -> updatePdfConversionProgress(completed, pageNumber, elapsedMillis));
								}

								@Override
								public void onCompleted(long totalMillis) {
									SwingUtilities.invokeLater(() -> updatePdfConversionProgress(-1, -1, totalMillis));
								}
							});
				}
				if (imagePaths == null || imagePaths.isEmpty()) {
					throw new IOException("No book assets available for PDF: " + pdfFile.getAbsolutePath());
				}
				String manifestJson = PdfUtils.generateBookReaderManifest(pdfFile.getAbsolutePath(), imagePaths,
						folderName);
				Files.writeString(bookFolder.resolve("manifest.json"), manifestJson, StandardCharsets.UTF_8);
				Path runtimeBookFolder = RUNTIME_BOOKS_DIR.resolve(folderName);
				Files.createDirectories(RUNTIME_BOOKS_DIR);
				if (!Files.exists(runtimeBookFolder) || !reuseExisting) {
					copyDirectory(bookFolder, runtimeBookFolder);
				}
				String htmlContent = PdfUtils.generateBookReaderHtml(pdfFile.getAbsolutePath(), imagePaths);
				String baseUrl = Paths.get("src", "main", "resources").toAbsolutePath().toUri().toString();
				String immersionEntryPoint = resolveBookReaderEntryPoint();
				String encodedFolder = URLEncoder.encode(folderName, StandardCharsets.UTF_8);
				String immersionUrl = immersionEntryPoint != null
						? immersionEntryPoint + (immersionEntryPoint.contains("?") ? "&" : "?") + "book="
								+ encodedFolder + "&ts=" + System.currentTimeMillis()
						: null;
				Platform.runLater(() -> {
					bookReaderReady = false;
					if (readAloudButton != null) {
						SwingUtilities.invokeLater(() -> readAloudButton.setEnabled(false));
					}
					WebEngine engine = bookReaderEngine;
					if (engine != null) {
						if (immersionUrl != null) {
							engine.load(immersionUrl);
						} else {
							engine.loadContent(htmlContent, baseUrl);
						}
					} else {
						SwingUtilities.invokeLater(() -> updateStatus("BookReader engine not initialized"));
					}
				});
				swingInvoke(() -> {
					hidePdfConversionProgress();
					updateStatus((reuseExisting ? "Loaded cached PDF: " : "Loaded PDF: ") + pdfFile.getName());
				});
			} catch (Exception e) {
				e.printStackTrace();
				swingInvoke(() -> {
					updateStatus("Error opening PDF: " + e.getMessage());
					hidePdfConversionProgress();
					JOptionPane.showMessageDialog(frame, "Error opening PDF: " + e.getMessage(), "Error",
							JOptionPane.ERROR_MESSAGE);
				});
			}
		}, "BookReader-PDF-Loader").start();
	}

	/**
	 * Copies a directory and all its contents from source to target.
	 * 
	 * @param sourceDir the source directory to copy from
	 * @param targetDir the target directory to copy to
	 * @throws IOException if an I/O error occurs
	 */
	private static void copyDirectory(Path sourceDir, Path targetDir) throws IOException {
		Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				Path targetPath = targetDir.resolve(sourceDir.relativize(dir));
				if (!Files.exists(targetPath)) {
					Files.createDirectories(targetPath);
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.copy(file, targetDir.resolve(sourceDir.relativize(file)),
						StandardCopyOption.REPLACE_EXISTING);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	/**
	 * 
	 * Shows a file chooser to select a PDF file and opens it in BookReader.
	 */
	private static void openPdfFileChooser() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Open PDF File");
		fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().toLowerCase().endsWith(".pdf");
			}

			@Override
			public String getDescription() {
				return "PDF Files (*.pdf)";
			}
		});

		int result = fileChooser.showOpenDialog(frame);
		if (result == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			openPdfInBookReader(selectedFile.getAbsolutePath());
		}
	}

	private static void purgeDirectory(File directory) {
		if (directory == null || !directory.exists()) {
			return;
		}
		File[] contents = directory.listFiles();
		if (contents == null) {
			return;
		}
		for (File entry : contents) {
			if (entry.isDirectory()) {
				purgeDirectory(entry);
			}
			if (!entry.delete()) {
				entry.deleteOnExit();
			}
		}
	}

	private static boolean isBookAlreadyConverted(Path bookFolder) {
		if (bookFolder == null || !Files.isDirectory(bookFolder)) {
			return false;
		}
		Path manifest = bookFolder.resolve("manifest.json");
		if (!Files.exists(manifest)) {
			return false;
		}
		try (Stream<Path> entries = Files.list(bookFolder)) {
			return entries.anyMatch(App::isSupportedBookAsset);
		} catch (IOException e) {
			return false;
		}
	}

	private static void openBookByFolderName(String folderName) {
		if (folderName == null || folderName.isBlank()) {
			return;
		}
		Path bookFolder = BOOKS_BASE_DIR.resolve(folderName);
		if (!Files.exists(bookFolder)) {
			swingInvoke(() -> updateStatus("Book assets missing for " + folderName));
			return;
		}
		try {
			swingInvoke(App::openBookReaderView);
			List<String> images = collectExistingBookImages(bookFolder);
			if (images.isEmpty()) {
				swingInvoke(() -> updateStatus("No pages available for " + folderName));
				return;
			}
			String htmlContent = PdfUtils.generateBookReaderHtml(folderName, images);
			String baseUrl = Paths.get("src", "main", "resources").toAbsolutePath().toUri().toString();
			String immersionEntry = resolveBookReaderEntryPoint();
			String encodedFolder = URLEncoder.encode(folderName, StandardCharsets.UTF_8);
			String immersionUrl = immersionEntry != null
					? immersionEntry + (immersionEntry.contains("?") ? "&" : "?") + "book="
							+ encodedFolder + "&ts=" + System.currentTimeMillis()
					: null;
			Platform.runLater(() -> {
				WebEngine engine = bookReaderEngine;
				if (engine != null) {
					bookReaderReady = false;
					if (readAloudButton != null) {
						SwingUtilities.invokeLater(() -> readAloudButton.setEnabled(false));
					}
					if (immersionUrl != null) {
						engine.load(immersionUrl);
					} else {
						engine.loadContent(htmlContent, baseUrl);
					}
				} else {
					swingInvoke(() -> updateStatus("BookReader engine not initialized"));
				}
			});
			swingInvoke(() -> updateStatus("Opened " + folderName));
		} catch (IOException e) {
			e.printStackTrace();
			swingInvoke(() -> updateStatus("Unable to open book: " + e.getMessage()));
		}
	}

	/**
	 * Process OCR on a book page image and return text data as JSON
	 * This method can be called from JavaScript via the BookReaderBridge
	 */
	public static String processBookPageOCR(String bookFolder, String imageFileName, int pageIndex) {
		try {
			LOGGER.info("=== JAVA OCR REQUEST ===");
			LOGGER.info("Book folder: " + bookFolder);
			LOGGER.info("Image file: " + imageFileName);
			LOGGER.info("Page index: " + pageIndex);
			LOGGER.info("Tess4JOCR initialized: " + Tess4JOCR.isInitialized());
			LOGGER.info("Tessdata path: " + Tess4JOCR.getTessdataPath());

			if (!Tess4JOCR.isInitialized()) {
				LOGGER.warning("Tesseract OCR not available, falling back to JavaScript OCR");
				return "{\"error\": \"Tesseract not initialized\", \"tessdataPath\": \"" + Tess4JOCR.getTessdataPath()
						+ "\"}";
			}

			String decodedImageFileName = imageFileName;
			try {
				decodedImageFileName = java.net.URLDecoder.decode(imageFileName, "UTF-8");
				if (!decodedImageFileName.equals(imageFileName)) {
					LOGGER.info("Decoded image filename: " + decodedImageFileName);
				}
			} catch (Exception e) {
				LOGGER.warning("Failed to decode image filename, using original: " + e.getMessage());
			}

			// Check runtime location first (where images are served from)
			Path runtimeBookPath = RUNTIME_BOOKS_DIR.resolve(bookFolder);
			Path bookPath;
			if (Files.exists(runtimeBookPath)) {
				bookPath = runtimeBookPath;
				LOGGER.info("Using runtime book path: " + bookPath);
			} else {
				bookPath = BOOKS_BASE_DIR.resolve(bookFolder);
				LOGGER.info("Using source book path: " + bookPath);
			}
			Path imagePath = bookPath.resolve(decodedImageFileName);

			if (!Files.exists(imagePath)) {
				// Try runtime location with decoded filename
				imagePath = runtimeBookPath.resolve(decodedImageFileName);
				if (!Files.exists(imagePath)) {
					// Try with original encoded filename as fallback
					imagePath = runtimeBookPath.resolve(imageFileName);
					if (!Files.exists(imagePath)) {
						LOGGER.warning(
								"Image file not found: " + decodedImageFileName + " (original: " + imageFileName + ")");
						return "{\"error\": \"Image file not found: " + decodedImageFileName + "\"}";
					}
				}
			}

			LOGGER.info("Processing OCR for book page: " + imagePath.toString());

			// Perform OCR
			List<net.sourceforge.tess4j.Word> words = Tess4JOCR.getWordsWithBoundingBoxes(imagePath.toFile());

			LOGGER.info("OCR completed, found " + words.size() + " words");

			// Convert to JSON format compatible with JavaScript
			StringBuilder json = new StringBuilder();
			json.append("{\"words\": [");

			for (int i = 0; i < words.size(); i++) {
				net.sourceforge.tess4j.Word word = words.get(i);
				if (i > 0)
					json.append(",");

				json.append("{");
				json.append("\"text\": \"").append(escapeJsonString(word.getText())).append("\",");
				json.append("\"x\": ").append(word.getBoundingBox().x).append(",");
				json.append("\"y\": ").append(word.getBoundingBox().y).append(",");
				json.append("\"width\": ").append(word.getBoundingBox().width).append(",");
				json.append("\"height\": ").append(word.getBoundingBox().height).append(",");
				json.append("\"confidence\": ").append(word.getConfidence());
				json.append("}");
			}

			json.append("], \"pageIndex\": ").append(pageIndex);
			json.append(", \"totalWords\": ").append(words.size());
			json.append("}");

			String result = json.toString();
			LOGGER.info("OCR JSON result length: " + result.length());
			LOGGER.info("=== JAVA OCR COMPLETE ===");

			return result;

		} catch (Exception e) {
			LOGGER.severe("OCR processing failed for page " + pageIndex + ": " + e.getMessage());
			e.printStackTrace();
			return "{\"error\": \"OCR processing failed: " + escapeJsonString(e.getMessage()) + "\"}";
		}
	}

	/**
	 * Escape special characters in JSON strings
	 */
	private static String escapeJsonString(String text) {
		if (text == null)
			return "";
		return text.replace("\\", "\\\\")
				.replace("\"", "\\\"")
				.replace("\n", "\\n")
				.replace("\r", "\\r")
				.replace("\t", "\\t");
	}

	private static List<String> collectExistingBookImages(Path bookFolder) throws IOException {
		if (bookFolder == null || !Files.isDirectory(bookFolder)) {
			return List.of();
		}
		try (Stream<Path> entries = Files.list(bookFolder)) {
			return entries.filter(App::isSupportedBookAsset)
					.sorted(Comparator.comparing(path -> path.getFileName().toString(), String.CASE_INSENSITIVE_ORDER))
					.map(Path::toAbsolutePath).map(Path::toString).collect(Collectors.toList());
		}
	}

	private static boolean isSupportedBookAsset(Path path) {
		if (path == null || !Files.isRegularFile(path)) {
			return false;
		}
		String name = path.getFileName().toString().toLowerCase(Locale.getDefault());
		for (String ext : BOOK_IMAGE_EXTENSIONS) {
			if (name.endsWith(ext)) {
				return true;
			}
		}
		return false;
	}

	private static String sanitizeBookFolderName(String name) {
		if (name == null || name.isBlank()) {
			return "book";
		}
		String sanitized = name.replaceAll("[^a-zA-Z0-9-_]", "_");
		return sanitized.isBlank() ? "book" : sanitized;
	}

	private static void swingInvoke(Runnable action) {
		if (action == null) {
			return;
		}
		if (SwingUtilities.isEventDispatchThread()) {
			action.run();
		} else {
			SwingUtilities.invokeLater(action);
		}
	}

	private static void promptDeleteBook() {
		swingInvoke(() -> {
			List<Path> bookFolders;
			try (Stream<Path> stream = Files.list(BOOKS_BASE_DIR)) {
				bookFolders = stream.filter(Files::isDirectory).sorted().limit(24).collect(Collectors.toList());
			} catch (IOException e) {
				updateStatus("Unable to list books: " + e.getMessage());
				JOptionPane.showMessageDialog(frame, "Unable to list books: " + e.getMessage(), "Delete Book",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (bookFolders.isEmpty()) {
				updateStatus("No books available to delete");
				JOptionPane.showMessageDialog(frame, "There are no books to delete.", "Delete Book",
						JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			List<String> folderNames = bookFolders.stream().map(path -> {
				Path name = path.getFileName();
				return name != null ? name.toString() : path.toString();
			}).collect(Collectors.toList());
			String selection = (String) JOptionPane.showInputDialog(frame,
					"Select a book to remove from your bookshelf:", "Delete Book", JOptionPane.PLAIN_MESSAGE, null,
					folderNames.toArray(new String[0]), folderNames.get(0));
			if (selection == null || selection.isBlank()) {
				return;
			}
			int confirm = JOptionPane.showConfirmDialog(frame, "Delete \"" + selection + "\" from your bookshelf?",
					"Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (confirm == JOptionPane.YES_OPTION) {
				deleteBook(selection);
			}
		});
	}

	private static void deleteBook(String folderName) {
		if (folderName == null || folderName.isBlank()) {
			return;
		}
		Path bookFolder = BOOKS_BASE_DIR.resolve(folderName);
		if (!Files.exists(bookFolder)) {
			swingInvoke(() -> updateStatus("Book assets missing for " + folderName));
			return;
		}
		swingInvoke(() -> updateStatus("Deleting " + folderName + "..."));
		CompletableFuture.runAsync(() -> {
			try {
				deleteDirectoryRecursive(bookFolder);
				Path runtimeFolder = Paths.get("target", "classes", "bookreader", "books", folderName);
				deleteDirectoryRecursive(runtimeFolder);
				refreshBookshelfView("Deleted " + folderName);
			} catch (IOException e) {
				e.printStackTrace();
				swingInvoke(() -> updateStatus("Unable to delete book: " + e.getMessage()));
			}
		});
	}

	private static void deleteDirectoryRecursive(Path path) throws IOException {
		if (path == null || !Files.exists(path)) {
			return;
		}
		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.deleteIfExists(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Files.deleteIfExists(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	public static final class BookReaderBridge {
		public void openBook(String folderName) {
			if (folderName == null || folderName.isBlank()) {
				return;
			}
			CompletableFuture.runAsync(() -> openBookByFolderName(folderName.trim()));
		}

		public void bookReaderEnterFullscreen() {
			SwingUtilities.invokeLater(App::enterBookReaderFullscreen);
		}

		public void bookReaderExitFullscreen() {
			SwingUtilities.invokeLater(App::exitBookReaderFullscreen);
		}

		/**
		 * Process OCR on a book page image and return text data as JSON
		 * This method can be called from JavaScript via the BookReaderBridge
		 */
		public String processBookPageOCR(String bookFolder, String imageFileName, int pageIndex) {
			return App.processBookPageOCR(bookFolder, imageFileName, pageIndex);
		}
	}

	private static void enterBookReaderFullscreen() {
		if (bookReaderFullscreenActive || frame == null) {
			return;
		}
		bookReaderFullscreenActive = true;
		savedFrameExtendedState = frame.getExtendedState();
		savedFrameResizable = frame.isResizable();
		savedFrameUndecorated = frame.isUndecorated();
		savedFrameAlwaysOnTop = frame.isAlwaysOnTop();
		if ((savedFrameExtendedState & Frame.MAXIMIZED_BOTH) != Frame.MAXIMIZED_BOTH) {
			savedFrameBounds = frame.getBounds();
		} else {
			savedFrameBounds = null;
		}
		savedContentBackground = frame.getContentPane().getBackground();
		if (mainViewPanel != null) {
			originalMainViewBackground = mainViewPanel.getBackground();
			mainViewPanel.setOpaque(true);
			mainViewPanel.setBackground(Color.BLACK);
		}
		if (bookReaderView != null) {
			originalBookReaderPanelBackground = bookReaderView.getBackground();
			bookReaderView.setOpaque(true);
			bookReaderView.setBackground(Color.BLACK);
		}
		if (bookReaderPanel != null) {
			bookReaderPanel.setBackground(Color.BLACK);
		}
		if (bookReaderHeader != null) {
			bookReaderHeader.setVisible(false);
		}
		if (taskbarPanel != null) {
			taskbarPanel.setVisible(false);
		}
		frame.getContentPane().setBackground(Color.BLACK);
		GraphicsConfiguration configuration = frame.getGraphicsConfiguration();
		fullscreenDevice = configuration != null ? configuration.getDevice() : null;
		if (fullscreenDevice != null && fullscreenDevice.isFullScreenSupported()) {
			if (!frame.isUndecorated()) {
				frame.dispose();
				frame.setUndecorated(true);
				frame.setResizable(false);
				frame.setVisible(true);

			}
			fullscreenDevice.setFullScreenWindow(frame);
		} else {
			boolean decorationChanged = false;
			if (!frame.isUndecorated()) {
				frame.dispose();
				frame.setUndecorated(true);
				decorationChanged = true;
			}
			frame.setResizable(false);
			Rectangle screenBounds = configuration != null ? configuration.getBounds()
					: GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
			frame.setBounds(screenBounds);
			frame.setExtendedState(Frame.MAXIMIZED_BOTH);
			frame.setAlwaysOnTop(true);
			if (decorationChanged) {
				frame.setVisible(true);
			}
		}
		frame.revalidate();
		frame.repaint();
		frame.toFront();
		frame.requestFocus();
	}

	private static void exitBookReaderFullscreen() {
		if (!bookReaderFullscreenActive || frame == null) {
			return;
		}
		bookReaderFullscreenActive = false;
		if (fullscreenDevice != null) {
			fullscreenDevice.setFullScreenWindow(null);
			fullscreenDevice = null;
		}
		if (taskbarPanel != null) {
			taskbarPanel.setVisible(true);
		}
		if (bookReaderHeader != null) {
			bookReaderHeader.setVisible(true);
		}
		if (mainViewPanel != null && originalMainViewBackground != null) {
			mainViewPanel.setBackground(originalMainViewBackground);
			mainViewPanel.setOpaque(false);
		}
		if (bookReaderView != null && originalBookReaderPanelBackground != null) {
			bookReaderView.setBackground(originalBookReaderPanelBackground);
			bookReaderView.setOpaque(false);
		}
		if (savedContentBackground != null) {
			frame.getContentPane().setBackground(savedContentBackground);
		}
		boolean decorationChanged = false;
		if (!savedFrameUndecorated) {
			frame.dispose();
			frame.setUndecorated(false);
			decorationChanged = true;
		}
		frame.setResizable(savedFrameResizable);
		if (savedFrameBounds != null) {
			frame.setBounds(savedFrameBounds);
		}
		frame.setExtendedState(savedFrameExtendedState);
		frame.setAlwaysOnTop(savedFrameAlwaysOnTop);
		if (decorationChanged) {
			frame.setVisible(true);

			// Process pending media file from command line arguments
			if (pendingMediaFile != null) {
				SwingUtilities.invokeLater(() -> {
					// Switch to media player view
					showMediaPlayerView();
					// Add the media file to the playlist and play it
					FileDropTargetListener.filePath = pendingMediaFile.getAbsolutePath();
					mediaPlayerListComponent.mediaListPlayer().list().media().add(pendingMediaFile.getAbsolutePath());
					playlistModel.addElement(pendingMediaFile.getName());
					playlistView.setSelectedIndex(playlistModel.getSize() - 1);
					mediaPlayerListComponent.mediaListPlayer().controls().play();
				});
			}
		}
		frame.revalidate();
		frame.repaint();
		frame.toFront();
		frame.requestFocus();
	}

	// private static void cleanupTorrentResources() {
	// if (libtorrentStreamer != null) {
	// try {
	// libtorrentStreamer.close();
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// libtorrentStreamer = null;
	// }
	// }

	public static void main(String[] args) {
		try {

			// Find the application base directory
			String appPathProp = System.getProperty("jpackage.app-path");
			File baseDir;
			if (appPathProp != null) {
				File exe = new File(appPathProp);
				baseDir = exe.getParentFile() != null ? exe.getParentFile() : new File(System.getProperty("user.dir"));
			} else {
				baseDir = new File(System.getProperty("user.dir"));
			}

			// Set up books directories relative to installation directory
			BOOKS_BASE_DIR = Paths.get(baseDir.getAbsolutePath(), "books");
			RUNTIME_BOOKS_DIR = Paths.get(baseDir.getAbsolutePath(), "target", "classes", "bookreader", "books");

			// Create bookreader folder in the working directory
			Path bookreaderDir = Paths.get(baseDir.getAbsolutePath(), "bookreader");
			try {
				Files.createDirectories(bookreaderDir);
			} catch (IOException e) {
				System.err.println("Failed to create bookreader directory: " + e.getMessage());
			}

			// Set up VLC paths
			String vlcPath = new File(baseDir, "resources").getAbsolutePath();
			String vlcPluginsPath = vlcPath + File.separator + "plugins";
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Failed to initialize VLC: " + e.getMessage(),
					"VLC Initialization Error", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}

		// // Handle command line arguments for opening media files directly
		// if (args.length > 0) {
		// String mediaPath = args[0];
		// File mediaFile = new File(mediaPath);
		// if (mediaFile.exists() && mediaFile.canRead()) {
		// // Store for later processing after UI is initialized
		// pendingMediaFile = mediaFile;

		// }
		// }

		// Process command line arguments after UI is fully initialized
		if (args.length > 0) {
			System.out.println("arguments are: " + args[0]);
			String mediaPath = args[0];
			File mediaFile = new File(mediaPath);
			if (mediaFile.exists() && mediaFile.canRead()) {
				SwingUtilities.invokeLater(() -> {
					// Switch to media player view
					showMediaPlayerView();
					// Add the media file to the playlist and play it
					FileDropTargetListener.filePath = mediaFile.getAbsolutePath();
					mediaPlayerListComponent.mediaListPlayer().list().media().add(mediaFile.getAbsolutePath());
					playlistModel.addElement(mediaFile.getName());
					playlistView.setSelectedIndex(playlistModel.getSize() - 1);
					mediaPlayerListComponent.mediaListPlayer().controls().play();
				});
			}
		}

		// Add shutdown hook for cleanup
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			// cleanupTorrentResources();
			// Add other cleanup tasks here if needed
		}));

		frame = new JFrame("LLG Media Center");
		ImageIcon frameIcon = loadApplicationLogo();
		if (frameIcon.getIconWidth() > 0 && frameIcon.getIconHeight() > 0) {
			frame.setIconImage(frameIcon.getImage());
		}
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
		frame.setLocationRelativeTo(null);

		ensureBackgroundImageLoaded();
		startAmbientAnimation();

		// Initialize card layout for PDF viewer
		cardLayout = new CardLayout();
		cards = new JPanel(cardLayout);

		mainViewLayout = new CardLayout();
		mainViewPanel = new JPanel(mainViewLayout) {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
			}
		};
		mainViewPanel.setOpaque(false);

		JPanel hubPanel = buildMediaCenterHub();
		mediaPlayerView = buildMediaPlayerView();
		bookReaderView = buildBookReaderView();
		youtubeView = buildYouTubeView();
		vimeoView = buildVimeoView();
		goMoviesView = buildGoMoviesView();
		torrentView = buildTorrentView();

		mainViewPanel.add(hubPanel, "hub");
		mainViewPanel.add(mediaPlayerView, "mediaplayer");
		mainViewPanel.add(bookReaderView, "bookreader");
		mainViewPanel.add(youtubeView, "youtube");
		mainViewPanel.add(vimeoView, "vimeo");
		mainViewPanel.add(goMoviesView, "gomovies");
		mainViewPanel.add(torrentView, "torrent");

		// Ensure the book reader view is properly initialized
		if (bookReaderView != null) {
			bookReaderView.setName("bookreader");
		}

		BackgroundPanel rootPanel = new BackgroundPanel();
		rootPanel.setLayout(new BorderLayout());
		rootPanel.add(mainViewPanel, BorderLayout.CENTER);
		taskbarPanel = createGlassPanel(new BorderLayout());
		taskbarPanel.setBorder(BorderFactory.createEmptyBorder(6, 18, 12, 18));

		// Create taskbar buttons panel
		taskbarButtonsPanel = buildTaskbarButtons();

		// Add status label
		statusLabel = new JLabel("Ready");
		statusLabel.setForeground(TEXT_PRIMARY);
		statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

		// Add components to taskbar
		JPanel leftPanel = new JPanel(new BorderLayout());
		leftPanel.setOpaque(false);
		leftPanel.add(taskbarButtonsPanel, BorderLayout.WEST);
		leftPanel.add(statusLabel, BorderLayout.CENTER);

		taskbarPanel.add(leftPanel, BorderLayout.WEST);
		rootPanel.add(taskbarPanel, BorderLayout.SOUTH);
		frame.setContentPane(rootPanel);
		mainViewLayout.show(mainViewPanel, "hub");
		setActiveTask("Hub");
		startBooksDirectoryWatcher();
		Runtime.getRuntime().addShutdownHook(new Thread(App::stopBooksDirectoryWatcher));
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent e) {
				stopBooksDirectoryWatcher();
			}

			@Override
			public void windowClosed(java.awt.event.WindowEvent e) {
				stopBooksDirectoryWatcher();
			}
		});
		frame.setVisible(true);
	}

	private static JPanel buildMediaCenterHub() {
		JPanel panel = new JPanel(new BorderLayout(16, 16));
		panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

		JPanel header = new JPanel(new BorderLayout());
		header.setOpaque(false);
		ImageIcon headerLogo = getScaledApplicationLogo(96);
		if (headerLogo != null) {
			JLabel logoLabel = new JLabel(headerLogo);
			logoLabel.setHorizontalAlignment(SwingConstants.LEFT);
			logoLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 16));
			header.add(logoLabel, BorderLayout.WEST);
		}
		JLabel title = new JLabel("LLG Media Center", SwingConstants.CENTER);
		title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
		header.add(title, BorderLayout.CENTER);
		panel.add(header, BorderLayout.NORTH);

		JPanel buttons = new JPanel(new GridLayout(2, 3, 24, 24));
		buttons.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

		buttons.add(createHubCard("Media Player", App::showMediaPlayerView));
		buttons.add(createHubCard("Book Reader", App::openBookReaderView));
		buttons.add(createHubCard("Torrent", App::openTorrentView));
		buttons.add(createHubCard("Vimeo", App::openVimeoView));
		buttons.add(createHubCard("Go Movies", App::openGoMoviesView));
		buttons.add(createHubCard("YouTube", App::openYouTubeView));

		panel.add(buttons, BorderLayout.CENTER);
		return panel;
	}

	private static JPanel buildBookReaderView() {
		JPanel container = new JPanel(new BorderLayout());
		container.setOpaque(false);
		container.setTransferHandler(new TransferHandler() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean canImport(TransferSupport support) {
				if (support == null || !support.isDrop()) {
					return false;
				}
				support.setDropAction(COPY);
				return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
			}

			@Override
			public boolean importData(TransferSupport support) {
				if (!canImport(support)) {
					return false;
				}
				try {
					Transferable transferable = support.getTransferable();
					@SuppressWarnings("unchecked")
					List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
					File pdf = files.stream().filter(File::isFile)
							.filter(file -> file.getName().toLowerCase(Locale.getDefault()).endsWith(".pdf"))
							.findFirst()
							.orElse(null);
					if (pdf != null) {
						String pdfPath = pdf.getAbsolutePath();
						SwingUtilities.invokeLater(() -> openPdfInBookReader(pdfPath));
						return true;
					}
					SwingUtilities.invokeLater(() -> updateStatus("Drop a PDF file to open in BookReader"));
				} catch (Exception ex) {
					ex.printStackTrace();
					SwingUtilities.invokeLater(() -> updateStatus("Unable to open dropped file"));
				}
				return false;
			}
		});
		JPanel toolbar = createGlassPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
		toolbar.setOpaque(false);
		JButton openPdfButton = new JButton("Open PDF");
		openPdfButton.addActionListener(e -> openPdfFileChooser());
		openPdfButton.setFocusPainted(false);
		openPdfButton.setContentAreaFilled(false);
		openPdfButton.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
		toolbar.add(openPdfButton);
		JButton openBookshelfButton = new JButton("Open Bookshelf");
		openBookshelfButton.addActionListener(e -> Platform.runLater(() -> {
			if (bookReaderEngine != null) {
				bookReaderReady = false;
				if (readAloudButton != null) {
					SwingUtilities.invokeLater(() -> readAloudButton.setEnabled(false));
				}
				bookReaderEngine.loadContent(generateBookshelfLandingHtml(), "text/html");
				SwingUtilities.invokeLater(() -> updateStatus("Browse your bookshelf"));
			}
		}));
		openBookshelfButton.setFocusPainted(false);
		openBookshelfButton.setContentAreaFilled(false);
		openBookshelfButton.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
		openBookshelfButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		openBookshelfButton.setFont(openBookshelfButton.getFont().deriveFont(Font.BOLD, 14f));
		toolbar.add(openBookshelfButton);
		JButton deleteBookButton = new JButton("Delete Book");
		deleteBookButton.addActionListener(e -> promptDeleteBook());
		deleteBookButton.setFocusPainted(false);
		deleteBookButton.setContentAreaFilled(false);
		deleteBookButton.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
		deleteBookButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		deleteBookButton.setFont(deleteBookButton.getFont().deriveFont(Font.BOLD, 14f));
		toolbar.add(deleteBookButton);
		openPdfButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		openPdfButton.setFont(openPdfButton.getFont().deriveFont(Font.BOLD, 14f));
		openPdfButton.setEnabled(true);
		JButton bookReaderFullScreenButton = new JButton("Full Screen");
		styleToolbarButton(bookReaderFullScreenButton);
		bookReaderFullScreenButton.addActionListener(e -> {
			mediaPlayerListComponent.mediaPlayer().fullScreen().toggle();
			SwingUtilities.invokeLater(() -> updateStatus("Full Screen"));
			SwingUtilities.invokeLater(() -> {
				boolean fullscreen = mediaPlayerListComponent.mediaPlayer().fullScreen().isFullScreen();
				bookReaderFullScreenButton.setText(fullscreen ? "Exit Full Screen" : "Full Screen");
				toolbar.setVisible(!fullscreen);
				if (taskbarPanel != null)
					taskbarPanel.setVisible(!fullscreen);
				if (taskbarButtonsPanel != null)
					taskbarButtonsPanel.setVisible(!fullscreen);
				if (statusLabel != null)
					statusLabel.setVisible(!fullscreen);
			});
		});
		toolbar.add(bookReaderFullScreenButton);
		KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		container.registerKeyboardAction(e -> {
			if (mediaPlayerListComponent == null)
				return;
			if (!mediaPlayerListComponent.mediaPlayer().fullScreen().isFullScreen())
				return;
			mediaPlayerListComponent.mediaPlayer().fullScreen().set(false);
			SwingUtilities.invokeLater(() -> {
				updateStatus("Exit Full Screen");
				bookReaderFullScreenButton.setText("Full Screen");
				toolbar.setVisible(true);
				if (taskbarPanel != null)
					taskbarPanel.setVisible(true);
				if (taskbarButtonsPanel != null)
					taskbarButtonsPanel.setVisible(true);
				if (statusLabel != null)
					statusLabel.setVisible(true);
			});
		}, escapeKeyStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		JButton readAloud = new JButton("Read This Book Aloud");
		readAloud.addActionListener(e -> triggerBookReaderReadAloud());
		readAloud.setFocusPainted(false);
		readAloud.setContentAreaFilled(false);
		readAloud.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
		readAloud.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		readAloud.setFont(readAloud.getFont().deriveFont(Font.BOLD, 14f));
		readAloud.setEnabled(false);
		readAloudButton = readAloud;
		toolbar.add(readAloud);
		JButton reloadButton = new JButton("Reload");
		reloadButton.addActionListener(e -> initializeBookReaderScene());
		reloadButton.setFocusPainted(false);
		reloadButton.setContentAreaFilled(false);
		reloadButton.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
		reloadButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		reloadButton.setFont(reloadButton.getFont().deriveFont(Font.BOLD, 14f));
		toolbar.add(reloadButton);

		JPanel progressPanel = createGlassPanel(new BorderLayout(8, 0));
		progressPanel.setOpaque(false);
		progressPanel.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
		pdfConversionProgressBar = new JProgressBar();
		pdfConversionProgressBar.setIndeterminate(true);
		pdfConversionProgressBar.setVisible(false);
		pdfConversionProgressBar.setPreferredSize(new Dimension(200, 14));
		pdfConversionStatusLabel = new JLabel("Preparing PDF...");
		pdfConversionStatusLabel.setForeground(TEXT_PRIMARY);
		pdfConversionStatusLabel.setVisible(false);
		progressPanel.add(pdfConversionStatusLabel, BorderLayout.WEST);
		progressPanel.add(pdfConversionProgressBar, BorderLayout.CENTER);

		JPanel header = new JPanel(new BorderLayout());
		header.setOpaque(false);
		header.add(toolbar, BorderLayout.NORTH);
		header.add(progressPanel, BorderLayout.SOUTH);
		bookReaderHeader = header;

		container.add(header, BorderLayout.NORTH);
		bookReaderPanel = new JFXPanel();
		bookReaderPanel.setTransferHandler(null);
		container.add(bookReaderPanel, BorderLayout.CENTER);
		initializeBookReaderScene();
		return container;
	}

	private static JPanel buildYouTubeView() {
		JPanel container = new JPanel(new BorderLayout());
		container.setOpaque(false);

		// Toolbar at the top
		JPanel toolbar = createGlassPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
		toolbar.setOpaque(false);

		// Reload button
		JButton reloadButton = new JButton("Reload");
		reloadButton.addActionListener(e -> initializeYouTubeScene());
		reloadButton.setFocusPainted(false);
		reloadButton.setContentAreaFilled(false);
		reloadButton.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
		reloadButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		reloadButton.setFont(reloadButton.getFont().deriveFont(Font.BOLD, 14f));
		toolbar.add(reloadButton);

		// Subtitles/Lyrics button
		JButton lyricsButton = new JButton("Subtitles / Lyrics");
		lyricsButton.addActionListener(e -> showYouTubeSubtitlesDialog());
		lyricsButton.setFocusPainted(false);
		lyricsButton.setContentAreaFilled(false);
		lyricsButton.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
		lyricsButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		lyricsButton.setFont(lyricsButton.getFont().deriveFont(Font.BOLD, 14f));
		lyricsButton.setEnabled(true);
		toolbar.add(lyricsButton);
		youtubeLyricsButton = lyricsButton;

		// Main content panel
		JPanel contentPanel = new JPanel(new BorderLayout());
		contentPanel.setOpaque(false);

		// YouTube panel
		youtubePanel = new JFXPanel();
		contentPanel.add(youtubePanel, BorderLayout.CENTER);

		// Subtitle panel at the bottom
		JPanel subtitlePanel = new JPanel(new BorderLayout());
		subtitlePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		subtitlePanel.setBackground(new Color(40, 40, 40));

		// Subtitle label
		// JLabel subtitleLabel = new JLabel(" ");
		// subtitleLabel.setForeground(Color.WHITE);
		// subtitleLabel.setFont(subtitleLabel.getFont().deriveFont(Font.BOLD, 14f));
		// subtitleLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		// subtitlePanel.add(subtitleLabel, BorderLayout.CENTER);
		// controlPanel.add(subtitleLabel, BorderLayout.CENTER);
		// Browse button for subtitles
		JButton browseButton = new JButton("Load Subtitles");
		browseButton.addActionListener(e -> {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileFilter(new FileNameExtensionFilter("Subtitle Files", "srt", "vtt", "ass"));
			int result = fileChooser.showOpenDialog(frame);
			if (result == JFileChooser.APPROVE_OPTION) {
				File subtitleFile = fileChooser.getSelectedFile();
				try {
					String content = new String(Files.readAllBytes(subtitleFile.toPath()), StandardCharsets.UTF_8);
					List<SrtParser.Subtitle> subtitles = SrtParser.parse(content);
					// Store subtitles for the current YouTube video
					String videoId = "youtube_" + System.currentTimeMillis(); // Generate a unique ID for the video
					subtitleAssociations.put(videoId,
							new SubtitleAssociation(subtitleFile.getAbsolutePath(), subtitles));
					// Show first subtitle if available
					if (!subtitles.isEmpty()) {
						subtitleLabel.setText(subtitles.get(0).text);

					}
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(frame, "Error loading subtitle file: " + ex.getMessage(), "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		browseButton.setFocusPainted(false);
		browseButton.setContentAreaFilled(false);
		browseButton.setForeground(Color.WHITE);
		browseButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
		browseButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.setOpaque(false);
		buttonPanel.add(browseButton);
		// subtitlePanel.add(buttonPanel, BorderLayout.EAST);
		// controlPanel.add(buttonPanel, BorderLayout.EAST);
		// Add components to container
		contentPanel.add(subtitlePanel, BorderLayout.SOUTH);
		container.add(toolbar, BorderLayout.NORTH);
		container.add(contentPanel, BorderLayout.CENTER);

		initializeYouTubeScene();
		return container;
	}

	private static JComponent createHubCard(String label, Runnable action) {
		return createHubButton(label, action);
	}

	private static JButton createHubButton(String label, Runnable action) {
		JButton button = new JButton(label) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				int width = getWidth();
				int height = getHeight();
				int inset = 6;
				int shadowOffset = 6;
				int arc = 32;

				// Drop shadow
				g2.setColor(new Color(0, 0, 0, 60));
				g2.fillRoundRect(inset + shadowOffset, inset + shadowOffset, width - 2 * inset - shadowOffset,
						height - 2 * inset - shadowOffset, arc, arc);

				ButtonModel model = getModel();
				Color topColor = model.isPressed() ? new Color(21, 101, 192)
						: (model.isRollover() ? new Color(66, 165, 245) : new Color(100, 181, 246));
				Color bottomColor = model.isPressed() ? new Color(30, 136, 229) : new Color(30, 136, 229);
				GradientPaint gradient = new GradientPaint(0, inset, topColor, 0, height - inset - shadowOffset,
						bottomColor);
				g2.setPaint(gradient);
				g2.fillRoundRect(inset, inset, width - 2 * inset - shadowOffset, height - 2 * inset - shadowOffset, arc,
						arc);

				g2.setColor(new Color(255, 255, 255, model.isPressed() ? 80 : 140));
				g2.setStroke(new BasicStroke(1.5f));
				g2.drawRoundRect(inset, inset, width - 2 * inset - shadowOffset, height - 2 * inset - shadowOffset, arc,
						arc);

				g2.dispose();
				super.paintComponent(g);
			}
		};
		button.setFont(button.getFont().deriveFont(Font.BOLD, 18f));
		button.setForeground(Color.WHITE);
		button.setContentAreaFilled(false);
		button.setBorderPainted(false);
		button.setFocusPainted(false);
		button.setOpaque(false);
		button.setRolloverEnabled(true);
		button.setHorizontalTextPosition(SwingConstants.CENTER);
		button.setVerticalTextPosition(SwingConstants.CENTER);
		button.setBorder(BorderFactory.createEmptyBorder(32, 24, 32, 24));
		button.addActionListener(e -> action.run());
		return button;
	}

	private static JPanel buildTaskbarButtons() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		panel.setOpaque(false);

		// Home button
		JButton homeButton = new JButton("Home");
		styleTaskbarButton(homeButton, "Hub".equals(activeTaskName));
		homeButton.addActionListener(e -> {
			if (mainViewLayout != null && mainViewPanel != null) {
				mainViewLayout.show(mainViewPanel, "hub");
				setActiveTask("Hub");
			}
		});
		viewButtons.put("hub", homeButton);
		panel.add(createTaskbarButtonPanel("hub", homeButton, () -> {
			mainViewLayout.show(mainViewPanel, "hub");
			setActiveTask("Hub");
		}));

		return panel;
	}

	private static JPanel createTaskbarButtonPanel(String viewName, JButton button, Runnable onClose) {
		JPanel panel = new JPanel(new BorderLayout(4, 0));
		panel.setOpaque(false);
		panel.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 4));

		button.setBorderPainted(false);
		button.setFocusPainted(false);
		button.setContentAreaFilled(false);
		button.setOpaque(false);

		// Create a clickable label for the close button
		JLabel closeLabel = new JLabel("×");
		closeLabel.setFont(closeLabel.getFont().deriveFont(Font.BOLD, 16f));
		closeLabel.setForeground(TEXT_PRIMARY);
		closeLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		closeLabel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

		// Add hover effects
		closeLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				closeLabel.setForeground(new Color(255, 100, 100));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				closeLabel.setForeground(TEXT_PRIMARY);
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				onClose.run();
			}
		});

		// Add components to panel
		panel.add(button, BorderLayout.CENTER);
		panel.add(closeLabel, BorderLayout.EAST);

		return panel;
	}

	private static void styleTaskbarButton(JButton button, boolean isActive) {
		button.setFont(button.getFont().deriveFont(Font.BOLD, 14f));
		button.setForeground(TEXT_PRIMARY);
		button.setContentAreaFilled(true);
		button.setBorderPainted(true);
		button.setFocusPainted(false);
		button.setOpaque(true);
		button.setBackground(isActive ? new Color(255, 255, 255, 60) : new Color(255, 255, 255, 30));
		button.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(255, 255, 255, 40), 1, true),
				BorderFactory.createEmptyBorder(4, 12, 4, 12)));

		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				if (!isActive) {
					button.setBackground(new Color(255, 255, 255, 80));
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				if (isActive) {
					button.setBackground(new Color(255, 255, 255, 60));
				} else {
					button.setBackground(new Color(255, 255, 255, 30));
				}
			}
		});
	}

	private static void updateTaskbarButtons() {
		if (taskbarButtonsPanel == null)
			return;

		// Update button styles
		for (Map.Entry<String, JButton> entry : viewButtons.entrySet()) {
			String viewName = entry.getKey();
			JButton button = entry.getValue();
			boolean isActive = viewName.equals(activeTaskName.toLowerCase().replace(" ", ""));
			styleTaskbarButton(button, isActive);
		}

		// Ensure all opened views have buttons
		for (String viewName : openedViews) {
			if (!viewButtons.containsKey(viewName)) {
				addViewButton(viewName);
			}
		}
	}

	private static final String YOUTUBE_URL = "https://www.youtube.com";
	private static final String VIMEO_URL = "https://vimeo.com/watch";
	private static final String GO_MOVIES_URL = "https://gomoviestv.to/";

	/**
	 * Initializes the BookReader scene with the specified WebView and WebEngine.
	 */
	private static void initializeBookReaderScene() {
		if (bookReaderPanel == null) {
			return;
		}

		// Initialize state
		bookReaderReady = false;
		bookReaderEngine = null;

		// Disable read-aloud button until ready
		SwingUtilities.invokeLater(() -> {
			if (readAloudButton != null) {
				readAloudButton.setEnabled(false);
			}
		});

		ensureJavaFxRuntime();

		// Run JavaFX operations on the JavaFX Application Thread
		Platform.runLater(() -> {
			try {
				// Set up the WebView and WebEngine
				WebView webView = new WebView();
				WebEngine engine = webView.getEngine();

				// Enable JavaScript
				engine.setJavaScriptEnabled(true);

				// Store the engine reference
				bookReaderEngine = engine;

				// Apply CSS to enable text selection and interaction
				String css = "* { " +
						"-webkit-user-select: text !important; " +
						"-moz-user-select: text !important; " +
						"-ms-user-select: text !important; " +
						"user-select: text !important; " +
						"cursor: auto !important; " +
						"pointer-events: auto !important; " +
						"}" +
						".BRtextOverlay, .BRtextOverlay * { " +
						"pointer-events: auto !important; " +
						"-webkit-user-select: text !important; " +
						"-moz-user-select: text !important; " +
						"-ms-user-select: text !important; " +
						"user-select: text !important; " +
						"}" +
						"div, span, p, a, h1, h2, h3, h4, h5, h6 { " +
						"user-select: text !important; " +
						"-webkit-user-select: text !important; " +
						"}";

				engine.setUserStyleSheetLocation("data:text/css;charset=utf-8;base64," +
						java.util.Base64.getEncoder().encodeToString(css.getBytes()));

				// Add a listener to inject text selection CSS when pages load
				engine.documentProperty().addListener((obs, oldDoc, newDoc) -> {
					if (newDoc != null) {
						// Inject CSS to make all text selectable
						engine.executeScript(
								"var style = document.createElement('style');" +
										"style.type = 'text/css';" +
										"style.innerHTML = '" + css.replace("'", "\\'") + "';" +
										"document.head.appendChild(style);");

						// Make sure all elements are selectable
						engine.executeScript(
								"function makeSelectable(element) {" +
										"  if (!element) return;" +
										"  element.style.setProperty('user-select', 'text', 'important');" +
										"  element.style.setProperty('-webkit-user-select', 'text', 'important');" +
										"  element.style.setProperty('pointer-events', 'auto', 'important');" +
										"  for (var i = 0; i < element.children.length; i++) {" +
										"    makeSelectable(element.children[i]);" +
										"  }" +
										"}" +
										"makeSelectable(document.body);");
					}
				});

				// Set the scene
				bookReaderPanel.setScene(new Scene(webView));

				// Add a listener for page load completion
				engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
					if (newState == Worker.State.SUCCEEDED) {
						try {
							engine.executeScript(
									"window.llgBookReader = window.BookReader && window.BookReader.instances ? window.BookReader.instances[0] : window.llgBookReader || null;");

							JSObject window = (JSObject) engine.executeScript("window");
							if (window != null) {
								window.setMember("llgBridge", BOOK_READER_BRIDGE);
							}
						} catch (Exception ignored) {
							// Ignore JavaScript errors
						}

						bookReaderReady = true;
						SwingUtilities.invokeLater(() -> {
							if (readAloudButton != null) {
								readAloudButton.setEnabled(true);
							}
							updateStatus("Book reader ready");
						});
					}
				});

				// Always load the bookshelf by default
				engine.loadContent(generateBookshelfLandingHtml(), "text/html");
				SwingUtilities.invokeLater(() -> updateStatus("Browse your bookshelf"));
			} catch (Exception e) {
				e.printStackTrace();
				SwingUtilities.invokeLater(() -> updateStatus("Error initializing book reader: " + e.getMessage()));
			}
		});
	}

	private static String resolveBookReaderEntryPoint() {
		System.out.println("Looking for BookReader entry point...");

		// Prefer filesystem in src/main/resources so dynamically generated assets are
		// visible
		Path sourcePath = Paths.get("src", "main", "resources", "bookreader", "BookReaderDemo", "immersion-mode.html");
		if (Files.exists(sourcePath)) {
			System.out.println("Found BookReader in filesystem: " + sourcePath.toAbsolutePath());
			return sourcePath.toUri().toString();
		} else {
			System.out.println("Not found in filesystem: " + sourcePath.toAbsolutePath());
		}

		// Check working directory
		Path workingDirPath = Paths.get("bookreader", "BookReaderDemo", "immersion-mode.html");
		if (Files.exists(workingDirPath)) {
			System.out.println("Found BookReader in working directory: " + workingDirPath.toAbsolutePath());
			return workingDirPath.toUri().toString();
		} else {
			System.out.println("Not found in working directory: " + workingDirPath.toAbsolutePath());
		}

		// Finally, fall back to classpath
		String resourcePath = "/bookreader/BookReaderDemo/immersion-mode.html";
		URL resource = App.class.getResource(resourcePath);
		if (resource != null) {
			System.out.println("Found BookReader in classpath: " + resource.toExternalForm());
			return resource.toExternalForm();
		} else {
			System.out.println("Not found in classpath: " + resourcePath);
		}

		System.out.println("BookReader entry point not found in any location");
		return null;
	}

	private static void ensureJavaFxRuntime() {
		if (javafxRuntimeInitialized.compareAndSet(false, true)) {
			try {
				Platform.startup(() -> {
				});
			} catch (IllegalStateException alreadyStarted) {
				// JavaFX runtime already initialized (e.g., by JFXPanel)
			}
		}
	}

	private static String generateBookshelfLandingHtml() {
		Path booksDir = BOOKS_BASE_DIR;
		List<Path> entries = Collections.emptyList();
		try (Stream<Path> stream = Files.list(booksDir)) {
			entries = stream.filter(Files::isDirectory).sorted().limit(24).collect(Collectors.toList());
		} catch (IOException ignored) {
		}

		String cards = entries.stream().map(path -> {
			String name = path.getFileName().toString();
			Path cover = findCoverImage(path);
			String coverUrl;
			if (cover != null) {
				coverUrl = cover.toUri().toString();
			} else {
				URL placeholder = App.class.getResource("/bookreader/placeholder-cover.png");
				coverUrl = placeholder != null ? placeholder.toExternalForm() : "";
			}
			String folder = escapeHtml(path.getFileName().toString());
			return "<div class='book-card' data-folder='" + folder
					+ "'><div class='cover' style=\"background-image:url('" + coverUrl
					+ "')\"></div><div class='title'>" + escapeHtml(name)
					+ "</div><div class='hint'>Open via 'Open PDF' or drag a file</div></div>";
		}).collect(Collectors.joining());

		if (cards.isEmpty()) {
			cards = "<div class='empty'>Drop PDFs here or use Open PDF to start building your library.</div>";
		}

		String styles = "body{margin:0;font-family:'Segoe UI',sans-serif;background:#0f172a;color:#f8fafc;}"
				+ ".hero{padding:48px 64px;background:linear-gradient(135deg,#1d4ed8,#9333ea);}"
				+ ".hero h1{margin:0;font-size:42px;font-weight:700;color:#f8fafc;}"
				+ ".hero p{margin:12px 0 0;font-size:18px;opacity:0.9;}"
				+ ".grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(180px,1fr));gap:24px;padding:32px;}"
				+ ".book-card{background:rgba(15,23,42,0.75);border-radius:18px;overflow:hidden;box-shadow:0 18px 35px rgba(15,23,42,0.45);display:flex;flex-direction:column;cursor:pointer;transition:transform .2s ease, box-shadow .2s ease;}"
				+ ".book-card:hover{transform:translateY(-6px);box-shadow:0 24px 45px rgba(15,23,42,0.55);}"
				+ ".book-card .cover{flex:1;min-height:220px;background-size:cover;background-position:center;}"
				+ ".book-card .title{padding:16px;font-weight:600;font-size:16px;letter-spacing:0.4px;}"
				+ ".book-card .hint{padding:0 16px 18px;color:rgba(148,163,184,0.85);font-size:13px;}"
				+ ".empty{padding:64px;text-align:center;font-size:18px;color:rgba(148,163,184,0.85);}";

		String script = "<script>(function(){var cards=document.querySelectorAll('.book-card');cards.forEach(function(card){card.addEventListener('click',function(){var folder=card.getAttribute('data-folder');if(window.llgBridge && folder){window.llgBridge.openBook(folder);}});});})();</script>";

		return "<html><head><meta charset='UTF-8'><style>" + styles
				+ "</style></head><body><section class='hero'><h1>Your Library</h1><p>Select 'Open PDF' or drag a book into the reader.</p></section><section class='grid'>"
				+ cards + "</section>" + script + "</body></html>";
	}

	private static Path findCoverImage(Path bookDir) {
		if (bookDir == null) {
			return null;
		}
		try (Stream<Path> stream = Files.list(bookDir)) {
			return stream.filter(Files::isRegularFile).filter(path -> {
				String lower = path.getFileName().toString().toLowerCase(Locale.getDefault());
				return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")
						|| lower.endsWith(".gif") || lower.endsWith(".webp");
			}).findFirst().orElse(null);
		} catch (IOException ignored) {
		}
		return null;
	}

	private static String escapeHtml(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
				.replace("'", "&#39;");
	}

	private static void triggerBookReaderReadAloud() {
		if (!bookReaderReady || bookReaderEngine == null) {
			SwingUtilities.invokeLater(() -> updateStatus("Book reader still loading"));
			return;
		}
		Platform.runLater(() -> {
			try {
				Object result = bookReaderEngine.executeScript(
						"(function(){var br=window.llgBookReader || (window.BookReader && window.BookReader.instances && window.BookReader.instances[0]);"
								+ " if(!br || !br.plugins || !br.plugins.tts){return 'missing';}"
								+ " br.plugins.tts.toggle();" + " return 'ok';})();");
				SwingUtilities.invokeLater(() -> {
					if (result != null && "missing".equals(result.toString())) {
						updateStatus("Read aloud controls unavailable");
					} else {
						updateStatus("Read aloud toggled");
					}
				});
			} catch (Exception ex) {
				SwingUtilities.invokeLater(() -> updateStatus("Unable to start read aloud"));
			}
		});
	}

	private static JPanel createGlassPanel(LayoutManager layout) {
		LayoutManager effectiveLayout = layout != null ? layout : new BorderLayout();
		JPanel panel = new BackgroundPanel();
		panel.setLayout(effectiveLayout);
		panel.setOpaque(false);
		return panel;
	}

	private static void openBookReaderView() {
		SwingUtilities.invokeLater(() -> {
			if (mainViewLayout == null || mainViewPanel == null) {
				return; // Not initialized yet
			}
			if (bookReaderView == null) {
				bookReaderView = buildBookReaderView();
				mainViewPanel.add(bookReaderView, "bookreader");
			}
			try {
				mainViewLayout.show(mainViewPanel, "bookreader");
				setActiveTask("bookreader");
				updateStatus("Book Reader");
			} catch (IllegalArgumentException e) {
				// If the card wasn't found, try to re-add it
				bookReaderView = buildBookReaderView();
				mainViewPanel.add(bookReaderView, "bookreader");
				mainViewLayout.show(mainViewPanel, "bookreader");
				setActiveTask("bookreader");
				updateStatus("Book Reader");
			}
		});
	}

	private static void openYouTubeView() {
		SwingUtilities.invokeLater(() -> {
			if (mainViewLayout == null || mainViewPanel == null || youtubeView == null) {
				youtubeView = buildYouTubeView();
				mainViewPanel.add(youtubeView, "youtube");
			}
			mainViewLayout.show(mainViewPanel, "youtube");
			setActiveTask("youtube");
			updateStatus("YouTube");
			initializeYouTubeScene();
		});
	}

	private static void openVimeoView() {
		SwingUtilities.invokeLater(() -> {
			if (mainViewLayout == null || mainViewPanel == null || vimeoView == null) {
				vimeoView = buildVimeoView();
				mainViewPanel.add(vimeoView, "vimeo");
			}
			mainViewLayout.show(mainViewPanel, "vimeo");
			setActiveTask("vimeo");
			updateStatus("Vimeo");
			initializeVimeoScene();
		});
	}

	private static void openGoMoviesView() {
		SwingUtilities.invokeLater(() -> {
			if (mainViewLayout == null || mainViewPanel == null || goMoviesView == null) {
				goMoviesView = buildGoMoviesView();
				mainViewPanel.add(goMoviesView, "gomovies");
			}
			mainViewLayout.show(mainViewPanel, "gomovies");
			setActiveTask("gomovies");
			updateStatus("Go Movies");
			initializeGoMoviesScene();
		});
	}

	private static void openTorrentView() {
		SwingUtilities.invokeLater(() -> {
			if (mainViewLayout == null || mainViewPanel == null || torrentView == null) {
				torrentView = buildTorrentView();
				mainViewPanel.add(torrentView, "torrent");

				// Initialize LibtorrentStreamer with the necessary UI components only if not
				// already initialized
				if (libtorrentStreamer == null) {
					// Get the JSplitPane from the torrent view
					JSplitPane splitPane = (JSplitPane) torrentView.getComponent(1);
					// The left component is the torrent selection panel (content panel)
					JPanel contentPanel = (JPanel) splitPane.getLeftComponent();
					libtorrentStreamer = new LibtorrentStreamer(frame, contentPanel, torrentMediaPanel);
				}
			}
			mainViewLayout.show(mainViewPanel, "torrent");
			setActiveTask("torrent");
			updateStatus("Torrent Stream");

			// Open streaming link if no torrent is loaded (with a small delay to ensure
			// component is visible)
			Timer timer = new Timer(500, e -> {
				openStreamingLinkIfNoTorrent();
				((Timer) e.getSource()).stop();
			});
			timer.setRepeats(false);
			timer.start();

			// Auto-start the demo torrent if not already started
			if (!torrentLandingAutoStarted) {
				torrentLandingAutoStarted = true;
				if (torrentMagnetField != null
						&& (torrentMagnetField.getText() == null || torrentMagnetField.getText().trim().isEmpty())) {
					torrentMagnetField.setText(DEFAULT_DEMO_MAGNET);
				}
			}
		});
	}

	private static void openStreamingLinkIfNoTorrent() {
		// Check if there's no torrent loaded
		if (libtorrentStreamer != null && libtorrentStreamer.getValidTorrentHandle() == null) {
			try {
				// Open YouTube streaming service when no torrent is loaded
				SwingUtilities.invokeLater(() -> {
					openYouTubeView();
					updateStatus("No torrent loaded - opened YouTube for streaming content");
				});
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, "Error opening streaming link: " + e.getMessage(), e);
				updateStatus("Unable to open streaming service");
			}
		}
	}

	private static void loadTorrentFromMagnet(String magnetLink) {
		isMagnetLink = true;
		if (magnetLink == null || magnetLink.trim().isEmpty()) {
			JOptionPane.showMessageDialog(frame, "Please enter a valid magnet link", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		// Ensure libtorrentStreamer is properly initialized with UI components
		if (libtorrentStreamer == null) {
			// Initialize torrent view first to get the proper UI components
			if (torrentView == null) {
				torrentView = buildTorrentView();
			}
			// The buildTorrentView method will initialize libtorrentStreamer properly
		}

		// Set up metadata callback
		libtorrentStreamer.setMetadataCallback((java.util.List<vlcj.llg_mc.App.TorrentFileEntry> entries) -> {
			if (entries != null && !entries.isEmpty()) {
				SwingUtilities.invokeLater(() -> {
					torrentFileListModel.clear();
					for (TorrentFileEntry entry : entries) {
						torrentFileListModel.addElement(entry);
					}
					updateStatus("Loaded " + entries.size() + " files from magnet link");
				});
			}
		});

		new Thread(() -> {
			try {
				// Fetch metadata using HTTP endpoints and create a torrent file
				List<TorrentFileEntry> entries = fetchTorrentMetadata(magnetLink);
				if (entries == null || entries.isEmpty()) {
					SwingUtilities.invokeLater(() -> {
						JOptionPane.showMessageDialog(frame,
								"Could not fetch metadata for this magnet link.\n" +
										"The magnet link may be invalid or no metadata is available.",
								"Magnet Link Error", JOptionPane.ERROR_MESSAGE);
					});
					return;
				}

				// Create a temporary torrent file from the metadata
				// Since we can't create a real .torrent file from magnet metadata easily,
				// we'll use the existing approach but ensure the streamer is properly
				// initialized
				SwingUtilities.invokeLater(() -> {
					torrentFileListModel.clear();
					for (TorrentFileEntry entry : entries) {
						torrentFileListModel.addElement(entry);
					}
					updateStatus("Loaded " + entries.size() + " files from magnet link");

					// Since we can't create a real torrent session from magnet metadata,
					// we'll show a message that magnet links need to be converted to torrent files
					JOptionPane.showMessageDialog(frame,
							"Magnet link metadata loaded successfully.\n\n" +
									"To actually download and stream files, you need to obtain the .torrent file\n" +
									"from a torrent site and load it using the 'Browse' button.\n\n" +
									"Magnet links in this version are used only for previewing torrent contents.",
							"Magnet Link Support", JOptionPane.INFORMATION_MESSAGE);
				});

			} catch (Exception e) {
				e.printStackTrace();
				SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame,
						"Failed to load magnet link: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
			}
		}, "MagnetLoader").start();
	}

	private static LibtorrentStreamer libtorrentStreamer;
	private static boolean isMagnetLink = false;

	private static void playSelectedTorrentFile(TorrentFileEntry entry) {
		if (entry == null) {
			return;
		}

		try {
			// Check if we have a valid libtorrent streamer with an active torrent
			if (libtorrentStreamer == null || libtorrentStreamer.getValidTorrentHandle() == null) {
				JOptionPane.showMessageDialog(frame,
						"Please load a torrent file or magnet link first.",
						"No Torrent Loaded",
						JOptionPane.WARNING_MESSAGE);
				return;
			}

			// Get the list of files from the current torrent
			List<TorrentFileEntry> files = libtorrentStreamer.getTorrentFiles();
			if (files == null || files.isEmpty()) {
				throw new IllegalStateException("No files found in the current torrent");
			}

			// Find the index of the selected file
			int fileIndex = -1;
			for (int i = 0; i < files.size(); i++) {
				if (files.get(i).getPath().equals(entry.getPath())) {
					fileIndex = i;
					break;
				}
			}

			if (fileIndex == -1) {
				throw new IllegalStateException("Selected file not found in the torrent");
			}

			// Show play options dialog
			String[] options = new String[] { "Download First", "Stream Now", "Cancel" };
			int choice = JOptionPane.showOptionDialog(frame, "How would you like to play this file?\n\n"
					+ "• Download First: Downloads the entire file before playing (recommended for stable playback)\n"
					+ "• Stream Now: Starts playing immediately while downloading (may buffer)", "Play Options",
					JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

			if (choice == 2 || choice == JOptionPane.CLOSED_OPTION) {
				// User cancelled
				return;
			}

			boolean downloadFirst = (choice == 0);

			// Update progress tracking
			if (currentlyDownloadingEntry != null) {
				currentlyDownloadingEntry.setProgress(0); // Reset previous entry's progress
			}
			currentlyDownloadingEntry = entry;
			entry.setProgress(0); // Initialize progress

			// Initialize LibtorrentStreamer if needed
			if (libtorrentStreamer == null) {
				libtorrentStreamer = new LibtorrentStreamer();
			}

			// Initialize media player if needed
			try {
				if (torrentMediaPlayer == null) {
					torrentMediaPlayer = new EmbeddedMediaPlayerComponent();
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(frame, "Error initializing media player: " + e.getMessage(),
						"Initialization Error",
						JOptionPane.ERROR_MESSAGE);
				if (currentlyDownloadingEntry != null) {
					currentlyDownloadingEntry.setProgress(0);
					currentlyDownloadingEntry = null;
					if (torrentFileList != null) {
						SwingUtilities.invokeLater(() -> torrentFileList.repaint());
					}
				}
				return;
			}

			// Add media player event listener
			torrentMediaPlayer.mediaPlayer().events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
				@Override
				public void positionChanged(MediaPlayer mediaPlayer, float newPosition) {
					if (currentlyDownloadingEntry != null) {
						currentlyDownloadingEntry.setProgress(newPosition);
						// Update the list to reflect the new progress
						if (torrentFileList != null) {
							SwingUtilities.invokeLater(() -> torrentFileList.repaint());
						}
					}
				}

				@Override
				public void finished(MediaPlayer mediaPlayer) {
					// Reset the currently downloading entry when playback finishes
					if (currentlyDownloadingEntry != null) {
						currentlyDownloadingEntry.setProgress(1.0);
						currentlyDownloadingEntry = null;
						if (torrentFileList != null) {
							SwingUtilities.invokeLater(() -> torrentFileList.repaint());
						}
					}
					updateStatus("Playback complete: " + entry.getPath());
				}

				@Override
				public void error(MediaPlayer mediaPlayer) {
					if (currentlyDownloadingEntry != null) {
						currentlyDownloadingEntry.setProgress(0);
						currentlyDownloadingEntry = null;
						if (torrentFileList != null) {
							SwingUtilities.invokeLater(() -> torrentFileList.repaint());
						}
					}
					updateStatus("Error playing: " + entry.getPath());
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(frame, "Error playing file: " + e.getMessage(), "Playback Error",
					JOptionPane.ERROR_MESSAGE);
			if (currentlyDownloadingEntry != null) {
				currentlyDownloadingEntry.setProgress(0);
				currentlyDownloadingEntry = null;
				if (torrentFileList != null) {
					SwingUtilities.invokeLater(() -> torrentFileList.repaint());
				}
			}
		}
	}

	private static void showMediaPlayerView() {
		SwingUtilities.invokeLater(() -> {
			if (mainViewLayout == null || mainViewPanel == null || mediaPlayerView == null)
				return;
			if (mediaPlayerView.getParent() != mainViewPanel) {
				mainViewPanel.add(mediaPlayerView, "mediaPlayer");
				mainViewPanel.revalidate();
			}
			if (mediaPlayerFrame != null) {
				Container parent = mediaPlayerView.getParent();
				if (parent != null && parent != mainViewPanel) {
					parent.remove(mediaPlayerView);
				}
				mediaPlayerFrame.dispose();
				mediaPlayerFrame = null;
			}
			try {
				mainViewLayout.show(mainViewPanel, "mediaplayer");
				setActiveTask("mediaplayer");
				updateStatus("Ready");
			} catch (IllegalArgumentException e) {
				// If the card wasn't found, try to re-add it
				mediaPlayerView = buildMediaPlayerView();
				mainViewPanel.add(mediaPlayerView, "mediaplayer");
				mainViewLayout.show(mainViewPanel, "mediaplayer");
				setActiveTask("mediaplayer");
				updateStatus("Ready");
			}
		});
	}

	private static final class FlipCard extends JPanel {
		private final JComponent front;
		private final JComponent back;
		private final Timer timer;
		private double progress;
		private double target;

		FlipCard(JComponent front, JComponent back) {
			this.front = front;
			this.back = back;
			setOpaque(false);
			setLayout(null);
			add(front);
			add(back);
			back.setVisible(false);
			timer = new Timer(16, e -> step());
			MouseAdapter hoverHandler = new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					start(1.0);
				}

				@Override
				public void mouseExited(MouseEvent e) {
					Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), FlipCard.this);
					if (!FlipCard.this.contains(p)) {
						start(0.0);
					}
				}
			};
			addMouseListener(hoverHandler);
			front.addMouseListener(hoverHandler);
			back.addMouseListener(hoverHandler);
		}

		private void start(double value) {
			target = value;
			if (!timer.isRunning()) {
				timer.start();
			}
		}

		private void step() {
			double step = 0.08;
			if (progress < target) {
				progress = Math.min(target, progress + step);
			} else if (progress > target) {
				progress = Math.max(target, progress - step);
			} else {
				timer.stop();
				return;
			}
			boolean showFront = progress < 0.5;
			front.setVisible(showFront);
			back.setVisible(!showFront);
			repaint();
		}

		@Override
		public void doLayout() {
			int w = getWidth();
			int h = getHeight();
			front.setBounds(0, 0, w, h);
			back.setBounds(0, 0, w, h);
		}

		@Override
		protected void paintChildren(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			double phase;
			if (progress <= 0.5) {
				phase = 1.0 - progress * 2.0;
			} else {
				phase = (progress - 0.5) * 2.0;
			}
			phase = Math.max(0.05, phase);
			int cx = getWidth() / 2;
			int cy = getHeight() / 2;
			g2.translate(cx, cy);
			g2.scale(phase, 1.0);
			g2.translate(-cx, -cy);
			super.paintChildren(g2);
			g2.dispose();
		}

	}

	private static void openMediaPlayerWindow() {
		SwingUtilities.invokeLater(() -> {
			if (mediaPlayerView == null)
				return;

			CardLayout layout = mainViewLayout;
			JPanel panel = mainViewPanel;
			if (layout != null && panel != null) {
				layout.show(panel, "mediaplayer");
			}

			if (mediaPlayerFrame != null) {
				if (mediaPlayerFrame.isDisplayable() && mediaPlayerFrame.isVisible()) {
					mediaPlayerFrame.toFront();
					mediaPlayerFrame.requestFocus();
					return;
				}
			}

			JFrame window = new JFrame("Media Player");
			window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			window.setContentPane(mediaPlayerView);
			window.setSize(FRAME_WIDTH, FRAME_HEIGHT);
			window.setLocationRelativeTo(frame);
			window.addWindowListener(new java.awt.event.WindowAdapter() {
				@Override
				public void windowClosed(java.awt.event.WindowEvent e) {
					SwingUtilities.invokeLater(() -> {
						mediaPlayerFrame = null;
						if (mainViewPanel != null) {
							mainViewPanel.add(mediaPlayerView, "mediaPlayer");
							mainViewPanel.revalidate();
							mainViewLayout.show(mainViewPanel, "mediaplayer");
							setActiveTask("Media Player");
						}
					});
				}
			});
			mediaPlayerFrame = window;
			window.setVisible(true);
		});
	}

	private static JPanel buildMediaPlayerView() {
		JPanel panel = new JPanel(new BorderLayout());
		mediaPlayerListComponent = new EmbeddedMediaListPlayerComponent();
		// Auto-repeat the playlist items (loop through the list)
		mediaPlayerListComponent.mediaListPlayer().controls().setMode(PlaybackMode.LOOP);
		mediaPlayerListComponent.mediaPlayer().fullScreen().strategy(new AdaptiveFullScreenStrategy(frame));
		// Configure the subtitle text area with 500px right shift
		myTextArea = new JTextArea("");
		myTextArea.setEditable(false);
		myTextArea.setLineWrap(false); // Disable line wrapping
		myTextArea.setWrapStyleWord(false); // Disable word wrapping
		myTextArea.setBackground(new Color(0, 0, 0, 150));
		myTextArea.setForeground(Color.WHITE);
		myTextArea.setFont(new Font("SansSerif", Font.BOLD, 14));
		// Add 500px left margin to shift content to the right
		myTextArea.setMargin(new Insets(5, 5, 5, 5));
		// Add empty border to ensure proper spacing
		myTextArea.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		myTextArea.setRows(1); // Set to single line
		myTextArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, myTextArea.getPreferredSize().height)); // Keep
																											// height
																											// fixed

		// Create a scroll pane for the text area
		// JScrollPane scrollPane = new JScrollPane(myTextArea);
		// scrollPane.setOpaque(false);
		// scrollPane.getViewport().setOpaque(false);

		// Configure the subtitle panel
		// Create a panel with BorderLayout to hold both controls and subtitle
		JPanel controlPanel = new JPanel(new BorderLayout());
		JPanel controlsContainer = new JPanel(new GridBagLayout());
		JPanel subtitlePanel = new JPanel(new BorderLayout());
		subtitlePanel.setOpaque(false);
		// Create a label for centered subtitle text
		subtitleLabel = new JLabel("Subtitles will appear here", JLabel.CENTER);
		subtitleLabel.setForeground(Color.WHITE);
		subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		subtitleLabel.setVerticalAlignment(JLabel.CENTER);
		subtitleLabel.setHorizontalAlignment(JLabel.CENTER);

		// Create and configure the text area panel
		JPanel textAreaPanel = new JPanel(new BorderLayout());
		textAreaPanel.setOpaque(false);
		// textAreaPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
		textAreaPanel.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));

		// Add the centered label to the panel with proper constraints
		JPanel centerPanel = new JPanel(new GridBagLayout());
		centerPanel.setOpaque(false);
		centerPanel.add(subtitleLabel);
		textAreaPanel.add(centerPanel, BorderLayout.CENTER);
		int panelWidth = 400; // Define the desired width for the playlist panel

		// Function to update textAreaPanel width
		java.util.function.Consumer<JFrame> updateTextAreaWidth = frame -> {
			if (frame != null) {
				int newWidth = frame.getWidth() - panelWidth;
				textAreaPanel.setPreferredSize(new Dimension(Math.max(600, newWidth), 30));
				textAreaPanel.revalidate();
			}
		};

		// Initial setup
		updateTextAreaWidth.accept(frame);

		// Add component listener to handle window resizing
		if (frame != null) {
			frame.addComponentListener(new java.awt.event.ComponentAdapter() {
				@Override
				public void componentResized(java.awt.event.ComponentEvent e) {
					updateTextAreaWidth.accept(frame);
				}
			});
		}
		controlPanel.add(textAreaPanel, BorderLayout.CENTER);
		// subtitlePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.ORANGE,
		// 2),
		// BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		// Add subtitle panel to the top of control panel
		// controlPanel.add(subtitlePanel, BorderLayout.NORTH);
		// Add controls container to the center
		controlPanel.add(controlsContainer, BorderLayout.CENTER);
		// Keep the red border for debugging
		// controlPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
		JPanel sidePanel = new JPanel(new BorderLayout());
		playlistModel = new DefaultListModel<>();
		playlistView = new JList<String>(playlistModel) {
			@Override
			public int locationToIndex(Point location) {
				int index = super.locationToIndex(location);
				if (index != -1) {
					Rectangle bounds = getCellBounds(index, index);
					if (bounds != null && bounds.contains(location)) {
						return index;
					}
				}
				return -1;
			}
		};
		// Create your FileDropTargetListener, using the same media player component
		FileDropTargetListener dropTargetListener = new FileDropTargetListener(mediaPlayerListComponent);

		// Create a DropTarget for the playlistView (the JList) and associate the
		// listener
		new DropTarget(playlistView, dropTargetListener);
		playlistView.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		JScrollPane playListScrollPane = new JScrollPane(playlistView);

		playListScrollPane.setPreferredSize(new Dimension(panelWidth, 10));
		sidePanel.add(playListScrollPane, BorderLayout.CENTER);
		sidePanel.setPreferredSize(new Dimension(panelWidth, 10));
		sidePanel.setMinimumSize(new Dimension(panelWidth, 10));
		JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem removeItem = new JMenuItem("Delete");
		JMenuItem repeatItem = new JMenuItem("Repeat");
		JMenuItem addMediaItem = new JMenuItem("Add Media...");
		JMenuItem clearPlaylistItem = new JMenuItem("Clear Playlist");
		JMenuItem addSubtitleItem = new JMenuItem("Add Subtitle...");

		popupMenu.add(removeItem);
		popupMenu.add(repeatItem);
		popupMenu.addSeparator();
		popupMenu.add(addMediaItem);
		popupMenu.add(addSubtitleItem);
		popupMenu.addSeparator();
		popupMenu.add(clearPlaylistItem);

		// Add action listener for the Add Subtitle menu item
		addSubtitleItem.addActionListener(e -> {
			int selectedIndex = playlistView.getSelectedIndex();
			if (selectedIndex == -1) {
				JOptionPane.showMessageDialog(frame, "Please select a media item first", "No Media Selected",
						JOptionPane.WARNING_MESSAGE);
				return;
			}

			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Subtitle Files", "srt", "ass",
					"ssa", "sub", "vtt"));

			if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
				File subtitleFile = chooser.getSelectedFile();
				String fileExtension = "";
				String fileName = subtitleFile.getName();
				int dotIndex = fileName.lastIndexOf('.');
				if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
					fileExtension = fileName.substring(dotIndex + 1).toLowerCase();
				}

				if (fileExtension.matches("srt|ass|ssa|sub|vtt")) {
					try {
						String absolutePath = subtitleFile.getAbsolutePath();
						List<SrtParser.Subtitle> subtitles = new SrtParser().parse(absolutePath);

						// Register subtitle for the currently selected media
						ListApi mediaList = mediaPlayerListComponent.mediaListPlayer().list();
						MediaRef mediaRef = mediaList.media().newMediaRef(selectedIndex);
						Media media = mediaRef.newMedia();
						String mediaMrl = media.info().mrl();
						media.release();

						// Store the subtitle association
						subtitleAssociations.put(mediaMrl, new SubtitleAssociation(absolutePath, subtitles));

						// If this is the currently playing item, apply the subtitle immediately
						MediaApi currentMedia = mediaPlayerListComponent.mediaPlayer().media();
						String currentMediaMrl = null;
						if (currentMedia != null) {
							try {
								InfoApi info = currentMedia.info();
								if (info != null) {
									currentMediaMrl = info.mrl();
								}
							} catch (Exception e1) {
								System.err.println("Error getting media info: " + e1.getMessage());
							}
						}

						int currentIndex = -1;
						ListApi currentMediaList = mediaPlayerListComponent.mediaListPlayer().list();
						int count = currentMediaList.media().count();
						for (int i = 0; i < count; i++) {
							Media mediaItem = currentMediaList.media().newMedia(i);
							if (mediaItem != null) {
								String itemMrl = mediaItem.info().mrl();
								mediaItem.release();
								if (itemMrl != null && itemMrl.equals(currentMediaMrl)) {
									currentIndex = i;
									break;
								}
							}
						}

						if (currentIndex == selectedIndex) {
							registerSubtitleForCurrentMedia(absolutePath, subtitles, mediaPlayerListComponent);
						}

						JOptionPane.showMessageDialog(frame, "Subtitle added successfully: " + subtitleFile.getName(),
								"Subtitle Added", JOptionPane.INFORMATION_MESSAGE);
					} catch (Exception ex) {
						ex.printStackTrace();
						JOptionPane.showMessageDialog(frame, "Error loading subtitle: " + ex.getMessage(), "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				} else {
					JOptionPane.showMessageDialog(frame, "Unsupported subtitle format: " + fileExtension, "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		addMediaItem.addActionListener(e -> {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setMultiSelectionEnabled(true);
			if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
				File[] files = chooser.getSelectedFiles();
				for (File file : files) {
					String absolutePath = file.getAbsolutePath();
					FileDropTargetListener.filePath = absolutePath;
					mediaPlayerListComponent.mediaListPlayer().list().media().add(absolutePath);
					playlistModel.addElement(file.getName());
				}
				playlistView.revalidate();
				playlistView.repaint();
			}
		});
		clearPlaylistItem.addActionListener(e -> {
			int count = playlistModel.getSize();
			if (count == 0)
				return;
			if (JOptionPane.showConfirmDialog(frame, "Clear all playlist items?", "Confirm",
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				mediaPlayerListComponent.mediaListPlayer().controls().stop();
				mediaPlayerListComponent.mediaListPlayer().list().media().clear();
				playlistModel.clear();
				playlistView.clearSelection();
				FileDropTargetListener.filePath = null;
				applyPlaybackMode();
			}
		});
		final JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mediaPlayerListComponent, sidePanel);
		// Allow either pane to fully expand/collapse by setting minimum sizes to 0
		mediaPlayerListComponent.setMinimumSize(new Dimension(0, 0));
		sidePanel.setMinimumSize(new Dimension(0, 0));
		mainSplit.setResizeWeight(1.0);
		mainSplit.setContinuousLayout(true);
		mainSplit.setOneTouchExpandable(true);
		int initialDivider = Math.max(400, FRAME_WIDTH - panelWidth);
		mainSplit.setDividerLocation(initialDivider);
		final int[] storedDividerLocation = { initialDivider };
		final boolean[] playlistVisible = { true };
		panel.add(mainSplit, BorderLayout.CENTER);
		JButton playButton = new JButton("Play");
		JButton pauseButton = new JButton("Pause");
		JButton stopButton = new JButton("Stop");
		JButton skipForwardButton = new JButton(">>");
		JButton skipBackButton = new JButton("<<");
		JLabel currentTimeLabel = new JLabel("00:00:00");
		JSlider progressBar = new JSlider(0, PERCENT_SCALE);
		JLabel totalTimeLabel = new JLabel("00:00:00");
		JButton muteSoundButton = new JButton("Mute");
		JSlider volumeSlider = new JSlider();
		JButton fullScreenButton = new JButton("Full Screen");
		JButton nextMediaButton = new JButton("Next");
		JButton playListButton = new JButton("Playlist");
		JButton previousMediaButton = new JButton("Previous");
		JButton savedItemsButton = new JButton("Saved");

		subtitleButton = new JButton("Subtitles/Lyrics");
		// Add the combined control panel to the main container
		panel.add(controlPanel, BorderLayout.SOUTH);
		mediaPlayerListComponent.mediaPlayer().input().enableMouseInputHandling(false);

		// Add media player event listener to update playlist selection
		mediaPlayerListComponent.mediaListPlayer().events()
				.addMediaListPlayerEventListener(new MediaListPlayerEventAdapter() {
					@Override
					public void nextItem(MediaListPlayer mediaListPlayer, MediaRef item) {
						if (mediaListPlayer == null || item == null) {
							return;
						}

						int currentIndex = -1;
						Media tempMedia = null;
						ListApi mediaList = mediaListPlayer.list();

						try {
							// Get the current media's MRL
							tempMedia = item.newMedia();
							if (tempMedia == null) {
								return;
							}

							String currentMrl = tempMedia.info().mrl();
							if (currentMrl == null) {
								return;
							}

							// Iterate through the media list to find the matching MRL
							int count = mediaList.media().count();
							for (int i = 0; i < count; i++) {
								MediaRef mediaRef = mediaList.media().newMediaRef(i);
								if (mediaRef != null) {
									try {
										Media media = mediaRef.newMedia();
										if (media != null) {
											try {
												if (currentMrl.equals(media.info().mrl())) {
													currentIndex = i;
													break;
												}
											} finally {
												media.release();
											}
										}
									} finally {
										mediaRef.release();
									}
								}
							}

							// Update the UI on the EDT - only if we're not already handling a user
							// selection
							if (!suppressSelectionPlay) {
								int finalIndex = currentIndex;
								SwingUtilities.invokeLater(() -> {
									if (finalIndex >= 0 && finalIndex < playlistView.getModel().getSize()) {
										suppressSelectionPlay = true;
										try {
											playlistView.setSelectedIndex(finalIndex);
											playlistView.ensureIndexIsVisible(finalIndex);
										} finally {
											suppressSelectionPlay = false;
										}
									} else {
										playlistView.clearSelection();
									}
								});
							}
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							if (tempMedia != null) {
								tempMedia.release();
							}
						}
					}

					@Override
					public void stopped(MediaListPlayer mediaListPlayer) {
						// Clear selection when playback is stopped
						SwingUtilities.invokeLater(() -> {
							playlistView.clearSelection();
						});
					}

					@Override
					public void mediaListPlayerFinished(MediaListPlayer mediaListPlayer) {
						// Clear selection when playlist finishes
						SwingUtilities.invokeLater(() -> {
							playlistView.clearSelection();
						});
					}
				});

		editorPane = new JTextPane();
		editorPane.setContentType("text/html");
		editorPane.setEditable(false);
		editorPane.setOpaque(false);
		editorPane.setBackground(Color.blue);

		HTMLEditorKit kit = new HTMLEditorKit();
		StyleSheet styleSheet = kit.getStyleSheet();
		Style style = styleSheet.getStyle("body");
		StyleConstants.setBackground(style, new Color(0, 0, 0, 0)); // Set with a transparent alpha value
		// try {
		StyledDocument doc = (StyledDocument) editorPane.getDocument();
		SimpleAttributeSet sas = new SimpleAttributeSet();
		Font font = myTextArea.getFont();
		Font newFont = font.deriveFont(Font.BOLD, 24f);
		myTextArea.setFont(newFont);
		myTextArea.setForeground(Color.BLACK);
		myTextArea.setBackground(new Color(230, 240, 255));
		myTextArea.setLineWrap(false);
		myTextArea.setWrapStyleWord(false);
		// Build centered JLabel overlay to truly center subtitle text
		myTextArea.setFont(newFont);
		myTextArea.setForeground(Color.BLACK);
		myTextArea.setOpaque(false);

		volumeSlider.setMinimum(0);
		volumeSlider.setMaximum(VOLUME_MAX);
		volumeSlider.setValue(mediaPlayerListComponent.mediaPlayer().audio().volume());
		volumeSlider.setPreferredSize(new Dimension(150, 20)); // Suggest a minimum size

		GridBagConstraints gbcProgressBar = new GridBagConstraints();
		gbcProgressBar.gridx = 6;
		gbcProgressBar.gridy = 0;
		gbcProgressBar.weightx = 1; // Stretch horizontally
		gbcProgressBar.fill = GridBagConstraints.HORIZONTAL;
		GridBagConstraints gbcPlayButton = gbc(1, 0);
		GridBagConstraints gbcPauseButton = gbc(2, 0);
		GridBagConstraints gbcStopButton = gbc(3, 0);
		GridBagConstraints gbcSkipBackButton = gbc(4, 0);
		GridBagConstraints gbcSkipForwardButton = gbc(5, 0);

		GridBagConstraints gbcCurrentTimeLabel = gbc(7, 0);
		GridBagConstraints gbcDelimeter = gbc(8, 0);
		GridBagConstraints gbctotalTimeLabel = gbc(9, 0);

		GridBagConstraints gbcPreviousMediaButton = gbc(10, 0);
		GridBagConstraints gbcPlayListButton = gbc(11, 0);
		GridBagConstraints gbcNextMediaButton = gbc(12, 0);

		GridBagConstraints gbcMuteSoundButton = gbc(13, 0);
		GridBagConstraints gbcVolumeSlider = gbc(14, 0);
		GridBagConstraints gbcSavedItemsButton = gbc(15, 0);
		GridBagConstraints gbcFullScreenButton = gbc(16, 0);

		GridBagConstraints gbcEditorPane = gbc(0, 0);
		gbcEditorPane.gridwidth = 5; // span across controls row
		gbcEditorPane.weightx = 1.0;
		gbcEditorPane.fill = GridBagConstraints.HORIZONTAL;
		gbcEditorPane.anchor = GridBagConstraints.CENTER;

		// Configure the controls container with a green border
		// controlsContainer.setBorder(BorderFactory.createLineBorder(Color.GREEN, 2));
		// Add all controls to the controlsContainer with their respective constraints
		controlsContainer.add(playButton, gbcPlayButton);
		controlsContainer.add(pauseButton, gbcPauseButton);
		controlsContainer.add(stopButton, gbcStopButton);
		controlsContainer.add(skipBackButton, gbcSkipBackButton);
		controlsContainer.add(skipForwardButton, gbcSkipForwardButton);
		controlsContainer.add(progressBar, gbcProgressBar);
		controlsContainer.add(currentTimeLabel, gbcCurrentTimeLabel);
		controlsContainer.add(new JLabel(" / "), gbcDelimeter);
		controlsContainer.add(totalTimeLabel, gbctotalTimeLabel);
		controlsContainer.add(muteSoundButton, gbcMuteSoundButton);
		controlsContainer.add(volumeSlider, gbcVolumeSlider);
		controlsContainer.add(fullScreenButton, gbcFullScreenButton);
		controlsContainer.add(previousMediaButton, gbcPreviousMediaButton);
		controlsContainer.add(playListButton, gbcPlayListButton);
		controlsContainer.add(nextMediaButton, gbcNextMediaButton);
		controlsContainer.add(savedItemsButton, gbcSavedItemsButton);
		// Add editor pane to the center of the subtitle panel (which uses BorderLayout)
		// subtitlePanel.add(editorPane, BorderLayout.CENTER);
		controlPanel.add(editorPane, BorderLayout.CENTER);
		// Main controls panel setup with BorderLayout
		// JPanel controlsPanel = new JPanel(new BorderLayout(4, 0));
		// controlsPanel.setOpaque(false);

		// Add the controls container to the center of controls panel
		controlsContainer.setOpaque(false);
		// controlsPanel.add(controlsContainer, BorderLayout.CENTER);
		controlPanel.add(controlsContainer, BorderLayout.CENTER);
		// Add a border to highlight the controls panel
		// controlsPanel.setBorder(BorderFactory.createLineBorder(Color.RED, 1));

		// Create a panel for the sync controls
		JPanel syncPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		syncPanel.setOpaque(false);
		// Create sync control buttons
		JButton eastMinusBtn = new JButton("-0.5s");
		JButton eastPlusBtn = new JButton("+0.5s");
		JButton eastResetBtn = new JButton("Reset");
		JLabel eastOffsetLabel = new JLabel(String.format("Offset: %.1f s", getSubtitleOffsetMs() / 1000.0));
		Runnable eastRefresh = () -> eastOffsetLabel
				.setText(String.format("Offset: %.1f s", getSubtitleOffsetMs() / 1000.0));

		eastMinusBtn.addActionListener(a -> {
			adjustSubtitleOffset(-500);
			eastRefresh.run();
		});
		eastPlusBtn.addActionListener(a -> {
			adjustSubtitleOffset(500);
			eastRefresh.run();
		});
		eastResetBtn.addActionListener(a -> {
			resetSubtitleOffset();
			eastRefresh.run();
		});

		// Add the subtitle button to the sync panel first
		syncPanel.add(subtitleButton);
		syncPanel.add(Box.createHorizontalStrut(0)); // Consistent spacing between buttons

		// Add sync controls to the sync panel
		syncPanel.add(eastMinusBtn);
		syncPanel.add(eastPlusBtn);
		syncPanel.add(eastResetBtn);
		syncPanel.add(eastOffsetLabel);
		// Add red border to highlight the sync panel
		syncPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));

		// Create a panel for the center area of controls
		JPanel controlsCenterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
		controlsCenterPanel.setOpaque(false);

		// Create a wrapper panel for the text area to center it
		JPanel textAreaWrapper = new JPanel(new GridBagLayout());
		textAreaWrapper.setOpaque(false);

		// Add components to the controls panel
		if (myTextArea == null) {
			myTextArea = new JTextArea(1, 20); // Initialize if not already done
			myTextArea.setLineWrap(true);
			myTextArea.setWrapStyleWord(true);
			myTextArea.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		}
		// Add some padding to the text area
		myTextArea.setBorder(BorderFactory.createEmptyBorder(-5, 10, 5, 10));

		// Add the text area to its wrapper with center alignment
		textAreaWrapper.add(myTextArea);
		textAreaPanel.add(textAreaWrapper, BorderLayout.CENTER);

		// Create a wrapper panel for the controls area with no gap
		JPanel controlsWrapper = new JPanel(new BorderLayout(0, 0)); // No gap between components
		controlsWrapper.setOpaque(false);

		// Remove any margin or border that might be adding space
		syncPanel.setBorder(BorderFactory.createEmptyBorder());

		// Add components to the wrapper panel
		controlsWrapper.add(textAreaPanel, BorderLayout.CENTER);
		controlsWrapper.add(syncPanel, BorderLayout.EAST);

		// Add the wrapper panel to the controls panel
		// controlsPanel.add(controlsWrapper, BorderLayout.NORTH);
		controlPanel.add(controlsWrapper, BorderLayout.NORTH);
		// Add the controls panel to the south of the subtitle panel
		// subtitlePanel.add(controlsPanel, BorderLayout.SOUTH);
		// controlPanel.add(controlsPanel, BorderLayout.SOUTH);
		// Update the side panel width based on the subtitle controls
		// Runnable adjustSidePanelToSubtitle = () -> {
		// try {
		// JPanel eastPanel = (JPanel) ((BorderLayout) controlsPanel.getLayout())
		// .getLayoutComponent(BorderLayout.EAST);
		// if (eastPanel != null) {
		// java.awt.LayoutManager lm = eastPanel.getLayout();
		// int hgap = 0;
		// java.awt.Insets insets = syncPanel.getInsets();
		// if (lm instanceof java.awt.FlowLayout fl) {
		// hgap = fl.getHgap();
		// }
		// java.awt.Component[] comps = syncPanel.getComponents();
		// int count = comps == null ? 0 : comps.length;
		// int totalW = insets.left + insets.right;
		// for (int i = 0; i < count; i++) {
		// java.awt.Dimension d = comps[i].getPreferredSize();
		// totalW += (d == null ? 0 : d.width);
		// if (i < count - 1)
		// totalW += hgap;
		// }
		// // Add width of the subtitle button
		// totalW += subtitleButton.getPreferredSize().width + 8; // 8px for padding
		//
		// int minSideW = Math.max(SIDE_PANEL_WIDTH, totalW);
		// sidePanel.setPreferredSize(new Dimension(minSideW, SIDE_PANEL_HEIGHT));
		// // Ensure JSplitPane shows the computed width for the playlist panel
		// int currentDiv = mainSplit.getDividerLocation();
		// int rightWidth = Math.max(0, frame.getWidth() - currentDiv);
		// if (rightWidth < minSideW) {
		// mainSplit.setDividerLocation(Math.max(0, frame.getWidth() - minSideW));
		// }
		// frame.revalidate();
		// frame.repaint();
		// }
		// } catch (Exception ignore) {
		// }
		// };

		// Adjust now and on layout/resize events so it stays aligned
		// adjustSidePanelToSubtitle.run();

		// Add component listener to the controls panel to adjust side panel when
		// resized
		// controlsPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
		// @Override
		// public void componentResized(java.awt.event.ComponentEvent e) {
		// adjustSidePanelToSubtitle.run();
		// }
		//
		// @Override
		// public void componentShown(java.awt.event.ComponentEvent e) {
		// adjustSidePanelToSubtitle.run();
		// }
		// });

		// This line is no longer needed as we add controlPanel directly to the panel

		playButton.addActionListener(e -> {
			try {
				if (mediaPlayerListComponent != null && mediaPlayerListComponent.mediaPlayer() != null) {
					mediaPlayerListComponent.mediaPlayer().controls().play();
					updateStatus("Playing media");
				} else {
					updateStatus("Media player not initialized");
				}
			} catch (Exception ex) {
				LOGGER.log(Level.WARNING, "Error playing media: " + ex.getMessage(), ex);
				updateStatus("Error playing media: " + ex.getMessage());
				JOptionPane.showMessageDialog(frame, "Error playing media: " + ex.getMessage(),
						"Playback Error", JOptionPane.ERROR_MESSAGE);
			}
		});

		pauseButton.addActionListener((ActionEvent e) -> {
			try {
				if (mediaPlayerListComponent != null && mediaPlayerListComponent.mediaPlayer() != null) {
					if (mediaPlayerListComponent.mediaPlayer().status().isPlaying()) {
						mediaPlayerListComponent.mediaPlayer().controls().pause();
						pauseButton.setText("Resume");
						updateStatus("Media paused");
					} else {
						mediaPlayerListComponent.mediaPlayer().controls().play();
						pauseButton.setText("Pause");
						updateStatus("Media resumed");
					}
				} else {
					updateStatus("Media player not initialized");
				}
			} catch (Exception ex) {
				LOGGER.log(Level.WARNING, "Error controlling playback: " + ex.getMessage(), ex);
				updateStatus("Error controlling playback: " + ex.getMessage());
				JOptionPane.showMessageDialog(frame, "Error controlling playback: " + ex.getMessage(),
						"Playback Error", JOptionPane.ERROR_MESSAGE);
			}
		});

		stopButton.addActionListener(e -> {
			try {
				if (mediaPlayerListComponent != null && mediaPlayerListComponent.mediaPlayer() != null) {
					mediaPlayerListComponent.mediaPlayer().controls().stop();
					updateStatus("Playback stopped");
				} else {
					updateStatus("Media player not initialized");
				}
			} catch (Exception ex) {
				LOGGER.log(Level.WARNING, "Error stopping media: " + ex.getMessage(), ex);
				updateStatus("Error stopping media: " + ex.getMessage());
				JOptionPane.showMessageDialog(frame, "Error stopping media: " + ex.getMessage(),
						"Playback Error", JOptionPane.ERROR_MESSAGE);
			}
		});

		skipForwardButton.addActionListener(e -> {
			try {
				if (mediaPlayerListComponent != null && mediaPlayerListComponent.mediaPlayer() != null) {
					mediaPlayerListComponent.mediaPlayer().controls().skipTime(SKIP_MS);
					updateStatus("Skipped forward " + (SKIP_MS / 1000) + " seconds");
				} else {
					updateStatus("Media player not initialized");
				}
			} catch (Exception ex) {
				LOGGER.log(Level.WARNING, "Error skipping forward: " + ex.getMessage(), ex);
				updateStatus("Error skipping forward: " + ex.getMessage());
			}
		});

		skipBackButton.addActionListener(e -> {
			try {
				if (mediaPlayerListComponent != null && mediaPlayerListComponent.mediaPlayer() != null) {
					mediaPlayerListComponent.mediaPlayer().controls().skipTime(-SKIP_MS);
					updateStatus("Skipped back " + (SKIP_MS / 1000) + " seconds");
				} else {
					updateStatus("Media player not initialized");
				}
			} catch (Exception ex) {
				LOGGER.log(Level.WARNING, "Error skipping back: " + ex.getMessage(), ex);
				updateStatus("Error skipping back: " + ex.getMessage());
			}
		});
		muteSoundButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (mediaPlayerListComponent.mediaPlayer().audio().isMute()) {
					mediaPlayerListComponent.mediaPlayer().audio().setMute(false);
					muteSoundButton.setText("Mute");
				} else {
					mediaPlayerListComponent.mediaPlayer().audio().setMute(true);
					muteSoundButton.setText("Unmute");
				}

			}
		});

		volumeSlider.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					int clickX = e.getX();
					int sliderWidth = volumeSlider.getWidth();
					float clickPosition = (float) clickX / sliderWidth;
					volumeSlider.setValue((int) (clickPosition * VOLUME_MAX));
				}
			}
		});

		progressBar.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				if ((float) progressBar.getValue() / PERCENT_SCALE < 1f) {
					mediaPlayerListComponent.mediaPlayer().controls()
							.setPosition((float) progressBar.getValue() / PERCENT_SCALE);
				}
			}
		});

		progressBar.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					int clickX = e.getX();
					int sliderWidth = progressBar.getWidth();
					float clickPosition = (float) clickX / sliderWidth;
					mediaPlayerListComponent.mediaPlayer().controls().setPosition(clickPosition);
				}
			}
		});

		fullScreenButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mediaPlayerListComponent.mediaPlayer().fullScreen().toggle();
				if (taskbarPanel != null) {
					taskbarPanel.setVisible(!taskbarPanel.isVisible());
				}
			}
		});

		volumeSlider.addChangeListener(e -> {
			JSlider source = (JSlider) e.getSource();
			if (!source.getValueIsAdjusting()) {
				int volume = source.getValue();
				mediaPlayerListComponent.mediaPlayer().audio().setVolume(volume);
			}
		});
		previousMediaButton.addActionListener(e -> {
			mediaPlayerListComponent.mediaListPlayer().controls().playPrevious();
			playlistView.setSelectedIndex(playlistView.getSelectedIndex() - 1);
		});
		playListButton.addActionListener(e -> {
			if (playlistVisible[0]) {
				storedDividerLocation[0] = mainSplit.getDividerLocation();
				sidePanel.setVisible(false);
				mainSplit.setDividerLocation(1.0);
				playlistVisible[0] = false;
			} else {
				sidePanel.setVisible(true);
				int restore = storedDividerLocation[0];
				if (restore <= 0) {
					restore = Math.max(400, frame.getWidth() - 200); // 200 matches our panel width
				}
				mainSplit.setDividerLocation(restore);
				playlistVisible[0] = true;
			}
			frame.revalidate();
			frame.repaint();
		});

		savedItemsButton.addActionListener(e -> {
			JDialog dlg = new JDialog(frame, "Saved Selections", true);
			javax.swing.table.DefaultTableModel tableModel = new javax.swing.table.DefaultTableModel(
					new Object[] { "Selection", "Translation", "File", "Time", "Play" }, 0) {
				@Override
				public boolean isCellEditable(int row, int column) {
					return column == 4;
				}
			};
			// Helper to display the absolute directory of the saved media selection
			java.util.function.Function<SavedItem, String> computeFileName = (saved) -> {
				if (saved == null || saved.mrl == null || saved.mrl.isBlank())
					return "";
				String normalized = App.normalizeMrl(saved.mrl);
				if (normalized == null || normalized.isBlank())
					normalized = saved.mrl;
				try {
					java.io.File mediaFile = new java.io.File(normalized);
					java.io.File absoluteFile = mediaFile.getAbsoluteFile();
					java.io.File parent = absoluteFile.getParentFile();
					if (parent != null && absoluteFile.getName() != null && !absoluteFile.getName().isBlank()) {
						return parent.getAbsolutePath() + java.io.File.separator + absoluteFile.getName();
					}
					return absoluteFile.getAbsolutePath();
				} catch (Exception ignore) {
				}
				return normalized;
			};

			for (SavedItem it : savedSelections) {
				String timeStr = formatTime(Math.max(0, it.timeMs));
				String fileName = computeFileName.apply(it);
				tableModel.addRow(new Object[] { it.selection, it.translation, fileName, timeStr, "Play" });
			}
			javax.swing.JTable table = new javax.swing.JTable(tableModel);
			table.setAutoCreateRowSorter(true);
			table.setFillsViewportHeight(true);
			// Render 'Play' as a button-like cell
			table.getColumnModel().getColumn(4).setCellRenderer((tbl, value, isSelected, hasFocus, row, col) -> {
				JButton btn = new JButton(value == null ? "Play" : value.toString());
				return btn;
			});
			// Refresh File column when playlist selection changes
			playlistView.addListSelectionListener(ev -> {
				for (int r = 0; r < tableModel.getRowCount(); r++) {
					String name = computeFileName.apply(savedSelections.get(r));
					tableModel.setValueAt(name, r, 2);
				}
			});

			// Handle clicks on Play column
			table.addMouseListener(new java.awt.event.MouseAdapter() {
				@Override
				public void mouseClicked(java.awt.event.MouseEvent e1) {
					int row = table.rowAtPoint(e1.getPoint());
					int col = table.columnAtPoint(e1.getPoint());
					if (row >= 0 && col == 4) {
						int modelRow = table.convertRowIndexToModel(row);
						SavedItem it = savedSelections.get(modelRow);
						if (it.mrl != null && !it.mrl.isEmpty()) {
							String currentMrl = FileDropTargetListener.filePath;
							if (currentMrl == null || !currentMrl.equals(it.mrl)) {
								mediaPlayerListComponent.mediaPlayer().media().play(it.mrl);
								new javax.swing.Timer(400, ae -> {
									mediaPlayerListComponent.mediaPlayer().controls().setTime(it.timeMs);
									mediaPlayerListComponent.mediaPlayer().controls().play();
									((javax.swing.Timer) ae.getSource()).stop();
								}).start();
							} else {
								mediaPlayerListComponent.mediaPlayer().controls().setTime(it.timeMs);
								mediaPlayerListComponent.mediaPlayer().controls().play();
							}
						}
					}
				}
			});

			javax.swing.JPanel bottom = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
			JButton btnDelete = new JButton("Delete Selected");
			JButton btnClear = new JButton("Clear All");
			JButton btnExport = new JButton("Export CSV");
			JButton btnImport = new JButton("Import CSV");
			bottom.add(btnImport);
			bottom.add(btnExport);
			bottom.add(btnClear);
			bottom.add(btnDelete);

			btnDelete.addActionListener(ae -> {
				int[] viewRows = table.getSelectedRows();
				if (viewRows.length == 0)
					return;
				java.util.Arrays.sort(viewRows);
				for (int i = viewRows.length - 1; i >= 0; i--) {
					int modelRow = table.convertRowIndexToModel(viewRows[i]);
					savedSelections.remove(modelRow);
					tableModel.removeRow(modelRow);
				}
				saveSavedSelections();
			});

			btnClear.addActionListener(ae -> {
				int res = JOptionPane.showConfirmDialog(dlg, "Clear all saved records?", "Confirm",
						JOptionPane.YES_NO_OPTION);
				if (res != JOptionPane.YES_OPTION)
					return;
				savedSelections.clear();
				tableModel.setRowCount(0);
				saveSavedSelections();
			});

			btnExport.addActionListener(ae -> {
				JFileChooser fc = new JFileChooser();
				fc.setSelectedFile(new java.io.File("saved-translations.csv"));
				if (fc.showSaveDialog(dlg) == JFileChooser.APPROVE_OPTION) {
					java.io.File f = fc.getSelectedFile();
					java.util.List<String> out = new java.util.ArrayList<>();
					out.add("Selection,Translation,MRL,TimeMs");
					for (SavedItem it : savedSelections) {
						out.add(toCsv(it.selection) + "," + toCsv(it.translation) + "," + toCsv(it.mrl) + ","
								+ it.timeMs);
					}
					try {
						java.nio.file.Files.write(f.toPath(), out, java.nio.charset.StandardCharsets.UTF_8,
								java.nio.file.StandardOpenOption.CREATE,
								java.nio.file.StandardOpenOption.TRUNCATE_EXISTING);
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(dlg, "Export failed: " + ex.getMessage(), "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			});

			btnImport.addActionListener(ae -> {
				JFileChooser fc = new JFileChooser();
				if (fc.showOpenDialog(dlg) == JFileChooser.APPROVE_OPTION) {
					java.io.File f = fc.getSelectedFile();
					try {
						java.util.List<String> lines = java.nio.file.Files.readAllLines(f.toPath(),
								java.nio.charset.StandardCharsets.UTF_8);
						boolean header = true;
						for (String line : lines) {
							if (header) {
								header = false;
								if (line.toLowerCase().contains("selection") && line.contains(","))
									continue;
							}
							java.util.List<String> cols = parseCsvLine(line);
							if (cols.size() >= 4) {
								String sel = cols.get(0);
								String tr = cols.get(1);
								String m = cols.get(2);
								long t = 0L;
								try {
									t = Long.parseLong(cols.get(3));
								} catch (Exception ex) {
									t = 0L;
								}
								SavedItem item = new SavedItem(sel, tr, m, Math.max(0, t));
								savedSelections.add(item);
								tableModel.addRow(new Object[] { sel, tr, formatTime(Math.max(0, t)), "Play" });
							}
						}
						saveSavedSelections();
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(dlg, "Import failed: " + ex.getMessage(), "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			});

			JScrollPane sp = new JScrollPane(table);
			dlg.setLayout(new BorderLayout());
			dlg.add(sp, BorderLayout.CENTER);
			dlg.add(bottom, BorderLayout.SOUTH);
			dlg.setSize(900, 560);
			dlg.setLocationRelativeTo(frame);
			dlg.setVisible(true);
		});

		nextMediaButton.addActionListener(e -> {
			mediaPlayerListComponent.mediaListPlayer().controls().playNext();
			playlistView.setSelectedIndex(playlistView.getSelectedIndex() + 1);
		});
		subtitleButton.addActionListener(e -> {
			SubtitleSettingsDialog.showDialog(frame, mediaPlayerListComponent);
		});
		playlistView.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting() && !suppressSelectionPlay) {
				suppressSelectionPlay = true;
				try {
					int selectedIndex = playlistView.getSelectedIndex();
					if (selectedIndex != -1) {
						// Store the selected index to prevent race conditions
						int indexToPlay = selectedIndex;
						// Play the selected item
						mediaPlayerListComponent.mediaListPlayer().controls().play(indexToPlay);
						// Force update the selection after a short delay to ensure it sticks
						SwingUtilities.invokeLater(() -> {
							playlistView.setSelectedIndex(indexToPlay);
							playlistView.ensureIndexIsVisible(indexToPlay);
						});
					}
				} finally {
					SwingUtilities.invokeLater(() -> suppressSelectionPlay = false);
				}
			}
		});
		playlistView.addMouseListener(new MouseAdapter() {
			private void showEmptyAreaMenu(MouseEvent e) {
				if (!popupMenu.isPopupTrigger(e))
					return;
				int index = playlistView.locationToIndex(e.getPoint());
				if (index == -1) {
					playlistView.clearSelection();
					removeItem.setEnabled(false);
					repeatItem.setEnabled(false);
					addMediaItem.setEnabled(true);
					clearPlaylistItem.setEnabled(playlistModel.getSize() > 0);
					popupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					// Get the index of the item that was right-clicked
					int index = playlistView.locationToIndex(e.getPoint());

					// Highlight the item if needed
					if (index != -1) {
						removeItem.setEnabled(true);
						repeatItem.setEnabled(true);
						addMediaItem.setEnabled(true);
						clearPlaylistItem.setEnabled(true);
						if (playlistView.getSelectedIndex() != index) {
							suppressSelectionPlay = true;
							playlistView.setSelectedIndex(index);
						}
						// Display the pop-up menu at the mouse cursor's position
						popupMenu.show(e.getComponent(), e.getX(), e.getY());
					} else {
						showEmptyAreaMenu(e);
					}
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					showEmptyAreaMenu(e);
				}
			}
		});

		removeItem.addActionListener(e -> {
			int selectedIndex = playlistView.getSelectedIndex();
			if (selectedIndex != -1) {
				String itemName = null;
				try {
					itemName = playlistModel.getElementAt(selectedIndex);
				} catch (Exception ignore) {
				}
				String message = "Remove selected item from playlist?";
				if (itemName != null && !itemName.isBlank()) {
					message = "Remove \"" + itemName + "\" from playlist?";
				}
				if (JOptionPane.showConfirmDialog(frame, message, "Confirm", JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
					return;
				}
				// Before removal, adjust loopItemIndex so it stays valid or is cleared
				if (loopItemIndex == selectedIndex) {
					loopItemIndex = -1; // removed the item being looped
				} else if (loopItemIndex > selectedIndex) {
					loopItemIndex -= 1; // shift left since an earlier item was removed
				}
				// Call the vlcj method to remove the item
				mediaPlayerListComponent.mediaListPlayer().list().media().remove(selectedIndex);
				System.out.println("Removing item at index: " + selectedIndex);
				applyPlaybackMode();
			}
		});

		repeatItem.addActionListener(e -> {
			int selectedIndex = playlistView.getSelectedIndex();
			if (selectedIndex != -1) {
				if (loopItemIndex == selectedIndex) {
					loopItemIndex = -1;
					System.out.println("Single-item repeat disabled");
					applyPlaybackMode();
				} else {
					loopItemIndex = selectedIndex;
					System.out.println("Looping item at index: " + loopItemIndex);
					applyPlaybackMode();
					mediaPlayerListComponent.mediaListPlayer().controls().play(loopItemIndex);
				}
			}
		});

		mediaPlayerListComponent.videoSurfaceComponent().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
					mediaPlayerListComponent.mediaPlayer().fullScreen().toggle();
					if (taskbarPanel != null) {
						taskbarPanel.setVisible(!taskbarPanel.isVisible());
					}
				}
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
					mediaPlayerListComponent.mediaPlayer().controls().pause();
				}
			}
		});

		mediaPlayerListComponent.mediaPlayer().events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
			@Override
			public void mediaChanged(MediaPlayer mediaPlayer, MediaRef media) {
				resetSubtitleState();
				String target = null;
				if (media != null) {
					uk.co.caprica.vlcj.media.Media mediaInstance = media.newMedia();
					try {
						uk.co.caprica.vlcj.media.InfoApi info = mediaInstance.info();
						if (info != null) {
							target = info.mrl();
						}
					} finally {
						mediaInstance.release();
					}
				}
				if (target != null && !target.isBlank()) {
					String normalized = normalizeMrl(target);
					if (normalized != null && !normalized.isBlank()) {
						FileDropTargetListener.filePath = normalized;
						applyAssociatedSubtitle(normalized);
					} else {
						applyAssociatedSubtitle(resolveActiveMediaMrl());
					}
				} else {
					applyAssociatedSubtitle(resolveActiveMediaMrl());
				}
			}

			@Override
			public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
				SwingUtilities.invokeLater(() -> {
					currentTimeLabel.setText(formatTime(newTime));
					long totalTime = mediaPlayerListComponent.mediaPlayer().status().length();
					totalTimeLabel.setText(formatTime(totalTime));
				});
			}

			@Override
			public void playing(MediaPlayer mediaPlayer) {
				mediaPlayerListComponent.mediaPlayer().overlay().enable(true);
				SwingUtilities.invokeLater(() -> {
					String resolved = null;
					try {
						uk.co.caprica.vlcj.media.InfoApi info = mediaPlayerListComponent.mediaPlayer().media().info();
						if (info != null) {
							resolved = normalizeMrl(info.mrl());
						}
					} catch (Exception ignore) {
					}
					if (resolved != null && !resolved.isBlank()) {
						FileDropTargetListener.filePath = resolved;
					}
					// Get the MRL of the currently playing media
					String currentMrl = FileDropTargetListener.filePath;

					// Find the index of the matching MRL in the JList's model
					DefaultListModel<String> model = (DefaultListModel<String>) playlistView.getModel();
					for (int i = 0; i < model.size(); i++) {
						String playlistItemMrl = model.getElementAt(i);
						if (playlistItemMrl.equals(currentMrl)) {
							playlistView.setSelectedIndex(i);
							// Scroll to the selected item to make it visible
							playlistView.ensureIndexIsVisible(i);
							break; // Found the item, exit the loop
						}
					}
				});
			}

			@Override
			public void finished(MediaPlayer mediaPlayer) {
				applyPlaybackMode();
			}
		});

		// 1. Correctly override the methods in MediaListEventAdapter
		mediaPlayerListComponent.mediaListPlayer().list().events()
				.addMediaListEventListener(new MediaListEventAdapter() {
					@Override
					public void mediaListItemDeleted(MediaList mediaList, MediaRef item, int index) {
						SwingUtilities.invokeLater(() -> {
							if (index >= 0 && index < playlistModel.getSize()) {
								playlistModel.remove(index);
							}
							playlistView.revalidate();
							playlistView.repaint();
						});
					}

					@Override
					public void mediaListEndReached(MediaList mediaList) {
						System.out.println("Finished playback of playlist item.");
					}
				});

		// Use a listener to detect when the selection changes
		myTextArea.addCaretListener(new CaretListener() {
			@Override
			public void caretUpdate(CaretEvent e) {
				JTextArea source = (JTextArea) e.getSource();
				String selectedText = source.getSelectedText();
				if (selectedText != null && !selectedText.isEmpty()) {
					promptTranslationForSelection(frame, selectedText);
				}
			}
		});
		Timer timer = new Timer(TIMER_DELAY_MS, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				progressBar.setValue(
						Math.round(mediaPlayerListComponent.mediaPlayer().status().position() * PERCENT_SCALE));
			}
		});
		timer.start();
		new DropTarget(mediaPlayerListComponent, new FileDropTargetListener(mediaPlayerListComponent));

		return panel;
	}

}
