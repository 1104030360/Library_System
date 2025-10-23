# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Java Swing-based library management system (中大圖書館借還系統) that allows administrators and members to manage book borrowing and returns. The application uses JOptionPane dialogs for the UI.

## Building and Running

**Compile and run:**
```bash
javac UserClass.java
java UserClass
```

The main entry point is `UserClass.java` with the `main` method that calls `Book.initialize()` to set up initial book inventory, then starts the user interface flow.

## Architecture

### Class Hierarchy

**Admin Hierarchy (Administrators):**
- `Admin` (abstract base class) - Contains account management and book editing functionality
  - `AdminUser` - Data class for admin account information (username, password, identity)
  - `Boss` (館長/Director) - Controls rest days and employee salaries
  - `Employee` (館員/Staff) - Can check salary and work schedule

**Member Hierarchy (Library Members):**
- `Member_1` (abstract base class) - Defines common member properties and abstract methods
  - `Student` - Students with 50-book borrowing limit
  - `Teacher_1` - Teachers with 100-book borrowing limit
  - `Staff_1` - Staff members with 50-book borrowing limit

**Core Classes:**
- `UserClass` - Main application entry point and UI flow controller. Contains static arrays storing all registered members and handles the entire navigation flow between admin and member systems
- `Book` - Manages book inventory using ArrayLists (oldbook, writters, publisher, bookID). Contains all book CRUD operations for both admins and members
- `realmember` - Provides book search/borrow/return functions for non-members

### Key Design Patterns

**Static Storage Arrays:**
The system uses static arrays in `UserClass` to store all members:
- `store_student[]` (size 20)
- `store_teacher[]` (size 20)
- `store_staff[]` (size 20)

**Two-Stage Authentication:**
Both admin and member login flows verify account existence first, then validate password.

**Book Management:**
The `Book` class uses parallel ArrayLists to manage book data:
- `oldbook` - Book titles
- `writters` - Authors
- `publisher` - Publishers
- `bookID` - Book IDs

Operations modify all four lists simultaneously to maintain data consistency.

## Common Development Patterns

### Adding New User Types

When adding a new member type:
1. Extend `Member_1` abstract class
2. Implement abstract methods: `checkPersonalInfo()`, `checkBorrowedBook()`, `toString()`
3. Add static storage array in `UserClass`
4. Add authentication logic with `checkName()` and `checkPassword()` static methods
5. Use a static `flag` variable to track the authenticated user's index

### Book Operations

Book operations use option codes:
- `bookoption==0`: Add book
- `bookoption==1`: Remove book
- `bookoption==2`: Search book

Search can be by ID (`bookoption2==0`) or name (`bookoption2==1`).

### Navigation Flow

The UI uses `JOptionPane.showOptionDialog()` extensively. Navigation options typically include:
- "回主頁" (Return to main page) - calls `UserClass.EnterUser()`
- "繼續使用功能" (Continue using features) - shows feature menu again
- "登出並關閉" (Logout and close) - calls `System.exit(0)`

## Important Notes

**Language:** All UI text is in Traditional Chinese. User messages, dialog titles, and prompts are in Chinese.

**Date Handling:** Uses `java.time.LocalDate` for tracking borrow/return dates. Default borrow period is 2 weeks (`d.plusWeeks(2L)`).

**Hardcoded Admin Accounts:** The `Admin` constructor initializes 6 hardcoded accounts (IDs: 0001-0006) with passwords (1111-6666).

**Incomplete Features:** Some methods are stubbed or incomplete:
- `checkBorrowedBook()` methods in member classes return empty strings
- Overdue book fine calculation is commented out in `realmember.java`
- `checkPersonalInfo()` implementations are minimal

**Global State:** The system relies heavily on static variables and class-level state for storing operation selections (`operation1`, `operation2`, etc.) and return values throughout the application flow.
