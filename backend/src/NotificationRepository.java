import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Notification Repository
 * Handles all database operations for notifications
 */
public class NotificationRepository {

    private final String dbUrl;

    public NotificationRepository(String dbUrl) {
        this.dbUrl = dbUrl;
        initDatabase();
    }

    /**
     * Initialize database tables and indexes
     */
    private void initDatabase() {
        String createNotificationsTable = """
            CREATE TABLE IF NOT EXISTS notifications (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id TEXT NOT NULL,
                type TEXT NOT NULL,
                title TEXT NOT NULL,
                message TEXT NOT NULL,
                link TEXT,
                read INTEGER DEFAULT 0,
                deleted INTEGER DEFAULT 0,
                created_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
                read_at TEXT,
                FOREIGN KEY (user_id) REFERENCES users(user_id)
            )
        """;

        String createIndex1 = """
            CREATE INDEX IF NOT EXISTS idx_notifications_user
            ON notifications(user_id, deleted, read, created_at DESC)
        """;

        String createIndex2 = """
            CREATE INDEX IF NOT EXISTS idx_notifications_type
            ON notifications(type, created_at DESC)
        """;

        String createReminderLogsTable = """
            CREATE TABLE IF NOT EXISTS due_reminder_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                borrow_history_id INTEGER NOT NULL,
                user_id TEXT NOT NULL,
                book_id TEXT NOT NULL,
                reminder_date TEXT NOT NULL,
                due_date TEXT NOT NULL,
                created_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
                UNIQUE(borrow_history_id, reminder_date),
                FOREIGN KEY (borrow_history_id) REFERENCES borrow_history(history_id)
            )
        """;

        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement()) {

            stmt.execute(createNotificationsTable);
            stmt.execute(createIndex1);
            stmt.execute(createIndex2);
            stmt.execute(createReminderLogsTable);

            System.out.println("Notification tables initialized successfully");

        } catch (SQLException e) {
            System.err.println("Failed to initialize notification database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Create a new notification
     */
    public boolean createNotification(Notification notification) {
        String sql = """
            INSERT INTO notifications (user_id, type, title, message, link)
            VALUES (?, ?, ?, ?, ?)
        """;

        System.out.println("🔵 NotificationRepository.createNotification called");
        System.out.println("   userId: " + notification.getUserId());
        System.out.println("   type: " + notification.getType());
        System.out.println("   title: " + notification.getTitle());
        System.out.println("   message: " + notification.getMessage());
        System.out.println("   link: " + notification.getLink());

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, notification.getUserId());
            pstmt.setString(2, notification.getType());
            pstmt.setString(3, notification.getTitle());
            pstmt.setString(4, notification.getMessage());
            pstmt.setString(5, notification.getLink());

            int rowsAffected = pstmt.executeUpdate();
            boolean success = rowsAffected > 0;

            if (success) {
                System.out.println("✅ Notification created successfully, rows affected: " + rowsAffected);
            } else {
                System.err.println("❌ Notification creation returned 0 rows affected");
            }

            return success;

        } catch (SQLException e) {
            System.err.println("❌ Failed to create notification: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Batch create notifications
     */
    public int batchCreateNotifications(List<Notification> notifications) {
        String sql = """
            INSERT INTO notifications (user_id, type, title, message, link)
            VALUES (?, ?, ?, ?, ?)
        """;

        int count = 0;
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DriverManager.getConnection(dbUrl);
            conn.setAutoCommit(false);
            pstmt = conn.prepareStatement(sql);

            for (Notification notification : notifications) {
                pstmt.setString(1, notification.getUserId());
                pstmt.setString(2, notification.getType());
                pstmt.setString(3, notification.getTitle());
                pstmt.setString(4, notification.getMessage());
                pstmt.setString(5, notification.getLink());
                pstmt.addBatch();
            }

            int[] results = pstmt.executeBatch();
            conn.commit();

            for (int result : results) {
                if (result > 0) count++;
            }

        } catch (SQLException e) {
            System.err.println("Failed to batch create notifications: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return count;
    }

    /**
     * Get user notifications with filters
     */
    public List<Notification> getUserNotifications(
            String userId,
            boolean unreadOnly,
            String type,
            int limit,
            int offset) {

        StringBuilder sql = new StringBuilder("""
            SELECT * FROM notifications
            WHERE user_id = ? AND deleted = 0
        """);

        if (unreadOnly) {
            sql.append(" AND read = 0");
        }

        if (type != null && !type.isEmpty()) {
            sql.append(" AND type = ?");
        }

        sql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");

        List<Notification> notifications = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            pstmt.setString(paramIndex++, userId);

            if (type != null && !type.isEmpty()) {
                pstmt.setString(paramIndex++, type);
            }

            pstmt.setInt(paramIndex++, limit);
            pstmt.setInt(paramIndex, offset);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                notifications.add(mapResultSetToNotification(rs));
            }

        } catch (SQLException e) {
            System.err.println("Failed to get notifications: " + e.getMessage());
        }

        return notifications;
    }

    /**
     * Get total count of notifications
     */
    public int getTotalCount(String userId, boolean unreadOnly, String type) {
        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(*) FROM notifications
            WHERE user_id = ? AND deleted = 0
        """);

        if (unreadOnly) {
            sql.append(" AND read = 0");
        }

        if (type != null && !type.isEmpty()) {
            sql.append(" AND type = ?");
        }

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            pstmt.setString(paramIndex++, userId);

            if (type != null && !type.isEmpty()) {
                pstmt.setString(paramIndex, type);
            }

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Failed to get total count: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Get unread count
     */
    public int getUnreadCount(String userId) {
        String sql = """
            SELECT COUNT(*) FROM notifications
            WHERE user_id = ? AND read = 0 AND deleted = 0
        """;

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Failed to get unread count: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Mark notification as read
     */
    public boolean markAsRead(int notificationId, String userId) {
        String sql = """
            UPDATE notifications
            SET read = 1, read_at = datetime('now', 'localtime')
            WHERE id = ? AND user_id = ?
        """;

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, notificationId);
            pstmt.setString(2, userId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Failed to mark as read: " + e.getMessage());
            return false;
        }
    }

    /**
     * Mark all notifications as read
     */
    public int markAllAsRead(String userId) {
        String sql = """
            UPDATE notifications
            SET read = 1, read_at = datetime('now', 'localtime')
            WHERE user_id = ? AND read = 0 AND deleted = 0
        """;

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            return pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Failed to mark all as read: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Clear (soft delete) all notifications
     */
    public int clearNotifications(String userId) {
        String sql = """
            UPDATE notifications
            SET deleted = 1
            WHERE user_id = ?
        """;

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            return pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Failed to clear notifications: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Check if reminder has been sent
     */
    public boolean hasReminderSent(int borrowHistoryId, String reminderDate) {
        String sql = """
            SELECT COUNT(*) FROM due_reminder_logs
            WHERE borrow_history_id = ? AND reminder_date = ?
        """;

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, borrowHistoryId);
            pstmt.setString(2, reminderDate);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Failed to check reminder: " + e.getMessage());
        }

        return false;
    }

    /**
     * Log due reminder
     */
    public boolean logDueReminder(int borrowHistoryId, String userId,
                                 String bookId, String reminderDate, String dueDate) {
        String sql = """
            INSERT INTO due_reminder_logs
            (borrow_history_id, user_id, book_id, reminder_date, due_date)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, borrowHistoryId);
            pstmt.setString(2, userId);
            pstmt.setString(3, bookId);
            pstmt.setString(4, reminderDate);
            pstmt.setString(5, dueDate);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            // Ignore duplicate entry errors (UNIQUE constraint)
            if (!e.getMessage().contains("UNIQUE constraint failed")) {
                System.err.println("Failed to log reminder: " + e.getMessage());
            }
            return false;
        }
    }

    /**
     * Map ResultSet to Notification object
     */
    private Notification mapResultSetToNotification(ResultSet rs) throws SQLException {
        Notification notification = new Notification();
        notification.setId(rs.getInt("id"));
        notification.setUserId(rs.getString("user_id"));
        notification.setType(rs.getString("type"));
        notification.setTitle(rs.getString("title"));
        notification.setMessage(rs.getString("message"));
        notification.setLink(rs.getString("link"));
        notification.setRead(rs.getInt("read") == 1);
        notification.setDeleted(rs.getInt("deleted") == 1);
        notification.setCreatedAt(rs.getString("created_at"));
        notification.setReadAt(rs.getString("read_at"));
        return notification;
    }
}
