public class BookInfo {
    private final String id;
    private final String title;
    private final String author;
    private final String publisher;
    private boolean isAvailable;

    // Constructor - all fields are required
    public BookInfo(String id, String title, String author, String publisher) {
        if (id == null || title == null || author == null || publisher == null) {
            throw new IllegalArgumentException("All book fields must be non-null");
        }
        this.id = id;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.isAvailable = true;
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

    public boolean isAvailable() {
        return isAvailable;
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
