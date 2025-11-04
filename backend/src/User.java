/**
 * User data model
 * Represents a user in the library system
 */
public class User {
    private String id;              // Student ID or Employee ID
    private String name;            // User's name
    private String passwordHash;    // Hashed password (SHA-256)
    private String email;           // Email address (optional)
    private String userType;        // 'user', 'staff', or 'admin'
    private String createdAt;       // Registration timestamp
    private String lastLogin;       // Last login timestamp

    /**
     * Constructor for creating new user
     */
    public User(String id, String name, String passwordHash, String email, String userType) {
        this.id = id;
        this.name = name;
        this.passwordHash = passwordHash;
        this.email = email;
        this.userType = userType;
        this.createdAt = java.time.LocalDateTime.now().toString();
        this.lastLogin = null;
    }

    /**
     * Constructor for loading user from database
     */
    public User(String id, String name, String passwordHash, String email,
                String userType, String createdAt, String lastLogin) {
        this.id = id;
        this.name = name;
        this.passwordHash = passwordHash;
        this.email = email;
        this.userType = userType;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getPasswordHash() { return passwordHash; }
    public String getEmail() { return email; }
    public String getUserType() { return userType; }
    public String getCreatedAt() { return createdAt; }
    public String getLastLogin() { return lastLogin; }

    // Helper methods
    public boolean isAdmin() {
        return "admin".equals(userType) || "館長".equals(userType);
    }

    public boolean isStaff() {
        return "staff".equals(userType) || "館員".equals(userType);
    }

    public boolean isRegularUser() {
        return "user".equals(userType);
    }

    public void updateLastLogin() {
        this.lastLogin = java.time.LocalDateTime.now().toString();
    }

    @Override
    public String toString() {
        return String.format("User{id='%s', name='%s', type='%s'}", id, name, userType);
    }
}
