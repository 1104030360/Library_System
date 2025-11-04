/**
 * Notification Entity
 * Represents a user notification in the system
 */
public class Notification {
    private int id;
    private String userId;
    private String type;          // system, borrow, return, review, due
    private String title;
    private String message;
    private String link;
    private boolean read;
    private boolean deleted;
    private String createdAt;
    private String readAt;

    // Default constructor
    public Notification() {}

    // Constructor for creating new notification
    public Notification(String userId, String type, String title, String message, String link) {
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.link = link;
        this.read = false;
        this.deleted = false;
    }

    // Getters
    public int getId() { return id; }
    public String getUserId() { return userId; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getLink() { return link; }
    public boolean isRead() { return read; }
    public boolean isDeleted() { return deleted; }
    public String getCreatedAt() { return createdAt; }
    public String getReadAt() { return readAt; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setType(String type) { this.type = type; }
    public void setTitle(String title) { this.title = title; }
    public void setMessage(String message) { this.message = message; }
    public void setLink(String link) { this.link = link; }
    public void setRead(boolean read) { this.read = read; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setReadAt(String readAt) { this.readAt = readAt; }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", type='" + type + '\'' +
                ", title='" + title + '\'' +
                ", read=" + read +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}
