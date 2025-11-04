#!/bin/bash

echo "=========================================="
echo "Chat RAG Integration Test"
echo "=========================================="

AI_SERVICE="http://localhost:8888"
BACKEND="http://localhost:7070"
COOKIE_FILE="/tmp/claude/cookies.txt"

# 檢查服務是否運行
echo ""
echo "[1] 檢查服務狀態..."
curl -s $AI_SERVICE/health > /dev/null
if [ $? -ne 0 ]; then
    echo "❌ AI Service 未運行"
    exit 1
fi
echo "✅ AI Service 運行中"

curl -s $BACKEND/api/health > /dev/null
if [ $? -ne 0 ]; then
    echo "❌ Backend 未運行"
    exit 1
fi
echo "✅ Backend 運行中"

# 測試 1: 不使用 RAG（向後兼容）
echo ""
echo "[2] 測試不使用 RAG（向後兼容）..."
response=$(curl -s -X POST $AI_SERVICE/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "你好", "history": []}')

if echo "$response" | jq -e '.success == true' > /dev/null; then
    echo "✅ Test 1 passed: 不使用 RAG 正常工作"
else
    echo "❌ Test 1 failed"
    echo "$response" | jq
    exit 1
fi

# 測試 2: 使用 RAG（with context）
echo ""
echo "[3] 測試使用 RAG（with context）..."
response=$(curl -s -X POST $AI_SERVICE/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "圖書館有多少書？",
    "history": [],
    "context": "{\"hasData\":true,\"stats\":{\"totalBooks\":20,\"availableBooks\":18,\"borrowedBooks\":2}}"
  }')

if echo "$response" | jq -e '.success == true' > /dev/null; then
    echo "✅ Test 2 passed: 使用 RAG 正常工作"
else
    echo "❌ Test 2 failed"
    echo "$response" | jq
    exit 1
fi

# 測試 3: 完整 RAG 流程（透過 Backend）
echo ""
echo "[4] 測試完整 RAG 流程（透過 Backend）..."

# 登入
login_response=$(curl -s -X POST $BACKEND/api/auth/login \
  -c $COOKIE_FILE \
  -H "Content-Type: application/json" \
  -d '{"username": "0001", "password": "1111"}')

if ! echo "$login_response" | jq -e '.success == true' > /dev/null; then
    echo "❌ 登入失敗"
    echo "$login_response" | jq
    exit 1
fi
echo "✅ 登入成功"

# 測試 RAG 查詢
chat_response=$(curl -s -X POST $BACKEND/api/chat \
  -b $COOKIE_FILE \
  -H "Content-Type: application/json" \
  -d '{"message": "我借過哪些書？", "history": []}')

if echo "$chat_response" | jq -e '.success == true' > /dev/null; then
    echo "✅ Test 3 passed: 完整 RAG 流程正常工作"
else
    echo "❌ Test 3 failed"
    echo "$chat_response" | jq
    exit 1
fi

# 測試 4: 錯誤處理（無效的 context）
echo ""
echo "[5] 測試錯誤處理（無效的 context）..."
response=$(curl -s -X POST $AI_SERVICE/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "測試",
    "history": [],
    "context": "invalid json"
  }')

# 應該降級為預設 prompt，仍然成功
if echo "$response" | jq -e '.success == true' > /dev/null; then
    echo "✅ Test 4 passed: 錯誤處理正常（自動降級）"
else
    echo "❌ Test 4 failed"
    echo "$response" | jq
    exit 1
fi

echo ""
echo "=========================================="
echo "✅ All tests passed!"
echo "=========================================="
