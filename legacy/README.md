# Legacy Code Archive

這個資料夾保存了專案早期版本的程式碼，作為學習歷史記錄。

## 資料夾結構

```
legacy/
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
| Swing GUI | HTML/JavaScript | 桌面應用 → Web 應用 |
| AuthenticationHelper | ApiAuthenticationHelper | Swing 認證 → API 認證 |
| BookRepository | BookDatabaseRepository | 記憶體儲存 → SQLite 資料庫 |
| Book.java | BookInfo.java | 舊模型 → 新模型 |
| MVC 架構 | RESTful API | 桌面 MVC → Web API |

## 新架構的優勢

- ✅ **跨平台**: Web 介面可在任何裝置存取
- ✅ **現代化**: RESTful API + SQLite
- ✅ **擴充性**: 易於新增功能
- ✅ **使用者體驗**: 暗色系 UI，搜尋、排序功能

---

**日期**: 2025-10-24
**狀態**: 已歸檔，僅供參考
