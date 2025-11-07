package vlcj.llg_mc;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/** Utility methods for subtitle IO and translation. */
public final class SubtitleService {

    private SubtitleService() {}

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
                            System.out.println("File deleted successfully: " + inputGzipFile);
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
        OkHttpClient httpClient = new OkHttpClient();
        String fullUrl = String.format("https://api.mymemory.translated.net/get?q=%s&langpair=%s|%s",
                java.net.URLEncoder.encode(text, StandardCharsets.UTF_8.toString()), sourceLang, targetLang);
        Request request = new Request.Builder().url(fullUrl).build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            String responseBody = response.body().string();
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
            JsonObject responseData = jsonObject.getAsJsonObject("responseData");
            return responseData.get("translatedText").getAsString();
        }
    }
}
