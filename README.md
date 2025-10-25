# 中大圖書館借還系統

這是一個圖書館管理系統，從 Swing 桌面應用程式演進為採用 RESTful API 和 SQLite 資料庫的現代 Web 應用程式。此專案為中央大學資管系期末專題。

---

## 系統簡介

- **Web-based UI**: 跨平台存取，支援任何瀏覽器。
- **SQLite 資料庫**: 持久化資料儲存。
- **Cookie-based 認證**: 安全的 Session 管理。
- **即時搜尋排序**: Client-side 即時過濾。
- **完整測試覆蓋**: 使用 JUnit 5 測試框架。

---

## 快速開始

### 方法 1: 一鍵啟動 (推薦)

```bash
./start-server.sh
```

### 方法 2: 手動啟動

```bash
# 編譯後端
javac -d backend/bin -cp "lib/*:backend/src" backend/src/*.java

# 啟動伺服器
java -cp "lib/*:backend/bin" LibraryApiServer
```

### 方法 3: 使用 Docker 啟動

確保您已安裝 Docker。執行以下指令來建立映像檔並啟動容器：

```bash
# 使用 docker-compose 進行建置與啟動 (推薦)
docker-compose up --build -d

# 或者，您也可以使用 docker-run.sh 腳本
./docker-run.sh
```

若要停止服務，請執行：
```bash
docker-compose down
```

### 開啟瀏覽器

伺服器將啟動在 `http://localhost:7070`。

- **首頁 (書籍清單)**: `http://localhost:7070`
- **登入頁面**: `http://localhost:7070/login.html`

---

## 功能

### 書籍清單
- 顯示所有書籍 (書名、作者、出版社、狀態)。
- 即時搜尋與排序。
- 借閱/歸還書籍。
- 顯示圖書館統計資訊。

### 登入
- 基於 Cookie 的身份驗證。
- 測試帳號:
    - 館長 (Boss): `0001` / `1111`
    - 館員 (Employee): `0002` / `2222`

---

## 專案結構

```
JavaProj 2/
├── backend/                    # 後端程式碼
│   ├── src/                   # 原始碼
│   │   ├── LibraryApiServer.java          # 主伺服器 (HTTP handlers)
│   │   ├── BookDatabaseRepository.java    # 資料庫存取層
│   │   ├── ApiAuthenticationHelper.java   # 認證模組
│   │   ├── ApiSessionManager.java         # Session 管理
│   │   ├── StaticFileHandler.java         # 靜態檔案處理
│   │   └── BookInfo.java                  # 資料模型
│   └── bin/                   # 編譯輸出
│
├── web/                        # 前端程式碼
│   ├── index.html             # 首頁 (書籍清單)
│   ├── login.html             # 登入頁面
│   ├── css/
│   │   └── style.css
│   └── js/
│       ├── api.js             # API 呼叫封裝
│       └── app.js             # 主應用邏輯
│
├── test/                       # 測試程式碼
│   ├── repository/
│   ├── api/
│   └── auth/
│
├── legacy/                     # 舊版程式碼 (已歸檔)
│
├── data/                       # 資料儲存
│   └── library.db             # SQLite 資料庫
│
├── lib/                        # 外部函式庫
│
├── start-server.sh             # 伺服器啟動腳本
├── run-tests.sh                # 測試執行腳本
└── README.md                   # 本文件
```

---

## 測試系統

使用 `./run-tests.sh` 指令執行所有測試。

測試涵蓋三個主要領域：
1.  **Repository 測試**: 測試資料庫層的 CRUD 操作。
2.  **認證系統測試**: 測試身份驗證和 Session 管理。
3.  **API 整合測試**: 測試完整的 API 堆疊。

詳細資訊請參閱 `test/README.md`。

---

## API 端點

### 書籍管理

| Method | Endpoint            | 說明           | 認證 |
|--------|---------------------|----------------|------|
| GET    | `/api/books`        | 取得所有書籍   | 否   |
| GET    | `/api/books?id=001` | 取得特定書籍   | 否   |
| GET    | `/api/stats`        | 取得統計資訊   | 否   |
| POST   | `/api/books/borrow` | 借閱書籍       | 是   |
| POST   | `/api/books/return` | 歸還書籍       | 是   |

### 認證管理

| Method | Endpoint           | 說明                   |
|--------|--------------------|------------------------|
| POST   | `/api/auth/login`  | 登入 (取得 session cookie) |
| POST   | `/api/auth/logout` | 登出 (清除 session)      |
| GET    | `/api/auth/whoami` | 取得當前使用者資訊     |

---

## 資料庫結構

**`books` 表**

| 欄位         | 類型    | 說明                     |
|--------------|---------|--------------------------|
| id           | TEXT    | 書籍 ID (Primary Key)    |
| title        | TEXT    | 書名                     |
| author       | TEXT    | 作者                     |
| publisher    | TEXT    | 出版社                   |
| is_available | INTEGER | 是否可借 (1=可借, 0=已借出) |

---

## 技術棧

### 後端
- **語言**: Java
- **HTTP Server**: `com.sun.net.httpserver.HttpServer`
- **資料庫**: SQLite 3.45.1.0
- **JSON**: Gson 2.10.1
- **測試**: JUnit 5.10.1

### 前端
- **HTML5**
- **CSS3**
- **JavaScript (ES6+)** (無框架)

---

## 貢獻

歡迎提交 Issue 和 Pull Request。

### 開發流程
1. Fork 專案
2. 建立功能分支 (`git checkout -b feature/新功能`)
3. 撰寫程式碼與測試
4. 執行測試 (`./run-tests.sh`)
5. Commit (`git commit -m 'Add 新功能'`)
6. Push (`git push origin feature/新功能`)
7. 建立 Pull Request

---

## 授權

本專案為學習專案，中央大學資管系期末作業。

---

## 致謝

- **中央大學資管系** - 提供學習環境
- **Linus Torvalds** - 啟發開發哲學

---

## 聯絡方式

- **GitHub**: https://github.com/1104030360
- **專案連結**: https://github.com/1104030360/JavaProj-2

---

> "Talk is cheap. Show me the code." - Linus Torvalds
