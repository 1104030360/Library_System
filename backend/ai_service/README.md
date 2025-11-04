# Ollama AI Recommendation Service

This document provides a comprehensive guide to configuring, running, monitoring, and debugging the Ollama AI Recommendation Service.

## 🎯 Quick Start (Recommended)

To see the AI service in action with detailed, real-time output, run the verbose mode script:

```bash
cd backend/ai_service
bash start_verbose.sh
```

This will start the service and display colored, timestamped logs, including the real-time generation of recommendations from Ollama.

## 📊 Features

*   **Real-time Streaming Output**: Watch the AI generate recommendations word by word.
*   **Colored Logs**: Easily distinguish between informational messages, successes, warnings, and errors.
*   **Detailed Timestamps**: Track the performance of each step in the recommendation process.
*   **Comprehensive Error Tracking**: See detailed error messages, retry attempts, and fallback triggers.

## 監控指南 (Monitoring Guide)

There are four primary ways to monitor the AI service:

### Method 1: Live Interactive Testing (Highly Recommended)

Use the interactive test script for a complete request/response overview.

```bash
cd backend/ai_service
source venv/bin/activate
python3 test_live.py
```

**Features**:
- Health checks
- Test personal and related recommendations
- Continuous monitoring mode
- Full test suite

### Method 2: Monitor Python AI Service Logs

View the real-time logs from the Flask service.

```bash
cd backend/ai_service
bash monitor.sh
```

This shows incoming HTTP requests, AI call progress, retries, and fallbacks.

### Method 3: Monitor Java Backend Logs

Check the output of the main Java application for high-level logging.

### Method 4: Manual Testing with `curl`

Send HTTP requests manually to test endpoints.

**Health Check**:
```bash
curl http://localhost:8000/health
```

**Personal Recommendations (requires login)**:
```bash
# First, log in to get a session cookie
curl -X POST http://localhost:7070/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"0001","password":"1111"}' \
  -c cookies.txt

# Then, request recommendations
curl http://localhost:7070/api/recommendations/personal -b cookies.txt -v
```

## ⚙️ Configuration (API Key and Environment)

### 1. Create `.env` file

```bash
cd backend/ai_service
cp .env_example .env
```

### 2. Edit `.env` file

Open `.env` and configure the following:

*   `OLLAMA_URL`: The URL of your Ollama instance (e.g., `http://localhost:11434` for local, `http://ollama-base:11434` for Docker).
*   `OLLAMA_API_KEY`: Your API key if using a cloud-based Ollama service. Leave blank for local instances.
*   `MODEL`: The model to use (e.g., `llama3.2:latest`).
*   `MAX_RETRIES`, `RETRY_DELAY`: Configure the retry mechanism.
*   `FLASK_ENV`, `FLASK_DEBUG`: Set the Flask environment.

### 3. Restart the Service

Restart the service for the changes to take effect.

## 🌊 Streaming Guide

To observe the real-time generation of recommendations, you can use one of the following methods:

### Docker Logs (Recommended)

```bash
docker logs library-ai-service -f
```

### Enable Streaming Version

If you want to see word-by-word generation, you can switch to the streaming version of the service.

1.  **Modify Dockerfile**: In `backend/ai_service/Dockerfile`, change `ollama_service.py` to `ollama_service_streaming.py`.
2.  **Rebuild and Restart**:
    ```docker
docker-compose build ai-service
docker-compose up -d ai-service
    ```

## 📜 Verbose Mode

The verbose mode provides the most detailed view of the AI service's operation.

**Start Verbose Mode**:
```bash
cd backend/ai_service
bash start_verbose.sh
```

**Example Output**:
```
[15:30:45.123] 📨 收到個人化推薦請求
[15:30:45.200] 🚀 開始 Ollama 串流生成...
[
  {"book_id": "005", "reason": "...", "score": 0.85},
  ...
]
[15:30:52.457] ✅ Ollama 生成完成
[15:30:52.461] 🎉 成功生成 5 筆推薦
```

## 🛠️ Troubleshooting

*   **Invalid API Key**: Check for a `401` error. Verify your API key and URL in the `.env` file.
*   **Connection Timeout**: Ensure the Ollama URL is correct and accessible. Check firewall settings.
*   **Connection Refused**: Make sure the local Ollama service is running (`ollama serve`).
