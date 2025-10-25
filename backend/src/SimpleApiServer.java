import static spark.Spark.*;
import com.google.gson.Gson;

/**
 * Simple API Server using Spark Java
 * Stage 1: Learn basic HTTP server setup
 *
 * This is MUCH simpler than Javalin - perfect for learning!
 */
public class SimpleApiServer {

    private static Gson gson = new Gson();

    public static void main(String[] args) {
        // Set port
        port(7070);

        System.out.println("=================================");
        System.out.println("Server starting...");
        System.out.println("URL: http://localhost:7070");
        System.out.println("=================================");

        // Route 1: Simple text response
        get("/api/hello", (req, res) -> {
            res.type("text/plain");
            return "Hello from Library API!";
        });

        // Route 2: JSON response
        get("/api/status", (req, res) -> {
            res.type("application/json");
            StatusResponse status = new StatusResponse(true, "Server is running");
            return gson.toJson(status);
        });

        // Route 3: Test endpoint with parameter
        get("/api/echo/:message", (req, res) -> {
            res.type("application/json");
            String message = req.params(":message");
            EchoResponse echo = new EchoResponse(message);
            return gson.toJson(echo);
        });

        System.out.println("\n✅ Server started successfully!");
        System.out.println("\nTest with curl:");
        System.out.println("  curl http://localhost:7070/api/hello");
        System.out.println("  curl http://localhost:7070/api/status");
        System.out.println("  curl http://localhost:7070/api/echo/test");
        System.out.println();
    }

    // Simple response classes
    static class StatusResponse {
        public boolean success;
        public String message;

        public StatusResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    static class EchoResponse {
        public String echo;

        public EchoResponse(String message) {
            this.echo = message;
        }
    }
}
