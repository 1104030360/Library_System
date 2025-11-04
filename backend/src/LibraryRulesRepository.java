import java.util.*;

/**
 * Library Rules Repository - Library rules data layer
 * Manages library rules and FAQ
 *
 * Status: Phase 0 - Empty implementation
 */
public class LibraryRulesRepository {

    public static class LibraryRule {
        public String category;  // "Borrowing Rules", "Overdue Rules", "Reservation Rules"
        public String question;
        public String answer;

        public LibraryRule(String category, String question, String answer) {
            this.category = category;
            this.question = question;
            this.answer = answer;
        }
    }

    // Empty implementation, will be completed in Phase 1
    public List<LibraryRule> getAllRules() {
        return new ArrayList<>();
    }

    public List<LibraryRule> getRulesByCategory(String category) {
        return new ArrayList<>();
    }
}
