import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * API Session Manager - Stage 4
 * Manages user sessions for API authentication
 * Uses in-memory session storage with UUID tokens
 */
public class ApiSessionManager {

    // Session storage: sessionId -> SessionData
    private static Map<String, SessionData> sessions = new ConcurrentHashMap<>();

    // Session timeout: 30 minutes (in milliseconds)
    private static final long SESSION_TIMEOUT = 30 * 60 * 1000;

    /**
     * SessionData - Stores user session information
     */
    public static class SessionData {
        public String username;
        public String userType;  // "館長" or "員工"
        public long createdAt;
        public long lastAccessedAt;

        public SessionData(String username, String userType) {
            this.username = username;
            this.userType = userType;
            this.createdAt = System.currentTimeMillis();
            this.lastAccessedAt = System.currentTimeMillis();
        }

        public boolean isExpired() {
            long now = System.currentTimeMillis();
            return (now - lastAccessedAt) > SESSION_TIMEOUT;
        }

        public void updateLastAccessed() {
            this.lastAccessedAt = System.currentTimeMillis();
        }
    }

    /**
     * Create a new session for a user
     * @return Session ID (UUID)
     */
    public static String createSession(String username, String userType) {
        String sessionId = UUID.randomUUID().toString();
        SessionData session = new SessionData(username, userType);
        sessions.put(sessionId, session);

        System.out.println("✅ Created session for " + username + " (" + userType + ")");
        return sessionId;
    }

    /**
     * Validate session and return session data
     * @param sessionId Session ID from cookie
     * @return SessionData if valid, null otherwise
     */
    public static SessionData validateSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return null;
        }

        SessionData session = sessions.get(sessionId);
        if (session == null) {
            return null;
        }

        // Check if expired
        if (session.isExpired()) {
            sessions.remove(sessionId);
            System.out.println("⏰ Session expired for " + session.username);
            return null;
        }

        // Update last accessed time
        session.updateLastAccessed();
        return session;
    }

    /**
     * Delete a session (logout)
     */
    public static boolean deleteSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return false;
        }

        SessionData session = sessions.remove(sessionId);
        if (session != null) {
            System.out.println("✅ Deleted session for " + session.username);
            return true;
        }
        return false;
    }

    /**
     * Get active session count
     */
    public static int getActiveSessionCount() {
        // Clean up expired sessions first
        sessions.entrySet().removeIf(entry -> entry.getValue().isExpired());
        return sessions.size();
    }

    /**
     * Clear all sessions (for testing)
     */
    public static void clearAllSessions() {
        sessions.clear();
        System.out.println("🗑️  Cleared all sessions");
    }
}
