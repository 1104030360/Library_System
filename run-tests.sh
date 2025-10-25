#!/bin/bash

# Library Management System - Test Runner
# Following Linus principles: Simple, effective testing

cd "/Users/linjunting/Documents/JavaProj 2"

echo "========================================="
echo "  Library API Server - Test Suite"
echo "  Testing Framework: JUnit 5"
echo "========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if JUnit is downloaded
if [ ! -f "lib/junit-platform-console-standalone-1.10.1.jar" ]; then
    echo -e "${RED}❌ JUnit not found!${NC}"
    echo "Please run: curl -L -o lib/junit-platform-console-standalone-1.10.1.jar https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.10.1/junit-platform-console-standalone-1.10.1.jar"
    exit 1
fi

# Clean old test binaries
echo "🧹 Cleaning old test binaries..."
rm -rf test/bin/*
mkdir -p test/bin

echo ""
echo "========================================="
echo "  PHASE 1: Repository Tests"
echo "========================================="
echo ""

# Compile BookDatabaseRepository test
echo "📦 Compiling BookDatabaseRepository test..."
javac -d test/bin \
    -cp "lib/*:backend/bin" \
    test/repository/BookDatabaseRepositoryTest.java

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Compilation failed for BookDatabaseRepository test${NC}"
    exit 1
fi

# Run repository tests
echo ""
echo "🧪 Running BookDatabaseRepository tests..."
java -jar lib/junit-platform-console-standalone-1.10.1.jar \
    --class-path "lib/*:backend/bin:test/bin" \
    --scan-class-path \
    --include-classname "BookDatabaseRepositoryTest"

REPO_TEST_RESULT=$?

echo ""
echo "========================================="
echo "  PHASE 2: Authentication Tests"
echo "========================================="
echo ""

# Compile Authentication test
echo "📦 Compiling Authentication test..."
javac -d test/bin \
    -cp "lib/*:backend/bin:test/bin" \
    test/auth/AuthenticationTest.java

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Compilation failed for Authentication test${NC}"
    exit 1
fi

# Run authentication tests
echo ""
echo "🧪 Running Authentication tests..."
java -jar lib/junit-platform-console-standalone-1.10.1.jar \
    --class-path "lib/*:backend/bin:test/bin" \
    --scan-class-path \
    --include-classname "AuthenticationTest"

AUTH_TEST_RESULT=$?

echo ""
echo "========================================="
echo "  PHASE 3: API Integration Tests"
echo "========================================="
echo ""

echo -e "${YELLOW}⚠️  API Integration tests require server to be running!${NC}"
echo "Please ensure server is running at http://localhost:7070"
echo ""
read -p "Is the server running? (y/n) " -n 1 -r
echo ""

if [[ $REPLY =~ ^[Yy]$ ]]; then
    # Compile API Integration test
    echo "📦 Compiling API Integration test..."
    javac -d test/bin \
        -cp "lib/*:backend/bin:test/bin" \
        test/api/LibraryApiIntegrationTest.java

    if [ $? -ne 0 ]; then
        echo -e "${RED}❌ Compilation failed for API Integration test${NC}"
        exit 1
    fi

    # Run API tests
    echo ""
    echo "🧪 Running API Integration tests..."
    java -jar lib/junit-platform-console-standalone-1.10.1.jar \
        --class-path "lib/*:backend/bin:test/bin" \
        --scan-class-path \
        --include-classname "LibraryApiIntegrationTest"

    API_TEST_RESULT=$?
else
    echo -e "${YELLOW}⏭️  Skipping API Integration tests${NC}"
    API_TEST_RESULT=0
fi

echo ""
echo "========================================="
echo "  TEST RESULTS SUMMARY"
echo "========================================="
echo ""

# Report results
if [ $REPO_TEST_RESULT -eq 0 ]; then
    echo -e "${GREEN}✅ Repository Tests: PASSED${NC}"
else
    echo -e "${RED}❌ Repository Tests: FAILED${NC}"
fi

if [ $AUTH_TEST_RESULT -eq 0 ]; then
    echo -e "${GREEN}✅ Authentication Tests: PASSED${NC}"
else
    echo -e "${RED}❌ Authentication Tests: FAILED${NC}"
fi

if [[ $REPLY =~ ^[Yy]$ ]]; then
    if [ $API_TEST_RESULT -eq 0 ]; then
        echo -e "${GREEN}✅ API Integration Tests: PASSED${NC}"
    else
        echo -e "${RED}❌ API Integration Tests: FAILED${NC}"
    fi
fi

echo ""

# Overall result
TOTAL_FAILURES=$((REPO_TEST_RESULT + AUTH_TEST_RESULT + API_TEST_RESULT))

if [ $TOTAL_FAILURES -eq 0 ]; then
    echo -e "${GREEN}🎉 ALL TESTS PASSED!${NC}"
    echo ""
    echo "Your code is solid. Ship it."
    exit 0
else
    echo -e "${RED}💥 SOME TESTS FAILED${NC}"
    echo ""
    echo "Fix the failures before shipping."
    exit 1
fi
