import io.javalin.Javalin;

/**
 * Hello World API Server
 * Stage 1: Learn basic HTTP server setup
 */
public class HelloApiServer {

    public static void main(String[] args) {
        // Create and start Javalin server on port 7070
        Javalin app = Javalin.create().start(7070);

        System.out.println("=================================");
        System.out.println("Server started successfully!");
        System.out.println("URL: http://localhost:7070");
        System.out.println("=================================");

        // Route 1: Simple hello endpoint
        app.get("/api/hello", ctx -> {
            ctx.result("Hello from Library API!");
        });

        // Route 2: JSON response
        app.get("/api/status", ctx -> {
            ctx.json(new StatusResponse(true, "Server is running"));
        });
    }

    /**
     * Simple response class for JSON
     */
    static class StatusResponse {
        public boolean success;
        public String message;

        public StatusResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}
