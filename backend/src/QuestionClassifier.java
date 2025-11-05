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

    /**
     * 分類使用者問題類型
     * 使用基於規則的多條件判斷，支援多類型返回
     *
     * @param message 使用者訊息
     * @return 問題類型集合（可能包含多個類型）
     */
    public Set<QuestionType> classify(String message) {
        if (message == null || message.isEmpty()) {
            return Collections.singleton(QuestionType.GENERAL_CHAT);
        }

        Set<QuestionType> types = new HashSet<>();
        String msg = message.toLowerCase();

        // 分析問句類型
        boolean isWhatQuestion = msg.contains("什麼") || msg.contains("哪些") ||
                                 msg.contains("什么") || msg.contains("甚麼");
        boolean isHowQuestion = msg.contains("怎麼") || msg.contains("如何") ||
                                msg.contains("怎么") || msg.contains("怎样");
        boolean isCanQuestion = msg.contains("可以") || msg.contains("能不能") ||
                                msg.contains("能否") || msg.contains("可不可以");
        boolean isWhereQuestion = msg.contains("在哪") || msg.contains("哪裡") ||
                                  msg.contains("哪里");
        boolean isWhenQuestion = msg.contains("什麼時候") || msg.contains("何時") ||
                                 msg.contains("什么时候");

        // 1. 借閱歷史 (BORROW_HISTORY)
        if (classifyBorrowHistory(msg)) {
            types.add(QuestionType.BORROW_HISTORY);
        }

        // 2. 書籍搜尋 (BOOK_SEARCH)
        if (classifyBookSearch(msg, isWhatQuestion)) {
            types.add(QuestionType.BOOK_SEARCH);
        }

        // 3. 書籍可借狀態 (BOOK_AVAILABILITY)
        if (classifyBookAvailability(msg, isCanQuestion)) {
            types.add(QuestionType.BOOK_AVAILABILITY);
        }

        // 4. 圖書館規則 (LIBRARY_RULES)
        if (classifyLibraryRules(msg, isHowQuestion, isCanQuestion, isWhenQuestion)) {
            types.add(QuestionType.LIBRARY_RULES);
        }

        // 5. 如果沒有匹配任何類型，返回 GENERAL_CHAT
        if (types.isEmpty()) {
            types.add(QuestionType.GENERAL_CHAT);
        }

        return types;
    }

    /**
     * 判斷是否為借閱歷史查詢
     */
    private boolean classifyBorrowHistory(String msg) {
        // 關鍵詞組合：借 + (過/記錄/歷史/了)
        if (msg.contains("借")) {
            if (msg.contains("過") || msg.contains("记录") || msg.contains("記錄") ||
                msg.contains("历史") || msg.contains("歷史") || msg.contains("了哪些") ||
                msg.contains("了什么") || msg.contains("了什麼")) {
                return true;
            }
        }

        // 直接關鍵詞
        String[] keywords = {
            "我借了", "我借过", "我借過", "借閱記錄", "借阅记录",
            "借書歷史", "借书历史", "借閱歷史", "借阅历史",
            "借過哪些", "借过哪些", "查看借閱", "查看借阅"
        };

        for (String keyword : keywords) {
            if (msg.contains(keyword.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    /**
     * 判斷是否為書籍搜尋
     */
    private boolean classifyBookSearch(String msg, boolean isWhatQuestion) {
        // "什麼書" 或 "哪些書"
        if (isWhatQuestion && (msg.contains("書") || msg.contains("书"))) {
            return true;
        }

        // 關鍵詞
        String[] keywords = {
            "推薦", "推荐", "找書", "找书", "搜尋", "搜索", "查詢", "查询",
            "有哪些書", "有哪些书", "有什麼書", "有什么书",
            "想看", "想借", "想找", "尋找", "寻找",
            "館藏", "馆藏", "書籍", "书籍", "藏書", "藏书"
        };

        for (String keyword : keywords) {
            if (msg.contains(keyword.toLowerCase())) {
                return true;
            }
        }

        // 主題相關：AI、Python、Java 等 + 書
        if ((msg.contains("書") || msg.contains("书")) &&
            (msg.contains("關於") || msg.contains("关于") || msg.contains("有關") || msg.contains("有关"))) {
            return true;
        }

        return false;
    }

    /**
     * 判斷是否為書籍可借狀態查詢
     */
    private boolean classifyBookAvailability(String msg, boolean isCanQuestion) {
        // 包含書名標記《》
        if (msg.contains("《") && msg.contains("》")) {
            return true;
        }

        // "可以借" 或 "能不能借"
        if (isCanQuestion && msg.contains("借")) {
            return true;
        }

        // 關鍵詞組合
        String[] patterns = {
            "有沒有", "有没有", "在不在", "還有", "还有",
            "可借", "能借", "借得到", "可以借嗎", "可以借吗",
            "有沒有這本", "有没有这本", "這本書", "这本书"
        };

        for (String pattern : patterns) {
            if (msg.contains(pattern.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    /**
     * 判斷是否為圖書館規則查詢
     */
    private boolean classifyLibraryRules(String msg, boolean isHowQuestion,
                                         boolean isCanQuestion, boolean isWhenQuestion) {
        // "怎麼借" 或 "如何借"
        if (isHowQuestion && msg.contains("借")) {
            return true;
        }

        // 關鍵詞
        String[] keywords = {
            "規則", "规则", "期限", "时限", "時限", "流程",
            "手續", "手续", "辦法", "办法", "方法",
            "逾期", "罰款", "罚款", "延期", "續借", "续借",
            "借多久", "借幾本", "借几本", "借多少",
            "還書", "还书", "歸還", "归还"
        };

        for (String keyword : keywords) {
            if (msg.contains(keyword.toLowerCase())) {
                return true;
            }
        }

        // "可以借幾本" 之類的問題
        if (isCanQuestion &&
            (msg.contains("幾") || msg.contains("几") || msg.contains("多少") || msg.contains("多久"))) {
            return true;
        }

        // "什麼時候還"
        if (isWhenQuestion && (msg.contains("還") || msg.contains("还"))) {
            return true;
        }

        return false;
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
