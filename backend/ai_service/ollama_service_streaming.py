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


@app.route('/chat', methods=['POST'])
def chat():
    """Simple chat endpoint - Linus style: just works, no fancy stuff"""
    print_log("💬", "=" * 70, "cyan")
    print_log("📨", "收到聊天請求", "cyan")

    data = request.json
    user_message = data.get('message', '')
    history = data.get('history', [])

    print_log("👤", f"使用者訊息: {user_message[:50]}...", "blue")
    print_log("📝", f"對話歷史: {len(history)} 輪", "blue")
    print()

    # Build messages array - simple and straightforward
    messages = [
        {
            "role": "system",
            "content": "你是中大圖書館的 AI 助手。協助使用者解答關於圖書館借還系統的問題。回答要簡潔、友善、使用繁體中文。"
        }
    ]

    # Add conversation history (keep last 5 rounds only - Linus: simple limits)
    for msg in history[-10:]:  # Last 5 rounds = 10 messages (user + assistant)
        messages.append({
            "role": msg.get('role', 'user'),
            "content": msg.get('content', '')
        })

    # Add current user message
    messages.append({
        "role": "user",
        "content": user_message
    })

    try:
        print_log("🤖", f"呼叫 Ollama ({MODEL})...", "yellow")

        # Call Ollama - no streaming for simplicity (Linus: start simple)
        response = ollama.client.chat(
            model=MODEL,
            messages=messages,
            options={'temperature': 0.7}
        )

        assistant_message = response['message']['content']
        print_log("✅", f"回應生成成功 ({len(assistant_message)} 字元)", "green")
        print_log("💬", "=" * 70, "cyan")
        print()

        return jsonify({
            "success": True,
            "message": assistant_message
        })

    except Exception as e:
        print_log("❌", f"錯誤: {str(e)}", "red")
        print_log("💬", "=" * 70, "cyan")
        print()

        # Fallback response - always have a backup (Linus: never fail silently)
        return jsonify({
            "success": False,
            "message": "抱歉，AI 服務暫時無法使用。請稍後再試或聯絡圖書館管理員。"
        }), 503


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
