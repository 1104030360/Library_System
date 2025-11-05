import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * BookRatingRepository - Database access layer for book ratings
 * Following Linus principles: Simple, direct database operations
 * Single responsibility: CRUD operations for book_ratings table
 */
public class BookRatingRepository {
    private Connection conn;
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public BookRatingRepository(Connection connection) {
        this.conn = connection;
        initialize();
    }

    /**
     * Initialize book_ratings table
     * Idempotent: safe to run multiple times
     */
    private void initialize() {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS book_ratings (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id TEXT NOT NULL,
                book_id TEXT NOT NULL,
                rating INTEGER NOT NULL CHECK(rating >= 1 AND rating <= 5),
                created_at TEXT NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users(id),
                FOREIGN KEY (book_id) REFERENCES books(id),
                UNIQUE(user_id, book_id)
            )
            """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
            System.out.println("✅ Book ratings table initialized successfully");
        } catch (SQLException e) {
            System.err.println("❌ Failed to initialize book_ratings table: " + e.getMessage());
        }
    }

    /**
     * Add or update a book rating
     * Uses UPSERT (INSERT OR REPLACE) to handle existing ratings
     */
    public boolean saveRating(String userId, String bookId, int rating) {
        if (rating < 1 || rating > 5) {
            System.err.println("Invalid rating: " + rating + ". Must be 1-5");
            return false;
        }

        String sql = """
            INSERT INTO book_ratings (user_id, book_id, rating, created_at)
            VALUES (?, ?, ?, ?)
            ON CONFLICT(user_id, book_id)
            DO UPDATE SET rating = excluded.rating, created_at = excluded.created_at
            """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, bookId);
            pstmt.setInt(3, rating);
            pstmt.setString(4, LocalDateTime.now().format(DATETIME_FORMAT));

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to save rating: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get a user's rating for a specific book
     * Returns null if no rating found
     */
    public BookRating getUserRating(String userId, String bookId) {
        String sql = """
            SELECT id, user_id, book_id, rating, created_at
            FROM book_ratings
            WHERE user_id = ? AND book_id = ?
            """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, bookId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new BookRating(
                    rs.getInt("id"),
                    rs.getString("user_id"),
                    rs.getString("book_id"),
                    rs.getInt("rating"),
                    rs.getString("created_at")
                );
            }
        } catch (SQLException e) {
            System.err.println("Failed to get user rating: " + e.getMessage());
        }

        return null;
    }

    /**
     * Get all ratings for a specific book
     */
    public ArrayList<BookRating> getBookRatings(String bookId) {
        String sql = """
            SELECT id, user_id, book_id, rating, created_at
            FROM book_ratings
            WHERE book_id = ?
            ORDER BY created_at DESC
            """;

        ArrayList<BookRating> ratings = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bookId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                ratings.add(new BookRating(
                    rs.getInt("id"),
                    rs.getString("user_id"),
                    rs.getString("book_id"),
                    rs.getInt("rating"),
                    rs.getString("created_at")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Failed to get book ratings: " + e.getMessage());
        }

        return ratings;
    }

    /**
     * Get average rating for a book
     * Returns 0.0 if no ratings
     */
    public double getAverageRating(String bookId) {
        String sql = """
            SELECT AVG(rating) as avg_rating, COUNT(*) as count
            FROM book_ratings
            WHERE book_id = ?
            """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bookId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next() && rs.getInt("count") > 0) {
                return rs.getDouble("avg_rating");
            }
        } catch (SQLException e) {
            System.err.println("Failed to get average rating: " + e.getMessage());
        }

        return 0.0;
    }

    /**
     * Get rating statistics for a book
     * Returns formatted string with average rating and count
     */
    public String getRatingStats(String bookId) {
        String sql = """
            SELECT AVG(rating) as avg_rating, COUNT(*) as count
            FROM book_ratings
            WHERE book_id = ?
            """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bookId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt("count");
                if (count > 0) {
                    double avg = rs.getDouble("avg_rating");
                    return String.format("%.1f stars (%d ratings)", avg, count);
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to get rating stats: " + e.getMessage());
        }

        return "No ratings yet";
    }

    /**
     * Delete a rating
     * Returns true if rating was deleted
     */
    public boolean deleteRating(String userId, String bookId) {
        String sql = "DELETE FROM book_ratings WHERE user_id = ? AND book_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, bookId);

            int deleted = pstmt.executeUpdate();
            return deleted > 0;
        } catch (SQLException e) {
            System.err.println("Failed to delete rating: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get top rated books
     * Returns list of book IDs ordered by average rating
     */
    public ArrayList<String> getTopRatedBooks(int limit) {
        String sql = """
            SELECT book_id, AVG(rating) as avg_rating, COUNT(*) as count
            FROM book_ratings
            GROUP BY book_id
            HAVING count >= 3
            ORDER BY avg_rating DESC, count DESC
            LIMIT ?
            """;

        ArrayList<String> bookIds = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                bookIds.add(rs.getString("book_id"));
            }
        } catch (SQLException e) {
            System.err.println("Failed to get top rated books: " + e.getMessage());
        }

        return bookIds;
    }

    /**
     * Get overall average rating across all books
     * @return average rating across all ratings in the database
     */
    public double getOverallAverageRating() {
        String sql = "SELECT AVG(rating) as avg_rating FROM book_ratings";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                double avgRating = rs.getDouble("avg_rating");
                return Math.round(avgRating * 10.0) / 10.0; // Round to 1 decimal place
            }

        } catch (SQLException e) {
            System.err.println("Error getting overall average rating: " + e.getMessage());
        }

        return 0.0;
    }

    /**
     * Get average rating from ratings made yesterday
     * @return average rating from yesterday
     */
    public double getYesterdayAverageRating() {
        String sql = """
            SELECT AVG(rating) as avg_rating FROM book_ratings
            WHERE strftime('%Y-%m-%d', created_at) = strftime('%Y-%m-%d', date('now', '-1 day'))
            """;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                double avgRating = rs.getDouble("avg_rating");
                return Math.round(avgRating * 10.0) / 10.0; // Round to 1 decimal place
            }

        } catch (SQLException e) {
            System.err.println("Error getting yesterday average rating: " + e.getMessage());
        }

        return 0.0;
    }
}
