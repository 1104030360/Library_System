#!/bin/bash

# Stage 4 API Server 啟動腳本

cd "/Users/linjunting/Documents/JavaProj 2"

echo "================================="
echo "開始編譯 Stage 4..."
echo "================================="

# 編譯認證相關類別
echo "1. 編譯 ApiSessionManager 和 ApiAuthenticationHelper..."
javac -cp "lib/gson-2.10.1.jar:.:api" api/ApiSessionManager.java api/ApiAuthenticationHelper.java
if [ $? -ne 0 ]; then
    echo "❌ 編譯認證類別失敗"
    exit 1
fi

# 編譯 BookFileRepository
echo "2. 編譯 BookFileRepository..."
javac -cp "lib/gson-2.10.1.jar:.:api" api/BookFileRepository.java
if [ $? -ne 0 ]; then
    echo "❌ 編譯 BookFileRepository 失敗"
    exit 1
fi

# 編譯 LibraryApiServer
echo "3. 編譯 LibraryApiServer..."
javac -cp "lib/gson-2.10.1.jar:.:api" api/LibraryApiServer.java
if [ $? -ne 0 ]; then
    echo "❌ 編譯 LibraryApiServer 失敗"
    exit 1
fi

echo "✅ 編譯完成！"
echo ""
echo "================================="
echo "啟動 Stage 4 API Server..."
echo "================================="
echo ""

# 啟動伺服器
java -cp "lib/gson-2.10.1.jar:.:api" LibraryApiServer
