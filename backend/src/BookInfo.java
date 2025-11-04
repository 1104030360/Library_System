public class BookInfo {
    private final String id;
    private final String title;
    private final String author;
    private final String publisher;
    private final String description;
    private boolean isAvailable;

    // 統計數據欄位 (Phase 12 Enhancement)
    public int borrowCount;        // 借閱次數
    public double averageRating;   // 平均評分
    public int reviewCount;        // 評論數量

    // Constructor with all fields including statistics (new)
    public BookInfo(String id, String title, String author, String publisher, String description,
                    int borrowCount, double averageRating, int reviewCount) {
        if (id == null || title == null || author == null || publisher == null) {
            throw new IllegalArgumentException("All book fields must be non-null");
        }
        this.id = id;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.description = description != null ? description : "";
        this.isAvailable = true;
        this.borrowCount = borrowCount;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
    }

    // Constructor with description (backward compatibility)
    public BookInfo(String id, String title, String author, String publisher, String description) {
        this(id, title, author, publisher, description, 0, 0.0, 0);
    }

    // Constructor without description (backward compatibility)
    public BookInfo(String id, String title, String author, String publisher) {
        this(id, title, author, publisher, "", 0, 0.0, 0);
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getDescription() {
        return description;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public int getBorrowCount() {
        return borrowCount;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    // State management methods
    public void markAsBorrowed() {
        this.isAvailable = false;
    }

    public void markAsReturned() {
        this.isAvailable = true;
    }

    // Display format for UI
    public String getDisplayString() {
        return "ID:" + id + " " + title + " 作者:" + author + " 出版社:" + publisher;
    }

    @Override
    public String toString() {
        return "BookInfo{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", publisher='" + publisher + '\'' +
                ", description='" + description + '\'' +
                ", isAvailable=" + isAvailable +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookInfo bookInfo = (BookInfo) o;
        return id.equals(bookInfo.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
