import java.util.List;

/**
 * Recommendation Task - Tracks async recommendation generation status
 * Used for WebSocket-based async recommendation delivery
 */
public class RecommendationTask {

    public enum Status {
        PROCESSING,  // AI is generating recommendations
        COMPLETED,   // Successfully completed
        FAILED       // Error occurred
    }

    private String taskId;
    private String userId;
    private Status status;
    private List<Recommendation> result;
    private String error;
    private long createdAt;
    private String type;  // "personal" or "related"

    public RecommendationTask(String taskId, String userId, String type) {
        this.taskId = taskId;
        this.userId = userId;
        this.type = type;
        this.status = Status.PROCESSING;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and setters
    public String getTaskId() {
        return taskId;
    }

    public String getUserId() {
        return userId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<Recommendation> getResult() {
        return result;
    }

    public void setResult(List<Recommendation> result) {
        this.result = result;
        this.status = Status.COMPLETED;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
        this.status = Status.FAILED;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getType() {
        return type;
    }

    public boolean isExpired(long maxAgeMs) {
        return System.currentTimeMillis() - createdAt > maxAgeMs;
    }
}
