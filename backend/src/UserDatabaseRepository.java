import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * User Database Repository
 * Manages user data with SQLite database persistence
 *
 * Following Linus principles:
 * - Single responsibility: Only handles user data access
 * - No special cases: All users (admin/staff/regular) in same table
 * - Simple and robust: Direct SQL, no ORM complexity
 */
public class UserDatabaseRepository {

    private final String dbUrl;

    /**
     * Constructor - uses same database as books
     */
    public UserDatabaseRepository(String dbPath) {
        this.dbUrl = "jdbc:sqlite:" + dbPath;
    }

    /**
     * Initialize users table and insert default admin accounts
     */
    public void initialize() {
        try (Connection conn = getConnection()) {
            // Create users table
            String createTableSQL = """
                CREATE TABLE IF NOT EXISTS users (
                    id TEXT PRIMARY KEY,
                    name TEXT NOT NULL,
                    password_hash TEXT NOT NULL,
                    email TEXT,
                    user_type TEXT NOT NULL DEFAULT 'user',
                    created_at TEXT NOT NULL,
                    last_login TEXT
                )
                """;

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createTableSQL);
            }

            // Insert default admin accounts if table is empty
            if (getUserCount() == 0) {
                insertDefaultAdmins();
            }

            System.out.println("✅ Users table initialized successfully");
        } catch (SQLException e) {
            System.err.println("Error initializing users table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get database connection
     */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }

    /**
     * Get total user count
     */
    private int getUserCount() {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting users: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Insert default admin accounts (migrated from hardcoded Admin.java)
     * Never break userspace - preserve existing admin accounts
     */
    private void insertDefaultAdmins() {
        System.out.println("📝 Inserting default admin accounts...");

        String sql = "INSERT INTO users (id, name, password_hash, email, user_type, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, datetime('now'))";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Default admin accounts (from original Admin.java)
            String[][] admins = {
                {"0001", "館長", "1111", "館長"},
                {"0002", "館員", "2222", "館員"},
                {"0003", "館員", "3333", "館員"},
                {"0004", "館員", "4444", "館員"},
                {"0005", "館員", "5555", "館員"},
                {"0006", "館員", "6666", "館員"}
            };

            for (String[] admin : admins) {
                pstmt.setString(1, admin[0]);
                pstmt.setString(2, admin[1]);
                pstmt.setString(3, hashPassword(admin[2]));
                pstmt.setString(4, null);  // No email for default admins
                pstmt.setString(5, admin[3]);
                pstmt.executeUpdate();
            }

            System.out.println("✅ Inserted " + admins.length + " default admin accounts");

            // Insert test users for development
            String[][] testUsers = {
                {"1001", "王小明", "1234", "wang@example.com", "user"},
                {"1002", "李小華", "1234", "lee@example.com", "user"}
            };

            for (String[] user : testUsers) {
                pstmt.setString(1, user[0]);
                pstmt.setString(2, user[1]);
                pstmt.setString(3, hashPassword(user[2]));
                pstmt.setString(4, user[3]);
                pstmt.setString(5, user[4]);
                pstmt.executeUpdate();
            }

            System.out.println("✅ Inserted " + testUsers.length + " test user accounts");

        } catch (SQLException e) {
            System.err.println("Error inserting default accounts: " + e.getMessage());
        }
    }

    /**
     * Hash password using SHA-256
     * Simple and sufficient for this use case
     */
    public String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Register new user
     * Returns true if successful, false if user ID already exists
     */
    public boolean registerUser(String id, String name, String password, String email) {
        // Check if user ID already exists
        if (findById(id) != null) {
            return false;
        }

        String sql = "INSERT INTO users (id, name, password_hash, email, user_type, created_at) " +
                     "VALUES (?, ?, ?, ?, 'user', datetime('now'))";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id.trim());
            pstmt.setString(2, name.trim());
            pstmt.setString(3, hashPassword(password));
            pstmt.setString(4, email != null && !email.trim().isEmpty() ? email.trim() : null);

            pstmt.executeUpdate();
            System.out.println("✅ User registered: " + id + " (" + name + ")");
            return true;

        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Authenticate user with username and password
     * Returns User object if successful, null if failed
     */
    public User authenticate(String id, String password) {
        User user = findById(id);

        if (user == null) {
            return null;
        }

        // Verify password
        String hashedPassword = hashPassword(password);
        if (!hashedPassword.equals(user.getPasswordHash())) {
            return null;
        }

        // Update last login time
        updateLastLogin(id);
        user.updateLastLogin();

        return user;
    }

    /**
     * Find user by ID
     */
    public User findById(String id) {
        String sql = "SELECT id, name, password_hash, email, user_type, created_at, last_login " +
                     "FROM users WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new User(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("password_hash"),
                    rs.getString("email"),
                    rs.getString("user_type"),
                    rs.getString("created_at"),
                    rs.getString("last_login")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by ID: " + e.getMessage());
        }

        return null;
    }

    /**
     * Update user's last login timestamp
     */
    private void updateLastLogin(String id) {
        String sql = "UPDATE users SET last_login = datetime('now') WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error updating last login: " + e.getMessage());
        }
    }

    /**
     * Check if user exists
     */
    public boolean userExists(String id) {
        return findById(id) != null;
    }

    /**
     * Get user profile (without password hash)
     */
    public User getUserProfile(String id) {
        return findById(id);
    }

    /**
     * Get all users
     */
    public java.util.List<User> getAllUsers() {
        String sql = "SELECT id, name, password_hash, email, user_type, created_at, last_login FROM users";
        java.util.List<User> users = new java.util.ArrayList<>();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(new User(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("password_hash"),
                    rs.getString("email"),
                    rs.getString("user_type"),
                    rs.getString("created_at"),
                    rs.getString("last_login")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Error getting all users: " + e.getMessage());
        }

        return users;
    }

    /**
     * Get users by role
     */
    public java.util.List<User> getUsersByRole(String role) {
        String sql = "SELECT id, name, password_hash, email, user_type, created_at, last_login " +
                     "FROM users WHERE user_type = ?";
        java.util.List<User> users = new java.util.ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, role);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                users.add(new User(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("password_hash"),
                    rs.getString("email"),
                    rs.getString("user_type"),
                    rs.getString("created_at"),
                    rs.getString("last_login")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Error getting users by role: " + e.getMessage());
        }

        return users;
    }

    /**
     * Delete user by ID
     * @param userId User ID to delete
     * @return true if deletion successful, false otherwise
     */
    public boolean deleteUser(String userId) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("✅ User deleted successfully: " + userId);
                return true;
            } else {
                System.out.println("⚠️ User not found: " + userId);
                return false;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error deleting user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Create staff member (館員)
     * @param id Staff ID
     * @param name Staff name
     * @param password Staff password
     * @param email Staff email
     * @return true if creation successful, false otherwise
     */
    public boolean createStaff(String id, String name, String password, String email) {
        if (userExists(id)) {
            System.out.println("⚠️ User ID already exists: " + id);
            return false;
        }

        String sql = "INSERT INTO users (id, name, password_hash, email, user_type, created_at) " +
                     "VALUES (?, ?, ?, ?, '館員', datetime('now'))";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            pstmt.setString(2, name);
            pstmt.setString(3, hashPassword(password));
            pstmt.setString(4, email != null ? email : "");

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("✅ Staff created successfully: " + id + " (" + name + ")");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error creating staff: " + e.getMessage());
        }

        return false;
    }

    /**
     * Get count of users created yesterday
     * @return number of users created yesterday
     */
    public int getYesterdayUserCount() {
        String sql = "SELECT COUNT(*) as count FROM users " +
                     "WHERE user_type = 'user' " +
                     "AND strftime('%Y-%m-%d', created_at) = strftime('%Y-%m-%d', date('now', '-1 day'))";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("count");
            }

        } catch (SQLException e) {
            System.err.println("Error getting yesterday user count: " + e.getMessage());
        }

        return 0;
    }
}
