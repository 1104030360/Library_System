#!/bin/bash

# 通知系統 API 測試腳本
# Phase 13 - Notification System Testing

echo "========================================="
echo "  通知系統 API 測試"
echo "========================================="
echo ""

BASE_URL="http://localhost:7070/api"
COOKIES_FILE="test-cookies.txt"

# 清理舊的 cookies
rm -f $COOKIES_FILE

echo "📝 步驟 1: 登入測試帳號"
echo "-----------------------------------"
curl -X POST "$BASE_URL/auth/login" \
  -c $COOKIES_FILE \
  -H "Content-Type: application/json" \
  -d '{"username":"1001","password":"1234"}' \
  -w "\nHTTP Status: %{http_code}\n\n"

echo ""
echo "📝 步驟 2: 獲取通知列表"
echo "-----------------------------------"
curl -X GET "$BASE_URL/notifications" \
  -b $COOKIES_FILE \
  -w "\nHTTP Status: %{http_code}\n\n"

echo ""
echo "📝 步驟 3: 獲取未讀數量"
echo "-----------------------------------"
curl -X GET "$BASE_URL/notifications/unread-count" \
  -b $COOKIES_FILE \
  -w "\nHTTP Status: %{http_code}\n\n"

echo ""
echo "📝 步驟 4: 借一本書 (觸發通知)"
echo "-----------------------------------"
curl -X POST "$BASE_URL/books/borrow" \
  -b $COOKIES_FILE \
  -H "Content-Type: application/json" \
  -d '{"bookId":"001"}' \
  -w "\nHTTP Status: %{http_code}\n\n"

echo ""
echo "📝 步驟 5: 再次獲取通知列表 (應該有新通知)"
echo "-----------------------------------"
curl -X GET "$BASE_URL/notifications" \
  -b $COOKIES_FILE \
  -w "\nHTTP Status: %{http_code}\n\n"

echo ""
echo "📝 步驟 6: 還書 (觸發還書通知)"
echo "-----------------------------------"
curl -X POST "$BASE_URL/books/return" \
  -b $COOKIES_FILE \
  -H "Content-Type: application/json" \
  -d '{"bookId":"001"}' \
  -w "\nHTTP Status: %{http_code}\n\n"

echo ""
echo "📝 步驟 7: 查看所有通知"
echo "-----------------------------------"
curl -X GET "$BASE_URL/notifications" \
  -b $COOKIES_FILE \
  -w "\nHTTP Status: %{http_code}\n\n"

echo ""
echo "📝 步驟 8: 標記全部為已讀"
echo "-----------------------------------"
curl -X POST "$BASE_URL/notifications/read-all" \
  -b $COOKIES_FILE \
  -w "\nHTTP Status: %{http_code}\n\n"

echo ""
echo "📝 步驟 9: 查看未讀數量 (應該是 0)"
echo "-----------------------------------"
curl -X GET "$BASE_URL/notifications/unread-count" \
  -b $COOKIES_FILE \
  -w "\nHTTP Status: %{http_code}\n\n"

echo ""
echo "📝 步驟 10: 測試篩選 - 只看未讀"
echo "-----------------------------------"
curl -X GET "$BASE_URL/notifications?unreadOnly=true" \
  -b $COOKIES_FILE \
  -w "\nHTTP Status: %{http_code}\n\n"

echo ""
echo "📝 步驟 11: 測試篩選 - 只看借閱通知"
echo "-----------------------------------"
curl -X GET "$BASE_URL/notifications?type=borrow" \
  -b $COOKIES_FILE \
  -w "\nHTTP Status: %{http_code}\n\n"

echo ""
echo "📝 步驟 12: 清空所有通知"
echo "-----------------------------------"
curl -X DELETE "$BASE_URL/notifications/clear" \
  -b $COOKIES_FILE \
  -w "\nHTTP Status: %{http_code}\n\n"

echo ""
echo "📝 步驟 13: 驗證清空結果 (應該是空的)"
echo "-----------------------------------"
curl -X GET "$BASE_URL/notifications" \
  -b $COOKIES_FILE \
  -w "\nHTTP Status: %{http_code}\n\n"

echo ""
echo "✅ 測試完成!"
echo ""
echo "🧹 清理 cookies 文件..."
rm -f $COOKIES_FILE
echo "Done."
