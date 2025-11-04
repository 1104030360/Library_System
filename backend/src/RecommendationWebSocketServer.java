import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket Server for Real-time Recommendation Delivery
 * Listens on port 7071 and pushes recommendation results to connected clients
 */
public class RecommendationWebSocketServer extends WebSocketServer {

    private final TaskManager taskManager;
    private final Gson gson;
    // Map taskId -> WebSocket connection
    private final Map<String, WebSocket> taskConnections;

    public RecommendationWebSocketServer(int port, TaskManager taskManager) {
        super(new InetSocketAddress(port));
        this.taskManager = taskManager;
        this.gson = new Gson();
        this.taskConnections = new ConcurrentHashMap<>();
        System.out.println("✓ WebSocket server created on port " + port);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("→ New WebSocket connection from: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        // Clean up task connections
        taskConnections.entrySet().removeIf(entry -> entry.getValue() == conn);
        System.out.println("→ WebSocket connection closed: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            // Client sends: { "action": "subscribe", "taskId": "xxx" }
            JsonObject json = gson.fromJson(message, JsonObject.class);
            String action = json.get("action").getAsString();

            if ("subscribe".equals(action)) {
                String taskId = json.get("taskId").getAsString();

                // Register connection for this task
                taskConnections.put(taskId, conn);
                System.out.println("→ Client subscribed to task: " + taskId);

                // Check if task is already completed
                RecommendationTask task = taskManager.getTask(taskId);
                if (task != null && task.getStatus() != RecommendationTask.Status.PROCESSING) {
                    // Send result immediately
                    sendTaskResult(conn, task);
                }
            }
        } catch (Exception e) {
            System.err.println("Error handling WebSocket message: " + e.getMessage());
            sendError(conn, "Invalid message format");
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("WebSocket error: " + ex.getMessage());
        if (conn != null) {
            // Clean up on error
            taskConnections.entrySet().removeIf(entry -> entry.getValue() == conn);
        }
    }

    @Override
    public void onStart() {
        System.out.println("✓ WebSocket server started successfully");
        setConnectionLostTimeout(30);
    }

    /**
     * Notify client when task is completed
     * Called by LibraryApiServer after AI processing
     */
    public void notifyTaskComplete(String taskId) {
        WebSocket conn = taskConnections.get(taskId);
        if (conn != null && conn.isOpen()) {
            RecommendationTask task = taskManager.getTask(taskId);
            if (task != null) {
                sendTaskResult(conn, task);
                taskConnections.remove(taskId);
            }
        }
    }

    /**
     * Send task result to client
     */
    private void sendTaskResult(WebSocket conn, RecommendationTask task) {
        try {
            JsonObject response = new JsonObject();
            response.addProperty("taskId", task.getTaskId());
            response.addProperty("status", task.getStatus().toString().toLowerCase());

            if (task.getStatus() == RecommendationTask.Status.COMPLETED) {
                // Convert recommendations to JSON
                String recommendationsJson = gson.toJson(task.getResult());
                response.addProperty("recommendations", recommendationsJson);
                System.out.println("✓ Sent recommendations for task: " + task.getTaskId());
            } else if (task.getStatus() == RecommendationTask.Status.FAILED) {
                response.addProperty("error", task.getError());
                System.err.println("✗ Sent error for task: " + task.getTaskId());
            }

            conn.send(response.toString());
        } catch (Exception e) {
            System.err.println("Error sending task result: " + e.getMessage());
        }
    }

    /**
     * Send error message to client
     */
    private void sendError(WebSocket conn, String error) {
        try {
            JsonObject response = new JsonObject();
            response.addProperty("status", "error");
            response.addProperty("error", error);
            conn.send(response.toString());
        } catch (Exception e) {
            System.err.println("Error sending error message: " + e.getMessage());
        }
    }

    /**
     * Get number of active connections
     */
    public int getActiveConnections() {
        return getConnections().size();
    }
}
