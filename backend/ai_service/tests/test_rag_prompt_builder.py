"""
Test for RAG Prompt Builder
測試 RAG Prompt 格式化函數
"""

import sys
import os

# 添加父目錄到 path
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from rag_prompt_builder import (
    truncate_text,
    format_borrow_history,
    format_current_borrowings,
    format_target_book,
    format_available_books,
    format_library_rules,
    format_library_stats,
    build_rag_system_prompt,
    validate_context
)


def test_truncate_text():
    """測試 truncate_text()"""
    print("\n" + "=" * 60)
    print("[Test 1] truncate_text()")
    print("=" * 60)

    text = "a" * 200
    truncated = truncate_text(text, 50)
    assert len(truncated) == 53  # 50 characters + "..."
    print("✅ Test 1 passed: 正確截斷長文字")


def test_format_borrow_history():
    """測試 format_borrow_history()"""
    print("\n" + "=" * 60)
    print("[Test 2] format_borrow_history()")
    print("=" * 60)

    history = [
        {
            "bookId": "001",
            "bookTitle": "深度學習",
            "borrowDate": "2025-10-01",
            "returnDate": "2025-10-15",
            "status": "returned"
        },
        {
            "bookId": "002",
            "bookTitle": "機器學習",
            "borrowDate": "2025-10-20",
            "returnDate": "未歸還",
            "status": "borrowed"
        }
    ]

    result = format_borrow_history(history)
    assert "借閱歷史記錄" in result
    assert "深度學習" in result
    assert "機器學習" in result
    assert "2 筆" in result
    print("✅ Test 2 passed: 正常格式化借閱歷史")
    print(result)

    empty_result = format_borrow_history([])
    assert empty_result == "無借閱記錄。"
    print("\n✅ Test 2b passed: 空資料正確處理")


def test_format_current_borrowings():
    """測試 format_current_borrowings()"""
    print("\n" + "=" * 60)
    print("[Test 3] format_current_borrowings()")
    print("=" * 60)

    current = [
        {
            "bookId": "010",
            "bookTitle": "演算法",
            "borrowDate": "2025-10-01",
            "dueDate": "2025-10-15",
            "status": "borrowed"
        },
        {
            "bookId": "011",
            "bookTitle": "資料科學",
            "borrowDate": "2025-10-05",
            "dueDate": "2025-10-19",
            "status": "overdue"
        }
    ]

    result = format_current_borrowings(current)
    assert "目前借閱中" in result
    assert "演算法" in result
    assert "資料科學" in result
    assert "2 本" in result
    print("✅ Test 3 passed: 正常格式化目前借閱清單")
    print(result)


def test_format_target_book():
    """測試 format_target_book()"""
    print("\n" + "=" * 60)
    print("[Test 4] format_target_book()")
    print("=" * 60)

    book = {
        "id": "020",
        "title": "AI 應用",
        "author": "林教授",
        "publisher": "科技出版社",
        "available": False,
        "description": "這是一本介紹人工智慧應用案例的圖書，涵蓋醫療、教育與金融等領域。",
        "borrowCount": 12,
        "averageRating": 4.5,
        "reviewCount": 6
    }

    result = format_target_book(book)
    assert "目標書籍資訊" in result
    assert "AI 應用" in result
    assert "已借出" in result
    assert "4.5" in result
    print("✅ Test 4 passed: 正常格式化目標書籍資訊")
    print(result)


def test_format_available_books():
    """測試 format_available_books()"""
    print("\n" + "=" * 60)
    print("[Test 5] format_available_books()")
    print("=" * 60)

    books = [
        {
            "id": "003",
            "title": "Python 程式設計",
            "author": "張三",
            "publisher": "XX出版社",
            "description": "這是一本適合初學者的 Python 入門書籍，內容深入淺出"
        },
        {
            "id": "004",
            "title": "資料結構",
            "author": "李四",
            "publisher": "YY出版社",
            "description": ""
        }
    ]

    result = format_available_books(books)
    assert "可借閱書籍" in result
    assert "Python 程式設計" in result
    assert "資料結構" in result
    assert "2 本" in result
    print("✅ Test 5 passed: 正常格式化書籍列表")
    print(result)


def test_format_library_rules():
    """測試 format_library_rules()"""
    print("\n" + "=" * 60)
    print("[Test 6] format_library_rules()")
    print("=" * 60)

    rules = [
        {
            "category": "借閱規則",
            "question": "借書期限是多久？",
            "answer": "一般書籍借期為 14 天。"
        },
        {
            "category": "借閱規則",
            "question": "一次可以借幾本書？",
            "answer": "使用者最多可同時借閱 3 本書。"
        },
        {
            "category": "逾期規則",
            "question": "逾期會有什麼後果？",
            "answer": "逾期書籍將影響您的借閱權限。"
        }
    ]

    result = format_library_rules(rules)
    assert "圖書館規則" in result
    assert "借閱規則" in result
    assert "逾期規則" in result
    assert "3 條" in result
    print("✅ Test 6 passed: 正常格式化規則，按類別分組")
    print(result)


def test_format_library_stats():
    """測試 format_library_stats()"""
    print("\n" + "=" * 60)
    print("[Test 7] format_library_stats()")
    print("=" * 60)

    stats = {
        "totalBooks": 20,
        "availableBooks": 18,
        "borrowedBooks": 2
    }

    result = format_library_stats(stats)
    assert "20 本" in result
    assert "18 本" in result
    assert "2 本" in result
    print("✅ Test 7 passed: 正常格式化統計資訊")
    print(result)


def test_build_rag_system_prompt():
    """測試 build_rag_system_prompt()"""
    print("\n" + "=" * 60)
    print("[Test 8] build_rag_system_prompt()")
    print("=" * 60)

    context = {
        "hasData": True,
        "stats": {
            "totalBooks": 20,
            "availableBooks": 18,
            "borrowedBooks": 2
        },
        "borrowHistory": [
            {
                "bookId": "001",
                "bookTitle": "深度學習",
                "borrowDate": "2025-10-01",
                "returnDate": "2025-10-15",
                "status": "returned"
            }
        ],
        "currentBorrowings": [
            {
                "bookId": "010",
                "bookTitle": "演算法",
                "borrowDate": "2025-10-01",
                "dueDate": "2025-10-15",
                "status": "borrowed"
            }
        ],
        "targetBook": {
            "id": "020",
            "title": "AI 應用",
            "author": "林教授",
            "publisher": "科技出版社",
            "available": False,
            "description": "這是一本介紹人工智慧應用案例的圖書。",
            "borrowCount": 12,
            "averageRating": 4.5,
            "reviewCount": 6
        },
        "availableBooks": [
            {
                "id": "003",
                "title": "Python 程式設計",
                "author": "張三",
                "publisher": "XX出版社",
                "description": "Python 入門書"
            }
        ],
        "libraryRules": [
            {
                "category": "借閱規則",
                "question": "借書期限是多久？",
                "answer": "一般書籍借期為 14 天。"
            }
        ]
    }

    prompt = build_rag_system_prompt(context)
    assert "圖書館 AI 助理" in prompt
    assert "即時圖書館資料" in prompt
    assert "目前借閱中" in prompt
    assert "目標書籍資訊" in prompt
    assert "Python 程式設計" in prompt
    assert "借書期限" in prompt
    print("✅ Test 8a passed: 有資料時生成完整 prompt")
    print("\n生成的 Prompt (前500字):")
    print("-" * 60)
    print(prompt[:500] + "..." if len(prompt) > 500 else prompt)

    empty_prompt = build_rag_system_prompt({"hasData": False})
    assert "沒有提供額外的圖書館資料" in empty_prompt
    print("\n✅ Test 8b passed: 無資料時生成基礎 prompt")


def test_validate_context():
    """測試 validate_context()"""
    print("\n" + "=" * 60)
    print("[Test 9] validate_context()")
    print("=" * 60)

    valid_context = {
        "hasData": True,
        "borrowHistory": [],
        "availableBooks": [],
        "libraryRules": [],
        "stats": {},
        "currentBorrowings": [],
        "targetBook": {}
    }
    is_valid, error = validate_context(valid_context)
    assert is_valid is True
    assert error == ""
    print("✅ Test 9a passed: 正確格式通過驗證")

    missing_field_context = {}
    is_valid, error = validate_context(missing_field_context)
    assert is_valid is False
    assert "Missing required field" in error
    print(f"✅ Test 9b passed: 缺少欄位正確檢測 - {error}")

    invalid_type_context = {
        "hasData": True,
        "borrowHistory": "not a list"
    }
    is_valid, error = validate_context(invalid_type_context)
    assert is_valid is False
    assert "must be a list" in error
    print(f"✅ Test 9c passed: 錯誤類型正確檢測 - {error}")


def run_all_tests():
    """執行所有測試"""
    print("\n" + "=" * 80)
    print("RAG Prompt Builder Test Suite")
    print("=" * 80)

    try:
        test_truncate_text()
        test_format_borrow_history()
        test_format_current_borrowings()
        test_format_target_book()
        test_format_available_books()
        test_format_library_rules()
        test_format_library_stats()
        test_build_rag_system_prompt()
        test_validate_context()

        print("\n" + "=" * 80)
        print("✅ All tests passed!")
        print("=" * 80)

    except AssertionError as e:
        print(f"\n❌ Test failed: {e}")
        import traceback
        traceback.print_exc()
        return False
    except Exception as e:
        print(f"\n❌ Unexpected error: {e}")
        import traceback
        traceback.print_exc()
        return False

    return True


if __name__ == "__main__":
    success = run_all_tests()
    sys.exit(0 if success else 1)
