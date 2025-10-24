import javax.swing.*;

/**
 * LibraryUI - User Interface layer
 * Only handles UI interactions (dialogs, input/output)
 * No business logic
 */
public class LibraryUI {

    // ===== DIALOG TYPES =====

    public void showSuccess(String message) {
        JOptionPane.showMessageDialog(null,
            message,
            "成功",
            JOptionPane.INFORMATION_MESSAGE);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(null,
            message,
            "錯誤",
            JOptionPane.ERROR_MESSAGE);
    }

    public void showInfo(String message) {
        JOptionPane.showMessageDialog(null,
            message,
            "資訊",
            JOptionPane.INFORMATION_MESSAGE);
    }

    public void showWarning(String message) {
        JOptionPane.showMessageDialog(null,
            message,
            "警告",
            JOptionPane.WARNING_MESSAGE);
    }

    // ===== OPTION DIALOGS =====

    public int showOptions(String title, String message, String[] options) {
        return JOptionPane.showOptionDialog(null,
            message,
            title,
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null,
            options,
            options[0]);
    }

    public int showYesNoDialog(String title, String message) {
        return JOptionPane.showConfirmDialog(null,
            message,
            title,
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
    }

    // ===== INPUT DIALOGS =====

    public String promptInput(String title, String message) {
        return JOptionPane.showInputDialog(null, message, title, JOptionPane.PLAIN_MESSAGE);
    }

    public String promptInput(String title, String message, String defaultValue) {
        return (String) JOptionPane.showInputDialog(null,
            message,
            title,
            JOptionPane.PLAIN_MESSAGE,
            null,
            null,
            defaultValue);
    }

    // ===== BOOK OPERATIONS UI =====

    public String promptBookId() {
        return promptInput("輸入書籍 ID", "請輸入書籍 ID：");
    }

    public String promptBookTitle() {
        return promptInput("輸入書名", "請輸入書名：");
    }

    public String promptBookAuthor() {
        return promptInput("輸入作者", "請輸入作者名稱：");
    }

    public String promptBookPublisher() {
        return promptInput("輸入出版社", "請輸入出版社名稱：");
    }

    public int promptSearchMethod() {
        String[] options = {"依 ID 搜尋", "依書名搜尋"};
        return showOptions("搜尋方式", "請選擇搜尋方式：", options);
    }

    // ===== MENU DISPLAYS =====

    public int showMainMenu() {
        String[] options = {"管理員", "借閱者"};
        return showOptions(
            "中大圖書館借還系統",
            "歡迎來到圖書館借還系統\n請問你是借閱者或管理員",
            options);
    }

    public int showAdminTypeMenu() {
        String[] options = {"館長", "館員"};
        return showOptions("身分確認", "請問你是館長還是館員", options);
    }

    public int showBossMenu() {
        String[] options = {"決定是否休館", "控制員工月薪", "編輯書籍", "查看統計資訊"};
        return showOptions("館長系統", "館長您好，請點擊你要使用的功能", options);
    }

    public int showEmployeeMenu() {
        String[] options = {"查看月薪", "查看今日班表", "更改書籍資訊", "查看統計資訊"};
        return showOptions("館員系統", "請問今天要使用甚麼功能呢?", options);
    }

    public int showMemberTypeMenu() {
        String[] options = {"以會員身分登入", "以非會員身分登入"};
        return showOptions("使用者系統", "親愛的用戶您好，請問您是會員嗎?", options);
    }

    public int showMemberIdentityMenu() {
        String[] options = {"學生", "教師", "職員"};
        return showOptions("會員系統", "親愛的會員您好，請問你要登入的身分為何?", options);
    }

    public int showMemberFunctionMenu(String memberType) {
        String[] options = {"查看個人資料", "查看借還書紀錄", "查書與借還書"};
        return showOptions(
            "(會員)" + memberType + "系統",
            memberType + "您好，請點擊你要使用的功能",
            options);
    }

    public int showNonMemberMenu() {
        String[] options = {"註冊成會員", "使用查書功能"};
        return showOptions("非會員介面", "請問要註冊成會員或使用查書功能?", options);
    }

    public int showBookOperationMenu() {
        String[] options = {"借書", "還書", "查書"};
        return showOptions("會員借還書系統", "親愛的會員您好，請問你要查書、借書還是還書", options);
    }

    public int showAdminBookMenu() {
        String[] options = {"新增書籍", "刪除書籍", "查詢書籍", "編輯書籍"};
        return showOptions("書籍管理", "請選擇要執行的操作", options);
    }

    public int showContinueMenu() {
        String[] options = {"回主頁", "繼續使用功能", "登出並關閉"};
        return showOptions("操作選項", "還要繼續使用系統嗎", options);
    }

    // ===== DISPLAY INFORMATION =====

    public void displayAllBooks(String booksInfo) {
        if (booksInfo == null || booksInfo.trim().isEmpty()) {
            showInfo("目前沒有任何書籍");
        } else {
            showInfo("目前館藏：\n\n" + booksInfo);
        }
    }

    public void displayBookInfo(BookInfo book) {
        if (book == null) {
            showError("找不到此書籍");
        } else {
            String info = "書籍資訊\n\n" +
                         "ID：" + book.getId() + "\n" +
                         "書名：" + book.getTitle() + "\n" +
                         "作者：" + book.getAuthor() + "\n" +
                         "出版社：" + book.getPublisher() + "\n" +
                         "狀態：" + (book.isAvailable() ? "可借閱" : "已借出");
            showInfo(info);
        }
    }

    public void displayStatistics(String stats) {
        showInfo(stats);
    }

    // ===== REGISTRATION =====

    public String[] promptRegistrationInfo() {
        String username = promptInput("註冊系統 I", "輸入使用者名稱");
        if (username == null || username.trim().isEmpty()) {
            return null;
        }

        String password = promptInput("註冊系統 II", "輸入您要的密碼");
        if (password == null || password.trim().isEmpty()) {
            return null;
        }

        String[] identityOptions = {"學生", "教師", "職員"};
        int identity = showOptions("註冊系統 III", "請問你是哪種身分?", identityOptions);
        if (identity < 0) {
            return null;
        }

        return new String[]{username, password, identityOptions[identity]};
    }

    // ===== CONFIRMATION =====

    public boolean confirmAction(String message) {
        return showYesNoDialog("確認", message) == JOptionPane.YES_OPTION;
    }
}
