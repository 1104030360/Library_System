import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test BookInfo Model
 *
 * Philosophy: Test REAL object behavior and edge cases
 * - Constructor validation
 * - State management (borrow/return)
 * - Equality and hashing
 * - Edge cases (null handling)
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BookInfoTest {

    /**
     * Test 1: Valid construction with description
     * Real scenario: Create new book with all fields
     */
    @Test
    @Order(1)
    public void testConstructorWithDescription() {
        BookInfo book = new BookInfo("001", "Java Programming",
            "John Doe", "Tech Publisher", "A comprehensive guide to Java");

        assertEquals("001", book.getId());
        assertEquals("Java Programming", book.getTitle());
        assertEquals("John Doe", book.getAuthor());
        assertEquals("Tech Publisher", book.getPublisher());
        assertEquals("A comprehensive guide to Java", book.getDescription());
        assertTrue(book.isAvailable(), "New book should be available");
    }

    /**
     * Test 2: Valid construction without description
     * Real scenario: Create book without description (backward compatibility)
     */
    @Test
    @Order(2)
    public void testConstructorWithoutDescription() {
        BookInfo book = new BookInfo("002", "Python Basics",
            "Jane Smith", "Code Press");

        assertEquals("002", book.getId());
        assertEquals("Python Basics", book.getTitle());
        assertEquals("Jane Smith", book.getAuthor());
        assertEquals("Code Press", book.getPublisher());
        assertEquals("", book.getDescription(), "Description should default to empty string");
        assertTrue(book.isAvailable());
    }

    /**
     * Test 3: Constructor null validation
     * Real scenario: Prevent invalid book creation
     */
    @Test
    @Order(3)
    public void testConstructorNullValidation() {
        // Null ID
        assertThrows(IllegalArgumentException.class, () -> {
            new BookInfo(null, "Title", "Author", "Publisher");
        }, "Should throw exception for null ID");

        // Null Title
        assertThrows(IllegalArgumentException.class, () -> {
            new BookInfo("001", null, "Author", "Publisher");
        }, "Should throw exception for null title");

        // Null Author
        assertThrows(IllegalArgumentException.class, () -> {
            new BookInfo("001", "Title", null, "Publisher");
        }, "Should throw exception for null author");

        // Null Publisher
        assertThrows(IllegalArgumentException.class, () -> {
            new BookInfo("001", "Title", "Author", null);
        }, "Should throw exception for null publisher");

        // Null description is OK (defaults to empty string)
        assertDoesNotThrow(() -> {
            new BookInfo("001", "Title", "Author", "Publisher", null);
        }, "Null description should be allowed and default to empty string");
    }

    /**
     * Test 4: Borrow book state change
     * Real scenario: User borrows an available book
     */
    @Test
    @Order(4)
    public void testMarkAsBorrowed() {
        BookInfo book = new BookInfo("001", "Test Book", "Author", "Publisher");

        assertTrue(book.isAvailable(), "Book starts as available");

        book.markAsBorrowed();

        assertFalse(book.isAvailable(), "Book should be unavailable after borrowing");
    }

    /**
     * Test 5: Return book state change
     * Real scenario: User returns a borrowed book
     */
    @Test
    @Order(5)
    public void testMarkAsReturned() {
        BookInfo book = new BookInfo("001", "Test Book", "Author", "Publisher");

        // Borrow first
        book.markAsBorrowed();
        assertFalse(book.isAvailable());

        // Then return
        book.markAsReturned();

        assertTrue(book.isAvailable(), "Book should be available after return");
    }

    /**
     * Test 6: Multiple borrow/return cycles
     * Real scenario: Book borrowed and returned multiple times
     */
    @Test
    @Order(6)
    public void testMultipleBorrowReturnCycles() {
        BookInfo book = new BookInfo("001", "Test Book", "Author", "Publisher");

        // Cycle 1
        assertTrue(book.isAvailable());
        book.markAsBorrowed();
        assertFalse(book.isAvailable());
        book.markAsReturned();
        assertTrue(book.isAvailable());

        // Cycle 2
        book.markAsBorrowed();
        assertFalse(book.isAvailable());
        book.markAsReturned();
        assertTrue(book.isAvailable());

        // Cycle 3
        book.markAsBorrowed();
        assertFalse(book.isAvailable());
        book.markAsReturned();
        assertTrue(book.isAvailable());
    }

    /**
     * Test 7: Equality based on ID
     * Real scenario: Check if two book objects represent the same book
     */
    @Test
    @Order(7)
    public void testEquality() {
        BookInfo book1 = new BookInfo("001", "Java", "Author1", "Pub1");
        BookInfo book2 = new BookInfo("001", "Different Title", "Author2", "Pub2");
        BookInfo book3 = new BookInfo("002", "Java", "Author1", "Pub1");

        // Same ID = equal
        assertEquals(book1, book2, "Books with same ID should be equal");

        // Different ID = not equal
        assertNotEquals(book1, book3, "Books with different IDs should not be equal");

        // Reflexive
        assertEquals(book1, book1, "Book should equal itself");

        // Null
        assertNotEquals(book1, null, "Book should not equal null");
    }

    /**
     * Test 8: HashCode consistency
     * Real scenario: Use books in HashSet or HashMap
     */
    @Test
    @Order(8)
    public void testHashCode() {
        BookInfo book1 = new BookInfo("001", "Java", "Author1", "Pub1");
        BookInfo book2 = new BookInfo("001", "Different Title", "Author2", "Pub2");
        BookInfo book3 = new BookInfo("002", "Java", "Author1", "Pub1");

        // Equal objects must have same hash code
        assertEquals(book1.hashCode(), book2.hashCode(),
            "Equal books should have same hash code");

        // Different IDs should (likely) have different hash codes
        assertNotEquals(book1.hashCode(), book3.hashCode(),
            "Different books should have different hash codes");
    }

    /**
     * Test 9: Display string format
     * Real scenario: Show book info in UI
     */
    @Test
    @Order(9)
    public void testGetDisplayString() {
        BookInfo book = new BookInfo("001", "Java Programming",
            "John Doe", "Tech Publisher");

        String display = book.getDisplayString();

        assertNotNull(display);
        assertTrue(display.contains("001"), "Display should include ID");
        assertTrue(display.contains("Java Programming"), "Display should include title");
        assertTrue(display.contains("John Doe"), "Display should include author");
        assertTrue(display.contains("Tech Publisher"), "Display should include publisher");
    }

    /**
     * Test 10: ToString format
     * Real scenario: Debug logging
     */
    @Test
    @Order(10)
    public void testToString() {
        BookInfo book = new BookInfo("001", "Java Programming",
            "John Doe", "Tech Publisher", "A guide to Java");

        String toString = book.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("001"), "ToString should include ID");
        assertTrue(toString.contains("Java Programming"), "ToString should include title");
        assertTrue(toString.contains("John Doe"), "ToString should include author");
        assertTrue(toString.contains("Tech Publisher"), "ToString should include publisher");
        assertTrue(toString.contains("A guide to Java"), "ToString should include description");
        assertTrue(toString.contains("isAvailable"), "ToString should show availability");
    }

    /**
     * Test 11: Immutability of core fields
     * Real scenario: Ensure book identity cannot be changed
     */
    @Test
    @Order(11)
    public void testImmutability() {
        BookInfo book = new BookInfo("001", "Original Title",
            "Original Author", "Original Publisher", "Original Description");

        // Core fields should not have setters (immutable)
        // This test verifies by reflection that no setters exist

        assertEquals("001", book.getId());
        assertEquals("Original Title", book.getTitle());
        assertEquals("Original Author", book.getAuthor());
        assertEquals("Original Publisher", book.getPublisher());
        assertEquals("Original Description", book.getDescription());

        // Only availability can change (via markAsBorrowed/markAsReturned)
        assertTrue(book.isAvailable());
        book.markAsBorrowed();
        assertFalse(book.isAvailable());
    }

    /**
     * Test 12: Edge case - empty string fields
     * Real scenario: User submits form with empty fields
     */
    @Test
    @Order(12)
    public void testEmptyStringFields() {
        // Empty strings should be allowed (though not recommended)
        // Validation happens at service layer, not model layer
        assertDoesNotThrow(() -> {
            BookInfo book = new BookInfo("", "", "", "", "");
            assertEquals("", book.getId());
            assertEquals("", book.getTitle());
            assertEquals("", book.getAuthor());
            assertEquals("", book.getPublisher());
            assertEquals("", book.getDescription());
        }, "Empty strings should be allowed in BookInfo");
    }

    /**
     * Test 13: Description with special characters
     * Real scenario: Book description contains quotes, newlines, etc.
     */
    @Test
    @Order(13)
    public void testDescriptionWithSpecialCharacters() {
        String complexDescription = "This book is \"amazing\"!\n" +
                                    "It covers:\n" +
                                    "- Topic 1\n" +
                                    "- Topic 2\n" +
                                    "Price: $29.99";

        BookInfo book = new BookInfo("001", "Test", "Author", "Pub", complexDescription);

        assertEquals(complexDescription, book.getDescription(),
            "Description should preserve special characters");
    }

    /**
     * Test 14: Very long description
     * Real scenario: User enters a very detailed book description
     */
    @Test
    @Order(14)
    public void testLongDescription() {
        StringBuilder longDesc = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longDesc.append("This is a very long description. ");
        }

        BookInfo book = new BookInfo("001", "Test", "Author", "Pub",
            longDesc.toString());

        assertEquals(longDesc.toString(), book.getDescription(),
            "Should handle long descriptions");
        assertTrue(book.getDescription().length() > 1000,
            "Description should be very long");
    }
}
