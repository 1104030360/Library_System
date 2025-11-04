#!/bin/bash
# Test Chat API - Linus style: simple and direct

echo "======================================"
echo "  Chat API 測試腳本"
echo "======================================"
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "📋 測試清單:"
echo "  1. Python AI Service (/chat)"
echo "  2. Java Backend API (/api/chat)"
echo "  3. 多輪對話測試"
echo ""

# Test 1: Python AI Service
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "測試 1: Python AI Service /chat"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if curl -s http://localhost:8888/health > /dev/null 2>&1; then
    echo -e "${GREEN}✅ AI Service 正在運行${NC}"
    echo ""
    echo "📨 發送測試訊息: \"如何借書？\""

    response=$(curl -s -X POST http://localhost:8888/chat \
      -H "Content-Type: application/json" \
      -d '{
        "message": "如何借書？",
        "history": []
      }')

    if echo "$response" | grep -q "success"; then
        echo -e "${GREEN}✅ 測試 1 通過${NC}"
        echo "回應:"
        echo "$response" | python3 -m json.tool 2>/dev/null || echo "$response"
    else
        echo -e "${RED}❌ 測試 1 失敗${NC}"
        echo "$response"
    fi
else
    echo -e "${YELLOW}⚠️  AI Service 未運行，跳過測試 1${NC}"
    echo "請先啟動: cd backend/ai_service && python3 ollama_service_streaming.py"
fi

echo ""
echo ""

# Test 2: Java Backend API
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "測試 2: Java Backend /api/chat"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if curl -s http://localhost:7070/api/hello > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Java Backend 正在運行${NC}"
    echo ""
    echo "📨 發送測試訊息: \"一次可以借幾本書？\""

    response=$(curl -s -X POST http://localhost:7070/api/chat \
      -H "Content-Type: application/json" \
      -d '{
        "message": "一次可以借幾本書？",
        "history": []
      }')

    if echo "$response" | grep -q "success"; then
        echo -e "${GREEN}✅ 測試 2 通過${NC}"
        echo "回應:"
        echo "$response" | python3 -m json.tool 2>/dev/null || echo "$response"
    else
        echo -e "${RED}❌ 測試 2 失敗${NC}"
        echo "$response"
    fi
else
    echo -e "${YELLOW}⚠️  Java Backend 未運行，跳過測試 2${NC}"
    echo "請先啟動: ./start-server.sh"
fi

echo ""
echo ""

# Test 3: Multi-turn conversation
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "測試 3: 多輪對話"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if curl -s http://localhost:7070/api/hello > /dev/null 2>&1; then
    echo "第一輪: 如何借書？"
    first_response=$(curl -s -X POST http://localhost:7070/api/chat \
      -H "Content-Type: application/json" \
      -d '{
        "message": "如何借書？",
        "history": []
      }')

    first_message=$(echo "$first_response" | python3 -c "import sys, json; print(json.load(sys.stdin).get('message', ''))" 2>/dev/null)

    echo "回應: ${first_message:0:100}..."
    echo ""

    echo "第二輪: 那還書呢？（應該記得上一輪對話）"
    second_response=$(curl -s -X POST http://localhost:7070/api/chat \
      -H "Content-Type: application/json" \
      -d "{
        \"message\": \"那還書呢？\",
        \"history\": [
          {\"role\": \"user\", \"content\": \"如何借書？\"},
          {\"role\": \"assistant\", \"content\": \"$(echo $first_message | sed 's/"/\\"/g')\"}
        ]
      }")

    second_message=$(echo "$second_response" | python3 -c "import sys, json; print(json.load(sys.stdin).get('message', ''))" 2>/dev/null)

    if [ -n "$second_message" ]; then
        echo -e "${GREEN}✅ 測試 3 通過${NC}"
        echo "回應: ${second_message:0:100}..."
    else
        echo -e "${RED}❌ 測試 3 失敗${NC}"
    fi
else
    echo -e "${YELLOW}⚠️  服務未運行，跳過測試 3${NC}"
fi

echo ""
echo ""
echo "======================================"
echo "  測試完成"
echo "======================================"
echo ""
echo "💡 提示:"
echo "  - 如果 AI Service 未運行: cd backend/ai_service && python3 ollama_service_streaming.py"
echo "  - 如果 Java Backend 未運行: ./start-server.sh"
echo "  - 確保 Ollama 已啟動 (本地或雲端)"
echo ""
