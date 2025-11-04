import java.util.Timer;
import java.util.TimerTask;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Notification Scheduler
 * Handles scheduled tasks for notifications (e.g., due reminders)
 */
public class NotificationScheduler {

    private static Timer timer;
    private static NotificationService notificationService;

    /**
     * Start the scheduler
     * Runs due reminders daily at 9:00 AM
     */
    public static void start(NotificationService service) {
        notificationService = service;

        if (timer != null) {
            timer.cancel();
        }

        timer = new Timer("NotificationScheduler", true);

        // Calculate delay to next 9:00 AM
        long delay = calculateDelayToNextRun();
        long period = 24 * 60 * 60 * 1000; // 24 hours

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    System.out.println("Starting due reminder task at: " + LocalDateTime.now());
                    int count = notificationService.sendDueReminders();
                    System.out.println("Due reminder task completed, sent " + count + " notifications");
                } catch (Exception e) {
                    System.err.println("Due reminder task failed: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }, delay, period);

        System.out.println("Notification scheduler started, will run daily at 9:00 AM");
    }

    /**
     * Stop the scheduler
     */
    public static void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
            System.out.println("Notification scheduler stopped");
        }
    }

    /**
     * Calculate delay in milliseconds to next 9:00 AM
     */
    private static long calculateDelayToNextRun() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = now.withHour(9).withMinute(0).withSecond(0).withNano(0);

        // If 9:00 AM has passed today, schedule for tomorrow
        if (now.isAfter(nextRun)) {
            nextRun = nextRun.plusDays(1);
        }

        return ChronoUnit.MILLIS.between(now, nextRun);
    }

    /**
     * Manually trigger due reminders (for testing)
     */
    public static int triggerDueReminders() {
        if (notificationService == null) {
            System.err.println("NotificationService not initialized");
            return 0;
        }

        System.out.println("Manually triggering due reminder task");
        return notificationService.sendDueReminders();
    }
}
