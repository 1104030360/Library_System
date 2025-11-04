"""
RAG Prompt Builder - 構建 RAG 系統提示詞
將 Java 後端傳來的 context 資料格式化為結構化的 system prompt
"""

from typing import Dict, List, Any, Optional
from datetime import datetime


def format_borrow_history(history: List[Dict[str, Any]]) -> str:
    """
    格式化借閱歷史記錄
    
    Args:
        history: 借閱歷史列表
        
    Returns:
        格式化的借閱歷史字串
    """
    if not history:
        return "無借閱記錄。"
    
    result = []
    result.append(f"借閱歷史記錄（共 {len(history)} 筆）：")
    
    for idx, record in enumerate(history, 1):
        book_id = record.get("bookId", "N/A")
        book_title = record.get("bookTitle", "未知書籍")
        borrow_date = record.get("borrowDate", "N/A")
        return_date = record.get("returnDate", "未歸還")
        status = record.get("status", "unknown")
        
        status_text = {
            "borrowed": "借閱中",
            "returned": "已歸還",
            "overdue": "逾期"
        }.get(status, status)
        
        result.append(
            f"{idx}. 《{book_title}》(ID: {book_id}) - "
            f"借出: {borrow_date}, 歸還: {return_date}, 狀態: {status_text}"
        )
    
    return "\n".join(result)


def format_available_books(books: List[Dict[str, Any]], limit: int = 20) -> str:
    """
    格式化可借閱書籍列表
    
    Args:
        books: 書籍列表
        limit: 最多顯示幾本書
        
    Returns:
        格式化的書籍列表字串
    """
    if not books:
        return "目前沒有可借閱的書籍。"
    
    result = []
    display_books = books[:limit]
    
    result.append(f"可借閱書籍（共 {len(books)} 本，顯示前 {len(display_books)} 本）：")
    
    for idx, book in enumerate(display_books, 1):
        book_id = book.get("id", "N/A")
        title = book.get("title", "未知書名")
        author = book.get("author", "未知作者")
        publisher = book.get("publisher", "未知出版社")
        description = book.get("description", "")
        
        book_info = f"{idx}. 《{title}》(ID: {book_id}) - {author} / {publisher}"
        
        if description and len(description) > 0:
            # 限制描述長度
            short_desc = description[:50] + "..." if len(description) > 50 else description
            book_info += f"\n   簡介: {short_desc}"
        
        result.append(book_info)
    
    if len(books) > limit:
        result.append(f"... 還有 {len(books) - limit} 本書籍未顯示")
    
    return "\n".join(result)


def format_library_rules(rules: List[Dict[str, Any]]) -> str:
    """
    格式化圖書館規則
    
    Args:
        rules: 規則列表
        
    Returns:
        格式化的規則字串
    """
    if not rules:
        return "無相關規則。"
    
    result = []
    result.append(f"圖書館規則（共 {len(rules)} 條）：")
    
    # 按類別分組
    by_category: Dict[str, List[Dict[str, Any]]] = {}
    for rule in rules:
        category = rule.get("category", "其他")
        if category not in by_category:
            by_category[category] = []
        by_category[category].append(rule)
    
    # 輸出每個類別的規則
    for category, category_rules in by_category.items():
        result.append(f"\n【{category}】")
        for idx, rule in enumerate(category_rules, 1):
            question = rule.get("question", "")
            answer = rule.get("answer", "")
            result.append(f"{idx}. {question}")
            result.append(f"   {answer}")
    
    return "\n".join(result)


def format_library_stats(stats: Dict[str, int]) -> str:
    """
    格式化圖書館統計資訊
    
    Args:
        stats: 統計資訊
        
    Returns:
        格式化的統計資訊字串
    """
    total = stats.get("totalBooks", 0)
    available = stats.get("availableBooks", 0)
    borrowed = stats.get("borrowedBooks", 0)
    
    return (
        f"圖書館統計資訊：\n"
        f"- 總藏書: {total} 本\n"
        f"- 可借閱: {available} 本\n"
        f"- 已借出: {borrowed} 本"
    )


def build_rag_system_prompt(context: Dict[str, Any]) -> str:
    """
    構建完整的 RAG System Prompt
    
    Args:
        context: ChatContext 資料
        
    Returns:
        完整的 system prompt 字串
    """
    # 基礎 system prompt
    base_prompt = """你是一個專業的圖書館 AI 助理，負責協助使用者處理圖書館相關問題。

你的職責：
1. 回答圖書館規則和常見問題
2. 協助使用者查詢書籍資訊
3. 提供借閱歷史查詢
4. 說明借還書流程
5. 提供圖書館統計資訊

回答原則：
- 基於以下提供的即時資料回答問題
- 如果資料中沒有相關資訊，誠實告知使用者
- 回答要簡潔、準確、友善
- 使用繁體中文回答
- 涉及具體書籍時，提供書籍 ID 方便使用者操作"""
    
    # 檢查是否有資料
    has_data = context.get("hasData", False)
    
    if not has_data:
        # 沒有額外資料，只返回基礎 prompt
        return base_prompt + "\n\n注意：目前沒有提供額外的圖書館資料，請根據一般常識回答。"
    
    # 有資料，構建完整的 prompt
    sections = [base_prompt, "\n" + "="*60, "以下是即時圖書館資料：", "="*60 + "\n"]
    
    # 1. 圖書館統計資訊（總是顯示）
    stats = context.get("stats")
    if stats:
        sections.append(format_library_stats(stats))
        sections.append("")
    
    # 2. 使用者借閱歷史
    borrow_history = context.get("borrowHistory")
    if borrow_history:
        sections.append(format_borrow_history(borrow_history))
        sections.append("")
    
    # 3. 可借閱書籍
    available_books = context.get("availableBooks")
    if available_books:
        sections.append(format_available_books(available_books))
        sections.append("")
    
    # 4. 圖書館規則
    library_rules = context.get("libraryRules")
    if library_rules:
        sections.append(format_library_rules(library_rules))
        sections.append("")
    
    # 5. 結束提示
    sections.append("="*60)
    sections.append("請基於以上資料回答使用者的問題。")
    
    return "\n".join(sections)


def validate_context(context: Dict[str, Any]) -> tuple:
    """
    驗證 context 資料格式
    
    Args:
        context: ChatContext 資料
        
    Returns:
        (is_valid, error_message)
    """
    if not isinstance(context, dict):
        return False, "Context must be a dictionary"
    
    # 檢查必要欄位
    required_fields = ["hasData"]
    for field in required_fields:
        if field not in context:
            return False, f"Missing required field: {field}"
    
    # 如果有資料，檢查各個欄位的類型
    if context.get("hasData"):
        if "borrowHistory" in context and not isinstance(context["borrowHistory"], list):
            return False, "borrowHistory must be a list"
        
        if "availableBooks" in context and not isinstance(context["availableBooks"], list):
            return False, "availableBooks must be a list"
        
        if "libraryRules" in context and not isinstance(context["libraryRules"], list):
            return False, "libraryRules must be a list"
        
        if "stats" in context and not isinstance(context["stats"], dict):
            return False, "stats must be a dictionary"
    
    return True, ""


# 測試用範例
if __name__ == "__main__":
    # 測試範例資料
    test_context = {
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
                "description": "這是一本適合初學者的 Python 入門書籍"
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
    
    # 驗證資料
    is_valid, error = validate_context(test_context)
    print(f"Context validation: {is_valid}")
    if not is_valid:
        print(f"Error: {error}")
    
    # 構建 prompt
    print("\n" + "="*80)
    print("Generated RAG System Prompt:")
    print("="*80)
    prompt = build_rag_system_prompt(test_context)
    print(prompt)
    print("\n" + "="*80)
