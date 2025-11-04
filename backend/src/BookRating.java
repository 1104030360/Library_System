/**
 * BookRating data model
 * Represents a user's rating for a book
 * Following Linus principles: Simple data structure, no business logic
 */
public class BookRating {
    private int id;
    private String userId;
    private String bookId;
    private int rating;            // 1-5 stars
    private String createdAt;

    /**
     * Constructor for new rating
     */
    public BookRating(String userId, String bookId, int rating, String createdAt) {
        this.userId = userId;
        this.bookId = bookId;
        this.rating = rating;
        this.createdAt = createdAt;
    }

    /**
     * Constructor for loading from database
     */
    public BookRating(int id, String userId, String bookId, int rating, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.rating = rating;
        this.createdAt = createdAt;
    }

    // Getters
    public int getId() { return id; }
    public String getUserId() { return userId; }
    public String getBookId() { return bookId; }
    public int getRating() { return rating; }
    public String getCreatedAt() { return createdAt; }

    // Setters
    public void setRating(int rating) {
        if (rating >= 1 && rating <= 5) {
            this.rating = rating;
        }
    }

    @Override
    public String toString() {
        return String.format("BookRating{id=%d, userId='%s', bookId='%s', rating=%d}",
                           id, userId, bookId, rating);
    }
}
