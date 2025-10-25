#!/bin/bash

# Library API Server - Stage 5 Startup Script
# SQLite Database Version

cd "/Users/linjunting/Documents/JavaProj 2"

echo "開始編譯 Stage 5 (SQLite 版本)..."

# Compile authentication helpers
echo "1️⃣ 編譯認證模組..."
javac -cp "lib/*:.:api" api/ApiSessionManager.java
javac -cp "lib/*:.:api" api/ApiAuthenticationHelper.java

# Compile database repository
echo "2️⃣ 編譯資料庫模組..."
javac -cp "lib/*:.:api" api/BookDatabaseRepository.java

# Compile static file handler
echo "3️⃣ 編譯靜態檔案處理器..."
javac -cp "lib/*:.:api" api/StaticFileHandler.java

# Compile main server
echo "4️⃣ 編譯主伺服器..."
javac -cp "lib/*:.:api" api/LibraryApiServer.java

echo ""
echo "✅ 編譯完成！"
echo ""
echo "🚀 啟動伺服器..."
echo ""

# Run server
java -cp "lib/*:.:api" LibraryApiServer
