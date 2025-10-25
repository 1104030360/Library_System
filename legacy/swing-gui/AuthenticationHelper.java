import javax.swing.*;

/**
 * AuthenticationHelper - Provides common authentication methods
 * Eliminates duplicate authentication logic across UserClass
 */
public class AuthenticationHelper {

    /**
     * Generic two-stage authentication method
     * @param userTypeName - Display name for the user type (e.g., "館長", "學生")
     * @param accountValidator - Function to check if account exists
     * @param passwordValidator - Function to check if password matches
     * @return true if authentication successful, false otherwise
     */
    public static boolean authenticate(
            String userTypeName,
            AccountValidator accountValidator,
            PasswordValidator passwordValidator) {

        String account;
        int attemptCount = 0;
        final int MAX_ATTEMPTS = 3;

        while (attemptCount < MAX_ATTEMPTS) {
            // Stage 1: Account verification
            account = JOptionPane.showInputDialog(null,
                "請輸入" + userTypeName + "帳號",
                userTypeName + "驗證 I");

            if (account == null) {
                // User cancelled
                return false;
            }

            if (accountValidator.checkAccount(account)) {
                // Stage 2: Password verification
                String passwordInput = JOptionPane.showInputDialog(null,
                    "請輸入密碼",
                    userTypeName + "驗證 II");

                if (passwordInput == null) {
                    // User cancelled
                    return false;
                }

                try {
                    int password = Integer.parseInt(passwordInput);

                    if (passwordValidator.checkPassword(password)) {
                        // Authentication successful
                        return true;
                    } else {
                        JOptionPane.showMessageDialog(null,
                            "密碼錯誤！請再試一次",
                            "驗證失敗",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null,
                        "密碼必須是數字",
                        "格式錯誤",
                        JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null,
                    "此帳號不存在！請再輸一次",
                    "驗證失敗",
                    JOptionPane.ERROR_MESSAGE);
            }

            attemptCount++;
        }

        // Max attempts reached
        JOptionPane.showMessageDialog(null,
            "驗證失敗次數過多，請稍後再試",
            "驗證失敗",
            JOptionPane.ERROR_MESSAGE);
        return false;
    }

    /**
     * Simplified authentication without attempt limit (for backward compatibility)
     */
    public static boolean authenticateUnlimited(
            String userTypeName,
            AccountValidator accountValidator,
            PasswordValidator passwordValidator) {

        String account;

        do {
            account = JOptionPane.showInputDialog(null,
                "請輸入" + userTypeName + "帳號",
                userTypeName + "驗證 I");

            if (account == null) {
                return false;
            }

            if (accountValidator.checkAccount(account)) {
                String passwordInput = JOptionPane.showInputDialog(null,
                    "請輸入密碼",
                    userTypeName + "驗證 II");

                if (passwordInput == null) {
                    return false;
                }

                try {
                    int password = Integer.parseInt(passwordInput);

                    if (passwordValidator.checkPassword(password)) {
                        return true;
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null,
                        "密碼必須是數字",
                        "格式錯誤",
                        JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null,
                    "此帳號不存在！請再輸一次",
                    "驗證失敗",
                    JOptionPane.ERROR_MESSAGE);
            }
        } while (true);
    }

    // Functional interfaces for validation
    @FunctionalInterface
    public interface AccountValidator {
        boolean checkAccount(String account);
    }

    @FunctionalInterface
    public interface PasswordValidator {
        boolean checkPassword(int password);
    }
}
