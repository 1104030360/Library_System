import javax.swing.*;
import java.time.LocalDate;

/**
 * UserClass3 - Final refactored version
 * Integrates all improvements from Phase 1-4:
 * - Bug fixes from Phase 1
 * - BookRepository and BookInfo from Phase 2
 * - AuthenticationHelper and UserSession from Phase 3
 * - MVC architecture from Phase 4
 */
public class UserClass3 {
    // Date utilities
    public static LocalDate currentDate = LocalDate.now();

    // Member storage
    public static Student[] store_student = new Student[20];
    public static Teacher_1[] store_teacher = new Teacher_1[20];
    public static Staff_1[] store_staff = new Staff_1[20];

    // MVC components
    private static BookRepository repository;
    private static BookService bookService;
    private static LibraryUI ui;
    private static BookController bookController;

    public static void main(String[] args) {
        initializeSystem();
        startMainLoop();
    }

    private static void initializeSystem() {
        // Initialize MVC components
        repository = new BookRepository();
        repository.initialize();

        bookService = new BookService(repository);
        ui = new LibraryUI();
        bookController = new BookController(bookService, ui);
    }

    private static void startMainLoop() {
        boolean running = true;

        while (running) {
            int userType = ui.showMainMenu();

            if (userType == 0) {
                handleAdminFlow();
            } else if (userType == 1) {
                handleMemberFlow();
            } else {
                // User cancelled
                if (ui.confirmAction("確定要離開系統嗎？")) {
                    running = false;
                }
            }
        }

        System.exit(0);
    }

    // ===== ADMIN FLOW =====

    private static void handleAdminFlow() {
        int adminType = ui.showAdminTypeMenu();

        if (adminType == 0) {
            handleBossFlow();
        } else if (adminType == 1) {
            handleEmployeeFlow();
        }
    }

    private static void handleBossFlow() {
        boolean authenticated = AuthenticationHelper.authenticateUnlimited(
            "館長",
            account -> new Boss().checkAccount(account),
            password -> new Boss().checkPassword(password)
        );

        if (!authenticated) {
            return;
        }

        Boss boss = new Boss();
        UserSession.setCurrentAdmin(boss, UserSession.UserType.BOSS);

        boolean continueUsing = true;
        while (continueUsing) {
            int choice = ui.showBossMenu();

            switch (choice) {
                case 0:
                    boss.Rest();
                    break;
                case 1:
                    boss.Salary();
                    break;
                case 2:
                    bookController.handleAdminBookManagement();
                    break;
                case 3:
                    bookController.handleShowStatistics();
                    break;
                default:
                    continueUsing = false;
                    break;
            }

            if (continueUsing) {
                int continueChoice = ui.showContinueMenu();
                if (continueChoice == 0) {
                    UserSession.logout();
                    return;  // Back to main menu
                } else if (continueChoice == 2) {
                    System.exit(0);
                }
            }
        }

        UserSession.logout();
    }

    private static void handleEmployeeFlow() {
        boolean authenticated = AuthenticationHelper.authenticateUnlimited(
            "館員",
            account -> new Employee().checkAccount(account),
            password -> new Employee().checkPassword(password)
        );

        if (!authenticated) {
            return;
        }

        Employee emp = new Employee();
        UserSession.setCurrentAdmin(emp, UserSession.UserType.EMPLOYEE);

        boolean continueUsing = true;
        while (continueUsing) {
            int choice = ui.showEmployeeMenu();

            switch (choice) {
                case 0:
                    emp.checkSalary();
                    break;
                case 1:
                    emp.checkWork();
                    break;
                case 2:
                    bookController.handleAdminBookManagement();
                    break;
                case 3:
                    bookController.handleShowStatistics();
                    break;
                default:
                    continueUsing = false;
                    break;
            }

            if (continueUsing) {
                int continueChoice = ui.showContinueMenu();
                if (continueChoice == 0) {
                    UserSession.logout();
                    return;
                } else if (continueChoice == 2) {
                    System.exit(0);
                }
            }
        }

        UserSession.logout();
    }

    // ===== MEMBER FLOW =====

    private static void handleMemberFlow() {
        int memberChoice = ui.showMemberTypeMenu();

        if (memberChoice == 0) {
            handleMemberLogin();
        } else if (memberChoice == 1) {
            handleNonMemberFlow();
        }
    }

    private static void handleMemberLogin() {
        String[] memberTypes = {"學生", "教師", "職員"};
        int memberType = ui.showMemberIdentityMenu();

        if (memberType < 0 || memberType > 2) {
            return;
        }

        Member_1 member = null;

        switch (memberType) {
            case 0:
                member = authenticateStudent();
                break;
            case 1:
                member = authenticateTeacher();
                break;
            case 2:
                member = authenticateStaff();
                break;
        }

        if (member != null) {
            handleMemberMenu(member, memberTypes[memberType]);
        }
    }

    private static Student authenticateStudent() {
        boolean authenticated = AuthenticationHelper.authenticateUnlimited(
            "學生會員",
            Student::checkName,
            Student::checkPassword
        );

        if (authenticated && Student.flag >= 0 && Student.flag < store_student.length) {
            Student student = store_student[Student.flag];
            UserSession.setCurrentMember(student, UserSession.UserType.STUDENT);
            return student;
        }
        return null;
    }

    private static Teacher_1 authenticateTeacher() {
        boolean authenticated = AuthenticationHelper.authenticateUnlimited(
            "教師會員",
            Teacher_1::checkName,
            Teacher_1::checkPassword
        );

        if (authenticated && Teacher_1.flag >= 0 && Teacher_1.flag < store_teacher.length) {
            Teacher_1 teacher = store_teacher[Teacher_1.flag];
            UserSession.setCurrentMember(teacher, UserSession.UserType.TEACHER);
            return teacher;
        }
        return null;
    }

    private static Staff_1 authenticateStaff() {
        boolean authenticated = AuthenticationHelper.authenticateUnlimited(
            "職員會員",
            Staff_1::checkName,
            Staff_1::checkPassword
        );

        if (authenticated && Staff_1.flag >= 0 && Staff_1.flag < store_staff.length) {
            Staff_1 staff = store_staff[Staff_1.flag];
            UserSession.setCurrentMember(staff, UserSession.UserType.STAFF);
            return staff;
        }
        return null;
    }

    private static void handleMemberMenu(Member_1 member, String memberType) {
        boolean continueUsing = true;

        while (continueUsing) {
            int choice = ui.showMemberFunctionMenu(memberType);

            switch (choice) {
                case 0:
                    member.checkPersonalInfo();
                    break;
                case 1:
                    member.checkBorrowedBook();
                    break;
                case 2:
                    bookController.handleMemberBookOperations();
                    break;
                default:
                    continueUsing = false;
                    break;
            }

            if (continueUsing) {
                int continueChoice = ui.showContinueMenu();
                if (continueChoice == 0) {
                    UserSession.logout();
                    return;
                } else if (continueChoice == 2) {
                    System.exit(0);
                }
            }
        }

        UserSession.logout();
    }

    private static void handleNonMemberFlow() {
        int choice = ui.showNonMemberMenu();

        if (choice == 0) {
            handleRegistration();
        } else if (choice == 1) {
            bookController.handleDisplayAllBooks();

            // Allow search
            int searchChoice = ui.showYesNoDialog("查詢", "要查詢特定書籍嗎？");
            if (searchChoice == JOptionPane.YES_OPTION) {
                int searchMethod = ui.promptSearchMethod();
                BookInfo book = null;

                if (searchMethod == 0) {
                    String id = ui.promptBookId();
                    if (id != null && !id.trim().isEmpty()) {
                        book = bookService.searchBookById(id);
                    }
                } else if (searchMethod == 1) {
                    String title = ui.promptBookTitle();
                    if (title != null && !title.trim().isEmpty()) {
                        book = bookService.searchBookByTitle(title);
                    }
                }

                if (book != null) {
                    ui.displayBookInfo(book);
                } else {
                    ui.showError("找不到此書籍");
                }
            }
        }
    }

    private static void handleRegistration() {
        String[] regInfo = ui.promptRegistrationInfo();

        if (regInfo == null) {
            ui.showWarning("註冊已取消");
            return;
        }

        String username = regInfo[0];
        String passwordStr = regInfo[1];
        String identity = regInfo[2];

        int password;
        try {
            password = Integer.parseInt(passwordStr);
        } catch (NumberFormatException e) {
            ui.showError("密碼必須是數字");
            return;
        }

        boolean success = false;
        switch (identity) {
            case "學生":
                Student student = new Student(username, password, "學生", null);
                Student.storeStudent(student);
                success = true;
                break;
            case "教師":
                Teacher_1 teacher = new Teacher_1(username, password, "教師", null);
                Teacher_1.storeTeacher(teacher);
                success = true;
                break;
            case "職員":
                Staff_1 staff = new Staff_1(username, password, "職員", null);
                Staff_1.storeStaff(staff);
                success = true;
                break;
        }

        if (success) {
            ui.showSuccess("成功建立" + identity + "會員！\n使用者名稱：" + username);

            int loginChoice = ui.showYesNoDialog("登入", "要直接登入嗎？");
            if (loginChoice == JOptionPane.YES_OPTION) {
                handleMemberLogin();
            }
        }
    }
}
