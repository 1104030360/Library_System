import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;

/**
 * Library API Server - Stage 2
 * Added book query endpoints
 *
 * New endpoints:
 * - GET /api/books          - Get all books
 * - GET /api/books?id=001   - Get book by ID
 * - GET /api/stats          - Get statistics
 */
public class LibraryApiServer {

    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static BookFileRepository repository;

    public static void main(String[] args) throws IOException {
        // Initialize repository
        repository = new BookFileRepository();
        repository.loadFromFile();

        // Create HTTP server on port 7070
        HttpServer server = HttpServer.create(new InetSocketAddress(7070), 0);

        System.out.println("=================================");
        System.out.println("Library API Server - Stage 2");
        System.out.println("URL: http://localhost:7070");
        System.out.println("=================================\n");

        // Register handlers
        server.createContext("/api/hello", new HelloHandler());
        server.createContext("/api/status", new StatusHandler());
        server.createContext("/api/books", new BooksHandler());
        server.createContext("/api/stats", new StatsHandler());

        // Start server
        server.setExecutor(null);
        server.start();

        System.out.println("✅ Server started successfully!\n");
        System.out.println("Test with curl:");
        System.out.println("  curl http://localhost:7070/api/hello");
        System.out.println("  curl http://localhost:7070/api/status");
        System.out.println("  curl http://localhost:7070/api/books");
        System.out.println("  curl http://localhost:7070/api/books?id=001");
        System.out.println("  curl http://localhost:7070/api/stats");
        System.out.println("\nPress Ctrl+C to stop server\n");
    }

    /**
     * Handler for /api/hello
     */
    static class HelloHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "Hello from Library API!";
            sendResponse(exchange, 200, "text/plain", response);
        }
    }

    /**
     * Handler for /api/status
     */
    static class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StatusResponse status = new StatusResponse(true, "Server is running", "2.0");
            String response = gson.toJson(status);
            sendResponse(exchange, 200, "application/json", response);
        }
    }

    /**
     * Handler for /api/books
     * Supports: GET /api/books or GET /api/books?id=001
     */
    static class BooksHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                String response = gson.toJson(new ErrorResponse("Method not allowed"));
                sendResponse(exchange, 405, "application/json", response);
                return;
            }

            // Parse query parameters
            URI uri = exchange.getRequestURI();
            String query = uri.getQuery();

            if (query != null && query.startsWith("id=")) {
                // GET /api/books?id=001
                String id = query.substring(3);
                handleGetBookById(exchange, id);
            } else {
                // GET /api/books
                handleGetAllBooks(exchange);
            }
        }

        private void handleGetAllBooks(HttpExchange exchange) throws IOException {
            List<BookInfo> books = repository.getAllBooks();

            BooksResponse response = new BooksResponse(true, books.size() + " books found", books);
            String json = gson.toJson(response);
            sendResponse(exchange, 200, "application/json", json);
        }

        private void handleGetBookById(HttpExchange exchange, String id) throws IOException {
            BookInfo book = repository.findById(id);

            if (book == null) {
                ErrorResponse error = new ErrorResponse("Book not found: " + id);
                String json = gson.toJson(error);
                sendResponse(exchange, 404, "application/json", json);
            } else {
                BookResponse response = new BookResponse(true, "Book found", book);
                String json = gson.toJson(response);
                sendResponse(exchange, 200, "application/json", json);
            }
        }
    }

    /**
     * Handler for /api/stats
     */
    static class StatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String stats = repository.getStatistics();
            StatsResponse response = new StatsResponse(true, stats);
            String json = gson.toJson(response);
            sendResponse(exchange, 200, "application/json", json);
        }
    }

    /**
     * Helper method to send HTTP response
     */
    private static void sendResponse(HttpExchange exchange, int statusCode,
                                      String contentType, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*"); // Enable CORS
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);

        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    // ===== Response Classes =====

    static class StatusResponse {
        public boolean success;
        public String message;
        public String version;

        public StatusResponse(boolean success, String message, String version) {
            this.success = success;
            this.message = message;
            this.version = version;
        }
    }

    static class BooksResponse {
        public boolean success;
        public String message;
        public List<BookInfo> books;

        public BooksResponse(boolean success, String message, List<BookInfo> books) {
            this.success = success;
            this.message = message;
            this.books = books;
        }
    }

    static class BookResponse {
        public boolean success;
        public String message;
        public BookInfo book;

        public BookResponse(boolean success, String message, BookInfo book) {
            this.success = success;
            this.message = message;
            this.book = book;
        }
    }

    static class StatsResponse {
        public boolean success;
        public String statistics;

        public StatsResponse(boolean success, String statistics) {
            this.success = success;
            this.statistics = statistics;
        }
    }

    static class ErrorResponse {
        public boolean success = false;
        public String error;

        public ErrorResponse(String error) {
            this.error = error;
        }
    }
}
