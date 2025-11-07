package vlcj.llg_mc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

import org.apache.xmlrpc.XmlRpcException;

import Opensubs.OpenSubtitle;
import Opensubs.SubtitleInfo;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaListPlayerComponent;

/**
 * Extracted dialog for subtitle search & download.
 * Keeps behavior identical to previous inline implementation in
 * App.showSettingsDialog().
 */
public final class SubtitleSettingsDialog {

    private static final HttpClient LYRICS_HTTP_CLIENT = HttpClient.newHttpClient();

    private SubtitleSettingsDialog() {
    }

    // Use App.savedLangCode to remember user's preferred language

    public static void showDialog(JFrame parentFrame, EmbeddedMediaListPlayerComponent mediaPlayerListComponent) {
        JFrame settingsDialog = new JFrame("Subtitles/Lyrics Download");
        settingsDialog.setSize(760, 460);
        settingsDialog.setMinimumSize(new Dimension(640, 380));
        settingsDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        settingsDialog.setResizable(true);

        JTabbedPane tabbedPane = new JTabbedPane();
        JTabbedPane subtitleTabs = new JTabbedPane();

        // Language dropdown removed; continue using savedLangCode internally.

        // Tab 1: Find by current media hash/MRL
        JPanel findByHashPanel = new JPanel(new BorderLayout());
        JButton findByHashButton = new JButton("FindSubtitle");
        findByHashPanel.add(findByHashButton, BorderLayout.NORTH);

        JPanel subtitlesByHashPanel = new JPanel(new BorderLayout());
        subtitlesByHashPanel.setPreferredSize(new Dimension(300, 250));
        DefaultListModel<Subs> subtitlesByHashModel = new DefaultListModel<>();
        JList<Subs> subtitlesByHashView = new JList<>(subtitlesByHashModel);
        subtitlesByHashView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane subtitlesByHashScrollPane = new JScrollPane(subtitlesByHashView);
        subtitlesByHashPanel.add(subtitlesByHashScrollPane, BorderLayout.CENTER);
        findByHashPanel.add(subtitlesByHashPanel, BorderLayout.CENTER);

        findByHashButton.addActionListener((ActionEvent e) -> {
            OpenSubtitle openSubtitle = new OpenSubtitle();
            try {
                openSubtitle.login();
                subtitlesByHashModel.clear();
                String mrl = null;
                // 1) Try current media info
                try {
                    uk.co.caprica.vlcj.media.InfoApi info = mediaPlayerListComponent.mediaPlayer().media().info();
                    if (info != null) {
                        mrl = info.mrl();
                    }
                } catch (Exception ignore) {
                }

                // Normalize and trim any MRL obtained so far
                if (mrl != null && !mrl.isBlank()) {
                    try {
                        mrl = normalizeMrl(mrl).trim();
                    } catch (Exception ignore) {
                    }
                }

                // Predicate to detect subtitle file paths
                java.util.function.Predicate<String> isSubtitle = (p) -> {
                    if (p == null)
                        return false;
                    String lower = p.toLowerCase();
                    return lower.endsWith(".srt") || lower.endsWith(".ass") || lower.endsWith(".ssa") ||
                            lower.endsWith(".vtt") || lower.endsWith(".sub") || lower.endsWith(".idx") ||
                            lower.endsWith(".ttml") || lower.endsWith(".sbv") || lower.endsWith(".lrc");
                };

                // 2) Fallback to App.playlistView nearest non-subtitle
                if (mrl == null || mrl.isBlank() || isSubtitle.test(mrl)) {
                    javax.swing.ListModel<?> model = App.playlistView == null ? null : App.playlistView.getModel();
                    int selIdx = App.playlistView == null ? -1 : App.playlistView.getSelectedIndex();
                    if (model != null && model.getSize() > 0) {
                        int n = model.getSize();
                        if (selIdx < 0 || selIdx >= n)
                            selIdx = 0;
                        int left = selIdx, right = selIdx + 1;
                        while (left >= 0 || right < n) {
                            if (left >= 0) {
                                Object v = model.getElementAt(left);
                                if (v != null) {
                                    String s = v.toString();
                                    if (!isSubtitle.test(s)) {
                                        mrl = s;
                                        break;
                                    }
                                }
                                left--;
                            }
                            if (right < n) {
                                Object v = model.getElementAt(right);
                                if (v != null) {
                                    String s = v.toString();
                                    if (!isSubtitle.test(s)) {
                                        mrl = s;
                                        break;
                                    }
                                }
                                right++;
                            }
                        }
                        // If still unresolved, try App.currentMediaIndex
                        if ((mrl == null || mrl.isBlank()) && App.currentMediaIndex >= 0 && App.currentMediaIndex < n) {
                            Object v = model.getElementAt(App.currentMediaIndex);
                            if (v != null) {
                                String s = v.toString();
                                if (!isSubtitle.test(s))
                                    mrl = s;
                            }
                        }
                    }
                }

                // 3) Fallback to drop target path
                if (mrl == null || mrl.isBlank() || isSubtitle.test(mrl)) {
                    String drop = FileDropTargetListener.filePath;
                    if (drop != null && !drop.isBlank() && !isSubtitle.test(drop))
                        mrl = drop;
                }

                // Debug print resolved MRL
                System.out.println("Find by hash - resolved MRL: " + mrl);
                if (mrl == null || mrl.isBlank()) {
                    javax.swing.JOptionPane.showMessageDialog(settingsDialog,
                            "No media selected or playing. Select or play a video to search by hash.",
                            "No Media",
                            javax.swing.JOptionPane.WARNING_MESSAGE);
                    return;
                }
                List<SubtitleInfo> subtitles = openSubtitle.Search(mrl);
                if (subtitles.isEmpty()) {
                    subtitlesByHashModel.addElement(new Subs("No Subtitles Found", ""));
                } else {
                    for (SubtitleInfo subtitle : subtitles) {
                        subtitlesByHashModel
                                .addElement(new Subs(subtitle.getSubFileName(), subtitle.getSubDownloadLink()));
                    }
                }
            } catch (XmlRpcException ex) {
                ex.printStackTrace();
            } finally {
                openSubtitle.logOut();
            }
        });

        subtitlesByHashView.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Subs selectedSubtitle = subtitlesByHashView.getSelectedValue();
                if (selectedSubtitle != null && selectedSubtitle.getLink() != null
                        && !selectedSubtitle.getLink().isEmpty()) {
                    SubtitleService.downloadFileWithChooser(selectedSubtitle.getLink(), selectedSubtitle.getName());
                }
            }
        });

        // Tab 2: Find by movie name
        JPanel findByMovieNamePanel = new JPanel(new BorderLayout());
        JPanel searchByNameTop = new JPanel();
        JTextField movieNameTextField = new JTextField(20);
        JButton findByMovieNameButton = new JButton("FindSubtitle");
        searchByNameTop.add(new JLabel("Movie Name : "));
        searchByNameTop.add(movieNameTextField);
        searchByNameTop.add(findByMovieNameButton);
        findByMovieNamePanel.add(searchByNameTop, BorderLayout.NORTH);

        JPanel subtitlesByMovieNamePanel = new JPanel(new BorderLayout());
        subtitlesByMovieNamePanel.setPreferredSize(new Dimension(300, 250));
        DefaultListModel<Subs> subtitlesByMovieNameModel = new DefaultListModel<>();
        JList<Subs> subtitlesByMovieNameView = new JList<>(subtitlesByMovieNameModel);
        JScrollPane subtitlesByMovieNameScrollPane = new JScrollPane(subtitlesByMovieNameView);
        subtitlesByMovieNamePanel.add(subtitlesByMovieNameScrollPane, BorderLayout.CENTER);
        JLabel movieResultsLabel = new JLabel("Found: 0");
        subtitlesByMovieNamePanel.add(movieResultsLabel, BorderLayout.SOUTH);
        findByMovieNamePanel.add(subtitlesByMovieNamePanel, BorderLayout.CENTER);

        findByMovieNameButton.addActionListener((ActionEvent e) -> {
            OpenSubtitle openSubtitle = new OpenSubtitle();
            try {
                openSubtitle.login();
                subtitlesByMovieNameModel.clear();
                String langCode = App.savedLangCode == null ? "eng" : App.savedLangCode;
                List<SubtitleInfo> subtitles = openSubtitle.getMovieSubsByName(movieNameTextField.getText(), "100",
                        langCode);
                movieResultsLabel.setText("Found: " + subtitles.size());
                if (subtitles.isEmpty()) {
                    subtitlesByMovieNameModel.addElement(new Subs("No Subtitles Found", ""));
                } else {
                    for (SubtitleInfo subtitle : subtitles) {
                        subtitlesByMovieNameModel
                                .addElement(new Subs(subtitle.getSubFileName(), subtitle.getSubDownloadLink()));
                    }
                }
            } catch (XmlRpcException ex) {
                ex.printStackTrace();
            } finally {
                openSubtitle.logOut();
            }
        });

        subtitlesByMovieNameView.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Subs selectedSubtitle = subtitlesByMovieNameView.getSelectedValue();
                if (selectedSubtitle != null && selectedSubtitle.getLink() != null
                        && !selectedSubtitle.getLink().isEmpty()) {
                    App.downloadFileWithChooser(selectedSubtitle.getLink(), selectedSubtitle.getName());
                }
            }
        });

        // Tab 3: Find by series/season/episode
        JPanel findBySeasonPanel = new JPanel(new BorderLayout());
        JPanel topSeason = new JPanel();
        JTextField seriesNameField = new JTextField(20);
        JTextField seasonNumberField = new JTextField(20);
        JTextField episodeNumberField = new JTextField(20);
        topSeason.add(new JLabel("Series Name : "));
        topSeason.add(seriesNameField);
        topSeason.add(new JLabel("Season Number: "));
        topSeason.add(seasonNumberField);
        topSeason.add(new JLabel("Episode Number: "));
        topSeason.add(episodeNumberField);
        JButton findBySeasonButton = new JButton("FindSubtitle");
        topSeason.add(findBySeasonButton);

        JPanel subtitlesBySeasonNamePanel = new JPanel(new BorderLayout());
        subtitlesBySeasonNamePanel.setPreferredSize(new Dimension(300, 250));
        DefaultListModel<Subs> subtitlesBySeasonNameModel = new DefaultListModel<>();
        JList<Subs> subtitlesBySeasonNameView = new JList<>(subtitlesBySeasonNameModel);
        JScrollPane subtitlesBySeasonNameScrollPane = new JScrollPane(subtitlesBySeasonNameView);
        // Make the list area visually larger by default
        subtitlesBySeasonNameScrollPane.setPreferredSize(new Dimension(300, 320));
        subtitlesBySeasonNamePanel.add(subtitlesBySeasonNameScrollPane, BorderLayout.CENTER);
        JLabel seasonResultsLabel = new JLabel("Found: 0");
        subtitlesBySeasonNamePanel.add(seasonResultsLabel, BorderLayout.SOUTH);

        // Use a vertical split so the top controls (including the Find button) are
        // always visible
        javax.swing.JSplitPane split = new javax.swing.JSplitPane(javax.swing.JSplitPane.VERTICAL_SPLIT, topSeason,
                subtitlesBySeasonNamePanel);
        split.setResizeWeight(0.0); // extra space goes to bottom (list)
        // Make the divider fixed and non-resizable
        split.setEnabled(false);
        split.setDividerSize(0);
        split.setOneTouchExpandable(false);
        // Position divider so bottom list area is larger
        split.setDividerLocation(100);
        findBySeasonPanel.add(split, BorderLayout.CENTER);

        findBySeasonButton.addActionListener((ActionEvent e) -> {
            OpenSubtitle openSubtitle = new OpenSubtitle();
            try {
                openSubtitle.login();
                subtitlesBySeasonNameModel.clear();
                String langCode = App.savedLangCode == null ? "eng" : App.savedLangCode;
                List<SubtitleInfo> subtitles = openSubtitle.getTvSeriesSubs(
                        seriesNameField.getText(), seasonNumberField.getText(), episodeNumberField.getText(), "100",
                        langCode);
                seasonResultsLabel.setText("Found: " + subtitles.size());
                if (subtitles.isEmpty()) {
                    subtitlesBySeasonNameModel.addElement(new Subs("No Subtitles Found", ""));
                } else {
                    for (SubtitleInfo subtitle : subtitles) {
                        subtitlesBySeasonNameModel
                                .addElement(new Subs(subtitle.getSubFileName(), subtitle.getSubDownloadLink()));
                    }
                }
            } catch (XmlRpcException ex) {
                ex.printStackTrace();
            } finally {
                openSubtitle.logOut();
            }
        });

        subtitlesBySeasonNameView.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Subs selectedSubtitle = subtitlesBySeasonNameView.getSelectedValue();
                if (selectedSubtitle != null && selectedSubtitle.getLink() != null
                        && !selectedSubtitle.getLink().isEmpty()) {
                    App.downloadFileWithChooser(selectedSubtitle.getLink(), selectedSubtitle.getName());
                }
            }
        });

        subtitleTabs.addTab("FindByHash", findByHashPanel);
        subtitleTabs.addTab("FindByMovieName", findByMovieNamePanel);
        subtitleTabs.addTab("FindBySeriesSeason/Episode", findBySeasonPanel);
        JPanel subtitlesContainer = new JPanel(new BorderLayout());
        subtitlesContainer.add(subtitleTabs, BorderLayout.CENTER);
        tabbedPane.addTab("Subtitles", subtitlesContainer);
        JPanel lyricsPanel = new JPanel(new BorderLayout());
        JPanel lyricsControls = new JPanel();
        JTextField lyricsArtistField = new JTextField(15);
        JTextField lyricsSongField = new JTextField(15);
        JButton findLyricsButton = new JButton("Find Lyric");
        lyricsControls.add(new JLabel("Singer:"));
        lyricsControls.add(lyricsArtistField);
        lyricsControls.add(new JLabel("Song:"));
        lyricsControls.add(lyricsSongField);
        lyricsControls.add(findLyricsButton);
        lyricsPanel.add(lyricsControls, BorderLayout.NORTH);

        JTextArea lyricsArea = new JTextArea();
        lyricsArea.setLineWrap(true);
        lyricsArea.setWrapStyleWord(true);
        lyricsArea.setEditable(false);
        lyricsArea.setOpaque(true);
        lyricsArea.setBackground(new Color(248, 248, 252));
        lyricsArea.setForeground(new Color(33, 37, 41));
        Font lyricsFont = lyricsArea.getFont().deriveFont(Font.PLAIN,
                Math.max(16f, lyricsArea.getFont().getSize2D() + 2f));
        lyricsArea.setFont(lyricsFont);
        lyricsArea.setMargin(new Insets(12, 16, 12, 16));
        lyricsArea.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        lyricsArea.addCaretListener(e -> {
            String selected = lyricsArea.getSelectedText();
            if (selected != null && !selected.isBlank()) {
                App.promptTranslationForSelection(settingsDialog, selected);
            }
        });
        JScrollPane lyricsScrollPane = new JScrollPane(lyricsArea);
        lyricsScrollPane.setPreferredSize(new Dimension(300, 250));
        lyricsScrollPane.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8),
                javax.swing.BorderFactory.createLineBorder(new Color(220, 224, 232), 1)));
        lyricsPanel.add(lyricsScrollPane, BorderLayout.CENTER);
        JLabel lyricsStatusLabel = new JLabel("Enter a singer and song, then press Find Lyric.");
        lyricsPanel.add(lyricsStatusLabel, BorderLayout.SOUTH);
        findLyricsButton.addActionListener(e -> {
            String artist = lyricsArtistField.getText() == null ? "" : lyricsArtistField.getText().trim();
            String song = lyricsSongField.getText() == null ? "" : lyricsSongField.getText().trim();
            if (artist.isEmpty() || song.isEmpty()) {
                lyricsStatusLabel.setText("Please provide both singer and song title.");
                return;
            }
            lyricsStatusLabel.setText("Searching...");
            lyricsArea.setText("");
            findLyricsButton.setEnabled(false);
            SwingWorker<String, Void> worker = new SwingWorker<>() {
                @Override
                protected String doInBackground() {
                    try {
                        return fetchLyricsFromApi(artist, song);
                    } catch (Exception ex) {
                        return "__ERROR__" + ex.getMessage();
                    }
                }

                @Override
                protected void done() {
                    findLyricsButton.setEnabled(true);
                    String result;
                    try {
                        result = get();
                    } catch (Exception ex) {
                        result = "__ERROR__" + ex.getMessage();
                    }
                    if (result == null || result.isBlank()) {
                        lyricsStatusLabel.setText("No lyrics found for the provided singer/song.");
                        lyricsArea.setText("");
                    } else if (result.startsWith("__ERROR__")) {
                        lyricsStatusLabel.setText("Failed to fetch lyrics: " + result.substring(9));
                        lyricsArea.setText("");
                    } else {
                        lyricsStatusLabel.setText("Lyrics loaded from Lyrics.ovh");
                        lyricsArea.setText(result);
                        lyricsArea.setCaretPosition(0);
                    }
                }
            };
            worker.execute();
        });
        tabbedPane.addTab("Lyrics", lyricsPanel);

        settingsDialog.add(tabbedPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        okButton.addActionListener(e -> settingsDialog.dispose());
        cancelButton.addActionListener(e -> settingsDialog.dispose());
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        settingsDialog.add(buttonPanel, BorderLayout.SOUTH);

        settingsDialog.setLocationRelativeTo(parentFrame);
        settingsDialog.setVisible(true);
    }

    private static String fetchLyricsFromApi(String artist, String song) throws IOException, InterruptedException {
        String encodedArtist = URLEncoder.encode(artist, StandardCharsets.UTF_8);
        String encodedSong = URLEncoder.encode(song, StandardCharsets.UTF_8);
        String url = "https://api.lyrics.ovh/v1/" + encodedArtist + "/" + encodedSong;
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = LYRICS_HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200)
            return null;
        return extractLyricsFromJson(response.body());
    }

    private static String extractLyricsFromJson(String json) {
        if (json == null || json.isBlank())
            return null;
        int keyIndex = json.indexOf("\"lyrics\"");
        if (keyIndex == -1)
            return null;
        int colonIndex = json.indexOf(':', keyIndex);
        if (colonIndex == -1)
            return null;
        int startQuote = json.indexOf('"', colonIndex);
        if (startQuote == -1)
            return null;
        StringBuilder sb = new StringBuilder();
        boolean escaped = false;
        for (int i = startQuote + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escaped) {
                switch (c) {
                    case 'n':
                        sb.append('\n');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case '\\':
                    case '"':
                        sb.append(c);
                        break;
                    default:
                        sb.append(c);
                }
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                break;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String normalizeMrl(String mrl) {
        if (mrl == null || mrl.isEmpty())
            return mrl;
        try {
            if (mrl.startsWith("file:/")) {
                java.net.URI uri = java.net.URI.create(mrl);
                java.io.File f = new java.io.File(uri);
                return f.getAbsolutePath();
            }
        } catch (Exception ignore) {
        }
        return mrl;
    }
}
