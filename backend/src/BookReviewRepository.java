import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * BookReviewRepository - Database access layer for book reviews
 * Phase 6: Book review system
 *
 * Follows Linus principles:
 * - Simple, direct SQL queries (no ORM complexity)
 * - Clear separation of concerns (Repository only handles DB)
 * - Predictable behavior (no magic, no surprises)
 */
public class BookReviewRepository {
    private Connection connection;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Constructor - uses shared database connection
     */
    public BookReviewRepository(Connection connection) {
        this.connection = connection;
        createTableIfNotExists();
    }

    /**
     * Create book_reviews table if it doesn't exist
     */
    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS book_reviews (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id TEXT NOT NULL, " +
                    "user_name TEXT NOT NULL, " +
                    "book_id TEXT NOT NULL, " +
                    "book_title TEXT NOT NULL, " +
                    "review_text TEXT NOT NULL, " +
                    "created_at TEXT NOT NULL, " +
                    "updated_at TEXT NOT NULL, " +
                    "FOREIGN KEY (user_id) REFERENCES users(id), " +
                    "FOREIGN KEY (book_id) REFERENCES books(id)" +
                    ")";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            System.out.println("✅ Book reviews table initialized successfully");
        } catch (SQLException e) {
            System.err.println("❌ Error creating book_reviews table: " + e.getMessage());
        }
    }

    /**
     * Add a new review
     * @return the ID of the new review, or -1 if failed
     */
    public int addReview(String userId, String userName, String bookId, String bookTitle, String reviewText) {
        String now = LocalDateTime.now().format(DATE_FORMATTER);
        String sql = "INSERT INTO book_reviews (user_id, user_name, book_id, book_title, review_text, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, userName);
            pstmt.setString(3, bookId);
            pstmt.setString(4, bookTitle);
            pstmt.setString(5, reviewText);
            pstmt.setString(6, now);
            pstmt.setString(7, now);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error adding review: " + e.getMessage());
        }

        return -1;
    }

    /**
     * Get all reviews for a book
     */
    public List<BookReview> getBookReviews(String bookId) {
        List<BookReview> reviews = new ArrayList<>();
        String sql = "SELECT * FROM book_reviews WHERE book_id = ? ORDER BY created_at DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, bookId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                BookReview review = new BookReview(
                    rs.getInt("id"),
                    rs.getString("user_id"),
                    rs.getString("user_name"),
                    rs.getString("book_id"),
                    rs.getString("book_title"),
                    rs.getString("review_text"),
                    rs.getString("created_at"),
                    rs.getString("updated_at")
                );
                reviews.add(review);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting book reviews: " + e.getMessage());
        }

        return reviews;
    }

    /**
     * Get all reviews by a user
     */
    public List<BookReview> getUserReviews(String userId) {
        List<BookReview> reviews = new ArrayList<>();
        String sql = "SELECT * FROM book_reviews WHERE user_id = ? ORDER BY created_at DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                BookReview review = new BookReview(
                    rs.getInt("id"),
                    rs.getString("user_id"),
                    rs.getString("user_name"),
                    rs.getString("book_id"),
                    rs.getString("book_title"),
                    rs.getString("review_text"),
                    rs.getString("created_at"),
                    rs.getString("updated_at")
                );
                reviews.add(review);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting user reviews: " + e.getMessage());
        }

        return reviews;
    }

    /**
     * Get a specific review by user and book
     */
    public BookReview getUserBookReview(String userId, String bookId) {
        String sql = "SELECT * FROM book_reviews WHERE user_id = ? AND book_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, bookId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new BookReview(
                    rs.getInt("id"),
                    rs.getString("user_id"),
                    rs.getString("user_name"),
                    rs.getString("book_id"),
                    rs.getString("book_title"),
                    rs.getString("review_text"),
                    rs.getString("created_at"),
                    rs.getString("updated_at")
                );
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting user book review: " + e.getMessage());
        }

        return null;
    }

    /**
     * Update an existing review
     */
    public boolean updateReview(int reviewId, String userId, String newReviewText) {
        String now = LocalDateTime.now().format(DATE_FORMATTER);
        String sql = "UPDATE book_reviews SET review_text = ?, updated_at = ? WHERE id = ? AND user_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newReviewText);
            pstmt.setString(2, now);
            pstmt.setInt(3, reviewId);
            pstmt.setString(4, userId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("❌ Error updating review: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete a review (only by the owner)
     */
    public boolean deleteReview(int reviewId, String userId) {
        String sql = "DELETE FROM book_reviews WHERE id = ? AND user_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, reviewId);
            pstmt.setString(2, userId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("❌ Error deleting review: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get review count for a book
     */
    public int getReviewCount(String bookId) {
        String sql = "SELECT COUNT(*) as count FROM book_reviews WHERE book_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, bookId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting review count: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Get latest reviews across all books
     */
    public List<BookReview> getLatestReviews(int limit) {
        List<BookReview> reviews = new ArrayList<>();
        String sql = "SELECT * FROM book_reviews ORDER BY created_at DESC LIMIT ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                BookReview review = new BookReview(
                    rs.getInt("id"),
                    rs.getString("user_id"),
                    rs.getString("user_name"),
                    rs.getString("book_id"),
                    rs.getString("book_title"),
                    rs.getString("review_text"),
                    rs.getString("created_at"),
                    rs.getString("updated_at")
                );
                reviews.add(review);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting latest reviews: " + e.getMessage());
        }

        return reviews;
    }
}
