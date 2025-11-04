import java.util.*;
import java.util.stream.Collectors;
import java.sql.*;

/**
 * Context Retriever - 上下文檢索器
 * 根據問題類型從資料庫檢索相關資料
 *
 * Status: 階段 4 - 完整實作
 */
public class ContextRetriever {

    private final BorrowHistoryRepository historyRepo;
    private final BookDatabaseRepository bookRepo;
    private final LibraryRulesRepository rulesRepo;

    public ContextRetriever(BorrowHistoryRepository historyRepo,
                           BookDatabaseRepository bookRepo,
                           LibraryRulesRepository rulesRepo) {
        this.historyRepo = historyRepo;
        this.bookRepo = bookRepo;
        this.rulesRepo = rulesRepo;
    }

    /**
     * 檢索上下文資料
     *
     * @param userId 使用者 ID
     * @param message 使用者訊息
     * @param types 問題類型集合
     * @return ChatContext 包含相關資料
     */
    public ChatContext retrieveContext(String userId, String message,
                                      Set<QuestionClassifier.QuestionType> types) {

        System.out.println("🔍 Retrieving context for user: " + userId);
        System.out.println("   Question types: " + types);

        ChatContext context = new ChatContext();

        // 1. 借閱記錄
        if (types.contains(QuestionClassifier.QuestionType.BORROW_HISTORY)) {
            retrieveBorrowHistory(userId, context);
        }

        // 2. 書籍搜尋
        if (types.contains(QuestionClassifier.QuestionType.BOOK_SEARCH)) {
            retrieveAvailableBooks(context);
        }

        // 3. 書籍可借狀態
        if (types.contains(QuestionClassifier.QuestionType.BOOK_AVAILABILITY)) {
            retrieveTargetBook(message, context);
        }

        // 4. 圖書館規則
        if (types.contains(QuestionClassifier.QuestionType.LIBRARY_RULES)) {
            retrieveLibraryRules(context);
        }

        // 5. 總是包含統計資訊
        retrieveStats(context);

        System.out.println("✅ Context retrieved: " + context.getSummary());

        return context;
    }

    /**
     * 檢索借閱歷史
     */
    private void retrieveBorrowHistory(String userId, ChatContext context) {
        try {
            List<BorrowHistory> history = historyRepo.getUserHistory(userId);

            if (history != null && !history.isEmpty()) {
                context.setBorrowHistory(history);

                // 自動篩選出當前借閱
                List<BorrowHistory> current = history.stream()
                    .filter(h -> "borrowed".equals(h.getStatus()))
                    .collect(Collectors.toList());

                context.setCurrentBorrowings(current);

                System.out.println("   📚 Borrow history: " + history.size() + " records");
                System.out.println("   📖 Current borrowings: " + current.size() + " books");
            }
        } catch (Exception e) {
            System.err.println("❌ Error retrieving borrow history: " + e.getMessage());
        }
    }

    /**
     * 檢索可借閱書籍
     */
    private void retrieveAvailableBooks(ChatContext context) {
        try {
            // 使用簡單的查詢方法，避免複雜的 JOIN
            List<BookInfo> allBooks = new ArrayList<>();

            // 獲取所有書籍（使用簡單的 SQL 查詢）
            try (java.sql.Connection conn = bookRepo.getConnection();
                 java.sql.Statement stmt = conn.createStatement();
                 java.sql.ResultSet rs = stmt.executeQuery(
                     "SELECT id, title, author, publisher, description, is_available FROM books WHERE is_available = 1 LIMIT 20")) {

                while (rs.next()) {
                    BookInfo book = new BookInfo(
                        rs.getString("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("publisher"),
                        rs.getString("description")
                    );
                    allBooks.add(book);
                }
            }

            if (!allBooks.isEmpty()) {
                context.setAvailableBooks(allBooks);
                System.out.println("   📚 Available books: " + allBooks.size() + " books");
            }
        } catch (Exception e) {
            System.err.println("❌ Error retrieving available books: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 檢索特定書籍
     */
    private void retrieveTargetBook(String message, ChatContext context) {
        try {
            // 使用 QuestionClassifier 提取書名
            QuestionClassifier classifier = new QuestionClassifier();
            String bookTitle = classifier.extractBookTitle(message);

            if (bookTitle != null && !bookTitle.isEmpty()) {
                BookInfo book = bookRepo.findByTitle(bookTitle);

                if (book != null) {
                    context.setTargetBook(book);
                    System.out.println("   📖 Target book: " + book.getTitle() +
                                     " (Available: " + book.isAvailable() + ")");
                } else {
                    System.out.println("   ❌ Target book not found: " + bookTitle);

                    // 嘗試模糊搜尋
                    List<BookInfo> searchResults = bookRepo.searchByTitle(bookTitle);
                    if (!searchResults.isEmpty()) {
                        System.out.println("   📚 Found similar books: " + searchResults.size());
                        // 將搜尋結果放入 availableBooks
                        context.setAvailableBooks(searchResults);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error retrieving target book: " + e.getMessage());
        }
    }

    /**
     * 檢索圖書館規則
     */
    private void retrieveLibraryRules(ChatContext context) {
        try {
            List<LibraryRulesRepository.LibraryRule> rules = rulesRepo.getAllRules();

            if (rules != null && !rules.isEmpty()) {
                context.setLibraryRules(rules);
                System.out.println("   📋 Library rules: " + rules.size() + " rules");
            }
        } catch (Exception e) {
            System.err.println("❌ Error retrieving library rules: " + e.getMessage());
        }
    }

    /**
     * 檢索統計資訊
     */
    private void retrieveStats(ChatContext context) {
        try {
            BookDatabaseRepository.LibraryStats stats = bookRepo.getStats();

            if (stats != null) {
                context.setStats(stats);
                System.out.println("   📊 Stats: " + stats);
            }
        } catch (Exception e) {
            System.err.println("❌ Error retrieving stats: " + e.getMessage());
        }
    }

    /**
     * 檢索特定類別的規則
     */
    public List<LibraryRulesRepository.LibraryRule> retrieveRulesByKeyword(String keyword) {
        try {
            return rulesRepo.searchRules(keyword);
        } catch (Exception e) {
            System.err.println("❌ Error searching rules: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
