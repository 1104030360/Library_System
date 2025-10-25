public class BorrowResult {
    private final boolean success;
    private final String message;

    private BorrowResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // Factory method for success
    public static BorrowResult success(String message) {
        return new BorrowResult(true, message);
    }

    // Factory method for failure
    public static BorrowResult failure(String message) {
        return new BorrowResult(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "BorrowResult{" +
                "success=" + success +
                ", message='" + message + '\'' +
                '}';
    }
}
