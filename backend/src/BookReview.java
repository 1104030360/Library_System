/**
 * BookReview data model
 * Represents a user's review for a book
 * Following Linus principles: Simple data structure, no business logic
 */
public class BookReview {
    private int id;
    private String userId;
    private String userName;        // Denormalized for display
    private String bookId;
    private String bookTitle;       // Denormalized for display
    private String reviewText;
    private String createdAt;
    private String updatedAt;

    /**
     * Constructor for new review
     */
    public BookReview(String userId, String userName, String bookId, String bookTitle,
                     String reviewText, String createdAt) {
        this.userId = userId;
        this.userName = userName;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.reviewText = reviewText;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    /**
     * Constructor for loading from database
     */
    public BookReview(int id, String userId, String userName, String bookId, String bookTitle,
                     String reviewText, String createdAt, String updatedAt) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.reviewText = reviewText;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public int getId() { return id; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getBookId() { return bookId; }
    public String getBookTitle() { return bookTitle; }
    public String getReviewText() { return reviewText; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    // Setters
    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return String.format("BookReview{id=%d, userId='%s', bookId='%s', text='%s'}",
                           id, userId, bookId, reviewText.substring(0, Math.min(50, reviewText.length())));
    }
}
