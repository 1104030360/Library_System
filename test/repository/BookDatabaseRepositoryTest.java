import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.util.List;

/**
 * Test BookDatabaseRepository
 *
 * Philosophy: Test REAL scenarios that users will encounter
 * - Not testing every edge case
 * - Focus on data integrity and common operations
 * - Keep tests simple and readable
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BookDatabaseRepositoryTest {

    private static final String TEST_DB = "data/test_library.db";
    private BookDatabaseRepository repository;

    @BeforeEach
    public void setup() {
        // Clean up test database before each test
        File dbFile = new File(TEST_DB);
        if (dbFile.exists()) {
            dbFile.delete();
        }

        // Create new repository with test database
        repository = new BookDatabaseRepository(TEST_DB);
        repository.initialize();
    }

    @AfterEach
    public void cleanup() {
        // Clean up after each test
        File dbFile = new File(TEST_DB);
        if (dbFile.exists()) {
            dbFile.delete();
        }
    }

    /**
     * Test 1: Basic initialization
     * Real scenario: Server starts, database initializes
     */
    @Test
    @Order(1)
    public void testInitialization() {
        // After initialization, should have default books
        List<BookInfo> books = repository.getAllBooks();

        assertNotNull(books, "Books list should not be null");
        assertEquals(5, books.size(), "Should have 5 default books");

        // Check first book
        BookInfo firstBook = books.get(0);
        assertEquals("001", firstBook.getId());
        assertEquals("Java", firstBook.getTitle());
        assertTrue(firstBook.isAvailable(), "Default books should be available");
    }

    /**
     * Test 2: Find book by ID
     * Real scenario: User searches for specific book
     */
    @Test
    @Order(2)
    public void testFindById() {
        // Find existing book
        BookInfo book = repository.findById("001");
        assertNotNull(book, "Should find book with ID 001");
        assertEquals("Java", book.getTitle());

        // Find non-existing book
        BookInfo notFound = repository.findById("999");
        assertNull(notFound, "Should return null for non-existing book");
    }

    /**
     * Test 3: Add new book
     * Real scenario: Admin adds a new book to library
     */
    @Test
    @Order(3)
    public void testAddBook() {
        BookInfo newBook = new BookInfo("999", "Test Book", "Test Author", "Test Publisher");

        // Add book
        boolean added = repository.addBook(newBook);
        assertTrue(added, "Should successfully add new book");

        // Verify it's in database
        BookInfo found = repository.findById("999");
        assertNotNull(found);
        assertEquals("Test Book", found.getTitle());

        // Try adding duplicate ID - should fail
        BookInfo duplicate = new BookInfo("999", "Another Book", "Another Author", "Another Publisher");
        boolean addedDuplicate = repository.addBook(duplicate);
        assertFalse(addedDuplicate, "Should not add book with duplicate ID");
    }

    /**
     * Test 4: Update book (borrow/return)
     * Real scenario: User borrows and returns a book
     */
    @Test
    @Order(4)
    public void testUpdateBook() {
        BookInfo book = repository.findById("001");
        assertNotNull(book);
        assertTrue(book.isAvailable(), "Book should start as available");

        // Borrow book
        book.markAsBorrowed();
        repository.updateBook(book);

        // Verify it's marked as borrowed
        BookInfo borrowed = repository.findById("001");
        assertFalse(borrowed.isAvailable(), "Book should be marked as borrowed");

        // Return book
        borrowed.markAsReturned();
        repository.updateBook(borrowed);

        // Verify it's marked as available again
        BookInfo returned = repository.findById("001");
        assertTrue(returned.isAvailable(), "Book should be available after return");
    }

    /**
     * Test 5: Remove book
     * Real scenario: Admin removes old/damaged book
     */
    @Test
    @Order(5)
    public void testRemoveBook() {
        // Remove existing book
        boolean removed = repository.removeBookById("001");
        assertTrue(removed, "Should successfully remove existing book");

        // Verify it's gone
        BookInfo notFound = repository.findById("001");
        assertNull(notFound, "Removed book should not be found");

        // Try removing non-existing book
        boolean removedAgain = repository.removeBookById("001");
        assertFalse(removedAgain, "Should return false for non-existing book");
    }

    /**
     * Test 6: Get all books
     * Real scenario: Display book list on homepage
     */
    @Test
    @Order(6)
    public void testGetAllBooks() {
        List<BookInfo> books = repository.getAllBooks();

        assertNotNull(books);
        assertEquals(5, books.size(), "Should have 5 default books");

        // Add a book
        repository.addBook(new BookInfo("999", "New Book", "Author", "Publisher"));

        // Should now have 6 books
        books = repository.getAllBooks();
        assertEquals(6, books.size());
    }

    /**
     * Test 7: Statistics
     * Real scenario: Admin views library statistics
     */
    @Test
    @Order(7)
    public void testGetStatistics() {
        String stats = repository.getStatistics();

        assertNotNull(stats);
        assertTrue(stats.contains("Total: 5"), "Should show 5 total books");
        assertTrue(stats.contains("Available: 5"), "Should show 5 available books");
        assertTrue(stats.contains("Borrowed: 0"), "Should show 0 borrowed books");

        // Borrow a book
        BookInfo book = repository.findById("001");
        book.markAsBorrowed();
        repository.updateBook(book);

        // Check stats again
        stats = repository.getStatistics();
        assertTrue(stats.contains("Available: 4"), "Should show 4 available after borrowing");
        assertTrue(stats.contains("Borrowed: 1"), "Should show 1 borrowed");
    }

    /**
     * Test 8: Find by title
     * Real scenario: User searches by book title
     */
    @Test
    @Order(8)
    public void testFindByTitle() {
        BookInfo book = repository.findByTitle("Java");

        assertNotNull(book);
        assertEquals("001", book.getId());
        assertEquals("Java", book.getTitle());

        // Non-existing title
        BookInfo notFound = repository.findByTitle("Non-existing Book");
        assertNull(notFound);
    }

    /**
     * Test 9: Concurrent operations simulation
     * Real scenario: Multiple users borrowing/returning books
     */
    @Test
    @Order(9)
    public void testMultipleOperations() {
        // Simulate real usage pattern

        // User 1 borrows book 001
        BookInfo book1 = repository.findById("001");
        book1.markAsBorrowed();
        repository.updateBook(book1);

        // User 2 borrows book 002
        BookInfo book2 = repository.findById("002");
        book2.markAsBorrowed();
        repository.updateBook(book2);

        // Admin adds new book
        repository.addBook(new BookInfo("999", "New Book", "Author", "Publisher"));

        // User 1 returns book 001
        book1 = repository.findById("001");
        book1.markAsReturned();
        repository.updateBook(book1);

        // Verify final state
        List<BookInfo> allBooks = repository.getAllBooks();
        assertEquals(6, allBooks.size(), "Should have 6 books total");

        BookInfo check001 = repository.findById("001");
        assertTrue(check001.isAvailable(), "Book 001 should be available");

        BookInfo check002 = repository.findById("002");
        assertFalse(check002.isAvailable(), "Book 002 should still be borrowed");

        String stats = repository.getStatistics();
        assertTrue(stats.contains("Borrowed: 1"), "Should have 1 borrowed book");
    }

    /**
     * Test 10: Database persistence
     * Real scenario: Server restarts, data should persist
     */
    @Test
    @Order(10)
    public void testDatabasePersistence() {
        // Add a new book
        repository.addBook(new BookInfo("888", "Persistent Book", "Author", "Publisher"));

        // Create new repository instance (simulating server restart)
        BookDatabaseRepository newRepository = new BookDatabaseRepository(TEST_DB);
        newRepository.initialize();

        // Data should still be there
        BookInfo book = newRepository.findById("888");
        assertNotNull(book, "Book should persist after 'restart'");
        assertEquals("Persistent Book", book.getTitle());

        // Default books should not be re-inserted
        List<BookInfo> books = newRepository.getAllBooks();
        // Should have 5 default + 1 added = 6 total
        assertEquals(6, books.size(), "Should not duplicate default books on re-initialization");
    }
}
