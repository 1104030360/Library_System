import java.util.*;

/**
 * Chat Context - Conversation context data structure
 * Contains all relevant data retrieved from the database
 *
 * Status: 階段 4 - 完整實作
 */
public class ChatContext {
    // User borrowing history
    private List<BorrowHistory> borrowHistory;

    // Currently borrowed books
    private List<BorrowHistory> currentBorrowings;

    // Available books list
    private List<BookInfo> availableBooks;

    // Specific target book
    private BookInfo targetBook;

    // Library rules
    private List<LibraryRulesRepository.LibraryRule> libraryRules;

    // Statistics
    private BookDatabaseRepository.LibraryStats stats;

    // Getters and Setters
    public List<BorrowHistory> getBorrowHistory() { return borrowHistory; }
    public void setBorrowHistory(List<BorrowHistory> borrowHistory) {
        this.borrowHistory = borrowHistory;
    }

    public List<BorrowHistory> getCurrentBorrowings() { return currentBorrowings; }
    public void setCurrentBorrowings(List<BorrowHistory> currentBorrowings) {
        this.currentBorrowings = currentBorrowings;
    }

    public List<BookInfo> getAvailableBooks() { return availableBooks; }
    public void setAvailableBooks(List<BookInfo> availableBooks) {
        this.availableBooks = availableBooks;
    }

    public BookInfo getTargetBook() { return targetBook; }
    public void setTargetBook(BookInfo targetBook) {
        this.targetBook = targetBook;
    }

    public List<LibraryRulesRepository.LibraryRule> getLibraryRules() { return libraryRules; }
    public void setLibraryRules(List<LibraryRulesRepository.LibraryRule> libraryRules) {
        this.libraryRules = libraryRules;
    }

    public BookDatabaseRepository.LibraryStats getStats() { return stats; }
    public void setStats(BookDatabaseRepository.LibraryStats stats) {
        this.stats = stats;
    }

    /**
     * 檢查是否有任何資料
     */
    public boolean isEmpty() {
        return borrowHistory == null &&
               currentBorrowings == null &&
               availableBooks == null &&
               targetBook == null &&
               libraryRules == null &&
               stats == null;
    }

    /**
     * 獲取資料摘要（用於 debug）
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder("ChatContext{");

        if (borrowHistory != null && !borrowHistory.isEmpty()) {
            sb.append("borrowHistory=").append(borrowHistory.size()).append(" items, ");
        }
        if (currentBorrowings != null && !currentBorrowings.isEmpty()) {
            sb.append("currentBorrowings=").append(currentBorrowings.size()).append(" items, ");
        }
        if (availableBooks != null && !availableBooks.isEmpty()) {
            sb.append("availableBooks=").append(availableBooks.size()).append(" items, ");
        }
        if (targetBook != null) {
            sb.append("targetBook=").append(targetBook.getTitle()).append(", ");
        }
        if (libraryRules != null && !libraryRules.isEmpty()) {
            sb.append("libraryRules=").append(libraryRules.size()).append(" items, ");
        }
        if (stats != null) {
            sb.append("stats=").append(stats).append(", ");
        }

        if (sb.length() > 13) {
            sb.setLength(sb.length() - 2);  // 移除最後的 ", "
        }
        sb.append("}");

        return sb.toString();
    }
}
