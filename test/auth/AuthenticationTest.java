import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Authentication System
 *
 * Philosophy: Test REAL authentication scenarios
 * - Valid/invalid credentials
 * - Session management
 * - Security edge cases
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthenticationTest {

    /**
     * Test 1: Valid authentication
     * Real scenario: User logs in with correct credentials
     */
    @Test
    @Order(1)
    public void testValidAuthentication() {
        // Boss account
        ApiAuthenticationHelper.AuthResult result = ApiAuthenticationHelper.authenticate("0001", 1111);

        assertTrue(result.success, "Boss should authenticate successfully");
        assertEquals("0001", result.username);
        assertEquals("Boss", result.userType);
        assertEquals("Login successful", result.message);

        // Employee account
        result = ApiAuthenticationHelper.authenticate("0002", 2222);

        assertTrue(result.success, "Employee should authenticate successfully");
        assertEquals("0002", result.username);
        assertEquals("Employee", result.userType);
    }

    /**
     * Test 2: Invalid credentials
     * Real scenario: User enters wrong password
     */
    @Test
    @Order(2)
    public void testInvalidCredentials() {
        // Wrong password
        ApiAuthenticationHelper.AuthResult result = ApiAuthenticationHelper.authenticate("0001", 9999);

        assertFalse(result.success, "Should fail with wrong password");
        assertNull(result.username);
        assertNull(result.userType);
        assertTrue(result.message.contains("Invalid"), "Error message should mention invalid credentials");

        // Non-existing user
        result = ApiAuthenticationHelper.authenticate("9999", 1111);

        assertFalse(result.success, "Should fail with non-existing user");
    }

    /**
     * Test 3: Session creation
     * Real scenario: Create session after successful login
     */
    @Test
    @Order(3)
    public void testSessionCreation() {
        String sessionId = ApiSessionManager.createSession("0001", "Boss");

        assertNotNull(sessionId, "Should create session ID");
        assertFalse(sessionId.isEmpty(), "Session ID should not be empty");

        // Verify session exists
        ApiSessionManager.SessionData session = ApiSessionManager.validateSession(sessionId);
        assertNotNull(session, "Session should be valid");
        assertEquals("0001", session.username);
        assertEquals("Boss", session.userType);
    }

    /**
     * Test 4: Session validation
     * Real scenario: Check if user is still logged in
     */
    @Test
    @Order(4)
    public void testSessionValidation() {
        // Create valid session
        String sessionId = ApiSessionManager.createSession("0002", "Employee");
        ApiSessionManager.SessionData session = ApiSessionManager.validateSession(sessionId);

        assertNotNull(session, "Valid session should be found");
        assertEquals("0002", session.username);

        // Invalid session
        ApiSessionManager.SessionData invalidSession = ApiSessionManager.validateSession("invalid-session-id");
        assertNull(invalidSession, "Invalid session should return null");

        // Null session
        ApiSessionManager.SessionData nullSession = ApiSessionManager.validateSession(null);
        assertNull(nullSession, "Null session ID should return null");
    }

    /**
     * Test 5: Session deletion
     * Real scenario: User logs out
     */
    @Test
    @Order(5)
    public void testSessionDeletion() {
        // Create session
        String sessionId = ApiSessionManager.createSession("0001", "Boss");
        ApiSessionManager.SessionData session = ApiSessionManager.validateSession(sessionId);
        assertNotNull(session, "Session should exist before deletion");

        // Delete session
        ApiSessionManager.deleteSession(sessionId);

        // Verify session is gone
        session = ApiSessionManager.validateSession(sessionId);
        assertNull(session, "Session should be null after deletion");
    }

    /**
     * Test 6: Multiple sessions
     * Real scenario: Multiple users logged in simultaneously
     */
    @Test
    @Order(6)
    public void testMultipleSessions() {
        // Create multiple sessions
        String session1 = ApiSessionManager.createSession("0001", "Boss");
        String session2 = ApiSessionManager.createSession("0002", "Employee");

        assertNotEquals(session1, session2, "Different users should have different session IDs");

        // Validate both
        ApiSessionManager.SessionData data1 = ApiSessionManager.validateSession(session1);
        ApiSessionManager.SessionData data2 = ApiSessionManager.validateSession(session2);

        assertNotNull(data1);
        assertNotNull(data2);
        assertEquals("0001", data1.username);
        assertEquals("0002", data2.username);

        // Delete one should not affect the other
        ApiSessionManager.deleteSession(session1);

        assertNull(ApiSessionManager.validateSession(session1), "Deleted session should be invalid");
        assertNotNull(ApiSessionManager.validateSession(session2), "Other session should still be valid");
    }

    /**
     * Test 7: Session count
     * Real scenario: Admin wants to know how many users are logged in
     */
    @Test
    @Order(7)
    public void testSessionCount() {
        // Clean up first
        int initialCount = ApiSessionManager.getActiveSessionCount();

        // Create sessions
        String s1 = ApiSessionManager.createSession("0001", "Boss");
        String s2 = ApiSessionManager.createSession("0002", "Employee");

        int countAfterCreation = ApiSessionManager.getActiveSessionCount();
        assertEquals(initialCount + 2, countAfterCreation, "Should have 2 more sessions");

        // Delete one
        ApiSessionManager.deleteSession(s1);

        int countAfterDeletion = ApiSessionManager.getActiveSessionCount();
        assertEquals(initialCount + 1, countAfterDeletion, "Should have 1 less session");
    }

    /**
     * Test 8: Edge cases
     * Real scenario: Handle unexpected inputs gracefully
     */
    @Test
    @Order(8)
    public void testEdgeCases() {
        // Null username
        ApiAuthenticationHelper.AuthResult result = ApiAuthenticationHelper.authenticate(null, 1111);
        assertFalse(result.success, "Null username should fail");

        // Empty username
        result = ApiAuthenticationHelper.authenticate("", 1111);
        assertFalse(result.success, "Empty username should fail");

        // Delete non-existing session (should not crash)
        assertDoesNotThrow(() -> {
            ApiSessionManager.deleteSession("non-existing-session");
        }, "Deleting non-existing session should not throw exception");

        // Validate empty string session
        ApiSessionManager.SessionData session = ApiSessionManager.validateSession("");
        assertNull(session, "Empty session ID should return null");
    }

    /**
     * Test 9: Same user multiple logins
     * Real scenario: User logs in from different devices
     */
    @Test
    @Order(9)
    public void testSameUserMultipleLogins() {
        // Same user logs in twice
        String session1 = ApiSessionManager.createSession("0001", "Boss");
        String session2 = ApiSessionManager.createSession("0001", "Boss");

        // Should create different sessions
        assertNotEquals(session1, session2, "Each login should get unique session ID");

        // Both should be valid
        assertNotNull(ApiSessionManager.validateSession(session1));
        assertNotNull(ApiSessionManager.validateSession(session2));

        // Both should have same user data
        ApiSessionManager.SessionData data1 = ApiSessionManager.validateSession(session1);
        ApiSessionManager.SessionData data2 = ApiSessionManager.validateSession(session2);

        assertEquals(data1.username, data2.username);
        assertEquals(data1.userType, data2.userType);
    }

    /**
     * Test 10: Complete authentication flow
     * Real scenario: Full login-use-logout cycle
     */
    @Test
    @Order(10)
    public void testCompleteAuthFlow() {
        // Step 1: Authenticate
        ApiAuthenticationHelper.AuthResult authResult = ApiAuthenticationHelper.authenticate("0001", 1111);
        assertTrue(authResult.success, "Authentication should succeed");

        // Step 2: Create session
        String sessionId = ApiSessionManager.createSession(authResult.username, authResult.userType);
        assertNotNull(sessionId);

        // Step 3: Validate session (simulate API request)
        ApiSessionManager.SessionData session = ApiSessionManager.validateSession(sessionId);
        assertNotNull(session);
        assertEquals("0001", session.username);
        assertEquals("Boss", session.userType);

        // Step 4: Use session multiple times
        for (int i = 0; i < 5; i++) {
            ApiSessionManager.SessionData check = ApiSessionManager.validateSession(sessionId);
            assertNotNull(check, "Session should remain valid across multiple checks");
        }

        // Step 5: Logout
        ApiSessionManager.deleteSession(sessionId);

        // Step 6: Verify session is invalid
        ApiSessionManager.SessionData afterLogout = ApiSessionManager.validateSession(sessionId);
        assertNull(afterLogout, "Session should be invalid after logout");
    }
}
