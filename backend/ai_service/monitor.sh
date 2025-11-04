#!/bin/bash

# AI Service Monitor Script
# 監控 AI 推薦服務的即時狀態

echo "========================================="
echo "  AI 推薦服務監控器"
echo "========================================="
echo ""
echo "監控內容："
echo "  - AI 呼叫請求"
echo "  - 推薦生成過程"
echo "  - 錯誤與重試"
echo "  - 效能指標"
echo ""
echo "按 Ctrl+C 停止監控"
echo "========================================="
echo ""

# Activate virtual environment and run service with verbose logging
cd "$(dirname "$0")"
source venv/bin/activate

# Run Flask with debug mode for detailed logging
export FLASK_ENV=development
python3 ollama_service.py
