import java.util.*;

/**
 * Test for ContextRetriever (階段 4)
 */
public class TestContextRetriever {

    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("Testing ContextRetriever");
        System.out.println("=".repeat(60));

        // 初始化組件
        BookDatabaseRepository bookRepo = new BookDatabaseRepository();
        bookRepo.initialize();

        BorrowHistoryRepository historyRepo = null;
        try {
            historyRepo = new BorrowHistoryRepository(bookRepo.getConnection());
        } catch (Exception e) {
            System.err.println("Error initializing historyRepo: " + e.getMessage());
            historyRepo = null;
        }

        LibraryRulesRepository rulesRepo = new LibraryRulesRepository();

        ContextRetriever retriever = new ContextRetriever(
            historyRepo, bookRepo, rulesRepo
        );

        QuestionClassifier classifier = new QuestionClassifier();

        // Test 1: 借閱記錄查詢
        testBorrowHistoryRetrieval(retriever, classifier);

        // Test 2: 書籍搜尋
        testBookSearchRetrieval(retriever, classifier);

        // Test 3: 特定書籍查詢
        testBookAvailabilityRetrieval(retriever, classifier);

        // Test 4: 圖書館規則查詢
        testLibraryRulesRetrieval(retriever, classifier);

        // Test 5: 混合查詢
        testMixedRetrieval(retriever, classifier);

        System.out.println("\n" + "=".repeat(60));
        System.out.println("All tests passed! ✅");
        System.out.println("=".repeat(60));
    }

    static void testBorrowHistoryRetrieval(ContextRetriever retriever,
                                          QuestionClassifier classifier) {
        System.out.println("\n[Test 1] 借閱記錄查詢");

        String message = "我借了哪些書？";
        // 直接指定問題類型（因為 classifier 尚未完整實作）
        Set<QuestionClassifier.QuestionType> types = new HashSet<>();
        types.add(QuestionClassifier.QuestionType.BORROW_HISTORY);

        String userId = "0001";
        ChatContext context = retriever.retrieveContext(userId, message, types);

        assert context != null : "Context should not be null";
        assert context.getStats() != null : "Stats should always be included";

        System.out.println("✅ Context retrieved: " + context.getSummary());
    }

    static void testBookSearchRetrieval(ContextRetriever retriever,
                                       QuestionClassifier classifier) {
        System.out.println("\n[Test 2] 書籍搜尋");

        String message = "有哪些書可以借？";
        // 直接指定問題類型
        Set<QuestionClassifier.QuestionType> types = new HashSet<>();
        types.add(QuestionClassifier.QuestionType.BOOK_SEARCH);

        String userId = "0001";
        ChatContext context = retriever.retrieveContext(userId, message, types);

        assert context != null : "Context should not be null";
        assert context.getAvailableBooks() != null : "Available books should be retrieved";
        assert !context.getAvailableBooks().isEmpty() : "Should have available books";

        System.out.println("✅ Found " + context.getAvailableBooks().size() + " available books");
    }

    static void testBookAvailabilityRetrieval(ContextRetriever retriever,
                                             QuestionClassifier classifier) {
        System.out.println("\n[Test 3] 特定書籍查詢");

        String message = "《深入淺出 Java 程式設計》可以借嗎？";
        // 直接指定問題類型
        Set<QuestionClassifier.QuestionType> types = new HashSet<>();
        types.add(QuestionClassifier.QuestionType.BOOK_AVAILABILITY);

        String userId = "0001";
        ChatContext context = retriever.retrieveContext(userId, message, types);

        assert context != null : "Context should not be null";

        // 應該找到目標書籍或相似書籍
        boolean hasTargetOrSimilar = context.getTargetBook() != null ||
                                    (context.getAvailableBooks() != null &&
                                     !context.getAvailableBooks().isEmpty());

        assert hasTargetOrSimilar : "Should find target book or similar books";

        if (context.getTargetBook() != null) {
            System.out.println("✅ Found target book: " + context.getTargetBook().getTitle());
        } else {
            System.out.println("✅ Found similar books: " + context.getAvailableBooks().size());
        }
    }

    static void testLibraryRulesRetrieval(ContextRetriever retriever,
                                         QuestionClassifier classifier) {
        System.out.println("\n[Test 4] 圖書館規則查詢");

        String message = "借書期限是多久？";
        // 直接指定問題類型
        Set<QuestionClassifier.QuestionType> types = new HashSet<>();
        types.add(QuestionClassifier.QuestionType.LIBRARY_RULES);

        String userId = "0001";
        ChatContext context = retriever.retrieveContext(userId, message, types);

        assert context != null : "Context should not be null";
        assert context.getLibraryRules() != null : "Library rules should be retrieved";
        assert !context.getLibraryRules().isEmpty() : "Should have library rules";

        System.out.println("✅ Found " + context.getLibraryRules().size() + " library rules");
    }

    static void testMixedRetrieval(ContextRetriever retriever,
                                  QuestionClassifier classifier) {
        System.out.println("\n[Test 5] 混合查詢");

        String message = "我借了哪些書？還有哪些可以借？";
        // 直接指定問題類型
        Set<QuestionClassifier.QuestionType> types = new HashSet<>();
        types.add(QuestionClassifier.QuestionType.BORROW_HISTORY);
        types.add(QuestionClassifier.QuestionType.BOOK_SEARCH);

        String userId = "0001";
        ChatContext context = retriever.retrieveContext(userId, message, types);

        assert context != null : "Context should not be null";
        assert context.getStats() != null : "Stats should always be included";

        // 混合查詢應該包含多種資料
        int dataTypes = 0;
        if (context.getBorrowHistory() != null) dataTypes++;
        if (context.getAvailableBooks() != null) dataTypes++;
        if (context.getStats() != null) dataTypes++;

        assert dataTypes >= 2 : "Mixed query should retrieve multiple data types";

        System.out.println("✅ Retrieved " + dataTypes + " types of data");
        System.out.println("   " + context.getSummary());
    }
}
