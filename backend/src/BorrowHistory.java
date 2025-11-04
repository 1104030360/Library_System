/**
 * BorrowHistory data model
 * Represents a book borrowing record
 * Following Linus principles: Simple data structure, no business logic
 */
public class BorrowHistory {
    private int id;
    private String userId;
    private String userName;        // User's display name (optional, for admin views)
    private String bookId;
    private String bookTitle;       // Denormalized for performance
    private String borrowDate;
    private String dueDate;
    private String returnDate;      // null if still borrowing
    private String status;          // 'borrowing', 'returned', 'overdue'

    /**
     * Constructor for new borrow record
     */
    public BorrowHistory(String userId, String bookId, String bookTitle,
                        String borrowDate, String dueDate) {
        this.userId = userId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = null;
        this.status = "borrowing";
    }

    /**
     * Constructor for loading from database
     */
    public BorrowHistory(int id, String userId, String bookId, String bookTitle,
                        String borrowDate, String dueDate, String returnDate, String status) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
    }

    // Getters
    public int getId() { return id; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getBookId() { return bookId; }
    public String getBookTitle() { return bookTitle; }
    public String getBorrowDate() { return borrowDate; }
    public String getDueDate() { return dueDate; }
    public String getReturnDate() { return returnDate; }
    public String getStatus() { return status; }

    // Setters
    public void setUserName(String userName) { this.userName = userName; }

    // Status methods
    public boolean isBorrowing() { return "borrowing".equals(status); }
    public boolean isReturned() { return "returned".equals(status); }
    public boolean isOverdue() { return "overdue".equals(status); }

    // Update methods
    public void markAsReturned(String returnDate) {
        this.returnDate = returnDate;
        this.status = "returned";
    }

    public void markAsOverdue() {
        this.status = "overdue";
    }

    @Override
    public String toString() {
        return String.format("BorrowHistory{id=%d, userId='%s', bookId='%s', status='%s'}",
                           id, userId, bookId, status);
    }
}
