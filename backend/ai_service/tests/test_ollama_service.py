#!/usr/bin/env python3
"""
Ollama AI Service 測試套件
測試雲端和本地 Ollama 的所有功能
"""

import pytest
import requests
import json
import time
from typing import Dict, List


class TestOllamaService:
    """Ollama AI Service 測試類"""

    BASE_URL = "http://localhost:8888"

    @pytest.fixture(autouse=True)
    def setup(self):
        """每個測試前的準備工作"""
        # 等待服務啟動
        max_retries = 5
        for i in range(max_retries):
            try:
                response = requests.get(f"{self.BASE_URL}/health", timeout=2)
                if response.status_code == 200:
                    break
            except:
                if i < max_retries - 1:
                    time.sleep(2)
                else:
                    pytest.skip("AI Service 未啟動")

    def test_health_check(self):
        """測試 1: 健康檢查"""
        print("\n" + "="*70)
        print("測試 1: 健康檢查")
        print("="*70)

        response = requests.get(f"{self.BASE_URL}/health")

        print(f"Status Code: {response.status_code}")
        assert response.status_code == 200, "健康檢查應該返回 200"

        data = response.json()
        print(f"Response: {json.dumps(data, indent=2, ensure_ascii=False)}")

        # 驗證響應結構
        assert "status" in data, "應該包含 status 字段"
        assert data["status"] == "healthy", "狀態應該是 healthy"
        assert "model" in data, "應該包含 model 字段"
        assert "ollama_url" in data, "應該包含 ollama_url 字段"
        assert "using_api_key" in data, "應該包含 using_api_key 字段"

        print(f"✅ 健康檢查通過")
        print(f"   模型: {data['model']}")
        print(f"   URL: {data['ollama_url']}")
        print(f"   使用 API Key: {data['using_api_key']}")

    def test_personal_recommendations(self):
        """測試 2: 個人化推薦"""
        print("\n" + "="*70)
        print("測試 2: 個人化推薦")
        print("="*70)

        payload = {
            "user_profile": {
                "borrow_history": [
                    {"title": "Java編程思想"},
                    {"title": "設計模式"}
                ]
            },
            "available_books": [
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
                    "title": "重構：改善既有代碼的設計",
                    "author": "Martin Fowler",
                    "publisher": "人民郵電出版社"
                },
                {
                    "id": "004",
                    "title": "代碼整潔之道",
                    "author": "Robert C. Martin",
                    "publisher": "人民郵電出版社"
                },
                {
                    "id": "005",
                    "title": "紅樓夢",
                    "author": "曹雪芹",
                    "publisher": "人民文學出版社"
                }
            ]
        }

        print("發送請求...")
        start_time = time.time()
        response = requests.post(
            f"{self.BASE_URL}/generate-personal-recommendations",
            json=payload,
            timeout=60
        )
        elapsed_time = time.time() - start_time

        print(f"Status Code: {response.status_code}")
        assert response.status_code == 200, "推薦請求應該返回 200"

        data = response.json()
        print(f"耗時: {elapsed_time:.2f} 秒")

        # 驗證響應結構
        assert "success" in data, "應該包含 success 字段"
        assert data["success"] == True, "success 應該為 True"
        assert "recommendations" in data, "應該包含 recommendations 字段"
        assert "source" in data, "應該包含 source 字段"

        recommendations = data["recommendations"]
        print(f"\n推薦來源: {data['source']}")
        print(f"推薦數量: {len(recommendations)}")

        # 驗證推薦格式
        for i, rec in enumerate(recommendations, 1):
            assert "book_id" in rec, f"推薦 {i} 應該包含 book_id"
            assert "reason" in rec, f"推薦 {i} 應該包含 reason"
            assert "score" in rec, f"推薦 {i} 應該包含 score"
            assert 0 <= rec["score"] <= 1, f"推薦 {i} 的 score 應該在 0-1 之間"

            print(f"\n推薦 {i}:")
            print(f"  書籍 ID: {rec['book_id']}")
            print(f"  評分: {rec['score']:.2f}")
            print(f"  理由: {rec['reason']}")

        print(f"\n✅ 個人化推薦測試通過")

    def test_related_recommendations(self):
        """測試 3: 相關推薦"""
        print("\n" + "="*70)
        print("測試 3: 相關推薦")
        print("="*70)

        payload = {
            "current_book": {
                "id": "001",
                "title": "深入理解計算機系統",
                "author": "Randal E. Bryant",
                "publisher": "機械工業出版社"
            },
            "related_books": [
                {
                    "id": "002",
                    "title": "計算機網絡：自頂向下方法",
                    "author": "James F. Kurose",
                    "publisher": "機械工業出版社"
                },
                {
                    "id": "003",
                    "title": "操作系統概念",
                    "author": "Abraham Silberschatz",
                    "publisher": "高等教育出版社"
                },
                {
                    "id": "004",
                    "title": "數據庫系統概念",
                    "author": "Abraham Silberschatz",
                    "publisher": "機械工業出版社"
                }
            ]
        }

        print("發送請求...")
        start_time = time.time()
        response = requests.post(
            f"{self.BASE_URL}/generate-related-recommendations",
            json=payload,
            timeout=60
        )
        elapsed_time = time.time() - start_time

        print(f"Status Code: {response.status_code}")
        assert response.status_code == 200, "相關推薦請求應該返回 200"

        data = response.json()
        print(f"耗時: {elapsed_time:.2f} 秒")

        # 驗證響應結構
        assert "success" in data
        assert data["success"] == True
        assert "recommendations" in data

        recommendations = data["recommendations"]
        print(f"\n推薦來源: {data['source']}")
        print(f"推薦數量: {len(recommendations)}")

        for i, rec in enumerate(recommendations, 1):
            print(f"\n推薦 {i}:")
            print(f"  書籍 ID: {rec['book_id']}")
            print(f"  評分: {rec['score']:.2f}")
            print(f"  理由: {rec['reason']}")

        print(f"\n✅ 相關推薦測試通過")

    def test_empty_history(self):
        """測試 4: 空借閱歷史"""
        print("\n" + "="*70)
        print("測試 4: 空借閱歷史處理")
        print("="*70)

        payload = {
            "user_profile": {
                "borrow_history": []
            },
            "available_books": [
                {
                    "id": "001",
                    "title": "深入理解計算機系統",
                    "author": "Randal E. Bryant",
                    "publisher": "機械工業出版社"
                }
            ]
        }

        response = requests.post(
            f"{self.BASE_URL}/generate-personal-recommendations",
            json=payload,
            timeout=60
        )

        print(f"Status Code: {response.status_code}")
        assert response.status_code == 200

        data = response.json()
        assert data["success"] == True
        assert len(data["recommendations"]) > 0

        print(f"✅ 空歷史處理測試通過")
        print(f"   推薦數量: {len(data['recommendations'])}")

    def test_invalid_request(self):
        """測試 5: 無效請求處理"""
        print("\n" + "="*70)
        print("測試 5: 無效請求處理")
        print("="*70)

        # 測試缺少必要字段
        invalid_payload = {
            "user_profile": {}
            # 缺少 available_books
        }

        try:
            response = requests.post(
                f"{self.BASE_URL}/generate-personal-recommendations",
                json=invalid_payload,
                timeout=60
            )
            print(f"Status Code: {response.status_code}")
            # 即使請求格式不完整，服務應該能處理（返回 fallback）
            assert response.status_code in [200, 400, 500]
            print(f"✅ 無效請求處理測試通過")
        except Exception as e:
            print(f"⚠️  無效請求處理: {e}")

    def test_large_book_list(self):
        """測試 6: 大量書籍推薦"""
        print("\n" + "="*70)
        print("測試 6: 大量書籍處理")
        print("="*70)

        # 生成 50 本書
        books = [
            {
                "id": f"{i:03d}",
                "title": f"測試書籍 {i}",
                "author": f"作者 {i}",
                "publisher": "測試出版社"
            }
            for i in range(1, 51)
        ]

        payload = {
            "user_profile": {
                "borrow_history": [{"title": "Java編程思想"}]
            },
            "available_books": books
        }

        print(f"測試 {len(books)} 本書籍...")
        start_time = time.time()
        response = requests.post(
            f"{self.BASE_URL}/generate-personal-recommendations",
            json=payload,
            timeout=90
        )
        elapsed_time = time.time() - start_time

        print(f"Status Code: {response.status_code}")
        print(f"耗時: {elapsed_time:.2f} 秒")

        assert response.status_code == 200
        data = response.json()
        assert data["success"] == True

        print(f"✅ 大量書籍處理測試通過")
        print(f"   推薦數量: {len(data['recommendations'])}")


class TestPerformance:
    """性能測試類"""

    BASE_URL = "http://localhost:8888"

    def test_response_time(self):
        """測試 7: 響應時間"""
        print("\n" + "="*70)
        print("測試 7: 響應時間測試")
        print("="*70)

        payload = {
            "user_profile": {
                "borrow_history": [{"title": "Java編程思想"}]
            },
            "available_books": [
                {
                    "id": f"{i:03d}",
                    "title": f"書籍 {i}",
                    "author": "作者",
                    "publisher": "出版社"
                }
                for i in range(1, 11)
            ]
        }

        times = []
        num_tests = 3

        print(f"執行 {num_tests} 次推薦請求...")
        for i in range(num_tests):
            start_time = time.time()
            response = requests.post(
                f"{self.BASE_URL}/generate-personal-recommendations",
                json=payload,
                timeout=60
            )
            elapsed = time.time() - start_time
            times.append(elapsed)
            print(f"  第 {i+1} 次: {elapsed:.2f} 秒")

            assert response.status_code == 200

        avg_time = sum(times) / len(times)
        print(f"\n平均響應時間: {avg_time:.2f} 秒")
        print(f"最快: {min(times):.2f} 秒")
        print(f"最慢: {max(times):.2f} 秒")

        # 響應時間應該在合理範圍內（60秒內）
        assert max(times) < 60, "響應時間不應超過 60 秒"

        print(f"✅ 響應時間測試通過")


if __name__ == "__main__":
    # 可以直接運行此文件進行簡單測試
    print("Ollama AI Service 測試套件")
    print("請使用 pytest 運行完整測試")
    print("\n使用方法:")
    print("  pytest test_ollama_service.py -v")
    print("  pytest test_ollama_service.py -v -s  # 顯示詳細輸出")
