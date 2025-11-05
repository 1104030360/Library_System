#!/bin/bash
# Ollama AI Service 測試運行腳本

set -e

echo "========================================"
echo "  Ollama AI Service 測試套件"
echo "========================================"
echo ""

# 檢查 pytest 是否安裝
if ! command -v pytest &> /dev/null; then
    echo "❌ pytest 未安裝"
    echo "正在安裝 pytest..."
    pip3 install pytest pytest-html requests
    echo ""
fi

# 檢查服務是否運行
echo "🔍 檢查 AI Service 狀態..."
if curl -s http://localhost:8888/health > /dev/null 2>&1; then
    echo "✅ AI Service 正在運行"
    echo ""
else
    echo "❌ AI Service 未運行"
    echo ""
    echo "請先啟動服務："
    echo "  docker-compose up ai-service -d"
    echo "或"
    echo "  python3 ollama_service_streaming.py"
    echo ""
    exit 1
fi

# 獲取當前目錄
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR/.."

# 運行測試
echo "🧪 開始運行測試..."
echo ""

# 根據參數選擇測試類型
case "${1:-all}" in
    health)
        echo "運行健康檢查測試..."
        pytest tests/test_ollama_service.py::TestOllamaService::test_health_check -v
        ;;
    recommendation)
        echo "運行推薦功能測試..."
        pytest tests/test_ollama_service.py::TestOllamaService::test_personal_recommendations -v -s
        ;;
    performance)
        echo "運行性能測試..."
        pytest tests/test_ollama_service.py::TestPerformance -v -s
        ;;
    all)
        echo "運行所有測試..."
        pytest tests/ -v
        ;;
    report)
        echo "運行測試並生成報告..."
        pytest tests/ -v --html=test-report.html --self-contained-html
        echo ""
        echo "✅ 測試報告已生成: test-report.html"
        ;;
    verbose)
        echo "運行所有測試（詳細輸出）..."
        pytest tests/ -v -s
        ;;
    *)
        echo "未知參數: $1"
        echo ""
        echo "用法:"
        echo "  ./run_tests.sh [選項]"
        echo ""
        echo "選項:"
        echo "  health         - 只運行健康檢查測試"
        echo "  recommendation - 只運行推薦功能測試"
        echo "  performance    - 只運行性能測試"
        echo "  all            - 運行所有測試（默認）"
        echo "  report         - 運行測試並生成 HTML 報告"
        echo "  verbose        - 運行所有測試（顯示詳細輸出）"
        echo ""
        exit 1
        ;;
esac

echo ""
echo "========================================"
echo "  測試完成"
echo "========================================"
