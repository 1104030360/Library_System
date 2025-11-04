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
import java.util.ArrayList;

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
    private static UserDatabaseRepository userRepository;
    private static BorrowHistoryRepository historyRepository;
    private static BookRatingRepository ratingRepository;
    private static BookReviewRepository reviewRepository;
    private static RecommendationService recommendationService;
    private static NotificationRepository notificationRepository;
    private static NotificationService notificationService;
    private static TaskManager taskManager;
    private static RecommendationWebSocketServer wsServer;
    private static long serverStartTime = System.currentTimeMillis();

    public static void main(String[] args) throws IOException, java.sql.SQLException {
        // Initialize SQLite database repositories
        repository = new BookDatabaseRepository();
        repository.initialize();

        userRepository = new UserDatabaseRepository("data/library.db");
        userRepository.initialize();

        // Initialize borrow history repository
        historyRepository = new BorrowHistoryRepository(repository.getConnection());

        // Initialize rating repository (Phase 6)
        ratingRepository = new BookRatingRepository(repository.getConnection());

        // Initialize review repository (Phase 6)
        reviewRepository = new BookReviewRepository(repository.getConnection());

        // Initialize recommendation service (Phase 10 - AI)
        recommendationService = new RecommendationService();

        // Initialize task manager for async recommendations (Phase 14 - WebSocket)
        taskManager = new TaskManager();

        // Initialize WebSocket server for real-time recommendation delivery
        wsServer = new RecommendationWebSocketServer(7071, taskManager);
        wsServer.start();

        // Initialize notification system (Phase 13)
        notificationRepository = new NotificationRepository("jdbc:sqlite:data/library.db");
        notificationService = new NotificationService(
            notificationRepository,
            historyRepository,
            repository,
            userRepository
        );

        // Start notification scheduler
        NotificationScheduler.start(notificationService);

        // Initialize authentication helper with user repository
        ApiAuthenticationHelper.initialize(userRepository);

        // Create HTTP server on port 7070
        HttpServer server = HttpServer.create(new InetSocketAddress(7070), 0);

        System.out.println("=================================");
        System.out.println("Library API Server - Stage 5");
        System.out.println("URL: http://localhost:7070");
        System.out.println("Database: SQLite (data/library.db)");
        System.out.println("User System: Enabled");
        System.out.println("=================================\n");

        // Register handlers
        server.createContext("/api/hello", new HelloHandler());
        server.createContext("/api/status", new StatusHandler());
        server.createContext("/api/books", new BooksHandler());
        server.createContext("/api/books/borrow", new BorrowHandler());
        server.createContext("/api/books/return", new ReturnHandler());
        server.createContext("/api/stats", new StatsHandler());

        // Authentication handlers (Stage 5)
        server.createContext("/api/auth/register", new RegisterHandler());
        server.createContext("/api/auth/login", new LoginHandler());
        server.createContext("/api/auth/logout", new LogoutHandler());
        server.createContext("/api/auth/whoami", new WhoAmIHandler());

        // Admin handlers (requires authentication + admin role)
        server.createContext("/api/books/add", new AddBookHandler());
        server.createContext("/api/books/update", new UpdateBookHandler());
        server.createContext("/api/books/delete", new DeleteBookHandler());
        server.createContext("/api/borrow-records", new BorrowRecordsHandler());

        // History handlers (Phase 6)
        server.createContext("/api/history/user", new UserHistoryHandler());
        server.createContext("/api/history/current", new CurrentBorrowingsHandler());
        server.createContext("/api/history/book", new BookHistoryHandler());
        server.createContext("/api/history/all", new AllHistoryHandler());

        // Rating handlers (Phase 6)
        server.createContext("/api/ratings/rate", new RateBookHandler());
        server.createContext("/api/ratings/book", new GetBookRatingsHandler());
        server.createContext("/api/ratings/user", new GetUserRatingHandler());
        server.createContext("/api/ratings/top", new GetTopRatedBooksHandler());

        // Review handlers (Phase 6)
        server.createContext("/api/reviews/add", new AddReviewHandler());
        server.createContext("/api/reviews/book", new GetBookReviewsHandler());
        server.createContext("/api/reviews/user", new GetUserReviewsHandler());
        server.createContext("/api/reviews/update", new UpdateReviewHandler());
        server.createContext("/api/reviews/delete", new DeleteReviewHandler());
        server.createContext("/api/reviews/latest", new GetLatestReviewsHandler());

        // AI Recommendation handlers (Phase 10)
        server.createContext("/api/recommendations/personal", new PersonalRecommendationsHandler());
        server.createContext("/api/recommendations/related", new RelatedRecommendationsHandler());
        server.createContext("/api/recommendations/health", new AIHealthHandler());

        // AI Chatbot handler (Phase 11)
        server.createContext("/api/chat", new ChatHandler());

        // Notification handlers (Phase 13)
        server.createContext("/api/notifications", new GetNotificationsHandler());
        server.createContext("/api/notifications/unread-count", new GetUnreadCountHandler());
        server.createContext("/api/notifications/read-all", new MarkAllAsReadHandler());
        server.createContext("/api/notifications/clear", new ClearNotificationsHandler());

        // Account Management handlers (Phase 12)
        server.createContext("/api/accounts/users", new AccountUsersHandler());
        server.createContext("/api/accounts/staff", new AccountStaffHandler());

        // Dashboard Statistics handler (Phase 12 Enhancement)
        server.createContext("/api/dashboard/stats", new DashboardStatsHandler());
        server.createContext("/api/dashboard/borrow-trend", new BorrowTrendHandler());
        server.createContext("/api/dashboard/system-info", new SystemInfoHandler());
        server.createContext("/api/dashboard/top-books", new TopBooksHandler());

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

            // Calculate borrow and due dates
            String borrowDate = java.time.LocalDate.now().toString();
            String dueDate = java.time.LocalDate.now().plusDays(14).toString();

            // Borrow book
            book.markAsBorrowed();
            repository.updateBook(book);

            // Record borrow history (Phase 6)
            historyRepository.createBorrowRecord(session.username, request.bookId, book.getTitle());

            // Send borrow notification (Phase 13)
            try {
                boolean sent = notificationService.sendBorrowNotification(
                    session.username,
                    book.getTitle(),
                    borrowDate,
                    dueDate
                );
                if (sent) {
                    System.out.println("✅ Borrow notification sent to user: " + session.username);
                } else {
                    System.err.println("❌ Failed to send borrow notification to user: " + session.username);
                }
            } catch (Exception e) {
                System.err.println("❌ Failed to send borrow notification: " + e.getMessage());
                e.printStackTrace();
            }

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

            // Get borrow info before returning (Phase 13)
            BorrowHistory borrowInfo = null;
            try {
                List<BorrowHistory> currentBorrowings = historyRepository.getCurrentBorrowings(session.username);
                for (BorrowHistory history : currentBorrowings) {
                    if (history.getBookId().equals(request.bookId)) {
                        borrowInfo = history;
                        break;
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to get borrow info: " + e.getMessage());
            }

            // Return book
            book.markAsReturned();
            repository.updateBook(book);

            // Mark as returned in history (Phase 6)
            historyRepository.markAsReturned(session.username, request.bookId);

            // Send return notification (Phase 13)
            if (borrowInfo != null) {
                try {
                    // Calculate borrow days and check if overdue
                    java.time.LocalDate borrowDate = java.time.LocalDate.parse(borrowInfo.getBorrowDate());
                    java.time.LocalDate returnDate = java.time.LocalDate.now();
                    java.time.LocalDate dueDate = java.time.LocalDate.parse(borrowInfo.getDueDate());

                    int borrowDays = (int) java.time.temporal.ChronoUnit.DAYS.between(borrowDate, returnDate);
                    boolean isOverdue = returnDate.isAfter(dueDate);

                    notificationService.sendReturnNotification(
                        session.username,
                        book.getTitle(),
                        borrowDays,
                        isOverdue
                    );
                } catch (Exception e) {
                    System.err.println("Failed to send return notification: " + e.getMessage());
                    e.printStackTrace();
                }
            }

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
     * Handler for /api/books/add
     * POST request to add a new book (requires admin authentication)
     */
    static class AddBookHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Only allow POST
            if (!"POST".equals(exchange.getRequestMethod())) {
                String response = gson.toJson(new ErrorResponse("Method not allowed. Use POST"));
                sendResponse(exchange, 405, "application/json", response);
                return;
            }

            // Validate session - must be logged in
            String sessionId = getSessionIdFromCookie(exchange);
            ApiSessionManager.SessionData session = ApiSessionManager.validateSession(sessionId);

            if (session == null) {
                String response = gson.toJson(new ErrorResponse("Authentication required. Please login first"));
                sendResponse(exchange, 401, "application/json", response);
                return;
            }

            // Check admin permission - only 館長 (Boss) and 館員 (Employee) can add books
            String userType = session.userType;
            if (!"館長".equals(userType) && !"館員".equals(userType)) {
                String response = gson.toJson(new ErrorResponse("Permission denied. Only administrators can add books"));
                sendResponse(exchange, 403, "application/json", response);
                return;
            }

            // Read request body
            String body = new String(exchange.getRequestBody().readAllBytes());
            AddBookRequest request = gson.fromJson(body, AddBookRequest.class);

            // Validate request data
            if (request == null) {
                String response = gson.toJson(new ErrorResponse("Invalid request body"));
                sendResponse(exchange, 400, "application/json", response);
                return;
            }

            if (request.id == null || request.id.trim().isEmpty()) {
                String response = gson.toJson(new ErrorResponse("Book ID is required"));
                sendResponse(exchange, 400, "application/json", response);
                return;
            }

            if (request.title == null || request.title.trim().isEmpty()) {
                String response = gson.toJson(new ErrorResponse("Book title is required"));
                sendResponse(exchange, 400, "application/json", response);
                return;
            }

            if (request.author == null || request.author.trim().isEmpty()) {
                String response = gson.toJson(new ErrorResponse("Book author is required"));
                sendResponse(exchange, 400, "application/json", response);
                return;
            }

            if (request.publisher == null || request.publisher.trim().isEmpty()) {
                String response = gson.toJson(new ErrorResponse("Book publisher is required"));
                sendResponse(exchange, 400, "application/json", response);
                return;
            }

            // Check if book ID already exists
            if (repository.findById(request.id) != null) {
                String response = gson.toJson(new ErrorResponse("Book ID already exists: " + request.id));
                sendResponse(exchange, 400, "application/json", response);
                return;
            }

            // Create new book (with optional description)
            String description = (request.description != null) ? request.description.trim() : "";
            BookInfo newBook = new BookInfo(
                request.id.trim(),
                request.title.trim(),
                request.author.trim(),
                request.publisher.trim(),
                description
            );

            // Add to database
            boolean success = repository.addBook(newBook);

            if (success) {
                AddBookResponse response = new AddBookResponse(
                    true,
                    "Book added successfully: " + newBook.getTitle(),
                    newBook
                );
                String json = gson.toJson(response);
                sendResponse(exchange, 200, "application/json", json);
            } else {
                String response = gson.toJson(new ErrorResponse("Failed to add book to database"));
                sendResponse(exchange, 500, "application/json", response);
            }
        }
    }

    /**
     * Handler for /api/books/update
     * PUT request to update book information (requires admin authentication)
     */
    static class UpdateBookHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Only allow PUT
            if (!"PUT".equals(exchange.getRequestMethod())) {
                String response = gson.toJson(new ErrorResponse("Method not allowed. Use PUT"));
                sendResponse(exchange, 405, "application/json", response);
                return;
            }

            // Validate session - must be logged in
            String sessionId = getSessionIdFromCookie(exchange);
            ApiSessionManager.SessionData session = ApiSessionManager.validateSession(sessionId);

            if (session == null) {
                String response = gson.toJson(new ErrorResponse("Authentication required. Please login first"));
                sendResponse(exchange, 401, "application/json", response);
                return;
            }

            // Check admin permission
            String userType = session.userType;
            if (!"館長".equals(userType) && !"館員".equals(userType)) {
                String response = gson.toJson(new ErrorResponse("Permission denied. Only administrators can update books"));
                sendResponse(exchange, 403, "application/json", response);
                return;
            }

            // Read request body
            String body = new String(exchange.getRequestBody().readAllBytes());
            UpdateBookRequest request = gson.fromJson(body, UpdateBookRequest.class);

            // Validate request data
            if (request == null || request.id == null || request.id.trim().isEmpty()) {
                String response = gson.toJson(new ErrorResponse("Book ID is required"));
                sendResponse(exchange, 400, "application/json", response);
                return;
            }

            if (request.title == null || request.title.trim().isEmpty()) {
                String response = gson.toJson(new ErrorResponse("Book title is required"));
                sendResponse(exchange, 400, "application/json", response);
                return;
            }

            if (request.author == null || request.author.trim().isEmpty()) {
                String response = gson.toJson(new ErrorResponse("Book author is required"));
                sendResponse(exchange, 400, "application/json", response);
                return;
            }

            if (request.publisher == null || request.publisher.trim().isEmpty()) {
                String response = gson.toJson(new ErrorResponse("Book publisher is required"));
                sendResponse(exchange, 400, "application/json", response);
                return;
            }

            // Check if book exists
            BookInfo existingBook = repository.findById(request.id.trim());
            if (existingBook == null) {
                String response = gson.toJson(new ErrorResponse("Book not found: " + request.id));
                sendResponse(exchange, 404, "application/json", response);
                return;
            }

            // Update book information
            String description = (request.description != null) ? request.description.trim() : "";
            boolean success = repository.updateBookInfo(
                request.id.trim(),
                request.title.trim(),
                request.author.trim(),
                request.publisher.trim(),
                description
            );

            if (success) {
                UpdateBookResponse response = new UpdateBookResponse(true, "Book updated successfully");
                String json = gson.toJson(response);
                sendResponse(exchange, 200, "application/json", json);
            } else {
                String response = gson.toJson(new ErrorResponse("Failed to update book"));
                sendResponse(exchange, 500, "application/json", response);
            }
        }
    }

    /**
     * Handler for /api/books/delete
     * DELETE request to delete a book (requires admin authentication)
     */
    static class DeleteBookHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Only allow DELETE
            if (!"DELETE".equals(exchange.getRequestMethod())) {
                String response = gson.toJson(new ErrorResponse("Method not allowed. Use DELETE"));
                sendResponse(exchange, 405, "application/json", response);
                return;
            }

            // Validate session - must be logged in
            String sessionId = getSessionIdFromCookie(exchange);
            ApiSessionManager.SessionData session = ApiSessionManager.validateSession(sessionId);

            if (session == null) {
                String response = gson.toJson(new ErrorResponse("Authentication required. Please login first"));
                sendResponse(exchange, 401, "application/json", response);
                return;
            }

            // Check admin permission
            String userType = session.userType;
            if (!"館長".equals(userType) && !"館員".equals(userType)) {
                String response = gson.toJson(new ErrorResponse("Permission denied. Only administrators can delete books"));
                sendResponse(exchange, 403, "application/json", response);
                return;
            }

            // Read request body
            String body = new String(exchange.getRequestBody().readAllBytes());
            DeleteBookRequest request = gson.fromJson(body, DeleteBookRequest.class);

            if (request == null || request.id == null || request.id.trim().isEmpty()) {
                String response = gson.toJson(new ErrorResponse("Book ID is required"));
                sendResponse(exchange, 400, "application/json", response);
                return;
            }

            // Delete book (repository will check if book is borrowed)
            boolean success = repository.deleteBook(request.id.trim());

            if (success) {
                DeleteBookResponse response = new DeleteBookResponse(true, "Book deleted successfully");
                String json = gson.toJson(response);
                sendResponse(exchange, 200, "application/json", json);
            } else {
                // Check why deletion failed
                BookInfo book = repository.findById(request.id.trim());
                if (book == null) {
                    String response = gson.toJson(new ErrorResponse("Book not found: " + request.id));
                    sendResponse(exchange, 404, "application/json", response);
                } else if (!book.isAvailable()) {
                    String response = gson.toJson(new ErrorResponse("Cannot delete: Book is currently borrowed"));
                    sendResponse(exchange, 400, "application/json", response);
                } else {
                    String response = gson.toJson(new ErrorResponse("Failed to delete book"));
                    sendResponse(exchange, 500, "application/json", response);
                }
            }
        }
    }

    /**
     * Handler for /api/borrow-records
     * GET request to get all borrowed books (requires admin authentication)
     */
    static class BorrowRecordsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Only allow GET
            if (!"GET".equals(exchange.getRequestMethod())) {
                String response = gson.toJson(new ErrorResponse("Method not allowed. Use GET"));
                sendResponse(exchange, 405, "application/json", response);
                return;
            }

            // Validate session - must be logged in
            String sessionId = getSessionIdFromCookie(exchange);
            ApiSessionManager.SessionData session = ApiSessionManager.validateSession(sessionId);

            if (session == null) {
                String response = gson.toJson(new ErrorResponse("Authentication required. Please login first"));
                sendResponse(exchange, 401, "application/json", response);
                return;
            }

            // Check admin permission
            String userType = session.userType;
            if (!"館長".equals(userType) && !"館員".equals(userType)) {
                String response = gson.toJson(new ErrorResponse("Permission denied. Only administrators can view borrow records"));
                sendResponse(exchange, 403, "application/json", response);
                return;
            }

            // Get borrowed books
            List<BookInfo> borrowedBooks = repository.getBorrowedBooks();

            BorrowRecordsResponse response = new BorrowRecordsResponse(true, borrowedBooks);
            String json = gson.toJson(response);
            sendResponse(exchange, 200, "application/json", json);
        }
    }

    /**
     * Handler for /api/auth/register
     * POST request to register new user account
     */
    static class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                String response = gson.toJson(new ErrorResponse("Method not allowed. Use POST"));
                sendResponse(exchange, 405, "application/json", response);
                return;
            }

            // Read request body
            String body = new String(exchange.getRequestBody().readAllBytes());
            RegisterRequest request = gson.fromJson(body, RegisterRequest.class);

            // Validate input
            if (request == null) {
                String response = gson.toJson(new ErrorResponse("Invalid request body"));
                sendResponse(exchange, 400, "application/json", response);
                return;
            }

            if (request.id == null || request.id.trim().isEmpty()) {
                String response = gson.toJson(new ErrorResponse("User ID is required"));
                sendResponse(exchange, 400, "application/json", response);
                return;
            }

            if (request.name == null || request.name.trim().isEmpty()) {
                String response = gson.toJson(new ErrorResponse("Name is required"));
                sendResponse(exchange, 400, "application/json", response);
                return;
            }

            if (request.password == null || request.password.length() < 6) {
                String response = gson.toJson(new ErrorResponse("Password must be at least 6 characters"));
                sendResponse(exchange, 400, "application/json", response);
                return;
            }

            // Register user
            boolean success = userRepository.registerUser(
                request.id,
                request.name,
                request.password,
                request.email
            );

            if (!success) {
                String response = gson.toJson(new ErrorResponse("User ID already exists"));
                sendResponse(exchange, 400, "application/json", response);
                return;
            }

            // Auto-login after registration
            String sessionId = ApiSessionManager.createSession(request.id, "user");

            // Set session cookie
            exchange.getResponseHeaders().add("Set-Cookie",
                "sessionId=" + sessionId + "; Path=/; HttpOnly; Max-Age=1800");

            // Send response
            RegisterResponse response = new RegisterResponse(
                true,
                "Registration successful",
                request.id,
                request.name,
                sessionId
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
                authResult.name,
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
        // CORS headers for credentials support
        String origin = exchange.getRequestHeaders().getFirst("Origin");
        // Support both development (5173) and Docker (7777) environments
        if (origin != null && (origin.equals("http://localhost:5173")
                            || origin.equals("http://localhost:7777")
                            || origin.equals("http://localhost:80")
                            || origin.equals("http://localhost"))) {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", origin);
        } else {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "http://localhost:5173");
        }
        exchange.getResponseHeaders().set("Access-Control-Allow-Credentials", "true");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Cookie");
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

    static class RegisterRequest {
        public String id;
        public String name;
        public String password;
        public String email;
    }

    static class RegisterResponse {
        public boolean success;
        public String message;
        public String userId;
        public String name;
        public String sessionId;

        public RegisterResponse(boolean success, String message, String userId,
                               String name, String sessionId) {
            this.success = success;
            this.message = message;
            this.userId = userId;
            this.name = name;
            this.sessionId = sessionId;
        }
    }

    static class LoginRequest {
        public String username;
        public String password;  // Changed to String to support both numeric and text passwords
    }

    static class LoginResponse {
        public boolean success;
        public String message;
        public String username;
        public String name;
        public String userType;
        public String sessionId;

        public LoginResponse(boolean success, String message, String username,
                             String name, String userType, String sessionId) {
            this.success = success;
            this.message = message;
            this.username = username;
            this.name = name;
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

    // ===== Add Book Request/Response Classes =====

    static class AddBookRequest {
        public String id;
        public String title;
        public String author;
        public String publisher;
        public String description;  // ✨ 新增：書籍描述
    }

    static class AddBookResponse {
        public boolean success;
        public String message;
        public BookInfo book;

        public AddBookResponse(boolean success, String message, BookInfo book) {
            this.success = success;
            this.message = message;
            this.book = book;
        }
    }

    // ===== Update Book Request/Response Classes =====

    static class UpdateBookRequest {
        public String id;
        public String title;
        public String author;
        public String publisher;
        public String description;
    }

    static class UpdateBookResponse {
        public boolean success;
        public String message;

        public UpdateBookResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    // ===== Delete Book Request/Response Classes =====

    static class DeleteBookRequest {
        public String id;
    }

    static class DeleteBookResponse {
        public boolean success;
        public String message;

        public DeleteBookResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    // ===== Borrow Records Response Class =====

    static class BorrowRecordsResponse {
        public boolean success;
        public List<BookInfo> records;

        public BorrowRecordsResponse(boolean success, List<BookInfo> records) {
            this.success = success;
            this.records = records;
        }
    }

    // ===== Phase 6: Borrow History Handlers =====

    /**
     * Handler for GET /api/history/user
     * Returns borrow history for current user
     */
    static class UserHistoryHandler implements HttpHandler {
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
                String response = gson.toJson(new ErrorResponse("Authentication required"));
                sendResponse(exchange, 401, "application/json", response);
                return;
            }

            // Get user history
            List<BorrowHistory> history = historyRepository.getUserHistory(session.username);

            HistoryResponse response = new HistoryResponse(true, history);
            String json = gson.toJson(response);
            sendResponse(exchange, 200, "application/json", json);
        }
    }

    /**
     * Handler for GET /api/history/current
     * Returns currently borrowed books for user
     */
    static class CurrentBorrowingsHandler implements HttpHandler {
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
                String response = gson.toJson(new ErrorResponse("Authentication required"));
                sendResponse(exchange, 401, "application/json", response);
                return;
            }

            // Get current borrowings
            List<BorrowHistory> current = historyRepository.getCurrentBorrowings(session.username);

            HistoryResponse response = new HistoryResponse(true, current);
            String json = gson.toJson(response);
            sendResponse(exchange, 200, "application/json", json);
        }
    }

    /**
     * Handler for GET /api/history/book/{bookId}
     * Returns borrow history for a specific book (admin only)
     */
    static class BookHistoryHandler implements HttpHandler {
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
                String response = gson.toJson(new ErrorResponse("Authentication required"));
                sendResponse(exchange, 401, "application/json", response);
                return;
            }

            // Get bookId from query parameter
            URI uri = exchange.getRequestURI();
            String query = uri.getQuery();
            String bookId = null;

            if (query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    String[] kv = param.split("=");
                    if (kv.length == 2 && "bookId".equals(kv[0])) {
                        bookId = kv[1];
                        break;
                    }
                }
            }

            if (bookId == null || bookId.isEmpty()) {
                String response = gson.toJson(new ErrorResponse("Missing bookId parameter"));
                sendResponse(exchange, 400, "application/json", response);
                return;
            }

            // Get book history
            List<BorrowHistory> history = historyRepository.getBookHistory(bookId);

            HistoryResponse response = new HistoryResponse(true, history);
            String json = gson.toJson(response);
            sendResponse(exchange, 200, "application/json", json);
        }
    }

    /**
     * Handler for GET /api/history/all
     * Returns all borrow history records (admin only)
     */
    static class AllHistoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                String response = gson.toJson(new ErrorResponse("Method not allowed. Use GET"));
                sendResponse(exchange, 405, "application/json", response);
                return;
            }

            // Validate session - must be logged in
            String sessionId = getSessionIdFromCookie(exchange);
            ApiSessionManager.SessionData session = ApiSessionManager.validateSession(sessionId);

            if (session == null) {
                String response = gson.toJson(new ErrorResponse("Authentication required"));
                sendResponse(exchange, 401, "application/json", response);
                return;
            }

            // Check admin permission - only 館長 and 館員 can view all history
            String userType = session.userType;
            if (!"館長".equals(userType) && !"館員".equals(userType)) {
                String response = gson.toJson(new ErrorResponse("Permission denied. Only administrators can view all borrow records"));
                sendResponse(exchange, 403, "application/json", response);
                return;
            }

            // Get all history
            List<BorrowHistory> history = historyRepository.getAllHistory();

            HistoryResponse response = new HistoryResponse(true, history);
            String json = gson.toJson(response);
            sendResponse(exchange, 200, "application/json", json);
        }
    }

    // ===== History Response Class =====

    static class HistoryResponse {
        public boolean success;
        public List<BorrowHistory> history;

        public HistoryResponse(boolean success, List<BorrowHistory> history) {
            this.success = success;
            this.history = history;
        }
    }

    // ===== Phase 6: Rating Handlers =====

    /**
     * Handler for POST /api/ratings/rate
     * Rate a book (1-5 stars)
     */
    static class RateBookHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                String response = gson.toJson(new ErrorResponse("Method not allowed. Use POST"));
                sendResponse(exchange, 405, "application/json", response);
                return;
            }

            // Validate session
            String sessionId = getSessionIdFromCookie(exchange);
            ApiSessionManager.SessionData session = ApiSessionManager.validateSession(sessionId);

            if (session == null) {
                String response = gson.toJson(new ErrorResponse("Authentication required"));
                sendResponse(exchange, 401, "application/json", response);
                return;
            }

            // Read request body
            String body = new String(exchange.getRequestBody().readAllBytes());
            RateRequest request = gson.fromJson(body, RateRequest.class);

            if (request == null || request.bookId == null) {
                String response = gson.toJson(new ErrorResponse("Missing bookId"));
                sendResponse(exchange, 400, "application/json", response);
                return;
            }

            if (request.rating < 1 || request.rating > 5) {
                String response = gson.toJson(new ErrorResponse("Rating must be 1-5"));
                sendResponse(exchange, 400, "application/json", response);
                return;
            }

            // Save rating
            boolean success = ratingRepository.saveRating(session.username, request.bookId, request.rating);

            if (success) {
                double avgRating = ratingRepository.getAverageRating(request.bookId);
                RatingResponse response = new RatingResponse(true, "Rating saved successfully", avgRating);
                String json = gson.toJson(response);
                sendResponse(exchange, 200, "application/json", json);
            } else {
                String response = gson.toJson(new ErrorResponse("Failed to save rating"));
                sendResponse(exchange, 500, "application/json", response);
            }
        }
    }

    /**
     * Handler for GET /api/ratings/book?bookId=XXX
     * Get all ratings for a book
     */
    static class GetBookRatingsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                String response = gson.toJson(new ErrorResponse("Method not allowed. Use GET"));
                sendResponse(exchange, 405, "application/json", response);
                return;
            }

            // Get bookId from query parameter
            URI uri = exchange.getRequestURI();
            String query = uri.getQuery();
            String bookId = null;

            if (query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    String[] kv = param.split("=");
                    if (kv.length == 2 && "bookId".equals(kv[0])) {
                        bookId = kv[1];
                        break;
                    }
                }
            }

            if (bookId == null || bookId.isEmpty()) {
                String response = gson.toJson(new ErrorResponse("Missing bookId parameter"));
                sendResponse(exchange, 400, "application/json", response);
                return;
            }

            // Get ratings
            List<BookRating> ratings = ratingRepository.getBookRatings(bookId);
            double avgRating = ratingRepository.getAverageRating(bookId);
            int ratingCount = ratings.size();
            String stats = ratingRepository.getRatingStats(bookId);

            // Get user's rating if logged in
            Integer userRating = null;
            String sessionId = getSessionIdFromCookie(exchange);
            ApiSessionManager.SessionData session = ApiSessionManager.validateSession(sessionId);
            if (session != null) {
                BookRating rating = ratingRepository.getUserRating(session.username, bookId);
                if (rating != null) {
                    userRating = rating.getRating();
                }
            }

            BookRatingsResponse response = new BookRatingsResponse(true, ratings, avgRating, ratingCount, stats, userRating);
            String json = gson.toJson(response);
            sendResponse(exchange, 200, "application/json", json);
        }
    }

    /**
     * Handler for GET /api/ratings/user?bookId=XXX
     * Get user's rating for a specific book
     */
    static class GetUserRatingHandler implements HttpHandler {
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
                String response = gson.toJson(new ErrorResponse("Authentication required"));
                sendResponse(exchange, 401, "application/json", response);
                return;
            }

            // Get bookId from query parameter
            URI uri = exchange.getRequestURI();
            String query = uri.getQuery();
            String bookId = null;

            if (query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    String[] kv = param.split("=");
                    if (kv.length == 2 && "bookId".equals(kv[0])) {
                        bookId = kv[1];
                        break;
                    }
                }
            }

            if (bookId == null || bookId.isEmpty()) {
                String response = gson.toJson(new ErrorResponse("Missing bookId parameter"));
                sendResponse(exchange, 400, "application/json", response);
                return;
            }

            // Get user rating
            BookRating rating = ratingRepository.getUserRating(session.username, bookId);

            UserRatingResponse response = new UserRatingResponse(true, rating);
            String json = gson.toJson(response);
            sendResponse(exchange, 200, "application/json", json);
        }
    }

    /**
     * Handler for GET /api/ratings/top?limit=10
     * Get top rated books
     */
    static class GetTopRatedBooksHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                String response = gson.toJson(new ErrorResponse("Method not allowed. Use GET"));
                sendResponse(exchange, 405, "application/json", response);
                return;
            }

            // Get limit from query parameter (default 10)
            int limit = 10;
            URI uri = exchange.getRequestURI();
            String query = uri.getQuery();

            if (query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    String[] kv = param.split("=");
                    if (kv.length == 2 && "limit".equals(kv[0])) {
                        try {
                            limit = Integer.parseInt(kv[1]);
                        } catch (NumberFormatException e) {
                            // Use default
                        }
                        break;
                    }
                }
            }

            // Get top rated books
            List<String> bookIds = ratingRepository.getTopRatedBooks(limit);
            List<BookInfo> books = new java.util.ArrayList<>();

            for (String bookId : bookIds) {
                BookInfo book = repository.findById(bookId);
                if (book != null) {
                    books.add(book);
                }
            }

            TopRatedBooksResponse response = new TopRatedBooksResponse(true, books);
            String json = gson.toJson(response);
            sendResponse(exchange, 200, "application/json", json);
        }
    }

    // ===== Rating Request/Response Classes =====

    static class RateRequest {
        public String bookId;
        public int rating;
    }

    static class RatingResponse {
        public boolean success;
        public String message;
        public double averageRating;

        public RatingResponse(boolean success, String message, double averageRating) {
            this.success = success;
            this.message = message;
            this.averageRating = averageRating;
        }
    }

    static class BookRatingsResponse {
        public boolean success;
        public List<BookRating> ratings;
        public double averageRating;
        public int ratingCount;
        public String stats;
        public Integer userRating;  // null if user not logged in or hasn't rated

        public BookRatingsResponse(boolean success, List<BookRating> ratings, double averageRating, int ratingCount, String stats, Integer userRating) {
            this.success = success;
            this.ratings = ratings;
            this.averageRating = averageRating;
            this.ratingCount = ratingCount;
            this.stats = stats;
            this.userRating = userRating;
        }
    }

    static class UserRatingResponse {
        public boolean success;
        public BookRating rating;

        public UserRatingResponse(boolean success, BookRating rating) {
            this.success = success;
            this.rating = rating;
        }
    }

    static class TopRatedBooksResponse {
        public boolean success;
        public List<BookInfo> books;

        public TopRatedBooksResponse(boolean success, List<BookInfo> books) {
            this.success = success;
            this.books = books;
        }
    }
    // ===== Phase 6: Review Handlers =====

    /**
     * Handler for POST /api/reviews/add
     * Add a new review for a book
     */
    static class AddReviewHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                String response = gson.toJson(new ErrorResponse("Method not allowed. Use POST"));
                sendResponse(exchange, 405, "application/json", response);
                return;
            }

            // Validate session
            String sessionId = getSessionIdFromCookie(exchange);
            ApiSessionManager.SessionData session = ApiSessionManager.validateSession(sessionId);

            if (session == null) {
                String response = gson.toJson(new ErrorResponse("Authentication required"));
                sendResponse(exchange, 401, "application/json", response);
                return;
            }

            // Read request body
            String body = new String(exchange.getRequestBody().readAllBytes());
            AddReviewRequest request = gson.fromJson(body, AddReviewRequest.class);

            if (request == null || request.bookId == null || request.reviewText == null) {
                String response = gson.toJson(new ErrorResponse("Missing bookId or reviewText"));
                sendResponse(exchange, 400, "application/json", response);
                return;
            }

            // Validate review text length
            if (request.reviewText.trim().isEmpty()) {
                String response = gson.toJson(new ErrorResponse("Review text cannot be empty"));
                sendResponse(exchange, 400, "application/json", response);
                return;
            }

            if (request.reviewText.length() > 1000) {
                String response = gson.toJson(new ErrorResponse("Review text too long (max 1000 characters)"));
                sendResponse(exchange, 400, "application/json", response);
                return;
            }

            // Get book info
            BookInfo book = repository.findById(request.bookId);
            if (book == null) {
                String response = gson.toJson(new ErrorResponse("Book not found"));
                sendResponse(exchange, 404, "application/json", response);
                return;
            }

            // Get user info
            User user = userRepository.findById(session.username);
            if (user == null) {
                String response = gson.toJson(new ErrorResponse("User not found"));
                sendResponse(exchange, 404, "application/json", response);
                return;
            }

            // Add review
            int reviewId = reviewRepository.addReview(
                session.username,
                user.getName(),
                request.bookId,
                book.getTitle(),
                request.reviewText
            );

            if (reviewId > 0) {
                // Send notification to users who borrowed this book (Phase 13)
                try {
                    notificationService.sendNewReviewNotification(
                        request.bookId,
                        book.getTitle(),
                        session.username
                    );
                } catch (Exception e) {
                    System.err.println("Failed to send review notification: " + e.getMessage());
                }

                ReviewResponse response = new ReviewResponse(true, "Review added successfully", reviewId);
                String json = gson.toJson(response);
                sendResponse(exchange, 200, "application/json", json);
            } else {
                String response = gson.toJson(new ErrorResponse("Failed to add review"));
                sendResponse(exchange, 500, "application/json", response);
            }
        }
    }

    /**
     * Handler for GET /api/reviews/book?bookId=XXX
     * Get all reviews for a book
     */
    static class GetBookReviewsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                String response = gson.toJson(new ErrorResponse("Method not allowed. Use GET"));
                sendResponse(exchange, 405, "application/json", response);
                return;
            }

            // Get bookId from query parameter
            URI uri = exchange.getRequestURI();
            String query = uri.getQuery();
            String bookId = null;

            if (query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    String[] kv = param.split("=");
                    if (kv.length == 2 && "bookId".equals(kv[0])) {
                        bookId = kv[1];
                        break;
                    }
                }
            }

            if (bookId == null || bookId.isEmpty()) {
                String response = gson.toJson(new ErrorResponse("Missing bookId parameter"));
                sendResponse(exchange, 400, "application/json", response);
                return;
            }

            // Get reviews
            List<BookReview> reviews = reviewRepository.getBookReviews(bookId);
            int reviewCount = reviewRepository.getReviewCount(bookId);

            BookReviewsResponse response = new BookReviewsResponse(true, reviews, reviewCount);
            String json = gson.toJson(response);
            sendResponse(exchange, 200, "application/json", json);
        }
    }

    /**
     * Handler for GET /api/reviews/user
     * Get all reviews by the current user
     */
    static class GetUserReviewsHandler implements HttpHandler {
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
                String response = gson.toJson(new ErrorResponse("Authentication required"));
                sendResponse(exchange, 401, "application/json", response);
                return;
            }

            // Get user reviews
            List<BookReview> reviews = reviewRepository.getUserReviews(session.username);

            UserReviewsResponse response = new UserReviewsResponse(true, reviews);
            String json = gson.toJson(response);
            sendResponse(exchange, 200, "application/json", json);
        }
    }

    /**
     * Handler for PUT /api/reviews/update
     * Update an existing review
     */
    static class UpdateReviewHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"PUT".equals(exchange.getRequestMethod())) {
                String response = gson.toJson(new ErrorResponse("Method not allowed. Use PUT"));
                sendResponse(exchange, 405, "application/json", response);
                return;
            }

            // Validate session
            String sessionId = getSessionIdFromCookie(exchange);
            ApiSessionManager.SessionData session = ApiSessionManager.validateSession(sessionId);

            if (session == null) {
                String response = gson.toJson(new ErrorResponse("Authentication required"));
                sendResponse(exchange, 401, "application/json", response);
                return;
            }

            // Read request body
            String body = new String(exchange.getRequestBody().readAllBytes());
            UpdateReviewRequest request = gson.fromJson(body, UpdateReviewRequest.class);

            if (request == null || request.reviewId <= 0 || request.reviewText == null) {
                String response = gson.toJson(new ErrorResponse("Missing reviewId or reviewText"));
                sendResponse(exchange, 400, "application/json", response);
                return;
            }

            // Validate review text
            if (request.reviewText.trim().isEmpty()) {
                String response = gson.toJson(new ErrorResponse("Review text cannot be empty"));
                sendResponse(exchange, 400, "application/json", response);
                return;
            }

            if (request.reviewText.length() > 1000) {
                String response = gson.toJson(new ErrorResponse("Review text too long (max 1000 characters)"));
                sendResponse(exchange, 400, "application/json", response);
                return;
            }

            // Update review
            boolean success = reviewRepository.updateReview(request.reviewId, session.username, request.reviewText);

            if (success) {
                SimpleResponse response = new SimpleResponse(true, "Review updated successfully");
                String json = gson.toJson(response);
                sendResponse(exchange, 200, "application/json", json);
            } else {
                String response = gson.toJson(new ErrorResponse("Failed to update review (not found or not owner)"));
                sendResponse(exchange, 403, "application/json", response);
            }
        }
    }

    /**
     * Handler for DELETE /api/reviews/delete
     * Delete a review
     */
    static class DeleteReviewHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"DELETE".equals(exchange.getRequestMethod())) {
                String response = gson.toJson(new ErrorResponse("Method not allowed. Use DELETE"));
                sendResponse(exchange, 405, "application/json", response);
                return;
            }

            // Validate session
            String sessionId = getSessionIdFromCookie(exchange);
            ApiSessionManager.SessionData session = ApiSessionManager.validateSession(sessionId);

            if (session == null) {
                String response = gson.toJson(new ErrorResponse("Authentication required"));
                sendResponse(exchange, 401, "application/json", response);
                return;
            }

            // Read request body
            String body = new String(exchange.getRequestBody().readAllBytes());
            DeleteReviewRequest request = gson.fromJson(body, DeleteReviewRequest.class);

            if (request == null || request.reviewId <= 0) {
                String response = gson.toJson(new ErrorResponse("Missing reviewId"));
                sendResponse(exchange, 400, "application/json", response);
                return;
            }

            // Delete review
            boolean success = reviewRepository.deleteReview(request.reviewId, session.username);

            if (success) {
                SimpleResponse response = new SimpleResponse(true, "Review deleted successfully");
                String json = gson.toJson(response);
                sendResponse(exchange, 200, "application/json", json);
            } else {
                String response = gson.toJson(new ErrorResponse("Failed to delete review (not found or not owner)"));
                sendResponse(exchange, 403, "application/json", response);
            }
        }
    }

    /**
     * Handler for GET /api/reviews/latest?limit=10
     * Get latest reviews across all books
     */
    static class GetLatestReviewsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                String response = gson.toJson(new ErrorResponse("Method not allowed. Use GET"));
                sendResponse(exchange, 405, "application/json", response);
                return;
            }

            // Get limit from query parameter (default 10)
            int limit = 10;
            URI uri = exchange.getRequestURI();
            String query = uri.getQuery();

            if (query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    String[] kv = param.split("=");
                    if (kv.length == 2 && "limit".equals(kv[0])) {
                        try {
                            limit = Integer.parseInt(kv[1]);
                        } catch (NumberFormatException e) {
                            // Use default
                        }
                        break;
                    }
                }
            }

            // Get latest reviews
            List<BookReview> reviews = reviewRepository.getLatestReviews(limit);

            LatestReviewsResponse response = new LatestReviewsResponse(true, reviews);
            String json = gson.toJson(response);
            sendResponse(exchange, 200, "application/json", json);
        }
    }

    // ===== Review Request/Response Classes =====

    static class AddReviewRequest {
        public String bookId;
        public String reviewText;
    }

    static class UpdateReviewRequest {
        public int reviewId;
        public String reviewText;
    }

    static class DeleteReviewRequest {
        public int reviewId;
    }

    static class ReviewResponse {
        public boolean success;
        public String message;
        public int reviewId;

        public ReviewResponse(boolean success, String message, int reviewId) {
            this.success = success;
            this.message = message;
            this.reviewId = reviewId;
        }
    }

    static class BookReviewsResponse {
        public boolean success;
        public List<BookReview> reviews;
        public int reviewCount;

        public BookReviewsResponse(boolean success, List<BookReview> reviews, int reviewCount) {
            this.success = success;
            this.reviews = reviews;
            this.reviewCount = reviewCount;
        }
    }

    static class UserReviewsResponse {
        public boolean success;
        public List<BookReview> reviews;

        public UserReviewsResponse(boolean success, List<BookReview> reviews) {
            this.success = success;
            this.reviews = reviews;
        }
    }

    static class LatestReviewsResponse {
        public boolean success;
        public List<BookReview> reviews;

        public LatestReviewsResponse(boolean success, List<BookReview> reviews) {
            this.success = success;
            this.reviews = reviews;
        }
    }

    // ===== AI Recommendation Handlers (Phase 10) =====

    /**
     * Handler for /api/recommendations/personal
     * Get AI-powered personal recommendations for current user (ASYNC with WebSocket)
     */
    static class PersonalRecommendationsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                String response = gson.toJson(new ErrorResponse("Method not allowed. Use POST"));
                sendResponse(exchange, 405, "application/json", response);
                return;
            }

            // Get current user from session
            String sessionId = getSessionIdFromCookie(exchange);
            ApiSessionManager.SessionData session = ApiSessionManager.validateSession(sessionId);

            if (session == null) {
                String response = gson.toJson(new ErrorResponse("Authentication required"));
                sendResponse(exchange, 401, "application/json", response);
                return;
            }

            try {
                String userId = session.username;

                // Generate unique task ID
                String taskId = java.util.UUID.randomUUID().toString();

                // Create task
                RecommendationTask task = taskManager.createTask(taskId, userId, "personal");

                // Return task ID immediately (non-blocking)
                TaskCreatedResponse taskResponse = new TaskCreatedResponse(true, taskId, "processing");
                sendResponse(exchange, 200, "application/json", gson.toJson(taskResponse));

                // Process recommendations asynchronously
                java.util.concurrent.CompletableFuture.runAsync(() -> {
                    try {
                        // Get user's borrow history
                        List<BorrowHistory> history = historyRepository.getUserHistory(userId);
                        List<BookInfo> borrowHistory = new ArrayList<>();
                        for (BorrowHistory h : history) {
                            BookInfo book = repository.findById(h.getBookId());
                            if (book != null) {
                                borrowHistory.add(book);
                            }
                        }

                        // Get available books
                        List<BookInfo> allBooks = repository.getAllBooks();
                        List<BookInfo> availableBooks = new ArrayList<>();
                        for (BookInfo book : allBooks) {
                            boolean alreadyBorrowed = false;
                            for (BookInfo borrowed : borrowHistory) {
                                if (borrowed.getId().equals(book.getId())) {
                                    alreadyBorrowed = true;
                                    break;
                                }
                            }
                            if (!alreadyBorrowed && book.isAvailable()) {
                                availableBooks.add(book);
                            }
                        }

                        // Generate recommendations asynchronously
                        recommendationService.getPersonalRecommendationsAsync(userId, borrowHistory, availableBooks)
                            .thenAccept(recommendations -> {
                                // Build response with full book info
                                List<RecommendationWithBook> result = new ArrayList<>();
                                for (Recommendation rec : recommendations) {
                                    BookInfo book = repository.findById(rec.getBookId());
                                    if (book != null) {
                                        result.add(new RecommendationWithBook(book, rec.getReason(), rec.getScore()));
                                    }
                                }

                                // Complete task
                                task.setResult(recommendations);
                                taskManager.completeTask(taskId, recommendations);

                                // Notify WebSocket clients
                                wsServer.notifyTaskComplete(taskId);

                                System.out.println("✓ Personal recommendations completed for user: " + userId);
                            })
                            .exceptionally(e -> {
                                // Handle error
                                String error = "AI service error: " + e.getMessage();
                                task.setError(error);
                                taskManager.failTask(taskId, error);
                                wsServer.notifyTaskComplete(taskId);
                                System.err.println("✗ Personal recommendations failed: " + error);
                                return null;
                            });

                    } catch (Exception e) {
                        task.setError(e.getMessage());
                        taskManager.failTask(taskId, e.getMessage());
                        wsServer.notifyTaskComplete(taskId);
                        System.err.println("✗ Error in async recommendation processing: " + e.getMessage());
                    }
                });

            } catch (Exception e) {
                System.err.println("Error creating recommendation task: " + e.getMessage());
                e.printStackTrace();
                String response = gson.toJson(new ErrorResponse("Failed to create recommendation task"));
                sendResponse(exchange, 500, "application/json", response);
            }
        }
    }

    /**
     * Handler for /api/recommendations/related?bookId=XXX
     * Get related book recommendations for a specific book
     */
    static class RelatedRecommendationsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                String response = gson.toJson(new ErrorResponse("Method not allowed. Use GET"));
                sendResponse(exchange, 405, "application/json", response);
                return;
            }

            try {
                // Parse query parameter
                URI uri = exchange.getRequestURI();
                String query = uri.getQuery();
                if (query == null || !query.startsWith("bookId=")) {
                    String response = gson.toJson(new ErrorResponse("Missing bookId parameter"));
                    sendResponse(exchange, 400, "application/json", response);
                    return;
                }

                String bookId = query.substring(7);
                BookInfo currentBook = repository.findById(bookId);

                if (currentBook == null) {
                    String response = gson.toJson(new ErrorResponse("Book not found"));
                    sendResponse(exchange, 404, "application/json", response);
                    return;
                }

                // Get books by same author or same publisher
                List<BookInfo> allBooks = repository.getAllBooks();
                List<BookInfo> relatedBooks = new ArrayList<>();

                for (BookInfo book : allBooks) {
                    if (!book.getId().equals(bookId)) {
                        if (book.getAuthor().equals(currentBook.getAuthor()) ||
                            book.getPublisher().equals(currentBook.getPublisher())) {
                            relatedBooks.add(book);
                        }
                    }
                }

                // If not enough, add other available books
                if (relatedBooks.size() < 10) {
                    for (BookInfo book : allBooks) {
                        if (!book.getId().equals(bookId) && !relatedBooks.contains(book) && book.isAvailable()) {
                            relatedBooks.add(book);
                            if (relatedBooks.size() >= 15) break;
                        }
                    }
                }

                // Generate related recommendations
                List<Recommendation> recommendations = recommendationService.getRelatedRecommendations(
                    currentBook, relatedBooks
                );

                // Build response
                List<RecommendationWithBook> result = new ArrayList<>();
                for (Recommendation rec : recommendations) {
                    BookInfo book = repository.findById(rec.getBookId());
                    if (book != null) {
                        result.add(new RecommendationWithBook(book, rec.getReason(), rec.getScore()));
                    }
                }

                RecommendationsResponse response = new RecommendationsResponse(true, result);
                sendResponse(exchange, 200, "application/json", gson.toJson(response));

            } catch (Exception e) {
                System.err.println("Error generating related recommendations: " + e.getMessage());
                e.printStackTrace();
                String response = gson.toJson(new ErrorResponse("Failed to generate recommendations"));
                sendResponse(exchange, 500, "application/json", response);
            }
        }
    }

    /**
     * Handler for /api/recommendations/health
     * Check if AI service is available
     */
    static class AIHealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                String response = gson.toJson(new ErrorResponse("Method not allowed. Use GET"));
                sendResponse(exchange, 405, "application/json", response);
                return;
            }

            boolean healthy = recommendationService.isHealthy();
            AIHealthResponse response = new AIHealthResponse(healthy);
            sendResponse(exchange, 200, "application/json", gson.toJson(response));
        }
    }

    /**
     * Handler for /api/chat
     * Simple chatbot endpoint - Linus style: just forward to AI service
     */
    static class ChatHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // CORS preflight
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 204, "text/plain", "");
                return;
            }

            if (!"POST".equals(exchange.getRequestMethod())) {
                String response = gson.toJson(new ErrorResponse("Method not allowed. Use POST"));
                sendResponse(exchange, 405, "application/json", response);
                return;
            }

            try {
                // Read request body
                String requestBody = new String(
                    exchange.getRequestBody().readAllBytes(),
                    java.nio.charset.StandardCharsets.UTF_8
                );

                System.out.println("📨 Received chat request");

                // Forward to AI service - simple and direct (Linus: no fancy abstractions)
                // Support both Docker (AI_SERVICE_URL env var) and local development (localhost:8888)
                String aiServiceUrl = System.getenv("AI_SERVICE_URL");
                if (aiServiceUrl == null || aiServiceUrl.isEmpty()) {
                    aiServiceUrl = "http://localhost:8888";
                }
                java.net.URL url = new java.net.URL(aiServiceUrl + "/chat");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000); // 5 seconds
                conn.setReadTimeout(30000);   // 30 seconds for AI response

                // Send request
                try (java.io.OutputStream os = conn.getOutputStream()) {
                    os.write(requestBody.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    os.flush();
                }

                // Read response
                int status = conn.getResponseCode();
                String responseBody;

                if (status == 200) {
                    responseBody = new String(
                        conn.getInputStream().readAllBytes(),
                        java.nio.charset.StandardCharsets.UTF_8
                    );
                    System.out.println("✅ Chat response received");
                } else {
                    // AI service error
                    responseBody = new String(
                        conn.getErrorStream().readAllBytes(),
                        java.nio.charset.StandardCharsets.UTF_8
                    );
                    System.err.println("⚠️  AI service returned status: " + status);
                }

                conn.disconnect();
                sendResponse(exchange, status, "application/json", responseBody);

            } catch (java.net.ConnectException e) {
                // AI service not running
                System.err.println("❌ AI service not available: " + e.getMessage());
                ChatResponse fallback = new ChatResponse(
                    false,
                    "抱歉，AI 服務目前無法使用。請稍後再試或聯絡圖書館管理員。"
                );
                sendResponse(exchange, 503, "application/json", gson.toJson(fallback));

            } catch (java.net.SocketTimeoutException e) {
                // Timeout
                System.err.println("⏱️  AI service timeout: " + e.getMessage());
                ChatResponse fallback = new ChatResponse(
                    false,
                    "AI 回應超時。請稍後再試。"
                );
                sendResponse(exchange, 504, "application/json", gson.toJson(fallback));

            } catch (Exception e) {
                // Other errors
                System.err.println("💥 Chat error: " + e.getMessage());
                e.printStackTrace();
                ChatResponse fallback = new ChatResponse(
                    false,
                    "發生錯誤：" + e.getMessage()
                );
                sendResponse(exchange, 500, "application/json", gson.toJson(fallback));
            }
        }
    }

    // ===== Recommendation Response Classes =====

    static class RecommendationWithBook {
        public BookInfo book;
        public String reason;
        public double score;

        public RecommendationWithBook(BookInfo book, String reason, double score) {
            this.book = book;
            this.reason = reason;
            this.score = score;
        }
    }

    static class RecommendationsResponse {
        public boolean success;
        public List<RecommendationWithBook> recommendations;

        public RecommendationsResponse(boolean success, List<RecommendationWithBook> recommendations) {
            this.success = success;
            this.recommendations = recommendations;
        }
    }

    static class TaskCreatedResponse {
        public boolean success;
        public String taskId;
        public String status;

        public TaskCreatedResponse(boolean success, String taskId, String status) {
            this.success = success;
            this.taskId = taskId;
            this.status = status;
        }
    }

    static class AIHealthResponse {
        public boolean healthy;

        public AIHealthResponse(boolean healthy) {
            this.healthy = healthy;
        }
    }

    // ===== Chat Response Classes =====

    static class ChatResponse {
        public boolean success;
        public String message;

        public ChatResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    // ===== Notification Handlers (Phase 13) =====

    /**
     * Handler for /api/notifications and /api/notifications/{id}/read
     * Handles both GET (list notifications) and POST (mark as read)
     */
    static class GetNotificationsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            // Check authentication
            String sessionId = getSessionIdFromCookie(exchange);
            ApiSessionManager.SessionData session = ApiSessionManager.validateSession(sessionId);

            if (session == null) {
                String response = gson.toJson(new ErrorResponse("Unauthorized"));
                sendResponse(exchange, 401, "application/json", response);
                return;
            }

            // Handle POST /api/notifications/{id}/read
            if ("POST".equals(method) && path.matches("/api/notifications/\\d+/read")) {
                handleMarkAsRead(exchange, session);
                return;
            }

            // Handle GET /api/notifications
            if (!"GET".equals(method)) {
                String response = gson.toJson(new ErrorResponse("Method not allowed"));
                sendResponse(exchange, 405, "application/json", response);
                return;
            }

            handleGetNotifications(exchange, session);
        }

        private void handleMarkAsRead(HttpExchange exchange, ApiSessionManager.SessionData session) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/");

            if (parts.length < 4) {
                String response = gson.toJson(new ErrorResponse("Invalid notification ID"));
                sendResponse(exchange, 400, "application/json", response);
                return;
            }

            try {
                int notificationId = Integer.parseInt(parts[3]);
                boolean success = notificationRepository.markAsRead(notificationId, session.username);

                if (success) {
                    SimpleResponse resp = new SimpleResponse(true, "Notification marked as read");
                    String jsonResponse = gson.toJson(resp);
                    sendResponse(exchange, 200, "application/json", jsonResponse);
                } else {
                    String response = gson.toJson(new ErrorResponse("Failed to mark as read"));
                    sendResponse(exchange, 400, "application/json", response);
                }
            } catch (NumberFormatException e) {
                String response = gson.toJson(new ErrorResponse("Invalid notification ID"));
                sendResponse(exchange, 400, "application/json", response);
            }
        }

        private void handleGetNotifications(HttpExchange exchange, ApiSessionManager.SessionData session) throws IOException {

            // Parse query parameters
            URI uri = exchange.getRequestURI();
            String query = uri.getQuery();
            boolean unreadOnly = false;
            String type = null;
            int limit = 50;
            int offset = 0;

            if (query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    String[] kv = param.split("=");
                    if (kv.length == 2) {
                        switch (kv[0]) {
                            case "unreadOnly":
                                unreadOnly = "true".equals(kv[1]);
                                break;
                            case "type":
                                type = kv[1];
                                break;
                            case "limit":
                                limit = Integer.parseInt(kv[1]);
                                break;
                            case "offset":
                                offset = Integer.parseInt(kv[1]);
                                break;
                        }
                    }
                }
            }

            // Get notifications
            List<Notification> notifications = notificationRepository.getUserNotifications(
                session.username, unreadOnly, type, limit, offset
            );

            int unreadCount = notificationRepository.getUnreadCount(session.username);
            int totalCount = notificationRepository.getTotalCount(session.username, unreadOnly, type);

            NotificationsResponse response = new NotificationsResponse(
                true, notifications, unreadCount, totalCount
            );

            String jsonResponse = gson.toJson(response);
            sendResponse(exchange, 200, "application/json", jsonResponse);
        }
    }

    /**
     * Handler for GET /api/notifications/unread-count
     * Get unread notification count
     */
    static class GetUnreadCountHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                String response = gson.toJson(new ErrorResponse("Method not allowed"));
                sendResponse(exchange, 405, "application/json", response);
                return;
            }

            // Check authentication
            String sessionId = getSessionIdFromCookie(exchange);
            ApiSessionManager.SessionData session = ApiSessionManager.validateSession(sessionId);

            if (session == null) {
                String response = gson.toJson(new ErrorResponse("Unauthorized"));
                sendResponse(exchange, 401, "application/json", response);
                return;
            }

            int count = notificationRepository.getUnreadCount(session.username);
            UnreadCountResponse response = new UnreadCountResponse(true, count);

            String jsonResponse = gson.toJson(response);
            sendResponse(exchange, 200, "application/json", jsonResponse);
        }
    }

    /**
     * Handler for POST /api/notifications/read-all
     * Mark all notifications as read
     */
    static class MarkAllAsReadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                String response = gson.toJson(new ErrorResponse("Method not allowed"));
                sendResponse(exchange, 405, "application/json", response);
                return;
            }

            // Check authentication
            String sessionId = getSessionIdFromCookie(exchange);
            ApiSessionManager.SessionData session = ApiSessionManager.validateSession(sessionId);

            if (session == null) {
                String response = gson.toJson(new ErrorResponse("Unauthorized"));
                sendResponse(exchange, 401, "application/json", response);
                return;
            }

            int count = notificationRepository.markAllAsRead(session.username);

            MarkAllAsReadResponse response = new MarkAllAsReadResponse(
                true, "All notifications marked as read", count
            );

            String jsonResponse = gson.toJson(response);
            sendResponse(exchange, 200, "application/json", jsonResponse);
        }
    }

    /**
     * Handler for DELETE /api/notifications/clear
     * Clear all notifications
     */
    static class ClearNotificationsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"DELETE".equals(exchange.getRequestMethod())) {
                String response = gson.toJson(new ErrorResponse("Method not allowed"));
                sendResponse(exchange, 405, "application/json", response);
                return;
            }

            // Check authentication
            String sessionId = getSessionIdFromCookie(exchange);
            ApiSessionManager.SessionData session = ApiSessionManager.validateSession(sessionId);

            if (session == null) {
                String response = gson.toJson(new ErrorResponse("Unauthorized"));
                sendResponse(exchange, 401, "application/json", response);
                return;
            }

            int count = notificationRepository.clearNotifications(session.username);

            ClearNotificationsResponse response = new ClearNotificationsResponse(
                true, "Notifications cleared", count
            );

            String jsonResponse = gson.toJson(response);
            sendResponse(exchange, 200, "application/json", jsonResponse);
        }
    }

    // ===== Notification Response Classes =====

    static class NotificationsResponse {
        public boolean success;
        public List<Notification> notifications;
        public int unreadCount;
        public int totalCount;

        public NotificationsResponse(boolean success, List<Notification> notifications,
                                    int unreadCount, int totalCount) {
            this.success = success;
            this.notifications = notifications;
            this.unreadCount = unreadCount;
            this.totalCount = totalCount;
        }
    }

    static class UnreadCountResponse {
        public boolean success;
        public int unreadCount;

        public UnreadCountResponse(boolean success, int unreadCount) {
            this.success = success;
            this.unreadCount = unreadCount;
        }
    }

    static class MarkAllAsReadResponse {
        public boolean success;
        public String message;
        public int updatedCount;

        public MarkAllAsReadResponse(boolean success, String message, int updatedCount) {
            this.success = success;
            this.message = message;
            this.updatedCount = updatedCount;
        }
    }

    static class ClearNotificationsResponse {
        public boolean success;
        public String message;
        public int deletedCount;

        public ClearNotificationsResponse(boolean success, String message, int deletedCount) {
            this.success = success;
            this.message = message;
            this.deletedCount = deletedCount;
        }
    }

    // ===== Account Management Handlers (Phase 12) =====

    /**
     * Handler for /api/accounts/users
     * GET: Get all users (館長+館員)
     * POST: Create user (館長+館員)
     * DELETE: Delete user (館長+館員)
     */
    static class AccountUsersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();

            // Authentication required (館長 or 館員)
            String sessionId = getSessionIdFromCookie(exchange);
            ApiSessionManager.SessionData session = ApiSessionManager.validateSession(sessionId);

            if (session == null) {
                String response = gson.toJson(new ErrorResponse("Unauthorized - Please login"));
                sendResponse(exchange, 401, "application/json", response);
                return;
            }

            if (!"館長".equals(session.userType) && !"館員".equals(session.userType)) {
                String response = gson.toJson(new ErrorResponse("Permission denied - Admin or Staff only"));
                sendResponse(exchange, 403, "application/json", response);
                return;
            }

            switch (method) {
                case "GET":
                    handleGetUsers(exchange);
                    break;
                case "POST":
                    handleCreateUser(exchange);
                    break;
                case "DELETE":
                    handleDeleteUser(exchange);
                    break;
                default:
                    String response = gson.toJson(new ErrorResponse("Method not allowed"));
                    sendResponse(exchange, 405, "application/json", response);
            }
        }

        private void handleGetUsers(HttpExchange exchange) throws IOException {
            List<User> users = userRepository.getUsersByRole("user");
            UsersListResponse response = new UsersListResponse(true, users);
            String json = gson.toJson(response);
            sendResponse(exchange, 200, "application/json", json);
        }

        private void handleCreateUser(HttpExchange exchange) throws IOException {
            String body = new String(exchange.getRequestBody().readAllBytes());
            CreateAccountRequest request = gson.fromJson(body, CreateAccountRequest.class);

            if (request == null || request.id == null || request.name == null || request.password == null) {
                String response = gson.toJson(new ErrorResponse("Missing required fields"));
                sendResponse(exchange, 400, "application/json", response);
                return;
            }

            boolean success = userRepository.registerUser(
                request.id,
                request.name,
                request.password,
                request.email
            );

            if (success) {
                SimpleMessageResponse response = new SimpleMessageResponse(true, "User created successfully");
                sendResponse(exchange, 200, "application/json", gson.toJson(response));
            } else {
                String response = gson.toJson(new ErrorResponse("User ID already exists"));
                sendResponse(exchange, 400, "application/json", response);
            }
        }

        private void handleDeleteUser(HttpExchange exchange) throws IOException {
            String body = new String(exchange.getRequestBody().readAllBytes());
            DeleteAccountRequest request = gson.fromJson(body, DeleteAccountRequest.class);

            if (request == null || request.userId == null) {
                String response = gson.toJson(new ErrorResponse("Missing userId"));
                sendResponse(exchange, 400, "application/json", response);
                return;
            }

            boolean success = userRepository.deleteUser(request.userId);

            if (success) {
                SimpleMessageResponse response = new SimpleMessageResponse(true, "User deleted successfully");
                sendResponse(exchange, 200, "application/json", gson.toJson(response));
            } else {
                String response = gson.toJson(new ErrorResponse("User not found"));
                sendResponse(exchange, 404, "application/json", response);
            }
        }
    }

    /**
     * Handler for /api/accounts/staff
     * GET: Get all staff (僅館長)
     * POST: Create staff (僅館長)
     * DELETE: Delete staff (僅館長)
     */
    static class AccountStaffHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();

            // Authentication required (僅館長)
            String sessionId = getSessionIdFromCookie(exchange);
            ApiSessionManager.SessionData session = ApiSessionManager.validateSession(sessionId);

            if (session == null) {
                String response = gson.toJson(new ErrorResponse("Unauthorized - Please login"));
                sendResponse(exchange, 401, "application/json", response);
                return;
            }

            if (!"館長".equals(session.userType)) {
                String response = gson.toJson(new ErrorResponse("Permission denied - Director only"));
                sendResponse(exchange, 403, "application/json", response);
                return;
            }

            switch (method) {
                case "GET":
                    handleGetStaff(exchange);
                    break;
                case "POST":
                    handleCreateStaff(exchange);
                    break;
                case "DELETE":
                    handleDeleteStaff(exchange);
                    break;
                default:
                    String response = gson.toJson(new ErrorResponse("Method not allowed"));
                    sendResponse(exchange, 405, "application/json", response);
            }
        }

        private void handleGetStaff(HttpExchange exchange) throws IOException {
            List<User> staff = userRepository.getUsersByRole("館員");
            UsersListResponse response = new UsersListResponse(true, staff);
            String json = gson.toJson(response);
            sendResponse(exchange, 200, "application/json", json);
        }

        private void handleCreateStaff(HttpExchange exchange) throws IOException {
            String body = new String(exchange.getRequestBody().readAllBytes());
            CreateAccountRequest request = gson.fromJson(body, CreateAccountRequest.class);

            if (request == null || request.id == null || request.name == null || request.password == null) {
                String response = gson.toJson(new ErrorResponse("Missing required fields"));
                sendResponse(exchange, 400, "application/json", response);
                return;
            }

            boolean success = userRepository.createStaff(
                request.id,
                request.name,
                request.password,
                request.email
            );

            if (success) {
                SimpleMessageResponse response = new SimpleMessageResponse(true, "Staff created successfully");
                sendResponse(exchange, 200, "application/json", gson.toJson(response));
            } else {
                String response = gson.toJson(new ErrorResponse("Staff ID already exists"));
                sendResponse(exchange, 400, "application/json", response);
            }
        }

        private void handleDeleteStaff(HttpExchange exchange) throws IOException {
            String body = new String(exchange.getRequestBody().readAllBytes());
            DeleteAccountRequest request = gson.fromJson(body, DeleteAccountRequest.class);

            if (request == null || request.userId == null) {
                String response = gson.toJson(new ErrorResponse("Missing userId"));
                sendResponse(exchange, 400, "application/json", response);
                return;
            }

            boolean success = userRepository.deleteUser(request.userId);

            if (success) {
                SimpleMessageResponse response = new SimpleMessageResponse(true, "Staff deleted successfully");
                sendResponse(exchange, 200, "application/json", gson.toJson(response));
            } else {
                String response = gson.toJson(new ErrorResponse("Staff not found"));
                sendResponse(exchange, 404, "application/json", response);
            }
        }
    }

    /**
     * Handler for /api/dashboard/stats
     * GET: Get comprehensive dashboard statistics
     * Requires authentication (館長 or 館員)
     */
    static class DashboardStatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                String response = gson.toJson(new ErrorResponse("Method not allowed. Use GET"));
                sendResponse(exchange, 405, "application/json", response);
                return;
            }

            // Authentication required (館長 or 館員)
            String sessionId = getSessionIdFromCookie(exchange);
            ApiSessionManager.SessionData session = ApiSessionManager.validateSession(sessionId);

            if (session == null) {
                String response = gson.toJson(new ErrorResponse("Unauthorized - Please login"));
                sendResponse(exchange, 401, "application/json", response);
                return;
            }

            if (!"館長".equals(session.userType) && !"館員".equals(session.userType)) {
                String response = gson.toJson(new ErrorResponse("Permission denied - Admin or Staff only"));
                sendResponse(exchange, 403, "application/json", response);
                return;
            }

            // Gather all statistics
            DashboardStats stats = new DashboardStats();

            // Total books (already available from existing stats)
            stats.totalBooks = repository.getAllBooks().size();

            // Total users (user role only, exclude staff and admin)
            stats.totalUsers = userRepository.getUsersByRole("user").size();

            // Today's borrows
            stats.todayBorrows = historyRepository.getTodayBorrowCount();

            // Average rating across all books
            stats.averageRating = ratingRepository.getOverallAverageRating();

            // Yesterday data for trends
            stats.yesterdayUsers = userRepository.getYesterdayUserCount();
            stats.yesterdayBorrows = historyRepository.getYesterdayBorrowCount();
            stats.yesterdayAvgRating = ratingRepository.getYesterdayAverageRating();

            DashboardStatsResponse response = new DashboardStatsResponse(true, stats);
            String json = gson.toJson(response);
            sendResponse(exchange, 200, "application/json", json);
        }
    }

    /**
     * Handler for /api/dashboard/borrow-trend
     * GET: Get daily borrow trend for the past N days
     * Requires authentication (館長 or 館員)
     */
    static class BorrowTrendHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                String response = gson.toJson(new ErrorResponse("Method not allowed. Use GET"));
                sendResponse(exchange, 405, "application/json", response);
                return;
            }

            // Authentication required (館長 or 館員)
            String sessionId = getSessionIdFromCookie(exchange);
            ApiSessionManager.SessionData session = ApiSessionManager.validateSession(sessionId);

            if (session == null) {
                String response = gson.toJson(new ErrorResponse("Unauthorized - Please login"));
                sendResponse(exchange, 401, "application/json", response);
                return;
            }

            if (!"館長".equals(session.userType) && !"館員".equals(session.userType)) {
                String response = gson.toJson(new ErrorResponse("Permission denied - Admin or Staff only"));
                sendResponse(exchange, 403, "application/json", response);
                return;
            }

            // Get query parameter for days (default 30)
            String query = exchange.getRequestURI().getQuery();
            int days = 30;
            if (query != null && query.contains("days=")) {
                try {
                    String daysParam = query.split("days=")[1].split("&")[0];
                    days = Integer.parseInt(daysParam);
                    if (days < 1 || days > 365) {
                        days = 30; // Default if out of range
                    }
                } catch (Exception e) {
                    days = 30; // Default on parse error
                }
            }

            // Get daily borrow trend data
            java.util.List<BorrowHistoryRepository.DailyBorrowCount> trendData =
                historyRepository.getDailyBorrowTrend(days);

            BorrowTrendResponse response = new BorrowTrendResponse(true, trendData);
            String json = gson.toJson(response);
            sendResponse(exchange, 200, "application/json", json);
        }
    }

    /**
     * Handler for /api/dashboard/system-info
     * GET: Get system information
     * Requires authentication (館長 or 館員)
     */
    static class SystemInfoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                String response = gson.toJson(new ErrorResponse("Method not allowed. Use GET"));
                sendResponse(exchange, 405, "application/json", response);
                return;
            }

            // Authentication required (館長 or 館員)
            String sessionId = getSessionIdFromCookie(exchange);
            ApiSessionManager.SessionData session = ApiSessionManager.validateSession(sessionId);

            if (session == null) {
                String response = gson.toJson(new ErrorResponse("Unauthorized - Please login"));
                sendResponse(exchange, 401, "application/json", response);
                return;
            }

            if (!"館長".equals(session.userType) && !"館員".equals(session.userType)) {
                String response = gson.toJson(new ErrorResponse("Permission denied - Admin or Staff only"));
                sendResponse(exchange, 403, "application/json", response);
                return;
            }

            // Collect system information
            SystemInfo info = new SystemInfo();

            // System version
            info.version = "v3.0.0-advanced";

            // Database size
            try {
                java.io.File dbFile = new java.io.File("data/library.db");
                if (dbFile.exists()) {
                    long sizeInBytes = dbFile.length();
                    info.databaseSize = String.format("%.2f MB", sizeInBytes / (1024.0 * 1024.0));
                } else {
                    info.databaseSize = "N/A";
                }
            } catch (Exception e) {
                info.databaseSize = "N/A";
            }

            // Total records (sum of all tables)
            info.totalRecords = repository.getAllBooks().size() +
                               userRepository.getUsersByRole("user").size() +
                               historyRepository.getAllHistory().size() +
                               ratingRepository.getTopRatedBooks(1000).size();

            // System uptime
            long uptimeMs = System.currentTimeMillis() - serverStartTime;
            long days = uptimeMs / (1000 * 60 * 60 * 24);
            long hours = (uptimeMs / (1000 * 60 * 60)) % 24;
            long minutes = (uptimeMs / (1000 * 60)) % 60;

            if (days > 0) {
                info.uptime = String.format("%d 天 %d 小時", days, hours);
            } else if (hours > 0) {
                info.uptime = String.format("%d 小時 %d 分鐘", hours, minutes);
            } else {
                info.uptime = String.format("%d 分鐘", minutes);
            }

            SystemInfoResponse response = new SystemInfoResponse(true, info);
            String json = gson.toJson(response);
            sendResponse(exchange, 200, "application/json", json);
        }
    }

    /**
     * Handler for /api/dashboard/top-books
     * GET: Get top books with complete statistics (borrow count, rating, reviews)
     * Query parameter: limit (default: 10, max: 50)
     * Requires authentication (館長 or 館員)
     */
    static class TopBooksHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                String response = gson.toJson(new ErrorResponse("Method not allowed. Use GET"));
                sendResponse(exchange, 405, "application/json", response);
                return;
            }

            // Authentication required (館長 or 館員)
            String sessionId = getSessionIdFromCookie(exchange);
            ApiSessionManager.SessionData session = ApiSessionManager.validateSession(sessionId);

            if (session == null) {
                String response = gson.toJson(new ErrorResponse("Unauthorized - Please login"));
                sendResponse(exchange, 401, "application/json", response);
                return;
            }

            if (!"館長".equals(session.userType) && !"館員".equals(session.userType)) {
                String response = gson.toJson(new ErrorResponse("Permission denied - Admin or Staff only"));
                sendResponse(exchange, 403, "application/json", response);
                return;
            }

            // Parse query parameters
            String query = exchange.getRequestURI().getQuery();
            int limit = 10;  // Default limit
            if (query != null && query.contains("limit=")) {
                try {
                    String limitParam = query.split("limit=")[1].split("&")[0];
                    limit = Integer.parseInt(limitParam);
                    // Validate limit (1-50 range)
                    if (limit < 1 || limit > 50) {
                        limit = 10;
                    }
                } catch (Exception e) {
                    limit = 10;
                }
            }

            // Get top books with statistics
            ArrayList<BookInfo> topBooks = repository.getTopBooksWithStats(limit);

            TopBooksResponse response = new TopBooksResponse(true, topBooks);
            String json = gson.toJson(response);
            sendResponse(exchange, 200, "application/json", json);
        }
    }

    // ===== Account Management Request/Response Classes =====

    static class CreateAccountRequest {
        public String id;
        public String name;
        public String password;
        public String email;
    }

    static class DeleteAccountRequest {
        public String userId;
    }

    static class UsersListResponse {
        public boolean success;
        public List<User> users;

        public UsersListResponse(boolean success, List<User> users) {
            this.success = success;
            this.users = users;
        }
    }

    static class SimpleMessageResponse {
        public boolean success;
        public String message;

        public SimpleMessageResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    // ===== Dashboard Statistics Classes =====

    static class DashboardStats {
        public int totalBooks;
        public int totalUsers;
        public int todayBorrows;
        public double averageRating;
        public int yesterdayUsers;
        public int yesterdayBorrows;
        public double yesterdayAvgRating;
    }

    static class DashboardStatsResponse {
        public boolean success;
        public DashboardStats stats;

        public DashboardStatsResponse(boolean success, DashboardStats stats) {
            this.success = success;
            this.stats = stats;
        }
    }

    static class BorrowTrendResponse {
        public boolean success;
        public java.util.List<BorrowHistoryRepository.DailyBorrowCount> data;

        public BorrowTrendResponse(boolean success, java.util.List<BorrowHistoryRepository.DailyBorrowCount> data) {
            this.success = success;
            this.data = data;
        }
    }

    static class SystemInfo {
        public String version;
        public String databaseSize;
        public int totalRecords;
        public String uptime;
    }

    static class SystemInfoResponse {
        public boolean success;
        public SystemInfo data;

        public SystemInfoResponse(boolean success, SystemInfo data) {
            this.success = success;
            this.data = data;
        }
    }

    static class TopBooksResponse {
        public boolean success;
        public ArrayList<BookInfo> books;

        public TopBooksResponse(boolean success, ArrayList<BookInfo> books) {
            this.success = success;
            this.books = books;
        }
    }

}
