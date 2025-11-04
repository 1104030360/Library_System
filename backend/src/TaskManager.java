import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Iterator;

/**
 * Task Manager - Manages async recommendation tasks
 * Stores task state and automatically cleans up expired tasks
 */
public class TaskManager {

    private static final long TASK_EXPIRY_MS = 5 * 60 * 1000;  // 5 minutes
    private static final long CLEANUP_INTERVAL_MS = 60 * 1000;  // 1 minute

    private final Map<String, RecommendationTask> tasks;
    private final ScheduledExecutorService cleanupScheduler;

    public TaskManager() {
        this.tasks = new ConcurrentHashMap<>();
        this.cleanupScheduler = Executors.newSingleThreadScheduledExecutor();

        // Schedule periodic cleanup of expired tasks
        cleanupScheduler.scheduleAtFixedRate(
            this::cleanupExpiredTasks,
            CLEANUP_INTERVAL_MS,
            CLEANUP_INTERVAL_MS,
            TimeUnit.MILLISECONDS
        );

        System.out.println("✓ TaskManager initialized with auto-cleanup");
    }

    /**
     * Create a new task
     */
    public RecommendationTask createTask(String taskId, String userId, String type) {
        RecommendationTask task = new RecommendationTask(taskId, userId, type);
        tasks.put(taskId, task);
        System.out.println("→ Created task: " + taskId + " for user: " + userId + " (type: " + type + ")");
        return task;
    }

    /**
     * Get task by ID
     */
    public RecommendationTask getTask(String taskId) {
        return tasks.get(taskId);
    }

    /**
     * Complete a task with result
     */
    public void completeTask(String taskId, java.util.List<Recommendation> result) {
        RecommendationTask task = tasks.get(taskId);
        if (task != null) {
            task.setResult(result);
            System.out.println("✓ Task completed: " + taskId);
        }
    }

    /**
     * Mark task as failed
     */
    public void failTask(String taskId, String error) {
        RecommendationTask task = tasks.get(taskId);
        if (task != null) {
            task.setError(error);
            System.err.println("✗ Task failed: " + taskId + " - " + error);
        }
    }

    /**
     * Remove a task (after client receives result)
     */
    public void removeTask(String taskId) {
        RecommendationTask removed = tasks.remove(taskId);
        if (removed != null) {
            System.out.println("→ Removed task: " + taskId);
        }
    }

    /**
     * Get number of active tasks
     */
    public int getActiveTaskCount() {
        return tasks.size();
    }

    /**
     * Cleanup expired tasks
     */
    private void cleanupExpiredTasks() {
        int before = tasks.size();
        Iterator<Map.Entry<String, RecommendationTask>> iterator = tasks.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, RecommendationTask> entry = iterator.next();
            if (entry.getValue().isExpired(TASK_EXPIRY_MS)) {
                System.out.println("→ Cleaning up expired task: " + entry.getKey());
                iterator.remove();
            }
        }

        int removed = before - tasks.size();
        if (removed > 0) {
            System.out.println("✓ Cleaned up " + removed + " expired tasks (remaining: " + tasks.size() + ")");
        }
    }

    /**
     * Shutdown task manager
     */
    public void shutdown() {
        cleanupScheduler.shutdown();
        try {
            if (!cleanupScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupScheduler.shutdownNow();
        }
        tasks.clear();
        System.out.println("✓ TaskManager shutdown");
    }
}
