import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Book Database Repository
 * Manages book data with SQLite database persistence
 *
 * Stage 5: Upgrade from JSON file to SQLite database
 * Following Linus principles: Simple, robust, no BS
 */
public class BookDatabaseRepository {

    private final String dbFile;
    private final String dbUrl;

    /**
     * Constructor with default database path
     */
    public BookDatabaseRepository() {
        this("data/library.db");
    }

    /**
     * Constructor with custom database path (for testing)
     */
    public BookDatabaseRepository(String dbPath) {
        this.dbFile = dbPath;
        this.dbUrl = "jdbc:sqlite:" + dbPath;
    }

    /**
     * Initialize database and create tables if not exist
     */
    public void initialize() {
        try (Connection conn = getConnection()) {
            // Create books table
            String createTableSQL = """
                CREATE TABLE IF NOT EXISTS books (
                    id TEXT PRIMARY KEY,
                    title TEXT NOT NULL,
                    author TEXT NOT NULL,
                    publisher TEXT NOT NULL,
                    is_available INTEGER NOT NULL DEFAULT 1
                )
                """;

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createTableSQL);
            }

            // Check if table is empty, if so, insert default books
            if (isEmpty()) {
                insertDefaultBooks();
            }

            System.out.println("✅ Database initialized successfully");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get database connection
     */
    private Connection getConnection() throws SQLException {
        // Create data directory if not exists
        java.io.File dbFileObj = new java.io.File(dbFile);
        java.io.File parentDir = dbFileObj.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        return DriverManager.getConnection(dbUrl);
    }

    /**
     * Check if books table is empty
     */
    private boolean isEmpty() {
        String sql = "SELECT COUNT(*) FROM books";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking if database is empty: " + e.getMessage());
        }
        return true;
    }

    /**
     * Insert default books
     */
    private void insertDefaultBooks() {
        String sql = "INSERT INTO books (id, title, author, publisher, is_available) VALUES (?, ?, ?, ?, 1)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Default books
            String[][] defaultBooks = {
                {"001", "Java", "吳柏毅", "中央大學"},
                {"002", "管數課本", "吳昀蓁", "台灣大學"},
                {"003", "英文課本", "林俊廷", "交通大學"},
                {"004", "國文課本", "屠安弟", "政治大學"},
                {"005", "體育課本", "陳重言", "清華大學"}
            };

            for (String[] book : defaultBooks) {
                pstmt.setString(1, book[0]);
                pstmt.setString(2, book[1]);
                pstmt.setString(3, book[2]);
                pstmt.setString(4, book[3]);
                pstmt.executeUpdate();
            }

            System.out.println("✅ Inserted " + defaultBooks.length + " default books");
        } catch (SQLException e) {
            System.err.println("Error inserting default books: " + e.getMessage());
        }
    }

    /**
     * Get all books
     */
    public List<BookInfo> getAllBooks() {
        List<BookInfo> books = new ArrayList<>();
        String sql = "SELECT id, title, author, publisher, is_available FROM books";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                BookInfo book = new BookInfo(
                    rs.getString("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("publisher")
                );

                // Set availability
                if (rs.getInt("is_available") == 0) {
                    book.markAsBorrowed();
                }

                books.add(book);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all books: " + e.getMessage());
        }

        return books;
    }

    /**
     * Find book by ID
     */
    public BookInfo findById(String id) {
        String sql = "SELECT id, title, author, publisher, is_available FROM books WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                BookInfo book = new BookInfo(
                    rs.getString("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("publisher")
                );

                if (rs.getInt("is_available") == 0) {
                    book.markAsBorrowed();
                }

                return book;
            }
        } catch (SQLException e) {
            System.err.println("Error finding book by ID: " + e.getMessage());
        }

        return null;
    }

    /**
     * Find book by title
     */
    public BookInfo findByTitle(String title) {
        String sql = "SELECT id, title, author, publisher, is_available FROM books WHERE title = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, title);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                BookInfo book = new BookInfo(
                    rs.getString("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("publisher")
                );

                if (rs.getInt("is_available") == 0) {
                    book.markAsBorrowed();
                }

                return book;
            }
        } catch (SQLException e) {
            System.err.println("Error finding book by title: " + e.getMessage());
        }

        return null;
    }

    /**
     * Add a new book
     */
    public boolean addBook(BookInfo book) {
        // Check if ID already exists
        if (findById(book.getId()) != null) {
            return false;
        }

        String sql = "INSERT INTO books (id, title, author, publisher, is_available) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, book.getId());
            pstmt.setString(2, book.getTitle());
            pstmt.setString(3, book.getAuthor());
            pstmt.setString(4, book.getPublisher());
            pstmt.setInt(5, book.isAvailable() ? 1 : 0);

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error adding book: " + e.getMessage());
            return false;
        }
    }

    /**
     * Remove book by ID
     */
    public boolean removeBookById(String id) {
        String sql = "DELETE FROM books WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error removing book: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update book availability
     */
    public void updateBook(BookInfo book) {
        String sql = "UPDATE books SET is_available = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, book.isAvailable() ? 1 : 0);
            pstmt.setString(2, book.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating book: " + e.getMessage());
        }
    }

    /**
     * Get statistics
     */
    public String getStatistics() {
        String sql = """
            SELECT
                COUNT(*) as total,
                SUM(CASE WHEN is_available = 1 THEN 1 ELSE 0 END) as available,
                SUM(CASE WHEN is_available = 0 THEN 1 ELSE 0 END) as borrowed
            FROM books
            """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                int total = rs.getInt("total");
                int available = rs.getInt("available");
                int borrowed = rs.getInt("borrowed");

                return String.format("Total: %d books | Available: %d | Borrowed: %d",
                                   total, available, borrowed);
            }
        } catch (SQLException e) {
            System.err.println("Error getting statistics: " + e.getMessage());
        }

        return "Statistics unavailable";
    }
}
