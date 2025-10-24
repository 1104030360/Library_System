import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Type;

/**
 * Book File Repository
 * Manages book data with JSON file persistence
 *
 * Stage 2: Learn data persistence with JSON files
 */
public class BookFileRepository {

    private static final String DATA_FILE = "data/books.json";
    private List<BookInfo> books;
    private Gson gson;

    public BookFileRepository() {
        this.books = new ArrayList<>();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Initialize with default books
     */
    public void initialize() {
        books.clear();
        books.add(new BookInfo("001", "Java", "吳柏毅", "中央大學"));
        books.add(new BookInfo("002", "管數課本", "吳昀蓁", "台灣大學"));
        books.add(new BookInfo("003", "英文課本", "林俊廷", "交通大學"));
        books.add(new BookInfo("004", "國文課本", "屠安弟", "政治大學"));
        books.add(new BookInfo("005", "體育課本", "陳重言", "清華大學"));

        System.out.println("Initialized " + books.size() + " books");
    }

    /**
     * Load books from JSON file
     */
    public void loadFromFile() {
        try {
            Path path = Paths.get(DATA_FILE);

            if (!Files.exists(path)) {
                System.out.println("No data file found, using default books");
                initialize();
                saveToFile();
                return;
            }

            String json = Files.readString(path);
            Type listType = new TypeToken<ArrayList<BookInfo>>(){}.getType();
            List<BookInfo> loadedBooks = gson.fromJson(json, listType);

            if (loadedBooks != null) {
                books = loadedBooks;
                System.out.println("✅ Loaded " + books.size() + " books from file");
            } else {
                System.out.println("File is empty, using default books");
                initialize();
            }

        } catch (IOException e) {
            System.err.println("Error loading books: " + e.getMessage());
            System.out.println("Using default books");
            initialize();
        }
    }

    /**
     * Save books to JSON file
     */
    public void saveToFile() {
        try {
            // Create data directory if not exists
            Path dir = Paths.get("data");
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            String json = gson.toJson(books);
            Files.writeString(Paths.get(DATA_FILE), json);

            System.out.println("✅ Saved " + books.size() + " books to file");
        } catch (IOException e) {
            System.err.println("Error saving books: " + e.getMessage());
        }
    }

    /**
     * Get all books
     */
    public List<BookInfo> getAllBooks() {
        return new ArrayList<>(books);
    }

    /**
     * Find book by ID
     */
    public BookInfo findById(String id) {
        for (BookInfo book : books) {
            if (book.getId().equals(id)) {
                return book;
            }
        }
        return null;
    }

    /**
     * Find book by title
     */
    public BookInfo findByTitle(String title) {
        for (BookInfo book : books) {
            if (book.getTitle().equals(title)) {
                return book;
            }
        }
        return null;
    }

    /**
     * Add a new book
     */
    public boolean addBook(BookInfo book) {
        // Check if ID already exists
        if (findById(book.getId()) != null) {
            return false;
        }
        books.add(book);
        saveToFile();
        return true;
    }

    /**
     * Remove book by ID
     */
    public boolean removeBookById(String id) {
        BookInfo book = findById(id);
        if (book == null) {
            return false;
        }
        books.remove(book);
        saveToFile();
        return true;
    }

    /**
     * Update book availability
     */
    public void updateBook(BookInfo book) {
        saveToFile();
    }

    /**
     * Get statistics
     */
    public String getStatistics() {
        int total = books.size();
        int available = 0;
        int borrowed = 0;

        for (BookInfo book : books) {
            if (book.isAvailable()) {
                available++;
            } else {
                borrowed++;
            }
        }

        return String.format("Total: %d books | Available: %d | Borrowed: %d",
                             total, available, borrowed);
    }
}
