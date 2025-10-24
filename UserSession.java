/**
 * UserSession - Manages the current logged-in user state
 * Replaces global variables and static flags
 */
public class UserSession {
    private static Member_1 currentMember = null;
    private static Admin currentAdmin = null;
    private static UserType currentUserType = null;

    public enum UserType {
        STUDENT,
        TEACHER,
        STAFF,
        BOSS,
        EMPLOYEE,
        GUEST
    }

    // Set current member (student/teacher/staff)
    public static void setCurrentMember(Member_1 member, UserType type) {
        currentMember = member;
        currentAdmin = null;
        currentUserType = type;
    }

    // Set current admin (boss/employee)
    public static void setCurrentAdmin(Admin admin, UserType type) {
        currentAdmin = admin;
        currentMember = null;
        currentUserType = type;
    }

    // Get current member
    public static Member_1 getCurrentMember() {
        return currentMember;
    }

    // Get current admin
    public static Admin getCurrentAdmin() {
        return currentAdmin;
    }

    // Get current user type
    public static UserType getCurrentUserType() {
        return currentUserType;
    }

    // Check if user is logged in
    public static boolean isLoggedIn() {
        return currentMember != null || currentAdmin != null;
    }

    // Check if current user is admin
    public static boolean isAdmin() {
        return currentAdmin != null;
    }

    // Check if current user is member
    public static boolean isMember() {
        return currentMember != null;
    }

    // Logout
    public static void logout() {
        currentMember = null;
        currentAdmin = null;
        currentUserType = null;
    }

    // Get display name for current user
    public static String getCurrentUserName() {
        if (currentMember != null) {
            return currentMember.getName();
        }
        if (currentAdmin != null) {
            return currentAdmin.getNowAccount().getName();
        }
        return "Guest";
    }

    // Get user type display name
    public static String getUserTypeDisplayName() {
        if (currentUserType == null) {
            return "訪客";
        }

        switch (currentUserType) {
            case STUDENT:
                return "學生";
            case TEACHER:
                return "教師";
            case STAFF:
                return "職員";
            case BOSS:
                return "館長";
            case EMPLOYEE:
                return "館員";
            case GUEST:
            default:
                return "訪客";
        }
    }
}
