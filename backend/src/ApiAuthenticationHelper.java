/**
 * API Authentication Helper - Stage 5
 * Authentication logic for API using database
 * Following Linus principles: Eliminate special cases
 * All users (admin/staff/regular) authenticated via database
 */
public class ApiAuthenticationHelper {

    private static UserDatabaseRepository userRepository;

    /**
     * Initialize with user repository
     */
    public static void initialize(UserDatabaseRepository repository) {
        userRepository = repository;
    }

    /**
     * AuthResult - Result of authentication attempt
     */
    public static class AuthResult {
        public boolean success;
        public String message;
        public String username;    // User ID (for backward compatibility)
        public String userType;
        public String name;        // User's display name

        public AuthResult(boolean success, String message, String username, String userType, String name) {
            this.success = success;
            this.message = message;
            this.username = username;
            this.userType = userType;
            this.name = name;
        }

        public static AuthResult failure(String message) {
            return new AuthResult(false, message, null, null, null);
        }

        public static AuthResult success(String username, String userType, String name) {
            return new AuthResult(true, "Authentication successful", username, userType, name);
        }
    }

    /**
     * Authenticate user with username and password
     * @param username Username (e.g., "0001" or "1001")
     * @param password Password (e.g., 1111 or "password")
     * @return AuthResult with success status and user info
     */
    public static AuthResult authenticate(String username, int password) {
        return authenticate(username, String.valueOf(password));
    }

    /**
     * Authenticate user with username and password (string version)
     * @param username Username (e.g., "0001" or "1001")
     * @param password Password as string
     * @return AuthResult with success status and user info
     */
    public static AuthResult authenticate(String username, String password) {
        // Validate input
        if (username == null || username.trim().isEmpty()) {
            return AuthResult.failure("Username cannot be empty");
        }

        if (password == null || password.isEmpty()) {
            return AuthResult.failure("Password cannot be empty");
        }

        if (userRepository == null) {
            System.err.println("❌ UserRepository not initialized!");
            return AuthResult.failure("Authentication service unavailable");
        }

        // Authenticate via database
        User user = userRepository.authenticate(username.trim(), password);

        if (user == null) {
            System.out.println("❌ Authentication failed: " + username);
            return AuthResult.failure("Invalid username or password");
        }

        // Success
        System.out.println("✅ Authentication successful: " + username +
                         " (" + user.getName() + ", " + user.getUserType() + ")");
        return AuthResult.success(user.getId(), user.getUserType(), user.getName());
    }
}
