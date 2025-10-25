# Library Management System - Test Suite

**Philosophy**: Following Linus Torvalds principles
- Test REAL scenarios, not imaginary problems
- Simple, effective tests
- No over-engineering

---

## 📋 Test Structure

```
test/
├── repository/           # Database layer tests
│   └── BookDatabaseRepositoryTest.java
├── api/                  # API integration tests
│   └── LibraryApiIntegrationTest.java
└── auth/                 # Authentication tests
    └── AuthenticationTest.java
```

---

## 🚀 Running Tests

### Option 1: Run All Tests (Recommended)

```bash
./run-tests.sh
```

This will:
1. Compile all tests
2. Run Repository tests
3. Run Authentication tests
4. Run API Integration tests (if server is running)
5. Show summary

### Option 2: Run Individual Test Suites

**Repository Tests** (Database operations):
```bash
javac -d test/bin -cp "lib/*:backend/bin" test/repository/BookDatabaseRepositoryTest.java

java -jar lib/junit-platform-console-standalone-1.10.1.jar \
    --class-path "lib/*:backend/bin:test/bin" \
    --scan-class-path \
    --include-classname "BookDatabaseRepositoryTest"
```

**Authentication Tests**:
```bash
javac -d test/bin -cp "lib/*:backend/bin" test/auth/AuthenticationTest.java

java -jar lib/junit-platform-console-standalone-1.10.1.jar \
    --class-path "lib/*:backend/bin:test/bin" \
    --scan-class-path \
    --include-classname "AuthenticationTest"
```

**API Integration Tests** (requires server running):
```bash
# Start server first:
./start-server.sh

# In another terminal:
javac -d test/bin -cp "lib/*:backend/bin" test/api/LibraryApiIntegrationTest.java

java -jar lib/junit-platform-console-standalone-1.10.1.jar \
    --class-path "lib/*:backend/bin:test/bin" \
    --scan-class-path \
    --include-classname "LibraryApiIntegrationTest"
```

---

## 📝 Test Coverage

### 1. BookDatabaseRepositoryTest (10 tests)
Tests the database layer - CRUD operations, persistence, data integrity.

**What it tests**:
- ✅ Database initialization
- ✅ Find book by ID
- ✅ Add new book
- ✅ Update book (borrow/return)
- ✅ Remove book
- ✅ Get all books
- ✅ Statistics calculation
- ✅ Find by title
- ✅ Multiple concurrent operations
- ✅ Database persistence (simulated restart)

**Why these tests**:
- Cover all CRUD operations users will perform
- Test data integrity (no duplicates, proper updates)
- Verify persistence across "restarts"

### 2. AuthenticationTest (10 tests)
Tests the authentication and session management system.

**What it tests**:
- ✅ Valid authentication (Boss, Employee)
- ✅ Invalid credentials
- ✅ Session creation
- ✅ Session validation
- ✅ Session deletion (logout)
- ✅ Multiple sessions
- ✅ Session count
- ✅ Edge cases (null, empty)
- ✅ Same user multiple logins
- ✅ Complete auth flow

**Why these tests**:
- Security critical - must work correctly
- Session management is core functionality
- Edge cases prevent crashes

### 3. LibraryApiIntegrationTest (10 tests)
Tests the entire API stack - HTTP requests to responses.

**What it tests**:
- ✅ Server is running
- ✅ GET /api/books (all books)
- ✅ GET /api/books?id=001 (specific book)
- ✅ GET /api/stats (statistics)
- ✅ POST /api/auth/login (authentication)
- ✅ GET /api/auth/whoami (session check)
- ✅ POST /api/books/borrow (borrow flow)
- ✅ POST /api/books/return (return flow)
- ✅ POST /api/auth/logout (logout)
- ✅ Invalid requests handling

**Why these tests**:
- Tests the REAL user experience
- Verifies HTTP layer works correctly
- Tests authentication flow end-to-end
- Ensures error handling works

---

## 🎯 Test Philosophy

### What We Test
1. **Real user scenarios** - Actions users will actually perform
2. **Data integrity** - Database stays consistent
3. **Security** - Authentication works correctly
4. **Error handling** - System handles bad input gracefully

### What We DON'T Test
1. ❌ Every possible edge case (diminishing returns)
2. ❌ Theoretical scenarios that never happen
3. ❌ Internal implementation details (test behavior, not code)

---

## 📊 Expected Results

### All Tests Pass
```
✅ Repository Tests: PASSED
✅ Authentication Tests: PASSED
✅ API Integration Tests: PASSED

🎉 ALL TESTS PASSED!

Your code is solid. Ship it.
```

### If Tests Fail
```
❌ Repository Tests: FAILED
```

**What to do**:
1. Read the test output - it tells you what broke
2. Fix the code, not the test
3. Re-run tests
4. Don't ship until tests pass

---

## 🔧 Adding New Tests

### Linus Principles for Writing Tests

**Before writing a test, ask**:
1. Does this test a REAL scenario?
2. Is this the simplest way to test it?
3. Will this catch actual bugs?

**Good Test Example**:
```java
@Test
public void testBorrowBook() {
    // Real scenario: User borrows a book
    BookInfo book = repository.findById("001");
    book.markAsBorrowed();
    repository.updateBook(book);

    BookInfo check = repository.findById("001");
    assertFalse(check.isAvailable());
}
```

**Bad Test Example**:
```java
@Test
public void testBookIdCannotBeNullUnlessEmptyStringOrZeroLength() {
    // This is testing implementation, not behavior
    // Over-engineered, unclear
}
```

### Test Naming
- Name: `testWhatYouAreTesting()`
- Clear, descriptive
- No abbreviations

### Test Structure
1. Setup (arrange)
2. Execute (act)
3. Verify (assert)

---

## 🐛 Debugging Failed Tests

### Test fails with "Connection refused"
**Problem**: Server not running
**Solution**: Start server with `./start-server.sh`

### Test fails with "Database locked"
**Problem**: Multiple test instances running
**Solution**: Kill old processes, re-run

### Test fails with "Class not found"
**Problem**: Compilation failed
**Solution**: Check compilation output, fix errors

---

## 📈 Future Tests

When adding new features, add tests for:
- **Book CRUD API** (add/edit/delete endpoints)
- **Borrow History** (history logging and retrieval)
- **Admin Dashboard** (statistics endpoints)
- **Search & Pagination** (search accuracy, pagination logic)

---

**Linus says**: "Tests are documentation. They show how the system SHOULD work."

Write tests that make sense. Ship code that works.
