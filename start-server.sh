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

echo "  2️⃣  編譯 User (使用者模型)..."
javac -d backend/bin -cp "lib/*:backend/bin" backend/src/User.java

echo "  3️⃣  編譯 ApiSessionManager (Session 管理)..."
javac -d backend/bin -cp "lib/*:backend/bin" backend/src/ApiSessionManager.java

echo "  4️⃣  編譯 UserDatabaseRepository (使用者資料庫)..."
javac -d backend/bin -cp "lib/*:backend/bin" backend/src/UserDatabaseRepository.java

echo "  5️⃣  編譯 ApiAuthenticationHelper (認證模組)..."
javac -d backend/bin -cp "lib/*:backend/bin" backend/src/ApiAuthenticationHelper.java

echo "  6️⃣  編譯 BookDatabaseRepository (圖書資料庫)..."
javac -d backend/bin -cp "lib/*:backend/bin" backend/src/BookDatabaseRepository.java

echo "  7️⃣  編譯 BorrowHistory (借閱歷史模型)..."
javac -d backend/bin -cp "lib/*:backend/bin" backend/src/BorrowHistory.java

echo "  8️⃣  編譯 BorrowHistoryRepository (借閱歷史資料庫)..."
javac -d backend/bin -cp "lib/*:backend/bin" backend/src/BorrowHistoryRepository.java

echo "  9️⃣  編譯 BookRating (書籍評分模型)..."
javac -d backend/bin -cp "lib/*:backend/bin" backend/src/BookRating.java

echo "  🔟 編譯 BookRatingRepository (書籍評分資料庫)..."
javac -d backend/bin -cp "lib/*:backend/bin" backend/src/BookRatingRepository.java

echo "  1️⃣1️⃣ 編譯 BookReview (書籍評論模型)..."
javac -d backend/bin -cp "lib/*:backend/bin" backend/src/BookReview.java

echo "  1️⃣2️⃣ 編譯 BookReviewRepository (書籍評論資料庫)..."
javac -d backend/bin -cp "lib/*:backend/bin" backend/src/BookReviewRepository.java

echo "  1️⃣3️⃣ 編譯 Recommendation (AI 推薦模型)..."
javac -d backend/bin -cp "lib/*:backend/bin" backend/src/Recommendation.java

echo "  1️⃣4️⃣ 編譯 RecommendationService (AI 推薦服務)..."
javac -d backend/bin -cp "lib/*:backend/bin" backend/src/RecommendationService.java

echo "  1️⃣5️⃣ 編譯 StaticFileHandler (靜態檔案處理)..."
javac -d backend/bin -cp "lib/*:backend/bin" backend/src/StaticFileHandler.java

echo "  1️⃣6️⃣ 編譯 NotificationType (通知類型)..."
javac -d backend/bin -cp "lib/*:backend/bin" backend/src/NotificationType.java

echo "  1️⃣7️⃣ 編譯 Notification (通知模型)..."
javac -d backend/bin -cp "lib/*:backend/bin" backend/src/Notification.java

echo "  1️⃣8️⃣ 編譯 NotificationRepository (通知資料庫)..."
javac -d backend/bin -cp "lib/*:backend/bin" backend/src/NotificationRepository.java

echo "  1️⃣9️⃣ 編譯 NotificationService (通知服務)..."
javac -d backend/bin -cp "lib/*:backend/bin" backend/src/NotificationService.java

echo "  2️⃣0️⃣ 編譯 NotificationScheduler (通知排程器)..."
javac -d backend/bin -cp "lib/*:backend/bin" backend/src/NotificationScheduler.java

echo "  2️⃣1️⃣ 編譯 RecommendationTask (推薦任務)..."
javac -d backend/bin -cp "lib/*:backend/bin" backend/src/RecommendationTask.java

echo "  2️⃣2️⃣ 編譯 TaskManager (任務管理器)..."
javac -d backend/bin -cp "lib/*:backend/bin" backend/src/TaskManager.java

echo "  2️⃣3️⃣ 編譯 RecommendationWebSocketServer (WebSocket伺服器)..."
javac -d backend/bin -cp "lib/*:backend/bin" backend/src/RecommendationWebSocketServer.java

echo "  2️⃣4️⃣ 編譯 QuestionClassifier (問題分類器)..."
javac -d backend/bin -cp "lib/*:backend/bin" backend/src/QuestionClassifier.java

echo "  2️⃣5️⃣ 編譯 LibraryRulesRepository (圖書館規則資料庫)..."
javac -d backend/bin -cp "lib/*:backend/bin" backend/src/LibraryRulesRepository.java

echo "  2️⃣6️⃣ 編譯 ChatContext (聊天上下文)..."
javac -d backend/bin -cp "lib/*:backend/bin" backend/src/ChatContext.java

echo "  2️⃣7️⃣ 編譯 ContextRetriever (上下文檢索器)..."
javac -d backend/bin -cp "lib/*:backend/bin" backend/src/ContextRetriever.java

echo "  2️⃣8️⃣ 編譯 LibraryApiServer (主伺服器)..."
javac -d backend/bin -cp "lib/*:backend/bin" backend/src/LibraryApiServer.java

echo ""
echo "✅ 編譯完成！"
echo ""
echo "🚀 啟動伺服器..."
echo ""

# Run server
java -cp "lib/*:backend/bin" LibraryApiServer
