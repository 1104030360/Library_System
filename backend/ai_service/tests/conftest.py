"""
Pytest 配置文件
定義測試環境和共用 fixtures
"""

import pytest
import sys
import os

# 將父目錄加入路徑，以便導入 ollama_service_streaming
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))


def pytest_configure(config):
    """Pytest 配置"""
    print("\n" + "="*70)
    print("Ollama AI Service 測試套件")
    print("="*70)
    print("測試目標: http://localhost:8888")
    print("="*70 + "\n")


def pytest_collection_modifyitems(items):
    """修改測試項目的顯示"""
    for item in items:
        # 添加自定義標記
        if "health" in item.nodeid:
            item.add_marker(pytest.mark.health)
        elif "performance" in item.nodeid:
            item.add_marker(pytest.mark.performance)
        elif "personal" in item.nodeid or "related" in item.nodeid:
            item.add_marker(pytest.mark.recommendation)


@pytest.fixture(scope="session")
def service_url():
    """提供服務 URL"""
    return "http://localhost:8888"


@pytest.fixture(scope="session")
def sample_books():
    """提供示例書籍數據"""
    return [
        {
            "id": "001",
            "title": "深入理解計算機系統",
            "author": "Randal E. Bryant",
            "publisher": "機械工業出版社"
        },
        {
            "id": "002",
            "title": "算法導論",
            "author": "Thomas H. Cormen",
            "publisher": "機械工業出版社"
        },
        {
            "id": "003",
            "title": "設計模式",
            "author": "Erich Gamma",
            "publisher": "機械工業出版社"
        },
        {
            "id": "004",
            "title": "代碼整潔之道",
            "author": "Robert C. Martin",
            "publisher": "人民郵電出版社"
        },
        {
            "id": "005",
            "title": "重構",
            "author": "Martin Fowler",
            "publisher": "人民郵電出版社"
        }
    ]
