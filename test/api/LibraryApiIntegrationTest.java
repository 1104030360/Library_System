import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Integration Test for Library API Server
 *
 * Philosophy: Test the REAL API endpoints that users will call
 * - Start actual server
 * - Make real HTTP requests
 * - Verify real responses
 * - No mocking - test the whole stack
 *
 * NOTE: Server must be running at http://localhost:7070
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LibraryApiIntegrationTest {

    private static final String BASE_URL = "http://localhost:7070";
    private static final Gson gson = new Gson();
    private String sessionCookie = null;

    /**
     * Test 1: Server is running
     * Real scenario: Check if API is accessible
     */
    @Test
    @Order(1)
    public void testServerIsRunning() throws IOException {
        HttpURLConnection conn = makeRequest("GET", "/api/status", null, null);
        int responseCode = conn.getResponseCode();

        assertEquals(200, responseCode, "Server should be running and respond with 200");

        String response = readResponse(conn);
        assertTrue(response.contains("success"), "Response should contain success field");
        assertTrue(response.contains("Server is running"), "Should confirm server is running");
    }

    /**
     * Test 2: Get all books
     * Real scenario: User views book list
     */
    @Test
    @Order(2)
    public void testGetAllBooks() throws IOException {
        HttpURLConnection conn = makeRequest("GET", "/api/books", null, null);
        assertEquals(200, conn.getResponseCode());

        String response = readResponse(conn);
        JsonObject json = gson.fromJson(response, JsonObject.class);

        assertTrue(json.get("success").getAsBoolean());
        assertTrue(json.has("books"));

        JsonArray books = json.getAsJsonArray("books");
        assertTrue(books.size() >= 5, "Should have at least 5 default books");
    }

    /**
     * Test 3: Get book by ID
     * Real scenario: User clicks on a book to see details
     */
    @Test
    @Order(3)
    public void testGetBookById() throws IOException {
        // Get existing book
        HttpURLConnection conn = makeRequest("GET", "/api/books?id=001", null, null);
        assertEquals(200, conn.getResponseCode());

        String response = readResponse(conn);
        JsonObject json = gson.fromJson(response, JsonObject.class);

        assertTrue(json.get("success").getAsBoolean());
        assertTrue(json.has("book"));

        JsonObject book = json.getAsJsonObject("book");
        assertEquals("001", book.get("id").getAsString());
        assertEquals("Java", book.get("title").getAsString());

        // Get non-existing book
        conn = makeRequest("GET", "/api/books?id=999", null, null);
        assertEquals(404, conn.getResponseCode());
    }

    /**
     * Test 4: Get statistics
     * Real scenario: Admin views library stats
     */
    @Test
    @Order(4)
    public void testGetStatistics() throws IOException {
        HttpURLConnection conn = makeRequest("GET", "/api/stats", null, null);
        assertEquals(200, conn.getResponseCode());

        String response = readResponse(conn);
        JsonObject json = gson.fromJson(response, JsonObject.class);

        assertTrue(json.get("success").getAsBoolean());
        String stats = json.get("statistics").getAsString();

        assertTrue(stats.contains("Total:"), "Stats should show total books");
        assertTrue(stats.contains("Available:"), "Stats should show available books");
        assertTrue(stats.contains("Borrowed:"), "Stats should show borrowed books");
    }

    /**
     * Test 5: Login flow
     * Real scenario: User logs in to system
     */
    @Test
    @Order(5)
    public void testLoginFlow() throws IOException {
        // Valid login
        String loginData = "{\"username\":\"0001\",\"password\":1111}";
        HttpURLConnection conn = makeRequest("POST", "/api/auth/login", loginData, null);
        assertEquals(200, conn.getResponseCode());

        String response = readResponse(conn);
        JsonObject json = gson.fromJson(response, JsonObject.class);

        assertTrue(json.get("success").getAsBoolean());
        assertEquals("0001", json.get("username").getAsString());
        assertEquals("Boss", json.get("userType").getAsString());
        assertTrue(json.has("sessionId"));

        // Extract session cookie for later tests
        Map<String, List<String>> headers = conn.getHeaderFields();
        List<String> cookies = headers.get("Set-Cookie");
        assertNotNull(cookies, "Should receive session cookie");

        for (String cookie : cookies) {
            if (cookie.startsWith("sessionId=")) {
                sessionCookie = cookie.split(";")[0]; // Extract just sessionId=xxx
                break;
            }
        }
        assertNotNull(sessionCookie, "Should extract session ID from cookie");

        // Invalid login
        String badLogin = "{\"username\":\"0001\",\"password\":9999}";
        conn = makeRequest("POST", "/api/auth/login", badLogin, null);
        assertEquals(401, conn.getResponseCode());
    }

    /**
     * Test 6: WhoAmI - Check current user
     * Real scenario: App checks if user is logged in
     */
    @Test
    @Order(6)
    public void testWhoAmI() throws IOException {
        // Without login - should fail
        HttpURLConnection conn = makeRequest("GET", "/api/auth/whoami", null, null);
        assertEquals(401, conn.getResponseCode());

        // With login - should work
        conn = makeRequest("GET", "/api/auth/whoami", null, sessionCookie);
        assertEquals(200, conn.getResponseCode());

        String response = readResponse(conn);
        JsonObject json = gson.fromJson(response, JsonObject.class);

        assertTrue(json.get("success").getAsBoolean());
        assertEquals("0001", json.get("username").getAsString());
        assertEquals("Boss", json.get("userType").getAsString());
    }

    /**
     * Test 7: Borrow book
     * Real scenario: User borrows a book
     */
    @Test
    @Order(7)
    public void testBorrowBook() throws IOException {
        // Try borrowing without login - should fail
        String borrowData = "{\"bookId\":\"001\"}";
        HttpURLConnection conn = makeRequest("POST", "/api/books/borrow", borrowData, null);
        assertEquals(401, conn.getResponseCode());

        // Borrow with login - should work
        conn = makeRequest("POST", "/api/books/borrow", borrowData, sessionCookie);
        assertEquals(200, conn.getResponseCode());

        String response = readResponse(conn);
        JsonObject json = gson.fromJson(response, JsonObject.class);

        assertTrue(json.get("success").getAsBoolean());
        assertTrue(json.get("message").getAsString().contains("Successfully borrowed"));

        JsonObject book = json.getAsJsonObject("book");
        assertEquals("001", book.get("id").getAsString());
        assertFalse(book.get("available").getAsBoolean(), "Book should be marked as unavailable");

        // Try borrowing same book again - should fail
        conn = makeRequest("POST", "/api/books/borrow", borrowData, sessionCookie);
        assertEquals(400, conn.getResponseCode());

        response = readResponse(conn);
        json = gson.fromJson(response, JsonObject.class);
        assertTrue(json.get("error").getAsString().contains("already borrowed"));
    }

    /**
     * Test 8: Return book
     * Real scenario: User returns a borrowed book
     */
    @Test
    @Order(8)
    public void testReturnBook() throws IOException {
        // Try returning without login - should fail
        String returnData = "{\"bookId\":\"001\"}";
        HttpURLConnection conn = makeRequest("POST", "/api/books/return", returnData, null);
        assertEquals(401, conn.getResponseCode());

        // Try returning with a different logged-in user - should fail (permission denied)
        String otherSession = loginAndGetSession("1001", "1234");
        conn = makeRequest("POST", "/api/books/return", returnData, otherSession);
        assertEquals(403, conn.getResponseCode(), "Different user should not be allowed to return someone else's book");

        // Cleanup: logout secondary session
        makeRequest("POST", "/api/auth/logout", null, otherSession);

        // Return with login - should work
        conn = makeRequest("POST", "/api/books/return", returnData, sessionCookie);
        assertEquals(200, conn.getResponseCode());

        String response = readResponse(conn);
        JsonObject json = gson.fromJson(response, JsonObject.class);

        assertTrue(json.get("success").getAsBoolean());
        assertTrue(json.get("message").getAsString().contains("Successfully returned"));

        JsonObject book = json.getAsJsonObject("book");
        assertEquals("001", book.get("id").getAsString());
        assertTrue(book.get("available").getAsBoolean(), "Book should be available again");

        // Try returning again - should fail
        conn = makeRequest("POST", "/api/books/return", returnData, sessionCookie);
        assertEquals(400, conn.getResponseCode());
    }

    /**
     * Test 9: Logout
     * Real scenario: User logs out
     */
    @Test
    @Order(9)
    public void testLogout() throws IOException {
        HttpURLConnection conn = makeRequest("POST", "/api/auth/logout", null, sessionCookie);
        assertEquals(200, conn.getResponseCode());

        String response = readResponse(conn);
        JsonObject json = gson.fromJson(response, JsonObject.class);

        assertTrue(json.get("success").getAsBoolean());
        assertEquals("Logout successful", json.get("message").getAsString());

        // After logout, whoami should fail
        conn = makeRequest("GET", "/api/auth/whoami", null, sessionCookie);
        assertEquals(401, conn.getResponseCode());
    }

    /**
     * Test 10: Invalid requests handling
     * Real scenario: Handle malformed requests gracefully
     */
    @Test
    @Order(10)
    public void testInvalidRequests() throws IOException {
        // Login first
        String loginData = "{\"username\":\"0001\",\"password\":1111}";
        HttpURLConnection loginConn = makeRequest("POST", "/api/auth/login", loginData, null);
        Map<String, List<String>> headers = loginConn.getHeaderFields();
        List<String> cookies = headers.get("Set-Cookie");
        String session = cookies.get(0).split(";")[0];

        // Invalid book ID
        String invalidBorrow = "{\"bookId\":\"999\"}";
        HttpURLConnection conn = makeRequest("POST", "/api/books/borrow", invalidBorrow, session);
        assertEquals(404, conn.getResponseCode());

        // Missing book ID
        String missingId = "{}";
        conn = makeRequest("POST", "/api/books/borrow", missingId, session);
        assertEquals(400, conn.getResponseCode());

        // Wrong HTTP method
        conn = makeRequest("GET", "/api/books/borrow", null, session);
        assertEquals(405, conn.getResponseCode());
    }

    // ===== Helper Methods =====

    /**
     * Make HTTP request to API
     */
    private HttpURLConnection makeRequest(String method, String endpoint, String body, String cookie) throws IOException {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json");

        if (cookie != null) {
            conn.setRequestProperty("Cookie", cookie);
        }

        if (body != null) {
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes());
                os.flush();
            }
        }

        return conn;
    }

    /**
     * Read response body from connection
     */
    private String readResponse(HttpURLConnection conn) throws IOException {
        InputStream is;
        try {
            is = conn.getInputStream();
        } catch (IOException e) {
            is = conn.getErrorStream();
        }

        if (is == null) {
            return "";
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        return response.toString();
        }
    }

    /**
     * Login helper that returns session cookie string
     */
    private String loginAndGetSession(String username, String password) throws IOException {
        String payload = String.format("{\"username\":\"%s\",\"password\":%s}", username, password);
        HttpURLConnection conn = makeRequest("POST", "/api/auth/login", payload, null);
        assertEquals(200, conn.getResponseCode(), "Login should succeed for helper");

        Map<String, List<String>> headers = conn.getHeaderFields();
        List<String> cookies = headers.get("Set-Cookie");
        assertNotNull(cookies, "Login response should contain Set-Cookie header");

        for (String cookie : cookies) {
            if (cookie.startsWith("sessionId=")) {
                return cookie.split(";")[0];
            }
        }

        fail("No sessionId cookie returned after login");
        return null; // Unreachable
    }
}
