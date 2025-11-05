# Ollama AI Service 測試文檔

這是 Ollama AI 推薦服務的完整測試套件，支持本地和雲端 Ollama 服務的測試。

## 📁 測試文件結構

```
tests/
├── README.md                    # 本文件 - 測試教學文檔
├── __init__.py                  # Python 包初始化文件
├── conftest.py                  # Pytest 配置文件
├── test_ollama_service.py       # 主測試套件
└── test_data.json               # 測試數據（可選）
```

---

## 🚀 快速開始

### 1. 安裝測試依賴

```bash
cd /Users/linjunting/Documents/JavaProj\ 2/backend/ai_service
pip3 install pytest pytest-html requests
```

### 2. 啟動 AI Service

**方式 A: Docker 方式（推薦）**
```bash
cd /Users/linjunting/Documents/JavaProj\ 2
docker-compose up ai-service -d
```

**方式 B: 本地直接運行**
```bash
cd /Users/linjunting/Documents/JavaProj\ 2/backend/ai_service
python3 ollama_service_streaming.py
```

### 3. 運行測試

```bash
cd /Users/linjunting/Documents/JavaProj\ 2/backend/ai_service
pytest tests/ -v
```

---

## 📊 測試套件說明

### 測試類別

#### **TestOllamaService** - 功能測試
包含 7 個核心功能測試：

1. **test_health_check** - 健康檢查測試
   - 測試服務是否正常運行
   - 驗證模型配置
   - 檢查 API Key 狀態

2. **test_personal_recommendations** - 個人化推薦測試
   - 測試基於用戶歷史的推薦
   - 驗證推薦格式和評分
   - 檢查推薦理由品質

3. **test_related_recommendations** - 相關推薦測試
   - 測試基於當前書籍的相關推薦
   - 驗證推薦相關性

4. **test_empty_history** - 空歷史處理測試
   - 測試新用戶（無借閱歷史）的推薦
   - 驗證 fallback 機制

5. **test_invalid_request** - 無效請求處理測試
   - 測試錯誤輸入的處理
   - 驗證錯誤處理機制

6. **test_large_book_list** - 大數據處理測試
   - 測試 50 本書籍的推薦性能
   - 驗證大數據量處理能力

#### **TestPerformance** - 性能測試
包含 1 個性能測試：

7. **test_response_time** - 響應時間測試
   - 執行多次推薦請求
   - 統計平均、最快、最慢響應時間
   - 驗證性能符合要求

---

## 🎯 運行測試的方式

### 1. 運行所有測試

```bash
cd /Users/linjunting/Documents/JavaProj\ 2/backend/ai_service
pytest tests/ -v
```

### 2. 運行特定測試

```bash
# 只運行健康檢查測試
pytest tests/test_ollama_service.py::TestOllamaService::test_health_check -v

# 只運行推薦功能測試
pytest tests/test_ollama_service.py::TestOllamaService::test_personal_recommendations -v -s

# 只運行性能測試
pytest tests/test_ollama_service.py::TestPerformance -v
```

### 3. 運行並顯示詳細輸出

```bash
# -s 參數會顯示 print 輸出
pytest tests/ -v -s
```

### 4. 運行並生成 HTML 報告

```bash
# 生成測試報告
pytest tests/ -v --html=report.html --self-contained-html

# 在瀏覽器中查看報告
open report.html
```

### 5. 運行特定類別的測試

```bash
# 只運行功能測試
pytest tests/test_ollama_service.py::TestOllamaService -v

# 只運行性能測試
pytest tests/test_ollama_service.py::TestPerformance -v
```

---

## 🔧 測試配置

### 修改測試目標 URL

如果 AI Service 運行在不同端口，修改 `test_ollama_service.py`:

```python
class TestOllamaService:
    BASE_URL = "http://localhost:8888"  # 修改這裡
```

### 調整測試超時時間

在 `test_ollama_service.py` 中修改 `timeout` 參數:

```python
response = requests.post(
    f"{self.BASE_URL}/generate-personal-recommendations",
    json=payload,
    timeout=60  # 修改這裡（秒）
)
```

---

## 📝 測試輸出示例

### 成功的測試輸出

```
======================================================================
測試 1: 健康檢查
======================================================================
Status Code: 200
Response: {
  "status": "healthy",
  "model": "gpt-oss:20b-cloud",
  "ollama_url": "https://ollama.com",
  "using_api_key": true,
  "available_models": 9
}
✅ 健康檢查通過
   模型: gpt-oss:20b-cloud
   URL: https://ollama.com
   使用 API Key: True

======================================================================
測試 2: 個人化推薦
======================================================================
發送請求...
Status Code: 200
耗時: 4.23 秒

推薦來源: ai
推薦數量: 3

推薦 1:
  書籍 ID: 001
  評分: 0.92
  理由: 深入理解計算機系統能夠幫助 Java 開發者更好地理解 JVM 的底層運作...

✅ 個人化推薦測試通過
```

---

## 🧪 測試場景

### 場景 1: 測試本地 Ollama

**配置 `.env`:**
```env
OLLAMA_URL=http://localhost:11434
OLLAMA_API_KEY=
MODEL=llama3.2:latest
```

**啟動本地 Ollama:**
```bash
# 使用 Docker
docker-compose up ollama-base -d

# 或本地 Ollama
ollama serve
```

**運行測試:**
```bash
pytest tests/ -v -s
```

---

### 場景 2: 測試雲端 Ollama

**配置 `.env`:**
```env
OLLAMA_URL=https://ollama.com
OLLAMA_API_KEY=your-api-key-here
MODEL=gpt-oss:20b-cloud
```

**運行測試:**
```bash
pytest tests/ -v -s
```

---

### 場景 3: 性能基準測試

只運行性能測試，評估響應時間:

```bash
pytest tests/test_ollama_service.py::TestPerformance -v -s
```

**預期輸出:**
```
測試 7: 響應時間測試
執行 3 次推薦請求...
  第 1 次: 4.23 秒
  第 2 次: 3.87 秒
  第 3 次: 4.01 秒

平均響應時間: 4.04 秒
最快: 3.87 秒
最慢: 4.23 秒
✅ 響應時間測試通過
```

---

## 🐛 故障排除

### 問題 1: 連接被拒絕

**錯誤信息:**
```
requests.exceptions.ConnectionError: Failed to connect to localhost port 8888
```

**解決方案:**
1. 確認 AI Service 正在運行:
   ```bash
   docker-compose ps ai-service
   # 或
   curl http://localhost:8888/health
   ```

2. 檢查端口是否正確:
   ```bash
   lsof -i :8888
   ```

3. 重啟服務:
   ```bash
   docker-compose restart ai-service
   ```

---

### 問題 2: 測試超時

**錯誤信息:**
```
requests.exceptions.ReadTimeout: HTTPConnectionPool: Read timed out. (read timeout=60)
```

**解決方案:**
1. 檢查 Ollama 服務狀態:
   ```bash
   curl http://localhost:8888/health
   ```

2. 查看 AI Service 日誌:
   ```bash
   docker-compose logs ai-service --tail=50
   ```

3. 增加測試超時時間（在 test_ollama_service.py 中修改）

---

### 問題 3: Ollama 模型未載入

**錯誤信息:**
```
{"status": "unhealthy", "error": "Model not found"}
```

**解決方案:**
1. 檢查模型是否存在（本地）:
   ```bash
   ollama list
   ```

2. 下載模型（如果需要）:
   ```bash
   ollama pull llama3.2:latest
   ```

3. 驗證雲端 API Key 和模型名稱是否正確

---

### 問題 4: 推薦品質不佳

**症狀:** 推薦結果都是 fallback，source 為 "fallback"

**解決方案:**
1. 檢查 Ollama URL 是否正確
2. 檢查 API Key 是否有效
3. 查看詳細日誌:
   ```bash
   docker-compose logs ai-service --tail=100
   ```

---

## 📊 進階用法

### 1. 持續集成 (CI)

在 CI 環境中運行測試:

```bash
# 運行測試並生成 JUnit XML 報告
pytest tests/ -v --junitxml=test-results.xml

# 生成覆蓋率報告
pytest tests/ -v --cov=. --cov-report=html
```

### 2. 自定義測試數據

創建 `test_data.json`:

```json
{
  "test_books": [
    {
      "id": "custom_001",
      "title": "自定義測試書籍",
      "author": "測試作者",
      "publisher": "測試出版社"
    }
  ],
  "test_history": [
    {"title": "測試歷史書籍 1"},
    {"title": "測試歷史書籍 2"}
  ]
}
```

在測試中載入:

```python
import json

with open('tests/test_data.json') as f:
    test_data = json.load(f)
```

### 3. 平行測試

使用 pytest-xdist 平行運行測試:

```bash
# 安裝
pip3 install pytest-xdist

# 使用 4 個 worker 平行運行
pytest tests/ -v -n 4
```

---

## 📈 測試覆蓋率

### 查看測試覆蓋率

```bash
# 安裝覆蓋率工具
pip3 install pytest-cov

# 生成覆蓋率報告
pytest tests/ --cov=ollama_service_streaming --cov-report=html

# 查看報告
open htmlcov/index.html
```

---

## 🎨 測試最佳實踐

### 1. 測試前檢查清單

- [ ] AI Service 正在運行
- [ ] Ollama 服務正常（本地或雲端）
- [ ] `.env` 配置正確
- [ ] 網路連接正常

### 2. 建議的測試流程

```bash
# 1. 先運行健康檢查
pytest tests/test_ollama_service.py::TestOllamaService::test_health_check -v

# 2. 運行單個推薦測試
pytest tests/test_ollama_service.py::TestOllamaService::test_personal_recommendations -v -s

# 3. 運行所有功能測試
pytest tests/test_ollama_service.py::TestOllamaService -v

# 4. 最後運行性能測試
pytest tests/test_ollama_service.py::TestPerformance -v
```

### 3. 測試環境切換

**開發環境 (本地 Ollama):**
```bash
# .env 設置
OLLAMA_URL=http://localhost:11434
MODEL=llama3.2:latest
```

**生產環境 (雲端 Ollama):**
```bash
# .env 設置
OLLAMA_URL=https://ollama.com
OLLAMA_API_KEY=your-key
MODEL=gpt-oss:20b-cloud
```

---

## 🔍 測試結果解讀

### 測試通過標準

✅ **所有測試通過** - 服務正常，可以部署
- 健康檢查返回 200
- 推薦生成成功（source: "ai"）
- 響應時間在合理範圍內（< 60秒）
- 錯誤處理正常

⚠️ **部分測試失敗** - 需要檢查
- 推薦返回 fallback - 檢查 Ollama 連接
- 超時 - 檢查網路和服務性能
- 格式錯誤 - 檢查數據格式

❌ **多數測試失敗** - 服務異常
- 健康檢查失敗 - 服務未啟動
- 連接失敗 - 檢查端口和配置

---

## 📚 相關文檔

- **主項目文檔**: `/Users/linjunting/Documents/JavaProj 2/README.md`
- **Docker 文檔**: `/Users/linjunting/Documents/JavaProj 2/DOCKER.md`
- **AI Service 源碼**: `../ollama_service_streaming.py`
- **環境配置**: `../.env`

---

## 💡 提示和技巧

### 快速命令

```bash
# 快速健康檢查（不使用 pytest）
curl http://localhost:8888/health | jq

# 快速推薦測試
curl -X POST http://localhost:8888/generate-personal-recommendations \
  -H "Content-Type: application/json" \
  -d '{"user_profile": {"borrow_history": [{"title": "Java"}]}, "available_books": [{"id":"001","title":"Book","author":"A","publisher":"P"}]}'

# 查看實時日誌
docker-compose logs -f ai-service

# 重啟服務並測試
docker-compose restart ai-service && sleep 5 && pytest tests/test_ollama_service.py::TestOllamaService::test_health_check -v
```

### 調試技巧

1. **使用 -s 顯示輸出**: `pytest tests/ -v -s`
2. **只運行失敗的測試**: `pytest tests/ --lf`
3. **進入調試模式**: `pytest tests/ --pdb`
4. **增加詳細度**: `pytest tests/ -vv`

---

## 🆘 需要幫助？

如果測試遇到問題：

1. 查看 AI Service 日誌:
   ```bash
   docker-compose logs ai-service --tail=100
   ```

2. 檢查服務狀態:
   ```bash
   docker-compose ps
   curl http://localhost:8888/health
   ```

3. 查看測試文檔（本文件）的「故障排除」章節

4. 參考主項目文檔: `CLAUDE.md` 和 `README.md`

---

**最後更新**: 2025-11-03
**測試套件版本**: 1.0.0
**作者**: 中央大學資管系
