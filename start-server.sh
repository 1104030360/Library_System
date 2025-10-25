#!/bin/bash

# Library API Server - Stage 5 Startup Script
# New Backend Structure

cd "/Users/linjunting/Documents/JavaProj 2"

echo "========================================="
echo "  圖書館管理系統 - Stage 5"
echo "  後端架構重組版本"
echo "========================================="
echo ""

# Clean old compiled files
echo "🧹 清理舊的編譯檔案..."
rm -rf backend/bin/*

# Compile backend
echo "📦 編譯後端程式..."
echo ""

echo "  1️⃣  編譯 BookInfo (資料模型)..."
javac -d backend/bin -cp "lib/*:backend/src" backend/src/BookInfo.java

echo "  2️⃣  編譯 ApiSessionManager (Session 管理)..."
javac -d backend/bin -cp "lib/*:backend/bin" backend/src/ApiSessionManager.java

echo "  3️⃣  編譯 ApiAuthenticationHelper (認證模組)..."
javac -d backend/bin -cp "lib/*:backend/bin" backend/src/ApiAuthenticationHelper.java

echo "  4️⃣  編譯 BookDatabaseRepository (資料庫存取)..."
javac -d backend/bin -cp "lib/*:backend/bin" backend/src/BookDatabaseRepository.java

echo "  5️⃣  編譯 StaticFileHandler (靜態檔案處理)..."
javac -d backend/bin -cp "lib/*:backend/bin" backend/src/StaticFileHandler.java

echo "  6️⃣  編譯 LibraryApiServer (主伺服器)..."
javac -d backend/bin -cp "lib/*:backend/bin" backend/src/LibraryApiServer.java

echo ""
echo "✅ 編譯完成！"
echo ""
echo "🚀 啟動伺服器..."
echo ""

# Run server
java -cp "lib/*:backend/bin" LibraryApiServer
