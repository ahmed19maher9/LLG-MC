package vlcj.llg_mc;

import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.concurrent.Worker;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for handling WebView console logging with different prefixes
 * for different views (BookReader, YouTube, Vimeo, GoMovies).
 */
public class WebViewLogger {
    private static final Logger LOGGER = Logger.getLogger(WebViewLogger.class.getName());
    
    public enum LogSource {
        BOOK_READER("[BookReader]"),
        YOUTUBE("[YouTube]"),
        VIMEO("[Vimeo]"),
        GOMOVIES("[GoMovies]");
        
        private final String prefix;
        
        LogSource(String prefix) {
            this.prefix = prefix;
        }
        
        public String getPrefix() {
            return prefix;
        }
    }
    
    /**
     * Configures a WebView with appropriate console logging for the specified source.
     * 
     * @param webView The WebView to configure
     * @param source The source of the logs (e.g., BookReader, YouTube, etc.)
     * @return The configured WebEngine
     */
    public static WebEngine configureWebViewLogging(WebView webView, LogSource source) {
        if (webView == null || source == null) {
            throw new IllegalArgumentException("WebView and source cannot be null");
        }
        
        WebEngine engine = webView.getEngine();
        String prefix = source.getPrefix();
        
        try {
            // Set up console message handler
            com.sun.javafx.webkit.WebConsoleListener.setDefaultListener((view, message, lineNumber, sourceId) -> {
                String logMessage = String.format("%s %s [%s:%d]", prefix, message, sourceId, lineNumber);
                
                // Log different message types appropriately
                if (message.toLowerCase().contains("error")) {
                    LOGGER.severe(logMessage);
                } else if (message.toLowerCase().contains("warn")) {
                    LOGGER.warning(logMessage);
                } else {
                    LOGGER.info(logMessage);
                }
            });
            
            // Set up alert handler
            engine.setOnAlert(event -> {
                LOGGER.info(String.format("%s [Alert] %s", prefix, event.getData()));
            });
            
            // Set up error handling
            engine.getLoadWorker().exceptionProperty().addListener((obs, oldException, exception) -> {
                if (exception != null) {
                    LOGGER.log(Level.SEVERE, prefix + " Error loading content", exception);
                }
            });
            
            // Add global error handler for JavaScript errors
            engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    injectErrorHandler(engine, prefix);
                }
            });
            
            return engine;
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not set up WebView console listener for " + source, e);
            return engine;
        }
    }
    
    /**
     * Injects a global error handler into the WebView's JavaScript context.
     */
    private static void injectErrorHandler(WebEngine engine, String prefix) {
        try {
            // Inject error handler
            engine.executeScript(
                "window.onerror = function(message, source, lineno, colno, error) {\n" +
                "  var errorMsg = '" + prefix + " JavaScript Error: ' + message + ' at ' + source + ':' + lineno + ':' + colno;\n" +
                "  if (error && error.stack) { errorMsg += '\\nStack: ' + error.stack; }\n" +
                "  console.error(errorMsg);\n" +
                "  return false;\n" +
                "};"
            );
            
            // Override console methods to include our prefix
            engine.executeScript(
                "(function() {\n" +
                "  var originalConsole = {\n" +
                "    log: console.log,\n" +
                "    error: console.error,\n" +
                "    warn: console.warn,\n" +
                "    info: console.info,\n" +
                "    debug: console.debug\n" +
                "  };\n" +
                "  \n" +
                "  function formatArgs(args) {\n" +
                "    return Array.from(args).map(arg => \n" +
                "      typeof arg === 'object' ? JSON.stringify(arg) : String(arg)\n" +
                "    ).join(' ');\n" +
                "  }\n" +
                "  \n" +
                "  console.log = function() {\n" +
                "    originalConsole.log.apply(console, ['" + prefix + " [LOG] ' + formatArgs(arguments)]);\n" +
                "  };\n" +
                "  \n" +
                "  console.error = function() {\n" +
                "    originalConsole.error.apply(console, ['" + prefix + " [ERROR] ' + formatArgs(arguments)]);\n" +
                "  };\n" +
                "  \n" +
                "  console.warn = function() {\n" +
                "    originalConsole.warn.apply(console, ['" + prefix + " [WARN] ' + formatArgs(arguments)]);\n" +
                "  };\n" +
                "  \n" +
                "  console.info = function() {\n" +
                "    originalConsole.info.apply(console, ['" + prefix + " [INFO] ' + formatArgs(arguments)]);\n" +
                "  };\n" +
                "  \n" +
                "  console.debug = function() {\n" +
                "    originalConsole.debug.apply(console, ['" + prefix + " [DEBUG] ' + formatArgs(arguments)]);\n" +
                "  };\n" +
                "})();"
            );
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to inject error handler for " + prefix, e);
        }
    }
}
