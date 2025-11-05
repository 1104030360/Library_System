#!/usr/bin/env python3
"""
即時測試 AI 推薦服務
可以看到完整的請求/回應過程
"""

import requests
import json
import time
from datetime import datetime

BASE_URL = "http://localhost:8888"

def print_header(title):
    print("\n" + "="*60)
    print(f"  {title}")
    print("="*60)

def print_timestamp():
    return datetime.now().strftime("%H:%M:%S")

def test_personal_recommendations():
    """測試個人化推薦並顯示詳細資訊"""
    print_header("測試個人化推薦")

    # 先登入取得 session
    print(f"[{print_timestamp()}] 1. 登入中...")
    login_data = {"username": "0001", "password": "1111"}
    r_login = requests.post("http://localhost:7070/api/auth/login", json=login_data)

    if r_login.status_code != 200:
        print(f"❌ 登入失敗: {r_login.status_code}")
        return

    print(f"[{print_timestamp()}] ✅ 登入成功")
    cookies = r_login.cookies

    # 發送推薦請求
    print(f"[{print_timestamp()}] 2. 發送推薦請求到 Java Backend...")
    print(f"   URL: http://localhost:7070/api/recommendations/personal")

    start_time = time.time()

    try:
        r = requests.get(
            "http://localhost:7070/api/recommendations/personal",
            cookies=cookies,
            timeout=90
        )

        elapsed = time.time() - start_time

        print(f"[{print_timestamp()}] 3. 收到回應")
        print(f"   狀態碼: {r.status_code}")
        print(f"   耗時: {elapsed:.2f} 秒")

        if r.status_code == 200:
            data = r.json()
            print(f"\n推薦結果:")
            print(f"  成功: {data.get('success')}")
            print(f"  推薦數量: {len(data.get('recommendations', []))}")
            print(f"\n詳細推薦:")

            for i, rec in enumerate(data.get('recommendations', []), 1):
                book = rec.get('book', {})
                print(f"\n  {i}. 《{book.get('title')}》")
                print(f"     作者: {book.get('author')}")
                print(f"     推薦理由: {rec.get('reason')}")
                print(f"     推薦分數: {rec.get('score'):.2f}")
                print(f"     可借閱: {'是' if book.get('isAvailable') else '否'}")
        else:
            print(f"\n❌ 請求失敗: {r.text}")

    except requests.exceptions.Timeout:
        print(f"[{print_timestamp()}] ⏱️  請求超時（超過 90 秒）")
    except Exception as e:
        print(f"[{print_timestamp()}] ❌ 錯誤: {e}")

def test_related_recommendations():
    """測試相關推薦"""
    print_header("測試相關推薦")

    book_id = "001"
    print(f"[{print_timestamp()}] 1. 發送相關推薦請求...")
    print(f"   書籍 ID: {book_id}")
    print(f"   URL: http://localhost:7070/api/recommendations/related?bookId={book_id}")

    start_time = time.time()

    try:
        r = requests.get(
            f"http://localhost:7070/api/recommendations/related?bookId={book_id}",
            timeout=90
        )

        elapsed = time.time() - start_time

        print(f"[{print_timestamp()}] 2. 收到回應")
        print(f"   狀態碼: {r.status_code}")
        print(f"   耗時: {elapsed:.2f} 秒")

        if r.status_code == 200:
            data = r.json()
            print(f"\n相關推薦結果:")
            print(f"  成功: {data.get('success')}")
            print(f"  推薦數量: {len(data.get('recommendations', []))}")

            for i, rec in enumerate(data.get('recommendations', []), 1):
                book = rec.get('book', {})
                print(f"\n  {i}. 《{book.get('title')}》")
                print(f"     推薦理由: {rec.get('reason')}")
                print(f"     推薦分數: {rec.get('score'):.2f}")
        else:
            print(f"\n❌ 請求失敗: {r.text}")

    except Exception as e:
        print(f"[{print_timestamp()}] ❌ 錯誤: {e}")

def test_health():
    """測試健康檢查"""
    print_header("測試 AI 服務健康狀態")

    print(f"[{print_timestamp()}] 檢查 Python AI Service...")

    try:
        r = requests.get(f"{BASE_URL}/health", timeout=5)
        print(f"   狀態碼: {r.status_code}")
        print(f"   回應: {r.json()}")
    except Exception as e:
        print(f"   ❌ AI Service 無法連線: {e}")
        return False

    print(f"\n[{print_timestamp()}] 檢查 Java Backend...")

    try:
        r = requests.get("http://localhost:7070/api/recommendations/health", timeout=5)
        print(f"   狀態碼: {r.status_code}")
        print(f"   回應: {r.json()}")
    except Exception as e:
        print(f"   ❌ Backend 無法連線: {e}")
        return False

    return True

def monitor_mode():
    """持續監控模式"""
    print_header("AI 推薦服務監控模式")
    print("\n持續監控 AI 服務狀態...")
    print("按 Ctrl+C 停止\n")

    try:
        while True:
            try:
                r = requests.get(f"{BASE_URL}/health", timeout=2)
                status = "🟢 運行中" if r.status_code == 200 else "🔴 異常"
                print(f"[{print_timestamp()}] AI Service: {status}", end="\r")
                time.sleep(2)
            except:
                print(f"[{print_timestamp()}] AI Service: 🔴 離線      ", end="\r")
                time.sleep(2)
    except KeyboardInterrupt:
        print("\n\n監控已停止")

if __name__ == '__main__':
    import sys

    print("""
╔═══════════════════════════════════════════════════════════╗
║           AI 推薦服務即時測試工具                         ║
╚═══════════════════════════════════════════════════════════╝

選擇測試項目:
  1. 健康檢查
  2. 個人化推薦（需登入）
  3. 相關推薦
  4. 持續監控模式
  5. 完整測試（全部）
    """)

    choice = input("請選擇 (1-5): ").strip()

    if choice == "1":
        test_health()
    elif choice == "2":
        test_personal_recommendations()
    elif choice == "3":
        test_related_recommendations()
    elif choice == "4":
        monitor_mode()
    elif choice == "5":
        test_health()
        test_personal_recommendations()
        test_related_recommendations()
    else:
        print("無效選擇")

    print("\n測試完成！")
