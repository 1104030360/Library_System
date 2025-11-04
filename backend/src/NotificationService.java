import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Notification Service
 * Handles business logic for notifications
 */
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final BorrowHistoryRepository borrowHistoryRepository;
    private final BookDatabaseRepository bookRepository;
    private final UserDatabaseRepository userRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            BorrowHistoryRepository borrowHistoryRepository,
            BookDatabaseRepository bookRepository,
            UserDatabaseRepository userRepository) {

        this.notificationRepository = notificationRepository;
        this.borrowHistoryRepository = borrowHistoryRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    /**
     * Send notification to a single user
     */
    public boolean sendNotification(
            String userId,
            NotificationType type,
            String title,
            String message,
            String link) {

        Notification notification = new Notification(
            userId, type.getCode(), title, message, link
        );

        return notificationRepository.createNotification(notification);
    }

    /**
     * Batch send notifications to multiple users
     */
    public int sendNotificationToUsers(
            List<String> userIds,
            NotificationType type,
            String title,
            String message,
            String link) {

        List<Notification> notifications = new ArrayList<>();

        for (String userId : userIds) {
            notifications.add(new Notification(
                userId, type.getCode(), title, message, link
            ));
        }

        return notificationRepository.batchCreateNotifications(notifications);
    }

    /**
     * Send borrow notification
     */
    public boolean sendBorrowNotification(String userId, String bookTitle,
                                         String borrowDate, String dueDate) {
        System.out.println("🟢 NotificationService.sendBorrowNotification called");
        System.out.println("   userId: " + userId);
        System.out.println("   bookTitle: " + bookTitle);
        System.out.println("   borrowDate: " + borrowDate);
        System.out.println("   dueDate: " + dueDate);

        String title = "借閱成功";
        String message = String.format(
            "您已成功借閱《%s》。\n借閱日期: %s\n應還日期: %s",
            bookTitle, borrowDate, dueDate
        );
        String link = "/history";

        boolean result = sendNotification(userId, NotificationType.BORROW, title, message, link);

        if (result) {
            System.out.println("✅ Borrow notification sent successfully");
        } else {
            System.err.println("❌ Failed to send borrow notification");
        }

        return result;
    }

    /**
     * Send return notification
     */
    public boolean sendReturnNotification(String userId, String bookTitle,
                                         int borrowDays, boolean isOverdue) {
        System.out.println("🟢 NotificationService.sendReturnNotification called");
        System.out.println("   userId: " + userId);
        System.out.println("   bookTitle: " + bookTitle);
        System.out.println("   borrowDays: " + borrowDays);
        System.out.println("   isOverdue: " + isOverdue);

        String title = "歸還成功";
        String message;

        if (isOverdue) {
            message = String.format(
                "您已成功歸還《%s》。\n借閱天數: %d 天（已逾期）",
                bookTitle, borrowDays
            );
        } else {
            message = String.format(
                "您已成功歸還《%s》。\n借閱天數: %d 天",
                bookTitle, borrowDays
            );
        }

        String link = "/history";

        boolean result = sendNotification(userId, NotificationType.RETURN, title, message, link);

        if (result) {
            System.out.println("✅ Return notification sent successfully");
        } else {
            System.err.println("❌ Failed to send return notification");
        }

        return result;
    }

    /**
     * Send new review notification to users who borrowed the book
     */
    public void sendNewReviewNotification(String bookId, String bookTitle,
                                         String reviewerUserId) {
        // Get all users who have borrowed this book (excluding reviewer)
        List<BorrowHistory> histories = borrowHistoryRepository.getBookHistory(bookId);

        String title = "新評論通知";
        String message = String.format(
            "您借閱過的《%s》收到新評論，快來看看其他讀者的心得吧！",
            bookTitle
        );
        String link = "/book/" + bookId;

        // Use Set to avoid duplicate user IDs
        Set<String> userIds = new HashSet<>();
        for (BorrowHistory history : histories) {
            if (!history.getUserId().equals(reviewerUserId)) {
                userIds.add(history.getUserId());
            }
        }

        if (!userIds.isEmpty()) {
            sendNotificationToUsers(
                new ArrayList<>(userIds),
                NotificationType.REVIEW,
                title,
                message,
                link
            );
        }
    }

    /**
     * Send due reminders (called by scheduler)
     */
    public int sendDueReminders() {
        int count = 0;
        LocalDate today = LocalDate.now();
        String todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE);

        // Get borrowings due within 3 days
        List<BorrowHistory> borrowings = getDueSoonBorrowings(3);

        for (BorrowHistory history : borrowings) {
            // Check if reminder already sent today
            if (notificationRepository.hasReminderSent(history.getId(), todayStr)) {
                continue;
            }

            // Calculate days left
            LocalDate dueDate = LocalDate.parse(history.getDueDate());
            long daysLeft = ChronoUnit.DAYS.between(today, dueDate);

            if (daysLeft < 0) {
                continue; // Already overdue, skip
            }

            // Get book info
            BookInfo book = bookRepository.findById(history.getBookId());
            if (book == null) {
                continue;
            }

            // Send reminder
            String title = "到期提醒";
            String message = String.format(
                "您借閱的《%s》還有 %d 天到期，請記得按時歸還。",
                book.getTitle(), daysLeft
            );
            String link = "/history";

            boolean sent = sendNotification(
                history.getUserId(),
                NotificationType.DUE,
                title,
                message,
                link
            );

            if (sent) {
                // Log reminder sent
                notificationRepository.logDueReminder(
                    history.getId(),
                    history.getUserId(),
                    history.getBookId(),
                    todayStr,
                    history.getDueDate()
                );
                count++;
            }
        }

        return count;
    }

    /**
     * Get borrowings that are due soon
     */
    private List<BorrowHistory> getDueSoonBorrowings(int daysThreshold) {
        List<BorrowHistory> allBorrowings = borrowHistoryRepository.getAllHistory();
        List<BorrowHistory> dueSoon = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (BorrowHistory history : allBorrowings) {
            if (!"borrowing".equals(history.getStatus())) {
                continue;
            }

            try {
                LocalDate dueDate = LocalDate.parse(history.getDueDate());
                long daysUntilDue = ChronoUnit.DAYS.between(today, dueDate);

                if (daysUntilDue >= 0 && daysUntilDue <= daysThreshold) {
                    dueSoon.add(history);
                }
            } catch (Exception e) {
                System.err.println("Failed to parse due date: " + history.getDueDate());
            }
        }

        return dueSoon;
    }

    /**
     * Send system announcement to all users or specific role
     */
    public int sendSystemAnnouncement(String title, String content, String targetRole) {
        List<User> users;

        if ("all".equals(targetRole)) {
            users = userRepository.getAllUsers();
        } else {
            users = userRepository.getUsersByRole(targetRole);
        }

        if (users.isEmpty()) {
            return 0;
        }

        List<String> userIds = new ArrayList<>();
        for (User user : users) {
            userIds.add(user.getId());
        }

        return sendNotificationToUsers(
            userIds,
            NotificationType.SYSTEM,
            title,
            content,
            null
        );
    }
}
