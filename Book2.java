import java.time.LocalDate;

/**
 * Book2 - New implementation using BookRepository
 * This class wraps BookRepository and provides compatibility with the old Book.java API
 * Eventually, this will replace Book.java after all code is migrated
 */
public class Book2 {
    // Single source of truth for book data
    private static BookRepository repository = new BookRepository();

    // Result storage for compatibility with old API (will be removed later)
    private static BorrowResult lastBorrowResult = null;
    private static BorrowResult lastReturnResult = null;
    private static BookInfo lastSearchResult = null;

    // Initialize books
    public static void initialize() {
        repository.initialize();
    }

    // Get repository (for direct access in new code)
    public static BookRepository getRepository() {
        return repository;
    }

    // ===== ADMIN OPERATIONS =====

    /**
     * Add a new book
     * @return true if successful, false if book with same ID exists
     */
    public static boolean addBook(String id, String title, String author, String publisher) {
        try {
            BookInfo newBook = new BookInfo(id, title, author, publisher);
            return repository.addBook(newBook);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Remove book by ID
     */
    public static boolean removeBookById(String id) {
        return repository.removeBookById(id);
    }

    /**
     * Remove book by title
     */
    public static boolean removeBookByTitle(String title) {
        return repository.removeBookByTitle(title);
    }

    /**
     * Edit book information
     */
    public static boolean editBook(String id, String newTitle, String newAuthor, String newPublisher) {
        return repository.updateBook(id, newTitle, newAuthor, newPublisher);
    }

    /**
     * Search book by ID
     */
    public static BookInfo searchById(String id) {
        lastSearchResult = repository.findById(id);
        return lastSearchResult;
    }

    /**
     * Search book by title
     */
    public static BookInfo searchByTitle(String title) {
        lastSearchResult = repository.findByTitle(title);
        return lastSearchResult;
    }

    /**
     * Get all books as formatted string
     */
    public static String getAllBooksString() {
        return repository.getAllBooksDisplayString();
    }

    // ===== MEMBER OPERATIONS =====

    /**
     * Borrow book by ID
     */
    public static BorrowResult borrowBookById(String bookId) {
        BookInfo book = repository.findById(bookId);

        if (book == null) {
            lastBorrowResult = BorrowResult.failure("找不到此書籍 ID: " + bookId);
            return lastBorrowResult;
        }

        if (!book.isAvailable()) {
            lastBorrowResult = BorrowResult.failure("此書已被借出");
            return lastBorrowResult;
        }

        book.markAsBorrowed();
        LocalDate today = LocalDate.now();
        LocalDate returnDate = today.plusWeeks(2);

        String message = "借書成功！\n" +
                        "書名: " + book.getTitle() + "\n" +
                        "借書日期: " + today + "\n" +
                        "應還日期: " + returnDate;

        lastBorrowResult = BorrowResult.success(message);
        return lastBorrowResult;
    }

    /**
     * Borrow book by title
     */
    public static BorrowResult borrowBookByTitle(String title) {
        BookInfo book = repository.findByTitle(title);

        if (book == null) {
            lastBorrowResult = BorrowResult.failure("找不到此書籍: " + title);
            return lastBorrowResult;
        }

        if (!book.isAvailable()) {
            lastBorrowResult = BorrowResult.failure("此書已被借出");
            return lastBorrowResult;
        }

        book.markAsBorrowed();
        LocalDate today = LocalDate.now();
        LocalDate returnDate = today.plusWeeks(2);

        String message = "借書成功！\n" +
                        "書名: " + book.getTitle() + "\n" +
                        "借書日期: " + today + "\n" +
                        "應還日期: " + returnDate;

        lastBorrowResult = BorrowResult.success(message);
        return lastBorrowResult;
    }

    /**
     * Return book
     */
    public static BorrowResult returnBook(String bookId, String title) {
        BookInfo book = null;

        // Try to find by ID first
        if (bookId != null && !bookId.trim().isEmpty()) {
            book = repository.findById(bookId);
        }

        // If not found, try by title
        if (book == null && title != null && !title.trim().isEmpty()) {
            book = repository.findByTitle(title);
        }

        if (book == null) {
            lastReturnResult = BorrowResult.failure("找不到此書籍");
            return lastReturnResult;
        }

        if (book.isAvailable()) {
            lastReturnResult = BorrowResult.failure("此書未被借出");
            return lastReturnResult;
        }

        book.markAsReturned();

        String message = "還書成功！\n書名: " + book.getTitle();
        lastReturnResult = BorrowResult.success(message);
        return lastReturnResult;
    }

    // ===== COMPATIBILITY METHODS (for old code) =====

    /**
     * Get last borrow result message (for compatibility)
     */
    public static String getLastBorrowMessage() {
        if (lastBorrowResult != null) {
            return lastBorrowResult.getMessage();
        }
        return null;
    }

    /**
     * Check if last operation was successful
     */
    public static boolean wasLastOperationSuccessful() {
        if (lastBorrowResult != null) {
            return lastBorrowResult.isSuccess();
        }
        if (lastReturnResult != null) {
            return lastReturnResult.isSuccess();
        }
        return false;
    }
}
