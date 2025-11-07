package vlcj.llg_mc;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TorrentHttpServer {
    private HttpServer server;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private File currentFile;
    private int port = 8080;
    private static final int BUFFER_SIZE = 8192;

    public void start() throws IOException {
        if (server != null) {
            stop();
        }
        
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/stream", new StreamHandler());
        server.setExecutor(executor);
        server.start();
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    public void setFile(File file) {
        this.currentFile = file;
    }

    public String getStreamUrl() {
        return "http://localhost:" + port + "/stream";
    }

    private class StreamHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (currentFile == null || !currentFile.exists()) {
                String response = "No file available";
                exchange.sendResponseHeaders(404, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                return;
            }

            // Set headers for streaming
            exchange.getResponseHeaders().add("Content-Type", getContentType(currentFile.getName()));
            exchange.getResponseHeaders().add("Accept-Ranges", "bytes");
            
            // Handle range requests for seeking
            String rangeHeader = exchange.getRequestHeaders().getFirst("Range");
            long fileSize = currentFile.length();
            long start = 0;
            long end = fileSize - 1;
            
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                rangeHeader = rangeHeader.substring("bytes=".length());
                String[] range = rangeHeader.split("-");
                try {
                    start = Long.parseLong(range[0]);
                    if (range.length > 1 && !range[1].isEmpty()) {
                        end = Long.parseLong(range[1]);
                    }
                } catch (NumberFormatException e) {
                    // Invalid range format, use full file
                }
            }
            
            long contentLength = end - start + 1;
            exchange.getResponseHeaders().add("Content-Length", String.valueOf(contentLength));
            exchange.getResponseHeaders().add("Content-Range", String.format("bytes %d-%d/%d", start, end, fileSize));
            exchange.sendResponseHeaders(206, contentLength);

            // Stream the file content
            try (FileInputStream fis = new FileInputStream(currentFile);
                 OutputStream os = exchange.getResponseBody()) {
                
                fis.skip(start);
                
                byte[] buffer = new byte[BUFFER_SIZE];
                long remaining = contentLength;
                int read;
                
                while (remaining > 0 && 
                       (read = fis.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
                    os.write(buffer, 0, read);
                    remaining -= read;
                }
            }
        }
    }

    private String getContentType(String filename) {
        String extension = "";
        int i = filename.lastIndexOf('.');
        if (i > 0) {
            extension = filename.substring(i + 1).toLowerCase();
        }
        
        return switch (extension) {
            case "mp4" -> "video/mp4";
            case "mkv" -> "video/x-matroska";
            case "avi" -> "video/x-msvideo";
            case "mov" -> "video/quicktime";
            case "webm" -> "video/webm";
            case "mp3" -> "audio/mpeg";
            case "wav" -> "audio/wav";
            case "ogg" -> "audio/ogg";
            default -> "application/octet-stream";
        };
    }
}
