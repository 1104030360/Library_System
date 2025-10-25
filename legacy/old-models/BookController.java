/**
 * BookController - Controller layer
 * Coordinates between LibraryUI (view) and BookService (model)
 * Handles user interaction flow
 */
public class BookController {
    private BookService bookService;
    private LibraryUI ui;

    public BookController(BookService bookService, LibraryUI ui) {
        this.bookService = bookService;
        this.ui = ui;
    }

    // ===== ADMIN OPERATIONS =====

    public void handleAdminBookManagement() {
        int choice = ui.showAdminBookMenu();

        switch (choice) {
            case 0:
                handleAddBook();
                break;
            case 1:
                handleRemoveBook();
                break;
            case 2:
                handleSearchBook();
                break;
            case 3:
                handleEditBook();
                break;
        }
    }

    private void handleAddBook() {
        String id = ui.promptBookId();
        if (id == null || id.trim().isEmpty()) {
            ui.showWarning("操作已取消");
            return;
        }

        String title = ui.promptBookTitle();
        if (title == null || title.trim().isEmpty()) {
            ui.showWarning("操作已取消");
            return;
        }

        String author = ui.promptBookAuthor();
        if (author == null || author.trim().isEmpty()) {
            ui.showWarning("操作已取消");
            return;
        }

        String publisher = ui.promptBookPublisher();
        if (publisher == null || publisher.trim().isEmpty()) {
            ui.showWarning("操作已取消");
            return;
        }

        BorrowResult result = bookService.addBook(id, title, author, publisher);

        if (result.isSuccess()) {
            ui.showSuccess(result.getMessage());
        } else {
            ui.showError(result.getMessage());
        }
    }

    private void handleRemoveBook() {
        int searchMethod = ui.promptSearchMethod();

        if (searchMethod == 0) {
            // Remove by ID
            String id = ui.promptBookId();
            if (id == null || id.trim().isEmpty()) {
                ui.showWarning("操作已取消");
                return;
            }

            BorrowResult result = bookService.removeBookById(id);
            if (result.isSuccess()) {
                ui.showSuccess(result.getMessage());
            } else {
                ui.showError(result.getMessage());
            }
        } else if (searchMethod == 1) {
            // Remove by title
            String title = ui.promptBookTitle();
            if (title == null || title.trim().isEmpty()) {
                ui.showWarning("操作已取消");
                return;
            }

            BorrowResult result = bookService.removeBookByTitle(title);
            if (result.isSuccess()) {
                ui.showSuccess(result.getMessage());
            } else {
                ui.showError(result.getMessage());
            }
        }
    }

    private void handleEditBook() {
        String id = ui.promptBookId();
        if (id == null || id.trim().isEmpty()) {
            ui.showWarning("操作已取消");
            return;
        }

        BookInfo book = bookService.searchBookById(id);
        if (book == null) {
            ui.showError("找不到此書籍");
            return;
        }

        ui.displayBookInfo(book);

        String newTitle = ui.promptInput("編輯書名",
            "請輸入新的書名（留空保持不變）：", book.getTitle());
        String newAuthor = ui.promptInput("編輯作者",
            "請輸入新的作者（留空保持不變）：", book.getAuthor());
        String newPublisher = ui.promptInput("編輯出版社",
            "請輸入新的出版社（留空保持不變）：", book.getPublisher());

        BorrowResult result = bookService.editBook(id, newTitle, newAuthor, newPublisher);

        if (result.isSuccess()) {
            ui.showSuccess(result.getMessage());
        } else {
            ui.showError(result.getMessage());
        }
    }

    // ===== MEMBER OPERATIONS =====

    public void handleMemberBookOperations() {
        int choice = ui.showBookOperationMenu();

        switch (choice) {
            case 0:
                handleBorrowBook();
                break;
            case 1:
                handleReturnBook();
                break;
            case 2:
                handleSearchBook();
                break;
        }
    }

    private void handleBorrowBook() {
        int searchMethod = ui.promptSearchMethod();
        boolean searchById = (searchMethod == 0);

        String searchKey;
        if (searchById) {
            searchKey = ui.promptBookId();
        } else {
            searchKey = ui.promptBookTitle();
        }

        if (searchKey == null || searchKey.trim().isEmpty()) {
            ui.showWarning("操作已取消");
            return;
        }

        BorrowResult result = bookService.borrowBook(searchKey, searchById);

        if (result.isSuccess()) {
            ui.showSuccess(result.getMessage());
        } else {
            ui.showError(result.getMessage());
        }
    }

    private void handleReturnBook() {
        int searchMethod = ui.promptSearchMethod();
        boolean searchById = (searchMethod == 0);

        String searchKey;
        if (searchById) {
            searchKey = ui.promptBookId();
        } else {
            searchKey = ui.promptBookTitle();
        }

        if (searchKey == null || searchKey.trim().isEmpty()) {
            ui.showWarning("操作已取消");
            return;
        }

        BorrowResult result = bookService.returnBook(searchKey, searchById);

        if (result.isSuccess()) {
            ui.showSuccess(result.getMessage());
        } else {
            ui.showError(result.getMessage());
        }
    }

    private void handleSearchBook() {
        // Show all books first
        ui.displayAllBooks(bookService.getAllBooksDisplay());

        int searchMethod = ui.promptSearchMethod();

        BookInfo book = null;
        if (searchMethod == 0) {
            String id = ui.promptBookId();
            if (id != null && !id.trim().isEmpty()) {
                book = bookService.searchBookById(id);
            }
        } else if (searchMethod == 1) {
            String title = ui.promptBookTitle();
            if (title != null && !title.trim().isEmpty()) {
                book = bookService.searchBookByTitle(title);
            }
        }

        if (book != null) {
            ui.displayBookInfo(book);
        } else if (searchMethod >= 0) {
            ui.showError("找不到此書籍");
        }
    }

    // ===== STATISTICS =====

    public void handleShowStatistics() {
        String stats = bookService.getLibraryStatistics();
        ui.displayStatistics(stats);
    }

    // ===== DISPLAY ALL BOOKS =====

    public void handleDisplayAllBooks() {
        ui.displayAllBooks(bookService.getAllBooksDisplay());
    }
}
