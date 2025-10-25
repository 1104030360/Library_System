import java.util.ArrayList;
import java.util.List;

public class BookRepository {
    private List<BookInfo> books = new ArrayList<>();

    // Initialize with default books
    public void initialize() {
        books.add(new BookInfo("001", "Java", "吳柏毅", "中央大學"));
        books.add(new BookInfo("002", "管數課本", "吳昀蓁", "台灣大學"));
        books.add(new BookInfo("003", "英文課本", "林俊廷", "交通大學"));
        books.add(new BookInfo("004", "國文課本", "屠安弟", "政治大學"));
        books.add(new BookInfo("005", "體育課本", "陳重言", "清華大學"));
    }

    // Add a new book
    public boolean addBook(BookInfo book) {
        if (book == null) {
            return false;
        }
        // Check if book with same ID already exists
        if (findById(book.getId()) != null) {
            return false;  // Duplicate ID
        }
        return books.add(book);
    }

    // Remove book by ID
    public boolean removeBookById(String id) {
        BookInfo book = findById(id);
        if (book != null) {
            return books.remove(book);
        }
        return false;
    }

    // Remove book by title
    public boolean removeBookByTitle(String title) {
        BookInfo book = findByTitle(title);
        if (book != null) {
            return books.remove(book);
        }
        return false;
    }

    // Find book by ID
    public BookInfo findById(String id) {
        if (id == null) return null;

        for (BookInfo book : books) {
            if (id.equals(book.getId())) {
                return book;
            }
        }
        return null;
    }

    // Find book by title
    public BookInfo findByTitle(String title) {
        if (title == null) return null;

        for (BookInfo book : books) {
            if (title.equals(book.getTitle())) {
                return book;
            }
        }
        return null;
    }

    // Find books by author
    public List<BookInfo> findByAuthor(String author) {
        List<BookInfo> result = new ArrayList<>();
        if (author == null) return result;

        for (BookInfo book : books) {
            if (author.equals(book.getAuthor())) {
                result.add(book);
            }
        }
        return result;
    }

    // Find books by publisher
    public List<BookInfo> findByPublisher(String publisher) {
        List<BookInfo> result = new ArrayList<>();
        if (publisher == null) return result;

        for (BookInfo book : books) {
            if (publisher.equals(book.getPublisher())) {
                result.add(book);
            }
        }
        return result;
    }

    // Get all available books
    public List<BookInfo> getAllAvailableBooks() {
        List<BookInfo> result = new ArrayList<>();
        for (BookInfo book : books) {
            if (book.isAvailable()) {
                result.add(book);
            }
        }
        return result;
    }

    // Get all books
    public List<BookInfo> getAllBooks() {
        return new ArrayList<>(books);  // Return a copy to prevent external modification
    }

    // Get total number of books
    public int getTotalBooks() {
        return books.size();
    }

    // Get number of available books
    public int getAvailableBooksCount() {
        int count = 0;
        for (BookInfo book : books) {
            if (book.isAvailable()) {
                count++;
            }
        }
        return count;
    }

    // Get formatted string of all books for display
    public String getAllBooksDisplayString() {
        StringBuilder output = new StringBuilder();
        for (BookInfo book : books) {
            output.append(book.getDisplayString()).append("\n");
        }
        return output.toString();
    }

    // Update book information (edit)
    public boolean updateBook(String id, String newTitle, String newAuthor, String newPublisher) {
        BookInfo oldBook = findById(id);
        if (oldBook == null) {
            return false;
        }

        // Remove old book and add updated one
        books.remove(oldBook);
        BookInfo newBook = new BookInfo(id, newTitle, newAuthor, newPublisher);
        newBook.markAsReturned();  // Preserve availability status
        if (!oldBook.isAvailable()) {
            newBook.markAsBorrowed();
        }
        books.add(newBook);
        return true;
    }
}
