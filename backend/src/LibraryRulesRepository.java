import java.util.*;
import java.util.stream.Collectors;

/**
 * Library Rules Repository - Library rules data layer
 * Manages library rules and FAQ
 *
 * Status: Phase 1 - Complete implementation
 */
public class LibraryRulesRepository {

    public static class LibraryRule {
        public String category;  // "借閱規則", "逾期規則", "預約規則"
        public String question;
        public String answer;

        public LibraryRule(String category, String question, String answer) {
            this.category = category;
            this.question = question;
            this.answer = answer;
        }

        @Override
        public String toString() {
            return String.format("[%s] %s: %s", category, question, answer);
        }
    }

    /**
     * Get all library rules
     */
    public List<LibraryRule> getAllRules() {
        List<LibraryRule> rules = new ArrayList<>();

        // ===== Borrowing Rules =====
        rules.add(new LibraryRule(
            "借閱規則",
            "借書期限是多久？",
            "一般書籍借期為 14 天。到期前可續借一次，續借期限為 14 天。"
        ));

        rules.add(new LibraryRule(
            "借閱規則",
            "一次可以借幾本書？",
            "使用者最多可同時借閱 3 本書。"
        ));

        rules.add(new LibraryRule(
            "借閱規則",
            "如何借書？",
            "登入系統後，在書籍列表中找到想借的書，點擊「借閱」按鈕即可。前提是該書目前可借閱且您未達借閱上限。"
        ));

        rules.add(new LibraryRule(
            "借閱規則",
            "如何還書？",
            "在「我的借閱」頁面中，找到要歸還的書籍，點擊「歸還」按鈕即可。"
        ));

        // ===== Overdue Rules =====
        rules.add(new LibraryRule(
            "逾期規則",
            "逾期會有什麼後果？",
            "逾期書籍將影響您的借閱權限。請在到期前歸還或續借。系統會在到期前 3 天發送提醒通知。"
        ));

        rules.add(new LibraryRule(
            "逾期規則",
            "逾期可以續借嗎？",
            "不可以。逾期書籍需要先歸還，才能再次借閱。"
        ));

        // ===== Reservation Rules =====
        rules.add(new LibraryRule(
            "預約規則",
            "可以預約書籍嗎？",
            "目前系統暫不支援預約功能，請等書籍歸還後再借閱。您可以隨時查看書籍的可借狀態。"
        ));

        // ===== Renewal Rules =====
        rules.add(new LibraryRule(
            "續借規則",
            "如何續借書籍？",
            "目前系統暫不支援線上續借功能，如需續借請聯繫圖書館館員。"
        ));

        // ===== Account Rules =====
        rules.add(new LibraryRule(
            "帳號規則",
            "如何註冊帳號？",
            "請在登入頁面點擊「註冊」按鈕，填寫相關資訊即可註冊新帳號。"
        ));

        rules.add(new LibraryRule(
            "帳號規則",
            "忘記密碼怎麼辦？",
            "請聯繫圖書館館員協助重置密碼。"
        ));

        return rules;
    }

    /**
     * Get rules by category
     */
    public List<LibraryRule> getRulesByCategory(String category) {
        return getAllRules().stream()
            .filter(rule -> rule.category.equals(category))
            .collect(Collectors.toList());
    }

    /**
     * Search rules by keyword
     */
    public List<LibraryRule> searchRules(String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        return getAllRules().stream()
            .filter(rule ->
                rule.question.toLowerCase().contains(lowerKeyword) ||
                rule.answer.toLowerCase().contains(lowerKeyword)
            )
            .collect(Collectors.toList());
    }

    /**
     * Get all category names
     */
    public List<String> getAllCategories() {
        return getAllRules().stream()
            .map(rule -> rule.category)
            .distinct()
            .collect(Collectors.toList());
    }
}
