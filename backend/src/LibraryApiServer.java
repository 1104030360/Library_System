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
 * Library API Server - Stage 5
 * Upgraded to SQLite database
 *
 * Endpoints:
 * - GET  /api/books             - Get all books
 * - GET  /api/books?id=001      - Get book by ID
 * - GET  /api/stats             - Get statistics
 * - POST /api/auth/login        - Login (get session cookie)
 * - POST /api/auth/logout       - Logout (clear session)
 * - GET  /api/auth/whoami       - Get current user info
 * - POST /api/books/borrow      - Borrow a book (requires login)
 * - POST /api/books/return      - Return a book (requires login)
 */
public class LibraryApiServer {

    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static BookDatabaseRepository repository;

    public static void main(String[] args) throws IOException {
        // Initialize SQLite database repository
        repository = new BookDatabaseRepository();
        repository.initialize();

        // Create HTTP server on port 7070
        HttpServer server = HttpServer.create(new InetSocketAddress(7070), 0);

        System.out.println("=================================");
        System.out.println("Library API Server - Stage 5");
        System.out.println("URL: http://localhost:7070");
        System.out.println("Database: SQLite (data/library.db)");
        System.out.println("=================================\n");

        // Register handlers
        server.createContext("/api/hello", new HelloHandler());
        server.createContext("/api/status", new StatusHandler());
        server.createContext("/api/books", new BooksHandler());
        server.createContext("/api/books/borrow", new BorrowHandler());
        server.createContext("/api/books/return", new ReturnHandler());
        server.createContext("/api/stats", new StatsHandler());

        // Authentication handlers (Stage 4)
        server.createContext("/api/auth/login", new LoginHandler());
        server.createContext("/api/auth/logout", new LogoutHandler());
        server.createContext("/api/auth/whoami", new WhoAmIHandler());

        // Static file handler for HTML frontend
        server.createContext("/", new StaticFileHandler("web"));

        // Start server
        server.setExecutor(null);
        server.start();

        System.out.println("✅ Server started successfully!\n");
        System.out.println("🌐 HTML Frontend:");
        System.out.println("  http://localhost:7070                - Home page (Book list)");
        System.out.println("  http://localhost:7070/login.html     - Login page");
        System.out.println("\n📚 API Endpoints (for curl testing):");
        System.out.println("  curl http://localhost:7070/api/books");
        System.out.println("  curl http://localhost:7070/api/stats");
        System.out.println("\n🔐 Authentication API:");
        System.out.println("  curl -X POST http://localhost:7070/api/auth/login -c cookies.txt -d '{\"username\":\"0001\",\"password\":1111}'");
        System.out.println("\n🔒 Protected endpoints (require login):");
        System.out.println("  curl -X POST http://localhost:7070/api/books/borrow -b cookies.txt -d '{\"bookId\":\"001\"}'");
        System.out.println("  curl -X POST http://localhost:7070/api/books/return -b cookies.txt -d '{\"bookId\":\"001\"}'");
        System.out.println("\n💡 Test Accounts:");
        System.out.println("  Boss:     0001 / 1111");
        System.out.println("  Employee: 0002 / 2222");
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
            int activeSessions = ApiSessionManager.getActiveSessionCount();
            StatusResponse status = new StatusResponse(true, "Server is running", "4.0", activeSessions);
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
     * Handler for /api/books/borrow
     * POST request to borrow a book (requires authentication)
     */
    static class BorrowHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                String response = gson.toJson(new ErrorResponse("Method not allowed. Use POST"));
                sendResponse(exchange, 405, "application/json", response);
                return;
            }

            // Validate session (Stage 4 - authentication required)
            String sessionId = getSessionIdFromCookie(exchange);
            ApiSessionManager.SessionData session = ApiSessionManager.validateSession(sessionId);

            if (session == null) {
                String response = gson.toJson(new ErrorResponse("Authentication required. Please login first"));
                sendResponse(exchange, 401, "application/json", response);
                return;
            }

            // Read request body
            String body = new String(exchange.getRequestBody().readAllBytes());
            BorrowRequest request = gson.fromJson(body, BorrowRequest.class);

            if (request == null || request.bookId == null) {
                String response = gson.toJson(new ErrorResponse("Missing bookId in request body"));
                sendResponse(exchange, 400, "application/json", response);
                return;
            }

            // Find book
            BookInfo book = repository.findById(request.bookId);
            if (book == null) {
                String response = gson.toJson(new ErrorResponse("Book not found: " + request.bookId));
                sendResponse(exchange, 404, "application/json", response);
                return;
            }

            // Check if available
            if (!book.isAvailable()) {
                String response = gson.toJson(new ErrorResponse("Book is already borrowed"));
                sendResponse(exchange, 400, "application/json", response);
                return;
            }

            // Borrow book
            book.markAsBorrowed();
            repository.updateBook(book);

            OperationResponse response = new OperationResponse(
                true,
                "Successfully borrowed: " + book.getTitle(),
                book
            );
            String json = gson.toJson(response);
            sendResponse(exchange, 200, "application/json", json);
        }
    }

    /**
     * Handler for /api/books/return
     * POST request to return a book (requires authentication)
     */
    static class ReturnHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                String response = gson.toJson(new ErrorResponse("Method not allowed. Use POST"));
                sendResponse(exchange, 405, "application/json", response);
                return;
            }

            // Validate session (Stage 4 - authentication required)
            String sessionId = getSessionIdFromCookie(exchange);
            ApiSessionManager.SessionData session = ApiSessionManager.validateSession(sessionId);

            if (session == null) {
                String response = gson.toJson(new ErrorResponse("Authentication required. Please login first"));
                sendResponse(exchange, 401, "application/json", response);
                return;
            }

            // Read request body
            String body = new String(exchange.getRequestBody().readAllBytes());
            BorrowRequest request = gson.fromJson(body, BorrowRequest.class);

            if (request == null || request.bookId == null) {
                String response = gson.toJson(new ErrorResponse("Missing bookId in request body"));
                sendResponse(exchange, 400, "application/json", response);
                return;
            }

            // Find book
            BookInfo book = repository.findById(request.bookId);
            if (book == null) {
                String response = gson.toJson(new ErrorResponse("Book not found: " + request.bookId));
                sendResponse(exchange, 404, "application/json", response);
                return;
            }

            // Check if borrowed
            if (book.isAvailable()) {
                String response = gson.toJson(new ErrorResponse("Book is not borrowed"));
                sendResponse(exchange, 400, "application/json", response);
                return;
            }

            // Return book
            book.markAsReturned();
            repository.updateBook(book);

            OperationResponse response = new OperationResponse(
                true,
                "Successfully returned: " + book.getTitle(),
                book
            );
            String json = gson.toJson(response);
            sendResponse(exchange, 200, "application/json", json);
        }
    }

    /**
     * Handler for /api/auth/login
     * POST request to login and get session cookie
     */
    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                String response = gson.toJson(new ErrorResponse("Method not allowed. Use POST"));
                sendResponse(exchange, 405, "application/json", response);
                return;
            }

            // Read request body
            String body = new String(exchange.getRequestBody().readAllBytes());
            LoginRequest request = gson.fromJson(body, LoginRequest.class);

            if (request == null || request.username == null) {
                String response = gson.toJson(new ErrorResponse("Missing username or password"));
                sendResponse(exchange, 400, "application/json", response);
                return;
            }

            // Authenticate
            ApiAuthenticationHelper.AuthResult authResult =
                ApiAuthenticationHelper.authenticate(request.username, request.password);

            if (!authResult.success) {
                String response = gson.toJson(new ErrorResponse(authResult.message));
                sendResponse(exchange, 401, "application/json", response);
                return;
            }

            // Create session
            String sessionId = ApiSessionManager.createSession(
                authResult.username,
                authResult.userType
            );

            // Set session cookie
            exchange.getResponseHeaders().add("Set-Cookie",
                "sessionId=" + sessionId + "; Path=/; HttpOnly; Max-Age=1800");

            // Send response
            LoginResponse response = new LoginResponse(
                true,
                "Login successful",
                authResult.username,
                authResult.userType,
                sessionId
            );
            String json = gson.toJson(response);
            sendResponse(exchange, 200, "application/json", json);
        }
    }

    /**
     * Handler for /api/auth/logout
     * POST request to logout and clear session
     */
    static class LogoutHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                String response = gson.toJson(new ErrorResponse("Method not allowed. Use POST"));
                sendResponse(exchange, 405, "application/json", response);
                return;
            }

            // Get session ID from cookie
            String sessionId = getSessionIdFromCookie(exchange);
            if (sessionId != null) {
                ApiSessionManager.deleteSession(sessionId);
            }

            // Clear cookie
            exchange.getResponseHeaders().add("Set-Cookie",
                "sessionId=; Path=/; HttpOnly; Max-Age=0");

            // Send response
            SimpleResponse response = new SimpleResponse(true, "Logout successful");
            String json = gson.toJson(response);
            sendResponse(exchange, 200, "application/json", json);
        }
    }

    /**
     * Handler for /api/auth/whoami
     * GET request to get current user info
     */
    static class WhoAmIHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                String response = gson.toJson(new ErrorResponse("Method not allowed. Use GET"));
                sendResponse(exchange, 405, "application/json", response);
                return;
            }

            // Validate session
            String sessionId = getSessionIdFromCookie(exchange);
            ApiSessionManager.SessionData session = ApiSessionManager.validateSession(sessionId);

            if (session == null) {
                String response = gson.toJson(new ErrorResponse("Not logged in"));
                sendResponse(exchange, 401, "application/json", response);
                return;
            }

            // Send user info
            UserInfoResponse response = new UserInfoResponse(
                true,
                session.username,
                session.userType
            );
            String json = gson.toJson(response);
            sendResponse(exchange, 200, "application/json", json);
        }
    }

    /**
     * Helper method to extract session ID from cookie
     */
    private static String getSessionIdFromCookie(HttpExchange exchange) {
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookieHeader == null) {
            return null;
        }

        // Parse cookie header: "sessionId=xxx; other=yyy"
        String[] cookies = cookieHeader.split(";");
        for (String cookie : cookies) {
            String[] parts = cookie.trim().split("=");
            if (parts.length == 2 && parts[0].equals("sessionId")) {
                return parts[1];
            }
        }
        return null;
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
        public int activeSessions;

        public StatusResponse(boolean success, String message, String version) {
            this.success = success;
            this.message = message;
            this.version = version;
            this.activeSessions = 0;
        }

        public StatusResponse(boolean success, String message, String version, int activeSessions) {
            this.success = success;
            this.message = message;
            this.version = version;
            this.activeSessions = activeSessions;
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

    static class BorrowRequest {
        public String bookId;
    }

    static class OperationResponse {
        public boolean success;
        public String message;
        public BookInfo book;

        public OperationResponse(boolean success, String message, BookInfo book) {
            this.success = success;
            this.message = message;
            this.book = book;
        }
    }

    // ===== Authentication Request/Response Classes (Stage 4) =====

    static class LoginRequest {
        public String username;
        public int password;
    }

    static class LoginResponse {
        public boolean success;
        public String message;
        public String username;
        public String userType;
        public String sessionId;

        public LoginResponse(boolean success, String message, String username,
                             String userType, String sessionId) {
            this.success = success;
            this.message = message;
            this.username = username;
            this.userType = userType;
            this.sessionId = sessionId;
        }
    }

    static class SimpleResponse {
        public boolean success;
        public String message;

        public SimpleResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    static class UserInfoResponse {
        public boolean success;
        public String username;
        public String userType;

        public UserInfoResponse(boolean success, String username, String userType) {
            this.success = success;
            this.username = username;
            this.userType = userType;
        }
    }
}
