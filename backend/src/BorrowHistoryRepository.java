import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * BorrowHistoryRepository - Database access layer for borrow history
 * Following Linus principles: Simple, direct database operations
 * Single responsibility: CRUD operations for borrow_history table
 */
public class BorrowHistoryRepository {
    private Connection conn;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public BorrowHistoryRepository(Connection connection) {
        this.conn = connection;
        initialize();
    }

    /**
     * Initialize borrow_history table
     * Idempotent: safe to run multiple times
     */
    private void initialize() {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS borrow_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id TEXT NOT NULL,
                book_id TEXT NOT NULL,
                book_title TEXT NOT NULL,
                borrow_date TEXT NOT NULL,
                due_date TEXT NOT NULL,
                return_date TEXT,
                status TEXT NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users(id),
                FOREIGN KEY (book_id) REFERENCES books(id)
            )
            """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
            System.out.println("✅ Borrow history table initialized successfully");
        } catch (SQLException e) {
            System.err.println("❌ Failed to initialize borrow_history table: " + e.getMessage());
        }
    }

    /**
     * Create new borrow record
     * Called when user borrows a book
     */
    public boolean createBorrowRecord(String userId, String bookId, String bookTitle) {
        String sql = "INSERT INTO borrow_history (user_id, book_id, book_title, borrow_date, due_date, status) VALUES (?, ?, ?, ?, ?, ?)";

        LocalDate today = LocalDate.now();
        LocalDate dueDate = today.plusDays(14);  // 14-day loan period

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, bookId);
            pstmt.setString(3, bookTitle);
            pstmt.setString(4, today.format(DATE_FORMAT));
            pstmt.setString(5, dueDate.format(DATE_FORMAT));
            pstmt.setString(6, "borrowing");

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to create borrow record: " + e.getMessage());
            return false;
        }
    }

    /**
     * Mark book as returned
     * Updates the most recent borrowing record for this book and user
     */
    public boolean markAsReturned(String userId, String bookId) {
        // SQLite doesn't support ORDER BY and LIMIT in UPDATE
        // So we first find the record ID, then update it
        String findSql = """
            SELECT id FROM borrow_history
            WHERE user_id = ? AND book_id = ? AND status = 'borrowing'
            ORDER BY id DESC
            LIMIT 1
            """;

        String updateSql = """
            UPDATE borrow_history
            SET return_date = ?, status = 'returned'
            WHERE id = ?
            """;

        try {
            // Find the most recent borrowing record
            int recordId = -1;
            try (PreparedStatement findStmt = conn.prepareStatement(findSql)) {
                findStmt.setString(1, userId);
                findStmt.setString(2, bookId);
                ResultSet rs = findStmt.executeQuery();
                if (rs.next()) {
                    recordId = rs.getInt("id");
                }
            }

            if (recordId == -1) {
                return false;  // No active borrowing found
            }

            // Update the found record
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, LocalDate.now().format(DATE_FORMAT));
                updateStmt.setInt(2, recordId);
                int updated = updateStmt.executeUpdate();
                return updated > 0;
            }
        } catch (SQLException e) {
            System.err.println("Failed to mark as returned: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get all borrow history for a user
     * Ordered by borrow date descending (most recent first)
     */
    public ArrayList<BorrowHistory> getUserHistory(String userId) {
        String sql = """
            SELECT id, user_id, book_id, book_title, borrow_date, due_date, return_date, status
            FROM borrow_history
            WHERE user_id = ?
            ORDER BY borrow_date DESC
            """;

        ArrayList<BorrowHistory> history = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                history.add(new BorrowHistory(
                    rs.getInt("id"),
                    rs.getString("user_id"),
                    rs.getString("book_id"),
                    rs.getString("book_title"),
                    rs.getString("borrow_date"),
                    rs.getString("due_date"),
                    rs.getString("return_date"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Failed to get user history: " + e.getMessage());
        }

        return history;
    }

    /**
     * Get all currently borrowed books for a user
     */
    public ArrayList<BorrowHistory> getCurrentBorrowings(String userId) {
        String sql = """
            SELECT id, user_id, book_id, book_title, borrow_date, due_date, return_date, status
            FROM borrow_history
            WHERE user_id = ? AND status = 'borrowing'
            ORDER BY borrow_date DESC
            """;

        ArrayList<BorrowHistory> current = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                current.add(new BorrowHistory(
                    rs.getInt("id"),
                    rs.getString("user_id"),
                    rs.getString("book_id"),
                    rs.getString("book_title"),
                    rs.getString("borrow_date"),
                    rs.getString("due_date"),
                    rs.getString("return_date"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Failed to get current borrowings: " + e.getMessage());
        }

        return current;
    }

    /**
     * Find active borrowing record for a book (status = 'borrowing')
     * Returns null if book is not currently borrowed
     */
    public BorrowHistory findActiveBorrowByBook(String bookId) {
        String sql = """
            SELECT id, user_id, book_id, book_title, borrow_date, due_date, return_date, status
            FROM borrow_history
            WHERE book_id = ? AND status = 'borrowing'
            ORDER BY id DESC
            LIMIT 1
            """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bookId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new BorrowHistory(
                    rs.getInt("id"),
                    rs.getString("user_id"),
                    rs.getString("book_id"),
                    rs.getString("book_title"),
                    rs.getString("borrow_date"),
                    rs.getString("due_date"),
                    rs.getString("return_date"),
                    rs.getString("status")
                );
            }
        } catch (SQLException e) {
            System.err.println("Failed to find active borrow record: " + e.getMessage());
        }

        return null;
    }

    /**
     * Get all borrow history for a specific book
     * Useful for tracking book popularity
     */
    public ArrayList<BorrowHistory> getBookHistory(String bookId) {
        String sql = """
            SELECT id, user_id, book_id, book_title, borrow_date, due_date, return_date, status
            FROM borrow_history
            WHERE book_id = ?
            ORDER BY borrow_date DESC
            """;

        ArrayList<BorrowHistory> history = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bookId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                history.add(new BorrowHistory(
                    rs.getInt("id"),
                    rs.getString("user_id"),
                    rs.getString("book_id"),
                    rs.getString("book_title"),
                    rs.getString("borrow_date"),
                    rs.getString("due_date"),
                    rs.getString("return_date"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Failed to get book history: " + e.getMessage());
        }

        return history;
    }

    /**
     * Get all borrow history (admin only)
     * Returns all borrow records from all users
     */
    public ArrayList<BorrowHistory> getAllHistory() {
        String sql = """
            SELECT h.id, h.user_id, h.book_id, h.book_title, h.borrow_date, h.due_date, h.return_date, h.status,
                   u.name as user_name
            FROM borrow_history h
            LEFT JOIN users u ON h.user_id = u.id
            ORDER BY h.borrow_date DESC
            """;

        ArrayList<BorrowHistory> history = new ArrayList<>();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                BorrowHistory record = new BorrowHistory(
                    rs.getInt("id"),
                    rs.getString("user_id"),
                    rs.getString("book_id"),
                    rs.getString("book_title"),
                    rs.getString("borrow_date"),
                    rs.getString("due_date"),
                    rs.getString("return_date"),
                    rs.getString("status")
                );
                // Set user name if available
                String userName = rs.getString("user_name");
                if (userName != null) {
                    record.setUserName(userName);
                }
                history.add(record);
            }
        } catch (SQLException e) {
            System.err.println("Failed to get all history: " + e.getMessage());
        }

        return history;
    }

    /**
     * Update overdue status for all borrowing records
     * Should be called periodically (e.g., daily cron job)
     */
    public int updateOverdueStatus() {
        String sql = """
            UPDATE borrow_history
            SET status = 'overdue'
            WHERE status = 'borrowing' AND due_date < ?
            """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, LocalDate.now().format(DATE_FORMAT));
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to update overdue status: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Get statistics about borrow history
     * Returns total borrows, current borrows, overdue count
     */
    public String getStatistics() {
        String sql = """
            SELECT
                COUNT(*) as total,
                SUM(CASE WHEN status = 'borrowing' THEN 1 ELSE 0 END) as current,
                SUM(CASE WHEN status = 'overdue' THEN 1 ELSE 0 END) as overdue
            FROM borrow_history
            """;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return String.format("Total borrows: %d | Current: %d | Overdue: %d",
                    rs.getInt("total"),
                    rs.getInt("current"),
                    rs.getInt("overdue"));
            }
        } catch (SQLException e) {
            System.err.println("Failed to get statistics: " + e.getMessage());
        }

        return "Statistics unavailable";
    }

    /**
     * Get count of borrows today
     * @return number of borrows today
     */
    public int getTodayBorrowCount() {
        String sql = """
            SELECT COUNT(*) as count FROM borrow_history
            WHERE strftime('%Y-%m-%d', borrow_date) = strftime('%Y-%m-%d', 'now')
            """;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("count");
            }

        } catch (SQLException e) {
            System.err.println("Error getting today borrow count: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Get count of borrows yesterday
     * @return number of borrows yesterday
     */
    public int getYesterdayBorrowCount() {
        String sql = """
            SELECT COUNT(*) as count FROM borrow_history
            WHERE strftime('%Y-%m-%d', borrow_date) = strftime('%Y-%m-%d', date('now', '-1 day'))
            """;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("count");
            }

        } catch (SQLException e) {
            System.err.println("Error getting yesterday borrow count: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Get daily borrow counts for the past N days
     * @param days Number of days to retrieve (e.g., 30)
     * @return List of daily counts, from oldest to newest
     */
    public java.util.List<DailyBorrowCount> getDailyBorrowTrend(int days) {
        java.util.List<DailyBorrowCount> result = new java.util.ArrayList<>();

        // Generate all dates for the past N days using Java, then query for each
        java.util.Map<String, Integer> borrowCounts = new java.util.LinkedHashMap<>();

        // Calculate date range
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate startDate = today.minusDays(days - 1);

        // Initialize all dates with 0 count
        for (int i = 0; i < days; i++) {
            java.time.LocalDate date = startDate.plusDays(i);
            borrowCounts.put(date.toString(), 0);
        }

        // Query actual borrow counts
        String sql = """
            SELECT
                strftime('%Y-%m-%d', borrow_date) as date,
                COUNT(*) as count
            FROM borrow_history
            WHERE borrow_date >= date('now', '-' || ? || ' days')
            GROUP BY strftime('%Y-%m-%d', borrow_date)
            """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, days - 1);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String date = rs.getString("date");
                int count = rs.getInt("count");
                if (borrowCounts.containsKey(date)) {
                    borrowCounts.put(date, count);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting daily borrow trend: " + e.getMessage());
            e.printStackTrace();
        }

        // Convert map to list
        for (java.util.Map.Entry<String, Integer> entry : borrowCounts.entrySet()) {
            result.add(new DailyBorrowCount(entry.getKey(), entry.getValue()));
        }

        return result;
    }

    /**
     * Simple data class for daily borrow count
     */
    public static class DailyBorrowCount {
        public String date;
        public int count;

        public DailyBorrowCount(String date, int count) {
            this.date = date;
            this.count = count;
        }
    }
}
