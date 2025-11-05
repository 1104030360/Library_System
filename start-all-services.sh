#!/bin/bash
# Quick Start Script for Phase 11 Chatbot Demo
# 快速啟動所有服務

echo "======================================"
echo "  Phase 11 AI Chatbot - 快速啟動"
echo "======================================"
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Step 1: Check AI Service
echo -e "${CYAN}步驟 1/3: 檢查 AI Service${NC}"
if curl -s http://localhost:8888/health > /dev/null 2>&1; then
    echo -e "${GREEN}✅ AI Service 已運行${NC}"
else
    echo -e "${YELLOW}⚠️  AI Service 未運行${NC}"
    echo ""
    echo "請在新的 Terminal 視窗執行："
    echo -e "${CYAN}cd backend/ai_service && python3 ollama_service_streaming.py${NC}"
    echo ""
    read -p "按 Enter 繼續檢查下一個服務..."
fi

echo ""

# Step 2: Check Java Backend
echo -e "${CYAN}步驟 2/3: 檢查 Java Backend${NC}"
if curl -s http://localhost:7070/api/hello > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Java Backend 已運行${NC}"
else
    echo -e "${YELLOW}⚠️  Java Backend 未運行${NC}"
    echo ""
    echo "請在新的 Terminal 視窗執行："
    echo -e "${CYAN}./start-server.sh${NC}"
    echo ""
    read -p "按 Enter 繼續檢查下一個服務..."
fi

echo ""

# Step 3: Check Frontend
echo -e "${CYAN}步驟 3/3: 檢查 Frontend${NC}"
if curl -s http://localhost:5173 > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Frontend 已運行${NC}"
else
    echo -e "${YELLOW}⚠️  Frontend 未運行${NC}"
    echo ""
    echo "請在新的 Terminal 視窗執行："
    echo -e "${CYAN}cd web && npm run dev${NC}"
    echo ""
fi

echo ""
echo "======================================"
echo "  服務狀態總結"
echo "======================================"

# Final check
AI_OK=false
BACKEND_OK=false
FRONTEND_OK=false

if curl -s http://localhost:8888/health > /dev/null 2>&1; then
    echo -e "${GREEN}✅ AI Service:      http://localhost:8888${NC}"
    AI_OK=true
else
    echo -e "${RED}❌ AI Service:      未運行${NC}"
fi

if curl -s http://localhost:7070/api/hello > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Java Backend:    http://localhost:7070${NC}"
    BACKEND_OK=true
else
    echo -e "${RED}❌ Java Backend:    未運行${NC}"
fi

if curl -s http://localhost:5173 > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Frontend:        http://localhost:5173${NC}"
    FRONTEND_OK=true
else
    echo -e "${RED}❌ Frontend:        未運行${NC}"
fi

echo ""

# Overall status
if [ "$AI_OK" = true ] && [ "$BACKEND_OK" = true ] && [ "$FRONTEND_OK" = true ]; then
    echo -e "${GREEN}🎉 所有服務已就緒！${NC}"
    echo ""
    echo "現在可以："
    echo "  1. 開啟瀏覽器: http://localhost:5173"
    echo "  2. 點擊右下角的聊天按鈕 💬"
    echo "  3. 開始與 AI 對話！"
else
    echo -e "${YELLOW}⚠️  部分服務未運行${NC}"
    echo ""
    echo "請按照上方提示啟動所需服務"
fi

echo ""
echo "======================================"
echo ""

# Quick test
if [ "$AI_OK" = true ] && [ "$BACKEND_OK" = true ]; then
    echo "🧪 快速測試聊天 API..."
    echo ""

    response=$(curl -s -X POST http://localhost:7070/api/chat \
      -H "Content-Type: application/json" \
      -d '{"message":"你好","history":[]}' 2>&1)

    if echo "$response" | grep -q "success"; then
        echo -e "${GREEN}✅ 聊天 API 測試通過！${NC}"
        echo "回應預覽:"
        echo "$response" | python3 -c "import sys, json; data = json.load(sys.stdin); print(data.get('message', '')[:100])" 2>/dev/null || echo "$response"
    else
        echo -e "${RED}❌ 聊天 API 測試失敗${NC}"
        echo "錯誤訊息:"
        echo "$response"
    fi
    echo ""
fi
