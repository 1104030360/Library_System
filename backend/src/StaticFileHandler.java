import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Static File Handler
 * Serves HTML, CSS, JS files from web/ directory
 * Simple implementation - no fancy features needed
 */
public class StaticFileHandler implements HttpHandler {

    private final String webRoot;

    public StaticFileHandler(String webRoot) {
        this.webRoot = webRoot;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestPath = exchange.getRequestURI().getPath();

        // Default to index.html for root path
        if (requestPath.equals("/")) {
            requestPath = "/index.html";
        }

        // Build file path
        Path filePath = Paths.get(webRoot + requestPath);

        // Security check: prevent directory traversal
        if (!filePath.normalize().startsWith(Paths.get(webRoot).normalize())) {
            send404(exchange);
            return;
        }

        // Check if file exists
        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            send404(exchange);
            return;
        }

        // Read file content
        byte[] fileContent = Files.readAllBytes(filePath);

        // Determine content type
        String contentType = getContentType(requestPath);

        // Send response
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(200, fileContent.length);

        OutputStream os = exchange.getResponseBody();
        os.write(fileContent);
        os.close();
    }

    /**
     * Send 404 Not Found
     */
    private void send404(HttpExchange exchange) throws IOException {
        String response = "404 - File Not Found";
        exchange.sendResponseHeaders(404, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    /**
     * Get content type based on file extension
     */
    private String getContentType(String path) {
        if (path.endsWith(".html")) return "text/html; charset=UTF-8";
        if (path.endsWith(".css")) return "text/css; charset=UTF-8";
        if (path.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (path.endsWith(".json")) return "application/json; charset=UTF-8";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".gif")) return "image/gif";
        if (path.endsWith(".svg")) return "image/svg+xml";
        return "text/plain";
    }
}
