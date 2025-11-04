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

            // Database migration: Add description column if not exists
            boolean hasDescription = false;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("PRAGMA table_info(books)")) {
                while (rs.next()) {
                    if ("description".equals(rs.getString("name"))) {
                        hasDescription = true;
                        break;
                    }
                }
            }

            if (!hasDescription) {
                System.out.println("📝 Adding description column to books table...");
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE books ADD COLUMN description TEXT");
                }
                System.out.println("✅ Description column added successfully");
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
     * Made public for Phase 6 - needed by BorrowHistoryRepository
     */
    public Connection getConnection() throws SQLException {
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
     * Insert default books with descriptions
     */
    private void insertDefaultBooks() {
        String sql = "INSERT INTO books (id, title, author, publisher, description, is_available) VALUES (?, ?, ?, ?, ?, 1)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Default books - 20 books with descriptions
            String[][] defaultBooks = {
                // Computer Science
                {"001", "深入淺出 Java 程式設計", "吳柏毅", "碁峰資訊",
                 "適合初學者的 Java 程式設計入門書籍，透過淺顯易懂的方式介紹 Java 核心概念與實務應用，涵蓋物件導向程式設計、例外處理、集合框架等重要主題。"},
                {"002", "Python 資料科學入門", "陳怡君", "博碩文化",
                 "介紹如何使用 Python 進行資料分析與視覺化，涵蓋 NumPy、Pandas、Matplotlib 等常用套件，適合想要進入資料科學領域的初學者。"},
                {"003", "演算法圖鑑", "林俊廷", "旗標出版",
                 "透過豐富的圖解說明各種經典演算法的運作原理，包括排序、搜尋、圖論等，適合資訊科系學生與程式開發者參考。"},
                {"004", "網頁設計必修課", "張雅婷", "松崗資訊",
                 "全面介紹現代網頁設計的基礎知識，包括 HTML5、CSS3、JavaScript 等前端技術，以及響應式設計的實作方法。"},
                {"005", "資料庫系統理論與實務", "李明哲", "全華圖書",
                 "深入探討資料庫系統的設計原理與實務應用，涵蓋關聯式資料庫、SQL 語法、正規化理論、交易處理等重要主題。"},

                // Business & Management
                {"006", "管理數學", "吳昀蓁", "華泰文化",
                 "介紹管理決策中常用的數學工具與方法，包括線性規劃、決策分析、機率統計等，幫助管理者做出更科學的決策。"},
                {"007", "行銷管理學", "黃建華", "雙葉書廊",
                 "全面探討行銷管理的理論與實務，涵蓋市場分析、消費者行為、行銷策略、品牌管理等核心議題。"},
                {"008", "財務管理", "王志成", "新陸書局",
                 "介紹企業財務管理的基本概念與應用，包括財務報表分析、投資決策、資本結構、風險管理等重要主題。"},
                {"009", "經濟學原理", "劉大年", "東華書局",
                 "系統性介紹個體經濟學與總體經濟學的基本原理，透過生活化的案例幫助讀者理解經濟運作的邏輯。"},
                {"010", "統計學", "陳順宇", "華泰文化",
                 "介紹統計學的基本概念與方法，包括敘述統計、機率分布、假設檢定、迴歸分析等，適合商管學院學生使用。"},

                // Literature
                {"011", "現代文學選讀", "屠安弟", "三民書局",
                 "精選台灣現代文學作品，涵蓋小說、散文、詩歌等不同文類，呈現台灣文學的多元面貌與獨特魅力。"},
                {"012", "唐詩三百首", "李白", "商務印書館",
                 "收錄唐代最具代表性的三百首詩作，包括李白、杜甫、王維等大家的經典之作，是學習中國古典詩詞的最佳入門書。"},
                {"013", "紅樓夢", "曹雪芹", "遠流出版",
                 "中國古典四大名著之一，透過賈寶玉與林黛玉的愛情故事，深刻描繪封建社會的興衰與人性的複雜。"},
                {"014", "莎士比亞戲劇選", "莎士比亞", "聯經出版",
                 "收錄莎士比亞最著名的劇作，包括《哈姆雷特》、《羅密歐與茱麗葉》等，展現大師對人性的深刻洞察。"},

                // Science
                {"015", "普通物理學", "張文亮", "高立圖書",
                 "介紹物理學的基本原理與應用，涵蓋力學、熱學、電磁學、光學等主題，適合理工科系學生作為入門教材。"},
                {"016", "基礎化學", "周芳妃", "華杏出版",
                 "系統性介紹化學的基本概念，包括原子結構、化學鍵、化學反應、有機化學等，適合醫護與生科領域學生使用。"},
                {"017", "生物學", "陳重言", "藝軒圖書",
                 "全面介紹生命科學的基礎知識，從細胞生物學到生態學，幫助讀者建立完整的生物學知識體系。"},

                // Language Learning
                {"018", "新多益聽力滿分攻略", "劉毅", "學習出版",
                 "針對多益聽力測驗設計的完整訓練教材，提供豐富的練習題與答題技巧，幫助考生快速提升聽力能力。"},
                {"019", "日語50音完全攻略", "林美惠", "大新書局",
                 "專為日語初學者設計的50音教材，透過系統化的學習方式，快速掌握平假名與片假名的讀寫。"},
                {"020", "西班牙語入門", "張芳琪", "瑞蘭國際",
                 "適合零基礎學習者的西班牙語入門書，涵蓋基本文法、常用會話與文化介紹，讓學習更有趣。"}
            };

            for (String[] book : defaultBooks) {
                pstmt.setString(1, book[0]);
                pstmt.setString(2, book[1]);
                pstmt.setString(3, book[2]);
                pstmt.setString(4, book[3]);
                pstmt.setString(5, book[4]);
                pstmt.executeUpdate();
            }

            System.out.println("✅ Inserted " + defaultBooks.length + " default books with descriptions");
        } catch (SQLException e) {
            System.err.println("Error inserting default books: " + e.getMessage());
        }
    }

    /**
     * Get all books
     */
    public List<BookInfo> getAllBooks() {
        List<BookInfo> books = new ArrayList<>();
        // Enhanced query with statistics (Phase 12 Enhancement)
        String sql = """
            SELECT
                b.id,
                b.title,
                b.author,
                b.publisher,
                b.description,
                b.is_available,
                COUNT(DISTINCT bh.id) as borrow_count,
                COALESCE(AVG(br.rating), 0) as average_rating,
                COUNT(DISTINCT brev.id) as review_count
            FROM books b
            LEFT JOIN borrow_history bh ON b.id = bh.book_id
            LEFT JOIN book_ratings br ON b.id = br.book_id
            LEFT JOIN book_reviews brev ON b.id = brev.book_id
            GROUP BY b.id, b.title, b.author, b.publisher, b.description, b.is_available
            """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Create book with statistics
                BookInfo book = new BookInfo(
                    rs.getString("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("publisher"),
                    rs.getString("description"),
                    rs.getInt("borrow_count"),
                    rs.getDouble("average_rating"),
                    rs.getInt("review_count")
                );

                // Set availability
                if (rs.getInt("is_available") == 0) {
                    book.markAsBorrowed();
                }

                books.add(book);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all books: " + e.getMessage());
            e.printStackTrace();
        }

        return books;
    }

    /**
     * Find book by ID
     */
    public BookInfo findById(String id) {
        String sql = "SELECT id, title, author, publisher, description, is_available FROM books WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                BookInfo book = new BookInfo(
                    rs.getString("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("publisher"),
                    rs.getString("description")
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
        String sql = "SELECT id, title, author, publisher, description, is_available FROM books WHERE title = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, title);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                BookInfo book = new BookInfo(
                    rs.getString("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("publisher"),
                    rs.getString("description")
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

        String sql = "INSERT INTO books (id, title, author, publisher, description, is_available) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, book.getId());
            pstmt.setString(2, book.getTitle());
            pstmt.setString(3, book.getAuthor());
            pstmt.setString(4, book.getPublisher());
            pstmt.setString(5, book.getDescription());
            pstmt.setInt(6, book.isAvailable() ? 1 : 0);

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
     * Update book information (title, author, publisher, description)
     * Does NOT update availability status - use updateBook() for that
     */
    public boolean updateBookInfo(String id, String title, String author, String publisher, String description) {
        String sql = "UPDATE books SET title = ?, author = ?, publisher = ?, description = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, title);
            pstmt.setString(2, author);
            pstmt.setString(3, publisher);
            pstmt.setString(4, description);
            pstmt.setString(5, id);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating book info: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete a book by ID
     * Only allows deletion of available (not borrowed) books
     */
    public boolean deleteBook(String id) {
        // First check if book exists and is available
        BookInfo book = findById(id);
        if (book == null) {
            System.err.println("Cannot delete: Book not found - " + id);
            return false;
        }

        if (!book.isAvailable()) {
            System.err.println("Cannot delete: Book is currently borrowed - " + id);
            return false;
        }

        String sql = "DELETE FROM books WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting book: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get all currently borrowed books
     */
    public List<BookInfo> getBorrowedBooks() {
        List<BookInfo> books = new ArrayList<>();
        String sql = "SELECT id, title, author, publisher, description, is_available FROM books WHERE is_available = 0";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                BookInfo book = new BookInfo(
                    rs.getString("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("publisher"),
                    rs.getString("description")
                );
                if (rs.getInt("is_available") == 0) {
                    book.markAsBorrowed();
                }
                books.add(book);
            }
        } catch (SQLException e) {
            System.err.println("Error getting borrowed books: " + e.getMessage());
        }

        return books;
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

    /**
     * Get top books with complete statistics (borrow count, rating, review count)
     * For dashboard TOP 10 display
     * @param limit Number of top books to return
     * @return List of BookInfo with statistics
     */
    public ArrayList<BookInfo> getTopBooksWithStats(int limit) {
        String sql = """
            SELECT
                b.id,
                b.title,
                b.author,
                b.publisher,
                b.description,
                b.is_available,
                COALESCE(borrow_stats.borrow_count, 0) as borrow_count,
                COALESCE(rating_stats.avg_rating, 0.0) as avg_rating,
                COALESCE(review_stats.review_count, 0) as review_count
            FROM books b
            LEFT JOIN (
                SELECT book_id, COUNT(*) as borrow_count
                FROM borrow_history
                GROUP BY book_id
            ) borrow_stats ON b.id = borrow_stats.book_id
            LEFT JOIN (
                SELECT book_id, AVG(rating) as avg_rating
                FROM book_ratings
                GROUP BY book_id
            ) rating_stats ON b.id = rating_stats.book_id
            LEFT JOIN (
                SELECT book_id, COUNT(*) as review_count
                FROM book_reviews
                GROUP BY book_id
            ) review_stats ON b.id = review_stats.book_id
            ORDER BY borrow_count DESC, avg_rating DESC
            LIMIT ?
            """;

        ArrayList<BookInfo> topBooks = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String id = rs.getString("id");
                String title = rs.getString("title");
                String author = rs.getString("author");
                String publisher = rs.getString("publisher");
                String description = rs.getString("description");
                int borrowCount = rs.getInt("borrow_count");
                double avgRating = rs.getDouble("avg_rating");
                int reviewCount = rs.getInt("review_count");

                BookInfo book = new BookInfo(id, title, author, publisher, description,
                                            borrowCount, avgRating, reviewCount);

                // Set availability status
                if (rs.getInt("is_available") == 0) {
                    book.markAsBorrowed();
                }

                topBooks.add(book);
            }
        } catch (SQLException e) {
            System.err.println("Error getting top books with stats: " + e.getMessage());
            e.printStackTrace();
        }

        return topBooks;
    }
}
