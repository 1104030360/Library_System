import java.util.ArrayList;

/**
 * API Authentication Helper - Stage 4
 * Authentication logic for API (without GUI/Swing dependencies)
 * Validates username and password against hardcoded admin accounts
 */
public class ApiAuthenticationHelper {

    // Hardcoded admin accounts (matching Admin.java)
    private static ArrayList<AdminAccount> accounts = new ArrayList<>();

    static {
        // Initialize accounts (same as Admin.java constructor)
        accounts.add(new AdminAccount("0001", 1111, "館長"));
        accounts.add(new AdminAccount("0002", 2222, "員工"));
        accounts.add(new AdminAccount("0003", 3333, "員工"));
        accounts.add(new AdminAccount("0004", 4444, "員工"));
        accounts.add(new AdminAccount("0005", 5555, "員工"));
        accounts.add(new AdminAccount("0006", 6666, "員工"));
    }

    /**
     * AdminAccount - Simple POJO for admin account
     */
    public static class AdminAccount {
        public String username;
        public int password;
        public String userType;  // "館長" or "員工"

        public AdminAccount(String username, int password, String userType) {
            this.username = username;
            this.password = password;
            this.userType = userType;
        }
    }

    /**
     * AuthResult - Result of authentication attempt
     */
    public static class AuthResult {
        public boolean success;
        public String message;
        public String username;
        public String userType;

        public AuthResult(boolean success, String message, String username, String userType) {
            this.success = success;
            this.message = message;
            this.username = username;
            this.userType = userType;
        }

        public static AuthResult failure(String message) {
            return new AuthResult(false, message, null, null);
        }

        public static AuthResult success(String username, String userType) {
            return new AuthResult(true, "Authentication successful", username, userType);
        }
    }

    /**
     * Authenticate user with username and password
     * @param username Username (e.g., "0001")
     * @param password Password (e.g., 1111)
     * @return AuthResult with success status and user info
     */
    public static AuthResult authenticate(String username, int password) {
        // Validate input
        if (username == null || username.isEmpty()) {
            return AuthResult.failure("Username cannot be empty");
        }

        // Find account
        AdminAccount account = findAccount(username);
        if (account == null) {
            System.out.println("❌ Account not found: " + username);
            return AuthResult.failure("Account not found");
        }

        // Verify password
        if (account.password != password) {
            System.out.println("❌ Wrong password for: " + username);
            return AuthResult.failure("Wrong password");
        }

        // Success
        System.out.println("✅ Authentication successful: " + username + " (" + account.userType + ")");
        return AuthResult.success(account.username, account.userType);
    }

    /**
     * Find account by username
     */
    private static AdminAccount findAccount(String username) {
        for (AdminAccount account : accounts) {
            if (account.username.equals(username)) {
                return account;
            }
        }
        return null;
    }

    /**
     * Get all accounts (for debugging)
     */
    public static ArrayList<AdminAccount> getAllAccounts() {
        return new ArrayList<>(accounts);
    }
}
