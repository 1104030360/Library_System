/**
 * TestMVC - Test program for MVC architecture
 * Demonstrates clean separation of concerns
 */
public class TestMVC {
    public static void main(String[] args) {
        System.out.println("=== Testing MVC Architecture ===\n");

        // Initialize layers
        BookRepository repository = new BookRepository();
        repository.initialize();

        BookService service = new BookService(repository);
        LibraryUI ui = new LibraryUI();
        BookController controller = new BookController(service, ui);

        System.out.println("✓ MVC layers initialized successfully\n");

        // Test 1: Add book through controller
        System.out.println("Test 1: Add book through Service");
        BorrowResult result = service.addBook("006", "演算法", "王五", "陽明大學");
        System.out.println(result.isSuccess() ? "✓ " : "✗ ");
        System.out.println(result.getMessage() + "\n");

        // Test 2: Search book
        System.out.println("Test 2: Search book");
        BookInfo book = service.searchBookById("001");
        if (book != null) {
            System.out.println("✓ Found: " + book.getTitle());
        } else {
            System.out.println("✗ Not found");
        }
        System.out.println();

        // Test 3: Borrow book
        System.out.println("Test 3: Borrow book");
        result = service.borrowBook("001", true);
        System.out.println(result.isSuccess() ? "✓ " : "✗ ");
        System.out.println(result.getMessage() + "\n");

        // Test 4: Try to borrow same book again
        System.out.println("Test 4: Try to borrow same book again (should fail)");
        result = service.borrowBook("001", true);
        System.out.println(result.isSuccess() ? "✗ Should have failed!" : "✓ Correctly rejected");
        System.out.println(result.getMessage() + "\n");

        // Test 5: Return book
        System.out.println("Test 5: Return book");
        result = service.returnBook("001", true);
        System.out.println(result.isSuccess() ? "✓ " : "✗ ");
        System.out.println(result.getMessage() + "\n");

        // Test 6: Statistics
        System.out.println("Test 6: Get statistics");
        String stats = service.getLibraryStatistics();
        System.out.println("✓ Statistics retrieved:");
        System.out.println(stats + "\n");

        // Test 7: Edit book
        System.out.println("Test 7: Edit book");
        result = service.editBook("001", "Java 進階", null, null);
        System.out.println(result.isSuccess() ? "✓ " : "✗ ");
        System.out.println(result.getMessage());

        book = service.searchBookById("001");
        if (book != null) {
            System.out.println("New title: " + book.getTitle() + "\n");
        }

        // Test 8: Remove book
        System.out.println("Test 8: Remove book");
        result = service.removeBookById("006");
        System.out.println(result.isSuccess() ? "✓ " : "✗ ");
        System.out.println(result.getMessage() + "\n");

        // Test 9: Input validation
        System.out.println("Test 9: Input validation");
        result = service.addBook("", "Test", "Test", "Test");
        System.out.println(!result.isSuccess() ? "✓ Correctly rejected empty ID" : "✗ Should reject");
        System.out.println(result.getMessage() + "\n");

        System.out.println("=== All MVC tests completed ===");
        System.out.println("\nMVC Architecture Benefits:");
        System.out.println("✓ BookService: Pure business logic, no UI code");
        System.out.println("✓ LibraryUI: Pure UI code, no business logic");
        System.out.println("✓ BookController: Coordinates between UI and Service");
        System.out.println("✓ Easy to test: Can test Service without UI");
        System.out.println("✓ Easy to replace: Can change UI framework easily");
    }
}
