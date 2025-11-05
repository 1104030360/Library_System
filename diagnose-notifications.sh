#!/bin/bash

# 通知系統完整診斷腳本
# 測試每個環節，找出問題所在

echo "========================================="
echo "  通知系統完整診斷"
echo "========================================="
echo ""

BASE_URL="http://localhost:7070/api"
COOKIES_FILE="diagnose-cookies.txt"
DB_PATH="data/library.db"

# 清理
rm -f $COOKIES_FILE

echo "📝 診斷 1: 檢查資料庫表是否存在"
echo "-----------------------------------"
if [ -f "$DB_PATH" ]; then
    echo "✅ 資料庫檔案存在: $DB_PATH"

    # 檢查 notifications 表
    TABLE_EXISTS=$(sqlite3 "$DB_PATH" "SELECT name FROM sqlite_master WHERE type='table' AND name='notifications';" 2>/dev/null)
    if [ -n "$TABLE_EXISTS" ]; then
        echo "✅ notifications 表存在"

        # 檢查表結構
        echo ""
        echo "表結構:"
        sqlite3 "$DB_PATH" "PRAGMA table_info(notifications);"

        # 檢查現有通知數量
        echo ""
        COUNT=$(sqlite3 "$DB_PATH" "SELECT COUNT(*) FROM notifications;" 2>/dev/null)
        echo "現有通知數量: $COUNT"

        # 顯示最近 5 條通知
        echo ""
        echo "最近 5 條通知:"
        sqlite3 "$DB_PATH" "SELECT id, user_id, type, title, read, created_at FROM notifications ORDER BY id DESC LIMIT 5;"
    else
        echo "❌ notifications 表不存在!"
    fi
else
    echo "❌ 資料庫檔案不存在: $DB_PATH"
fi

echo ""
echo ""
echo "📝 診斷 2: 測試登入"
echo "-----------------------------------"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -c $COOKIES_FILE \
  -H "Content-Type: application/json" \
  -d '{"username":"1001","password":"1234"}')

echo "登入回應: $LOGIN_RESPONSE"

if echo "$LOGIN_RESPONSE" | grep -q '"success":true'; then
    echo "✅ 登入成功"
else
    echo "❌ 登入失敗"
    exit 1
fi

echo ""
echo ""
echo "📝 診斷 3: 測試獲取通知 (借書前)"
echo "-----------------------------------"
BEFORE_NOTIF=$(curl -s -X GET "$BASE_URL/notifications" -b $COOKIES_FILE)
echo "$BEFORE_NOTIF" | python3 -m json.tool 2>/dev/null || echo "$BEFORE_NOTIF"

BEFORE_COUNT=$(echo "$BEFORE_NOTIF" | grep -o '"totalCount":[0-9]*' | grep -o '[0-9]*')
echo ""
echo "借書前通知總數: $BEFORE_COUNT"

echo ""
echo ""
echo "📝 診斷 4: 執行借書操作"
echo "-----------------------------------"
BORROW_RESPONSE=$(curl -s -X POST "$BASE_URL/books/borrow" \
  -b $COOKIES_FILE \
  -H "Content-Type: application/json" \
  -d '{"bookId":"001"}')

echo "借書回應: $BORROW_RESPONSE"

if echo "$BORROW_RESPONSE" | grep -q '"success":true'; then
    echo "✅ 借書成功"
else
    echo "❌ 借書失敗"
    echo "$BORROW_RESPONSE"
fi

echo ""
echo "⏳ 等待 2 秒讓資料庫寫入..."
sleep 2

echo ""
echo ""
echo "📝 診斷 5: 直接檢查資料庫 (借書後)"
echo "-----------------------------------"
if [ -f "$DB_PATH" ]; then
    NEW_COUNT=$(sqlite3 "$DB_PATH" "SELECT COUNT(*) FROM notifications;" 2>/dev/null)
    echo "資料庫中通知總數: $NEW_COUNT"

    # 檢查用戶 1001 的通知
    USER_NOTIF=$(sqlite3 "$DB_PATH" "SELECT COUNT(*) FROM notifications WHERE user_id='1001';" 2>/dev/null)
    echo "用戶 1001 的通知數: $USER_NOTIF"

    echo ""
    echo "最新通知記錄:"
    sqlite3 "$DB_PATH" "SELECT id, user_id, type, title, substr(message, 1, 50), read, deleted, created_at FROM notifications WHERE user_id='1001' ORDER BY id DESC LIMIT 3;"

    # 檢查是否有 borrow 類型的通知
    BORROW_NOTIF=$(sqlite3 "$DB_PATH" "SELECT COUNT(*) FROM notifications WHERE user_id='1001' AND type='borrow';" 2>/dev/null)
    echo ""
    echo "用戶 1001 的借書通知數: $BORROW_NOTIF"
fi

echo ""
echo ""
echo "📝 診斷 6: 透過 API 獲取通知 (借書後)"
echo "-----------------------------------"
AFTER_NOTIF=$(curl -s -X GET "$BASE_URL/notifications" -b $COOKIES_FILE)
echo "$AFTER_NOTIF" | python3 -m json.tool 2>/dev/null || echo "$AFTER_NOTIF"

AFTER_COUNT=$(echo "$AFTER_NOTIF" | grep -o '"totalCount":[0-9]*' | grep -o '[0-9]*')
echo ""
echo "API 返回通知總數: $AFTER_COUNT"

# 提取 notifications 陣列
NOTIFICATIONS=$(echo "$AFTER_NOTIF" | grep -o '"notifications":\[.*\]' || echo "無法提取")
echo ""
echo "通知陣列: $NOTIFICATIONS"

echo ""
echo ""
echo "📝 診斷 7: 檢查未讀數量"
echo "-----------------------------------"
UNREAD_RESPONSE=$(curl -s -X GET "$BASE_URL/notifications/unread-count" -b $COOKIES_FILE)
echo "$UNREAD_RESPONSE"

echo ""
echo ""
echo "📝 診斷 8: 測試還書並檢查通知"
echo "-----------------------------------"
RETURN_RESPONSE=$(curl -s -X POST "$BASE_URL/books/return" \
  -b $COOKIES_FILE \
  -H "Content-Type: application/json" \
  -d '{"bookId":"001"}')

echo "還書回應: $RETURN_RESPONSE"

if echo "$RETURN_RESPONSE" | grep -q '"success":true'; then
    echo "✅ 還書成功"
else
    echo "❌ 還書失敗"
fi

echo ""
echo "⏳ 等待 2 秒..."
sleep 2

echo ""
echo "還書後的通知:"
sqlite3 "$DB_PATH" "SELECT id, user_id, type, title, substr(message, 1, 80), created_at FROM notifications WHERE user_id='1001' ORDER BY id DESC LIMIT 5;"

echo ""
echo ""
echo "📝 診斷總結"
echo "-----------------------------------"
FINAL_DB_COUNT=$(sqlite3 "$DB_PATH" "SELECT COUNT(*) FROM notifications WHERE user_id='1001';" 2>/dev/null)
FINAL_API_RESPONSE=$(curl -s -X GET "$BASE_URL/notifications" -b $COOKIES_FILE)
FINAL_API_COUNT=$(echo "$FINAL_API_RESPONSE" | grep -o '"totalCount":[0-9]*' | grep -o '[0-9]*')

echo "資料庫中用戶 1001 的通知數: $FINAL_DB_COUNT"
echo "API 返回的通知總數: $FINAL_API_COUNT"

if [ "$FINAL_DB_COUNT" -gt "0" ] && [ "$FINAL_API_COUNT" -gt "0" ]; then
    echo ""
    echo "✅ 通知系統正常運作"
elif [ "$FINAL_DB_COUNT" -gt "0" ] && [ "$FINAL_API_COUNT" -eq "0" ]; then
    echo ""
    echo "⚠️  資料庫有通知，但 API 沒有返回 - API 層問題"
elif [ "$FINAL_DB_COUNT" -eq "0" ]; then
    echo ""
    echo "❌ 資料庫中沒有通知記錄 - 通知創建失敗"
fi

echo ""
echo "🧹 清理 cookies..."
rm -f $COOKIES_FILE
echo "完成!"
