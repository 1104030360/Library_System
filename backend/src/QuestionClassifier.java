import java.util.*;

/**
 * Question Classifier - Question type classifier
 * Identifies the type of user question to determine which data to retrieve
 *
 * Status: Phase 0 - Empty implementation
 */
public class QuestionClassifier {

    public enum QuestionType {
        BORROW_HISTORY,      // Borrowing history query
        BOOK_SEARCH,         // Book search
        BOOK_AVAILABILITY,   // Book availability status
        LIBRARY_RULES,       // Library rules
        GENERAL_CHAT,        // General conversation
        MIXED                // Mixed types
    }

    // Empty implementation, will be completed in Phase 2
    public Set<QuestionType> classify(String message) {
        // Temporarily return GENERAL_CHAT
        return Collections.singleton(QuestionType.GENERAL_CHAT);
    }

    /**
     * 從訊息中提取書名
     * 簡單實作：提取《》中的內容
     */
    public String extractBookTitle(String message) {
        if (message == null || message.isEmpty()) {
            return null;
        }

        // 查找《》符號
        int start = message.indexOf("《");
        int end = message.indexOf("》");

        if (start != -1 && end != -1 && end > start) {
            return message.substring(start + 1, end);
        }

        // 如果沒有《》，返回 null
        return null;
    }
}
