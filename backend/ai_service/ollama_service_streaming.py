#!/usr/bin/env python3
"""
Ollama AI Recommendation Service - Enhanced Streaming & Verbose Mode
融合版本：串流生成 + 詳細日誌 + 彩色輸出
支持本地 Ollama 和云端 Ollama API (使用官方 Ollama Python Client)
"""

from flask import Flask, request, jsonify
from flask_cors import CORS
import json
import time
import re
import os
import requests
from datetime import datetime
from dotenv import load_dotenv
from ollama import Client

# Load .env file
load_dotenv()

app = Flask(__name__)
CORS(app)

# Configuration
OLLAMA_URL = os.getenv("OLLAMA_URL", "http://localhost:11434")
OLLAMA_API_KEY = os.getenv("OLLAMA_API_KEY", "")
MODEL = os.getenv("MODEL", "llama3.2:latest")
MAX_RETRIES = int(os.getenv("MAX_RETRIES", "2"))
RETRY_DELAY = int(os.getenv("RETRY_DELAY", "1"))


def print_timestamp():
    """返回格式化的時間戳"""
    return datetime.now().strftime("%H:%M:%S.%f")[:-3]


def print_log(emoji, message, color=""):
    """Print colored log with timestamp"""
    colors = {
        "blue": "\033[94m",
        "green": "\033[92m",
        "yellow": "\033[93m",
        "red": "\033[91m",
        "cyan": "\033[96m",
        "magenta": "\033[95m",
        "reset": "\033[0m"
    }
    c = colors.get(color, "")
    reset = colors["reset"] if color else ""
    print(f"{c}[{print_timestamp()}] {emoji} {message}{reset}", flush=True)


class OllamaStreamClient:
    """Ollama client using official Ollama Python library"""

    def __init__(self, url=OLLAMA_URL, model=MODEL, api_key=OLLAMA_API_KEY):
        self.url = url
        self.model = model
        self.api_key = api_key

        # Initialize Ollama client
        # If API key is provided, pass it via headers for cloud authentication
        client_kwargs = {"host": url}
        if api_key:
            # Pass API key as Bearer token in Authorization header
            client_kwargs["headers"] = {
                "Authorization": f"Bearer {api_key}"
            }
            print_log("🔑", f"使用 API Key 连接雲端 Ollama: {url}", "cyan")
        else:
            print_log("🏠", f"连接本地 Ollama: {url}", "cyan")

        self.client = Client(**client_kwargs)

    def _clean_json(self, text):
        """Remove markdown code blocks from response"""
        text = re.sub(r'```json\s*', '', text)
        text = re.sub(r'```\s*', '', text)
        return text.strip()

    def _validate_recommendations(self, data):
        """Validate recommendation data structure"""
        if not isinstance(data, list):
            return False
        for item in data:
            required_keys = ['book_id', 'reason', 'score']
            if not all(k in item for k in required_keys):
                return False
        return True

    def generate_stream(self, prompt, temp=0.7, retries=MAX_RETRIES):
        """Generate recommendations with streaming output using Ollama"""
        last_error = None

        for attempt in range(retries):
            try:
                print_log(
                    "🎯",
                    f"Ollama 呼叫 (嘗試 {attempt + 1}/{retries})",
                    "cyan"
                )
                print_log("📝", f"模型: {self.model}", "blue")
                print_log("🌡️", f"Temperature: {temp}", "blue")

                # Show prompt preview
                preview_len = 200
                prompt_preview = (
                    prompt[:preview_len] + "..."
                    if len(prompt) > preview_len
                    else prompt
                )
                print_log("💬", "Prompt 預覽:", "magenta")
                print(f"    {prompt_preview}")
                print()

                print_log("🚀", "開始 Ollama 串流生成...", "yellow")
                print_log("📡", "=" * 60, "yellow")

                # Prepare messages in OpenAI-compatible format
                messages = [
                    {
                        'role': 'user',
                        'content': prompt,
                    }
                ]

                # Stream response from Ollama using official client
                full_content = ""
                chunk_count = 0

                # Green color for AI output
                print("\n\033[92m", end="", flush=True)

                for part in self.client.chat(
                    model=self.model,
                    messages=messages,
                    stream=True,
                    options={'temperature': temp}
                ):
                    if 'message' in part and 'content' in part['message']:
                        content = part['message']['content']
                        print(content, end="", flush=True)
                        full_content += content
                        chunk_count += 1

                # Reset color
                print("\033[0m\n", flush=True)
                print_log("📡", "=" * 60, "yellow")
                print_log(
                    "✅",
                    f"Ollama 生成完成！共 {chunk_count} 個片段",
                    "green"
                )
                print_log("📊", f"總長度: {len(full_content)} 字元", "blue")
                print()

                # Clean and parse JSON
                print_log("🔧", "清理和解析 JSON...", "blue")
                content = self._clean_json(full_content)

                try:
                    result = json.loads(content)
                    print_log("✅", "JSON 解析成功", "green")

                    if self._validate_recommendations(result):
                        rec_count = len(result)
                        print_log(
                            "✅",
                            f"驗證成功，找到 {rec_count} 筆推薦",
                            "green"
                        )
                        return result
                    else:
                        print_log("❌", "推薦格式驗證失敗", "red")
                        raise Exception("Invalid recommendation format")

                except json.JSONDecodeError as e:
                    print_log("❌", f"JSON 解析失敗: {e}", "red")
                    print_log("📄", "原始回應內容:", "yellow")
                    print(f"    {content[:500]}")
                    raise

            except Exception as e:
                last_error = str(e)
                print_log("❌", f"錯誤: {last_error}", "red")

                if attempt < retries - 1:
                    print_log(
                        "⏳",
                        f"等待 {RETRY_DELAY} 秒後重試...",
                        "yellow"
                    )
                    time.sleep(RETRY_DELAY)
                else:
                    print_log("💥", "所有重試都失敗", "red")

        raise Exception(f"All retries failed: {last_error}")


def fallback_recommendations(books, count=3):
    """Fallback when AI fails - simple but works"""
    print_log("🔄", "使用 Fallback 推薦機制", "yellow")
    return [
        {
            'book_id': book['id'],
            'reason': f"推薦閱讀《{book['title']}》by {book['author']}",
            'score': 0.7
        }
        for book in books[:count]
    ]


ollama = OllamaStreamClient()


@app.route('/health', methods=['GET'])
def health():
    """Health check endpoint"""
    print_log("💚", "健康檢查請求", "green")
    try:
        # Try to list models to verify connection
        models = ollama.client.list()
        return jsonify({
            "status": "healthy",
            "model": MODEL,
            "ollama_url": OLLAMA_URL,
            "using_api_key": bool(OLLAMA_API_KEY),
            "available_models": len(models.get('models', []))
        }), 200
    except Exception as e:
        print_log("❌", f"健康檢查失敗: {e}", "red")
        return jsonify({
            "status": "unhealthy",
            "error": str(e)
        }), 503


@app.route('/generate-personal-recommendations', methods=['POST'])
def generate_personal():
    """Generate personal recommendations based on user history"""
    print_log("🎯", "=" * 70, "cyan")
    print_log("📨", "收到個人化推薦請求", "cyan")

    data = request.json
    user_profile = data.get('user_profile', {})
    books = data.get('available_books', [])

    history_len = len(user_profile.get('borrow_history', []))
    print_log("👤", f"使用者借閱歷史: {history_len} 本書", "blue")
    print_log("📚", f"可借閱書籍: {len(books)} 本", "blue")
    print()

    # Build prompt
    history = user_profile.get('borrow_history', [])
    history_titles = [item['title'] for item in history]
    book_list = [
        f"{b['id']}: {b['title']} by {b['author']}"
        for b in books
    ]

    history_str = ', '.join(history_titles) if history_titles else 'No history'
    prompt = f"""Based on reading history: {history_str}

Recommend 3 books from this list:
{chr(10).join(book_list)}

Return ONLY a JSON array like this (no markdown, no explanation):
[
  {{"book_id": "001", "reason": "推薦理由（中文）", "score": 0.85}},
  {{"book_id": "002", "reason": "推薦理由（中文）", "score": 0.80}},
  {{"book_id": "003", "reason": "推薦理由（中文）", "score": 0.75}}
]"""

    try:
        recommendations = ollama.generate_stream(prompt, temp=0.7)
        print_log(
            "🎉",
            f"成功生成 {len(recommendations)} 筆推薦",
            "green"
        )
        print_log("🎯", "=" * 70, "cyan")
        print()
        return jsonify({
            "success": True,
            "recommendations": recommendations,
            "source": "ai"
        })

    except Exception as e:
        print_log("⚠️", f"AI 生成失敗，使用 Fallback: {e}", "yellow")
        fallback = fallback_recommendations(books, 3)
        print_log("🎯", "=" * 70, "cyan")
        print()
        return jsonify({
            "success": True,
            "recommendations": fallback,
            "source": "fallback"
        })


@app.route('/generate-related-recommendations', methods=['POST'])
def generate_related():
    """Generate related book recommendations"""
    print_log("🎯", "=" * 70, "cyan")
    print_log("📨", "收到相關推薦請求", "cyan")

    data = request.json
    current_book = data.get('current_book', {})
    related_books = data.get('related_books', [])

    print_log("📖", f"當前書籍: {current_book.get('title')}", "blue")
    print_log("📚", f"相關書籍: {len(related_books)} 本", "blue")
    print()

    book_list = [
        f"{b['id']}: {b['title']} by {b['author']}"
        for b in related_books
    ]

    current_title = current_book.get('title')
    current_author = current_book.get('author')
    prompt = f"""For readers who liked: {current_title} by {current_author}

Recommend 3 related books from:
{chr(10).join(book_list)}

Return ONLY a JSON array (no markdown):
[
  {{"book_id": "002", "reason": "相關理由（中文）", "score": 0.8}},
  {{"book_id": "003", "reason": "相關理由（中文）", "score": 0.75}},
  {{"book_id": "004", "reason": "相關理由（中文）", "score": 0.70}}
]"""

    try:
        recommendations = ollama.generate_stream(prompt, temp=0.6)
        rec_count = len(recommendations)
        print_log("🎉", f"成功生成 {rec_count} 筆相關推薦", "green")
        print_log("🎯", "=" * 70, "cyan")
        print()
        return jsonify({
            "success": True,
            "recommendations": recommendations,
            "source": "ai"
        })

    except Exception as e:
        print_log("⚠️", f"AI 生成失敗，使用 Fallback: {e}", "yellow")
        fallback = fallback_recommendations(related_books, 3)
        print_log("🎯", "=" * 70, "cyan")
        print()
        return jsonify({
            "success": True,
            "recommendations": fallback,
            "source": "fallback"
        })


def get_default_system_prompt():
    """
    獲取預設的 system prompt（不使用 RAG）
    """
    return """你是一個專業的圖書館 AI 助理，負責協助使用者處理圖書館相關問題。

你的職責：
1. 回答圖書館規則和常見問題
2. 協助使用者查詢書籍資訊
3. 提供借閱歷史查詢
4. 說明借還書流程
5. 提供圖書館統計資訊

回答原則：
- 回答要簡潔、準確、友善
- 使用繁體中文回答
- 如果不確定答案，誠實告知使用者
- 涉及具體操作時，提供清楚的步驟說明"""


@app.route('/chat', methods=['POST'])
def chat():
    """
    聊天端點（支援 RAG）

    Request Body:
        {
            "message": "使用者訊息",
            "history": [{"role": "user", "content": "..."}, ...],
            "context": "{...}"  // 可選：ChatContext JSON
        }
    """
    print_log("💬", "=" * 70, "cyan")
    print_log("📨", "收到聊天請求", "cyan")

    try:
        data = request.json
        user_message = data.get('message', '')
        history = data.get('history', [])
        context_json = data.get('context', None)  # 新增：可選的 context

        if not user_message:
            return jsonify({'success': False, 'message': '訊息不能為空'}), 400

        print_log("👤", f"使用者訊息: {user_message[:50]}...", "blue")
        print_log("📝", f"對話歷史: {len(history)} 輪", "blue")

        # 構建訊息列表
        messages = []

        # 1. 決定 system prompt（根據是否有 context）
        if context_json:
            # 有 context：使用 RAG system prompt
            try:
                from rag_prompt_builder import build_rag_system_prompt, validate_context

                # 解析 context
                context_data = json.loads(context_json)

                # 驗證 context
                is_valid, error_msg = validate_context(context_data)
                if not is_valid:
                    print_log("⚠️", f"Context validation failed: {error_msg}", "yellow")
                    # 驗證失敗，使用預設 prompt
                    system_prompt = get_default_system_prompt()
                else:
                    # 構建 RAG system prompt
                    system_prompt = build_rag_system_prompt(context_data)
                    has_data = context_data.get('hasData', False)
                    print_log("✅", f"Using RAG system prompt (hasData={has_data})", "green")

            except Exception as e:
                print_log("❌", f"Error building RAG prompt: {e}", "red")
                import traceback
                traceback.print_exc()
                # 發生錯誤，使用預設 prompt
                system_prompt = get_default_system_prompt()
        else:
            # 沒有 context：使用預設 system prompt
            system_prompt = get_default_system_prompt()
            print_log("ℹ️", "Using default system prompt (no context provided)", "blue")

        # 2. 添加 system prompt
        messages.append({
            "role": "system",
            "content": system_prompt
        })

        # 3. 添加歷史記錄（限制在最近 5 輪對話）
        if history:
            recent_history = history[-10:]  # 5 輪 = 10 條訊息（user + assistant）
            for msg in recent_history:
                if msg.get('role') in ['user', 'assistant']:
                    messages.append({
                        "role": msg['role'],
                        "content": msg['content']
                    })

        # 4. 添加當前使用者訊息
        messages.append({
            "role": "user",
            "content": user_message
        })

        # 5. 呼叫 Ollama API
        print_log("📤", f"Sending to Ollama: {len(messages)} messages", "yellow")

        response = ollama.client.chat(
            model=MODEL,
            messages=messages,
            options={'temperature': 0.7}
        )

        assistant_message = response['message']['content']
        print_log("✅", f"Ollama response received: {len(assistant_message)} chars", "green")
        print_log("💬", "=" * 70, "cyan")
        print()

        return jsonify({
            'success': True,
            'message': assistant_message
        })

    except requests.exceptions.Timeout:
        print_log("⏱️", "Ollama request timeout", "red")
        print_log("💬", "=" * 70, "cyan")
        print()
        return jsonify({
            'success': False,
            'message': 'AI 服務響應超時，請稍後再試'
        }), 504

    except Exception as e:
        print_log("❌", f"Chat error: {str(e)}", "red")
        import traceback
        traceback.print_exc()
        print_log("💬", "=" * 70, "cyan")
        print()
        return jsonify({
            'success': False,
            'message': f'處理請求時發生錯誤: {str(e)}'
        }), 500


if __name__ == '__main__':
    print()
    print("=" * 70)
    print("  🤖 Ollama AI Recommendation Service - Official Client")
    print("=" * 70)
    print()
    print("  📍 Service URL: http://localhost:8888")
    print("  🔗 Ollama URL:", OLLAMA_URL)
    api_status = "✅ Configured" if OLLAMA_API_KEY else "❌ Not set"
    print("  🔑 API Key:", api_status)
    print("  🧠 Model:", MODEL)
    print("  🔄 Max Retries:", MAX_RETRIES)
    print("  ⏱️  Retry Delay:", RETRY_DELAY, "seconds")
    print()
    print("  🎯 Features:")
    print("     - ✨ Real-time Ollama streaming (official client)")
    print("     - 🎨 Colored logs with timestamps")
    print("     - 📊 Detailed progress tracking")
    print("     - 🔍 Detailed error messages")
    print("     - 🔄 Retry mechanism with fallback")
    print("     - 💚 Health check endpoint")
    print("     - 🔑 Support for cloud Ollama API")
    print()
    print("  📊 Endpoints:")
    print("     - GET  /health")
    print("     - POST /generate-personal-recommendations")
    print("     - POST /generate-related-recommendations")
    print("     - POST /chat (AI Chatbot)")
    print()
    print("=" * 70)
    print()

    # Enable debug mode for hot reload during development
    app.run(host='0.0.0.0', port=8888, debug=True)
