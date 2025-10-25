/**
 * Simple test program for Book2 and BookRepository
 * Run this to verify the new book management system works
 */
public class TestBook2 {
    public static void main(String[] args) {
        System.out.println("=== Testing Book2 System ===\n");

        // Initialize
        System.out.println("1. Initializing books...");
        Book2.initialize();
        System.out.println("✓ Initialized\n");

        // Display all books
        System.out.println("2. All books:");
        System.out.println(Book2.getAllBooksString());

        // Test search by ID
        System.out.println("3. Search by ID '001':");
        BookInfo book = Book2.searchById("001");
        if (book != null) {
            System.out.println("✓ Found: " + book.getTitle());
        } else {
            System.out.println("✗ Not found");
        }
        System.out.println();

        // Test search by title
        System.out.println("4. Search by title 'Java':");
        book = Book2.searchByTitle("Java");
        if (book != null) {
            System.out.println("✓ Found: " + book.getDisplayString());
        } else {
            System.out.println("✗ Not found");
        }
        System.out.println();

        // Test borrow
        System.out.println("5. Borrow book '001':");
        BorrowResult result = Book2.borrowBookById("001");
        System.out.println(result.isSuccess() ? "✓ " : "✗ ");
        System.out.println(result.getMessage());
        System.out.println();

        // Test borrow same book again (should fail)
        System.out.println("6. Try to borrow same book again:");
        result = Book2.borrowBookById("001");
        System.out.println(result.isSuccess() ? "✓ " : "✗ ");
        System.out.println(result.getMessage());
        System.out.println();

        // Test return
        System.out.println("7. Return book '001':");
        result = Book2.returnBook("001", null);
        System.out.println(result.isSuccess() ? "✓ " : "✗ ");
        System.out.println(result.getMessage());
        System.out.println();

        // Test add book
        System.out.println("8. Add new book:");
        boolean added = Book2.addBook("006", "資料結構", "張三", "成功大學");
        System.out.println(added ? "✓ Added successfully" : "✗ Failed to add");
        System.out.println();

        // Test remove book
        System.out.println("9. Remove book '006':");
        boolean removed = Book2.removeBookById("006");
        System.out.println(removed ? "✓ Removed successfully" : "✗ Failed to remove");
        System.out.println();

        System.out.println("=== All tests completed ===");
    }
}
