import java.time.LocalDate;

/**
 * BookService - Business logic layer for book operations
 * No UI code, only business rules and data manipulation
 */
public class BookService {
    private BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    // ===== SEARCH OPERATIONS =====

    public BookInfo searchBookById(String bookId) {
        if (bookId == null || bookId.trim().isEmpty()) {
            return null;
        }
        return repository.findById(bookId.trim());
    }

    public BookInfo searchBookByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return null;
        }
        return repository.findByTitle(title.trim());
    }

    public String getAllBooksDisplay() {
        return repository.getAllBooksDisplayString();
    }

    public int getTotalBooksCount() {
        return repository.getTotalBooks();
    }

    public int getAvailableBooksCount() {
        return repository.getAvailableBooksCount();
    }

    // ===== ADMIN OPERATIONS =====

    public BorrowResult addBook(String id, String title, String author, String publisher) {
        // Validate input
        if (id == null || id.trim().isEmpty()) {
            return BorrowResult.failure("書籍 ID 不能為空");
        }
        if (title == null || title.trim().isEmpty()) {
            return BorrowResult.failure("書名不能為空");
        }
        if (author == null || author.trim().isEmpty()) {
            return BorrowResult.failure("作者不能為空");
        }
        if (publisher == null || publisher.trim().isEmpty()) {
            return BorrowResult.failure("出版社不能為空");
        }

        // Check if book already exists
        if (repository.findById(id.trim()) != null) {
            return BorrowResult.failure("此 ID 的書籍已存在");
        }

        // Add book
        try {
            BookInfo newBook = new BookInfo(id.trim(), title.trim(), author.trim(), publisher.trim());
            boolean success = repository.addBook(newBook);

            if (success) {
                return BorrowResult.success("成功新增書籍：" + title);
            } else {
                return BorrowResult.failure("新增書籍失敗");
            }
        } catch (Exception e) {
            return BorrowResult.failure("新增書籍時發生錯誤：" + e.getMessage());
        }
    }

    public BorrowResult removeBookById(String bookId) {
        if (bookId == null || bookId.trim().isEmpty()) {
            return BorrowResult.failure("書籍 ID 不能為空");
        }

        BookInfo book = repository.findById(bookId.trim());
        if (book == null) {
            return BorrowResult.failure("找不到此書籍");
        }

        if (!book.isAvailable()) {
            return BorrowResult.failure("此書籍已被借出，無法刪除");
        }

        boolean success = repository.removeBookById(bookId.trim());
        if (success) {
            return BorrowResult.success("成功刪除書籍：" + book.getTitle());
        } else {
            return BorrowResult.failure("刪除書籍失敗");
        }
    }

    public BorrowResult removeBookByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return BorrowResult.failure("書名不能為空");
        }

        BookInfo book = repository.findByTitle(title.trim());
        if (book == null) {
            return BorrowResult.failure("找不到此書籍");
        }

        if (!book.isAvailable()) {
            return BorrowResult.failure("此書籍已被借出，無法刪除");
        }

        boolean success = repository.removeBookByTitle(title.trim());
        if (success) {
            return BorrowResult.success("成功刪除書籍：" + title);
        } else {
            return BorrowResult.failure("刪除書籍失敗");
        }
    }

    public BorrowResult editBook(String bookId, String newTitle, String newAuthor, String newPublisher) {
        if (bookId == null || bookId.trim().isEmpty()) {
            return BorrowResult.failure("書籍 ID 不能為空");
        }

        BookInfo book = repository.findById(bookId.trim());
        if (book == null) {
            return BorrowResult.failure("找不到此書籍");
        }

        // Use existing values if new values are empty
        String title = (newTitle != null && !newTitle.trim().isEmpty()) ? newTitle.trim() : book.getTitle();
        String author = (newAuthor != null && !newAuthor.trim().isEmpty()) ? newAuthor.trim() : book.getAuthor();
        String publisher = (newPublisher != null && !newPublisher.trim().isEmpty()) ? newPublisher.trim() : book.getPublisher();

        boolean success = repository.updateBook(bookId.trim(), title, author, publisher);
        if (success) {
            return BorrowResult.success("成功更新書籍資訊");
        } else {
            return BorrowResult.failure("更新書籍失敗");
        }
    }

    // ===== MEMBER OPERATIONS =====

    public BorrowResult borrowBook(String searchKey, boolean searchById) {
        if (searchKey == null || searchKey.trim().isEmpty()) {
            return BorrowResult.failure("請輸入書籍 ID 或書名");
        }

        BookInfo book;
        if (searchById) {
            book = repository.findById(searchKey.trim());
        } else {
            book = repository.findByTitle(searchKey.trim());
        }

        if (book == null) {
            return BorrowResult.failure("找不到此書籍");
        }

        if (!book.isAvailable()) {
            return BorrowResult.failure("此書已被借出");
        }

        // Calculate dates
        LocalDate today = LocalDate.now();
        LocalDate returnDate = today.plusWeeks(2);

        // Mark as borrowed
        book.markAsBorrowed();

        String message = "借書成功！\n" +
                        "書名：" + book.getTitle() + "\n" +
                        "作者：" + book.getAuthor() + "\n" +
                        "借書日期：" + today + "\n" +
                        "應還日期：" + returnDate;

        return BorrowResult.success(message);
    }

    public BorrowResult returnBook(String searchKey, boolean searchById) {
        if (searchKey == null || searchKey.trim().isEmpty()) {
            return BorrowResult.failure("請輸入書籍 ID 或書名");
        }

        BookInfo book;
        if (searchById) {
            book = repository.findById(searchKey.trim());
        } else {
            book = repository.findByTitle(searchKey.trim());
        }

        if (book == null) {
            return BorrowResult.failure("找不到此書籍");
        }

        if (book.isAvailable()) {
            return BorrowResult.failure("此書未被借出");
        }

        // Mark as returned
        book.markAsReturned();

        String message = "還書成功！\n" +
                        "書名：" + book.getTitle() + "\n" +
                        "作者：" + book.getAuthor();

        return BorrowResult.success(message);
    }

    // ===== STATISTICS =====

    public String getLibraryStatistics() {
        int total = repository.getTotalBooks();
        int available = repository.getAvailableBooksCount();
        int borrowed = total - available;

        return "圖書館統計資訊\n" +
               "總藏書量：" + total + " 本\n" +
               "可借閱：" + available + " 本\n" +
               "已借出：" + borrowed + " 本";
    }
}
