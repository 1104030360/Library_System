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
}
