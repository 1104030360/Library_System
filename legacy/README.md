# Legacy Code Archive

這個資料夾保存了專案早期版本的程式碼，作為學習歷史記錄。

## 資料夾結構

```
legacy/
├── web-original/       # 原始 HTML/CSS/JS 前端（第一版 Web 介面）
│   ├── index.html      # 書籍列表頁面
│   ├── login.html      # 登入頁面
│   ├── css/
│   │   └── style.css   # 原始樣式
│   └── js/
│       ├── api.js      # API 客戶端
│       └── app.js      # 應用邏輯
│
├── swing-gui/          # Swing 桌面 GUI 版本
│   ├── LibraryUI.java
│   ├── AuthenticationHelper.java
│   └── UserSession.java
│
└── old-models/         # 舊的資料模型和 MVC 架構
    ├── Book.java, Book2.java
    ├── BookRepository.java
    ├── BookController.java, BookService.java
    ├── Admin.java, Boss.java, Employee.java
    ├── Student.java, Teacher_1.java
    ├── Member_1.java, Staff_1.java
    ├── UserClass.java, UserClass2.java
    └── TestMVC.java, TestBook2.java
```

## 為什麼保留這些程式碼？

1. **學習歷史**: 記錄專案演進過程
2. **參考價值**: 未來可能需要回顧舊的設計思路
3. **教學用途**: 展示不同的實作方式（Swing vs Web）

## 已被取代的功能

| 舊版本 | 新版本 | 說明 |
|--------|--------|------|
| Swing GUI | Vue 3 Web App | 桌面應用 → 現代 Web 應用 |
| HTML/CSS/JS (web-original) | Vue 3 + TypeScript | 原始 Web → 現代前端框架 |
| AuthenticationHelper | ApiAuthenticationHelper | Swing 認證 → API 認證 |
| BookRepository | BookDatabaseRepository | 記憶體儲存 → SQLite 資料庫 |
| Book.java | BookInfo.java | 舊模型 → 新模型 |
| MVC 架構 | RESTful API + Vue 3 | 桌面 MVC → 現代 Web 架構 |

## 專案演進歷史

### 第一階段：Java Swing 桌面應用（最早期）
- 使用 Java Swing 建立桌面 GUI
- 資料儲存在記憶體中
- 單機應用程式

### 第二階段：原始 Web 介面（2025-10-25）
- 創建基本的 HTML/CSS/JavaScript 前端
- 連接 Java RESTful API 後端
- 使用 SQLite 資料庫
- 簡單的原生 JavaScript 實作

### 第三階段：Vue 3 現代化前端（2025-10-31 至今）⭐ 當前版本
- 採用 Vue 3 + TypeScript + Vite
- 使用 Naive UI 元件庫
- Tailwind CSS v4 樣式框架
- Pinia 狀態管理
- 完整的型別安全和現代化開發體驗

## 新架構的優勢

### Vue 3 前端 vs 原始 HTML/JS
- ✅ **元件化**: 可重用的 Vue 元件
- ✅ **型別安全**: TypeScript 提供完整型別檢查
- ✅ **狀態管理**: Pinia 集中管理應用狀態
- ✅ **路由管理**: Vue Router 處理頁面導航
- ✅ **開發體驗**: 熱重載、Vite 快速建置
- ✅ **UI 元件**: Naive UI 提供豐富的現代元件
- ✅ **樣式系統**: Tailwind CSS v4 靈活的實用類別

### 整體系統優勢
- ✅ **跨平台**: Web 介面可在任何裝置存取
- ✅ **現代化**: RESTful API + SQLite + Vue 3
- ✅ **擴充性**: 易於新增功能和維護
- ✅ **使用者體驗**: 極簡設計，流暢互動
- ✅ **可維護性**: 清晰的專案結構和代碼組織

---

**最後更新**: 2025-11-01
**狀態**: 已歸檔，僅供參考和學習
