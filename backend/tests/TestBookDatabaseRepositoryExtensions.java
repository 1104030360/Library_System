import java.util.*;

/**
 * Test for BookDatabaseRepository Extensions (Phase 3)
 * Tests: findByTitle(), searchByTitle(), getStats()
 */
public class TestBookDatabaseRepositoryExtensions {

    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("Testing BookDatabaseRepository Extensions (Phase 3)");
        System.out.println("=".repeat(60));

        // Initialize repository
        BookDatabaseRepository repo = new BookDatabaseRepository();
        repo.initialize();

        // Test 1: findByTitle() - exact match
        testFindByTitle(repo);

        // Test 2: searchByTitle() - fuzzy search
        testSearchByTitle(repo);

        // Test 3: getStats() - library statistics
        testGetStats(repo);

        System.out.println("\n" + "=".repeat(60));
        System.out.println("All tests passed! ✅");
        System.out.println("=".repeat(60));
    }

    static void testFindByTitle(BookDatabaseRepository repo) {
        System.out.println("\n[Test 1] findByTitle() - Exact title search");

        // Test 1.1: Find existing book
        String existingTitle = "深入淺出 Java 程式設計";
        BookInfo book = repo.findByTitle(existingTitle);

        assert book != null : "Should find existing book";
        assert book.getTitle().equals(existingTitle) : "Title should match exactly";

        System.out.println("✅ Found book: " + book.getTitle());
        System.out.println("   Author: " + book.getAuthor());
        System.out.println("   Publisher: " + book.getPublisher());
        System.out.println("   Available: " + (book.isAvailable() ? "Yes" : "No"));

        // Test 1.2: Find non-existing book
        String nonExistingTitle = "不存在的書籍";
        BookInfo notFound = repo.findByTitle(nonExistingTitle);

        assert notFound == null : "Should return null for non-existing book";
        System.out.println("✅ Correctly returned null for non-existing book");
    }

    static void testSearchByTitle(BookDatabaseRepository repo) {
        System.out.println("\n[Test 2] searchByTitle() - Fuzzy title search");

        // Test 2.1: Search with keyword "Java"
        String keyword1 = "Java";
        List<BookInfo> results1 = repo.searchByTitle(keyword1);

        assert results1 != null : "Results should not be null";
        assert results1.size() > 0 : "Should find books with keyword: " + keyword1;

        System.out.println("✅ Found " + results1.size() + " book(s) with keyword: " + keyword1);
        for (BookInfo book : results1) {
            System.out.println("   - " + book.getTitle());
        }

        // Test 2.2: Search with keyword "學"
        String keyword2 = "學";
        List<BookInfo> results2 = repo.searchByTitle(keyword2);

        assert results2 != null : "Results should not be null";
        assert results2.size() > 0 : "Should find books with keyword: " + keyword2;

        System.out.println("✅ Found " + results2.size() + " book(s) with keyword: " + keyword2);

        // Test 2.3: Search with non-matching keyword
        String keyword3 = "不存在的關鍵字XYZ";
        List<BookInfo> results3 = repo.searchByTitle(keyword3);

        assert results3 != null : "Results should not be null even when empty";
        assert results3.size() == 0 : "Should return empty list for non-matching keyword";

        System.out.println("✅ Correctly returned empty list for non-matching keyword");

        // Test 2.4: Verify max 20 results limit
        String keyword4 = "";  // Empty string should match all
        List<BookInfo> results4 = repo.searchByTitle(keyword4);

        assert results4.size() <= 20 : "Should not exceed 20 results";
        System.out.println("✅ Correctly limited results to " + results4.size() + " (max 20)");
    }

    static void testGetStats(BookDatabaseRepository repo) {
        System.out.println("\n[Test 3] getStats() - Library statistics");

        BookDatabaseRepository.LibraryStats stats = repo.getStats();

        assert stats != null : "Stats should not be null";
        assert stats.totalBooks > 0 : "Should have total books";
        assert stats.availableBooks >= 0 : "Available books should be non-negative";
        assert stats.borrowedBooks >= 0 : "Borrowed books should be non-negative";
        assert stats.totalBooks == stats.availableBooks + stats.borrowedBooks :
            "Total should equal available + borrowed";

        System.out.println("✅ Statistics retrieved successfully:");
        System.out.println("   Total Books: " + stats.totalBooks);
        System.out.println("   Available: " + stats.availableBooks);
        System.out.println("   Borrowed: " + stats.borrowedBooks);

        // Test toString() method
        String statsString = stats.toString();
        assert statsString != null : "toString() should not return null";
        assert statsString.contains("Total") : "toString() should contain 'Total'";
        assert statsString.contains("Available") : "toString() should contain 'Available'";
        assert statsString.contains("Borrowed") : "toString() should contain 'Borrowed'";

        System.out.println("✅ toString() method works: " + statsString);
    }
}
