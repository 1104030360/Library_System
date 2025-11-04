/**
 * Recommendation data structure
 * Simple POJO - Linus style
 */
public class Recommendation {
    private String bookId;
    private String reason;
    private double score;

    public Recommendation(String bookId, String reason, double score) {
        this.bookId = bookId;
        this.reason = reason;
        this.score = score;
    }

    // Getters
    public String getBookId() { return bookId; }
    public String getReason() { return reason; }
    public double getScore() { return score; }

    // Setters
    public void setBookId(String bookId) { this.bookId = bookId; }
    public void setReason(String reason) { this.reason = reason; }
    public void setScore(double score) { this.score = score; }
}
