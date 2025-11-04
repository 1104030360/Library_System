"""
Test for RAG Prompt Builder
測試 RAG Prompt 格式化函數
"""

import sys
import os

# 添加父目錄到 path
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from rag_prompt_builder import (
    format_borrow_history,
    format_available_books,
    format_library_rules,
    format_library_stats,
    build_rag_system_prompt,
    validate_context
)


def test_format_borrow_history():
    """測試 format_borrow_history()"""
    print("\n" + "="*60)
    print("[Test 1] format_borrow_history()")
    print("="*60)

    # Test 1a: 有資料
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
    print("✅ Test 1a passed: 正常格式化借閱歷史")
    print(result)

    # Test 1b: 空資料
    empty_result = format_borrow_history([])
    assert empty_result == "無借閱記錄。"
    print("\n✅ Test 1b passed: 空資料正確處理")
    print(empty_result)


def test_format_available_books():
    """測試 format_available_books()"""
    print("\n" + "="*60)
    print("[Test 2] format_available_books()")
    print("="*60)

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
    print("✅ Test 2 passed: 正常格式化書籍列表")
    print(result)


def test_format_library_rules():
    """測試 format_library_rules()"""
    print("\n" + "="*60)
    print("[Test 3] format_library_rules()")
    print("="*60)

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
    print("✅ Test 3 passed: 正常格式化規則，按類別分組")
    print(result)


def test_format_library_stats():
    """測試 format_library_stats()"""
    print("\n" + "="*60)
    print("[Test 4] format_library_stats()")
    print("="*60)

    stats = {
        "totalBooks": 20,
        "availableBooks": 18,
        "borrowedBooks": 2
    }

    result = format_library_stats(stats)
    assert "20 本" in result
    assert "18 本" in result
    assert "2 本" in result
    print("✅ Test 4 passed: 正常格式化統計資訊")
    print(result)


def test_build_rag_system_prompt():
    """測試 build_rag_system_prompt()"""
    print("\n" + "="*60)
    print("[Test 5] build_rag_system_prompt()")
    print("="*60)

    # Test 5a: 有資料
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
    assert "20 本" in prompt
    assert "深度學習" in prompt
    assert "Python 程式設計" in prompt
    assert "借書期限" in prompt
    print("✅ Test 5a passed: 有資料時生成完整 prompt")
    print("\n生成的 Prompt (前500字):")
    print("-" * 60)
    print(prompt[:500] + "..." if len(prompt) > 500 else prompt)

    # Test 5b: 無資料
    empty_context = {"hasData": False}
    empty_prompt = build_rag_system_prompt(empty_context)
    assert "圖書館 AI 助理" in empty_prompt
    assert "沒有提供額外的圖書館資料" in empty_prompt
    print("\n✅ Test 5b passed: 無資料時生成基礎 prompt")


def test_validate_context():
    """測試 validate_context()"""
    print("\n" + "="*60)
    print("[Test 6] validate_context()")
    print("="*60)

    # Test 6a: 正確格式
    valid_context = {
        "hasData": True,
        "borrowHistory": [],
        "availableBooks": [],
        "libraryRules": [],
        "stats": {}
    }
    is_valid, error = validate_context(valid_context)
    assert is_valid == True
    assert error == ""
    print("✅ Test 6a passed: 正確格式通過驗證")

    # Test 6b: 缺少必要欄位
    invalid_context = {}
    is_valid, error = validate_context(invalid_context)
    assert is_valid == False
    assert "Missing required field" in error
    print(f"✅ Test 6b passed: 缺少欄位正確檢測 - {error}")

    # Test 6c: 錯誤類型
    invalid_type_context = {
        "hasData": True,
        "borrowHistory": "not a list"  # 應該是 list
    }
    is_valid, error = validate_context(invalid_type_context)
    assert is_valid == False
    assert "must be a list" in error
    print(f"✅ Test 6c passed: 錯誤類型正確檢測 - {error}")


def run_all_tests():
    """執行所有測試"""
    print("\n" + "="*80)
    print("RAG Prompt Builder Test Suite")
    print("="*80)

    try:
        test_format_borrow_history()
        test_format_available_books()
        test_format_library_rules()
        test_format_library_stats()
        test_build_rag_system_prompt()
        test_validate_context()

        print("\n" + "="*80)
        print("✅ All tests passed\!")
        print("="*80)

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
