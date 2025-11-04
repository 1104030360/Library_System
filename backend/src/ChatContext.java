import java.util.*;

/**
 * Chat Context - Conversation context data structure
 * Contains all relevant data retrieved from the database
 *
 * Status: Phase 0 - Empty implementation
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
}
