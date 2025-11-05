#!/bin/bash

# Start AI Service in Verbose Mode
# 啟動詳細模式的 AI 服務，可以看到 Ollama 即時生成過程

echo ""
echo "========================================="
echo "  啟動 AI 服務 - 詳細模式"
echo "========================================="
echo ""
echo "📺 你將看到："
echo "   - Ollama 即時生成的每個字"
echo "   - 彩色的日誌輸出"
echo "   - 詳細的時間戳記"
echo "   - 錯誤和重試過程"
echo ""
echo "按 Ctrl+C 停止服務"
echo "========================================="
echo ""

cd "$(dirname "$0")"
source venv/bin/activate
python3 ollama_service_verbose.py
