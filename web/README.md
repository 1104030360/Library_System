# 中大圖書館借還系統 - 前端 (Vue 3)

現代化的圖書館管理系統前端，採用 Vue 3 + Vite + Tailwind CSS + Naive UI 構建。

## 技術棧

- **前端框架**: Vue 3.5+ (Composition API)
- **構建工具**: Vite 7.1+
- **UI 框架**: Naive UI 2.x (極簡設計)
- **樣式**: Tailwind CSS v4
- **狀態管理**: Pinia
- **路由**: Vue Router 4
- **HTTP 客戶端**: Axios
- **語言**: TypeScript

## 設計理念

本專案採用 **2025 年極簡設計趨勢**：

✅ **超極簡主義** - 去除所有非必要元素
✅ **扁平化設計** - 避免 3D 效果、陰影、漸層
✅ **有限色彩方案** - 中性基底 + 1-2 個強調色
✅ **功能優先** - 清晰簡潔的介面
✅ **無 AI 感** - 無漸層、無 emoji、無霓虹效果

### 色彩設計

- **背景**: 純白 (#ffffff) / 淺灰 (#f8fafc)
- **主色調**: 簡約藍 (#2563eb)
- **成功**: 綠色 (#10b981)
- **警告**: 橙色 (#f59e0b)
- **錯誤**: 紅色 (#ef4444)

## 快速開始

### 安裝依賴

```bash
npm install
```

### 開發模式

```bash
npm run dev
```

前端將在 `http://localhost:5173` 啟動（如端口被占用會自動選擇其他端口）

**注意**: 需要後端 API 服務器運行在 `http://localhost:7070`

### 構建生產版本

```bash
npm run build
```

構建輸出位於 `dist/` 目錄

### 預覽生產版本

```bash
npm run preview
```

## 專案結構

```
web-vue/
├── public/                  # 靜態資源
├── src/
│   ├── api/                # API 通訊層
│   │   └── index.ts        # axios 封裝和 API 方法
│   ├── assets/             # 圖片、字體等
│   ├── components/         # 可重用組件
│   │   ├── AppHeader.vue   # 頁首導航
│   │   ├── BookCard.vue    # 書籍卡片
│   │   ├── BookList.vue    # 書籍列表
│   │   ├── LoginForm.vue   # 登入表單
│   │   ├── SearchBar.vue   # 搜尋和排序
│   │   └── StatsBar.vue    # 統計資訊
│   ├── router/             # 路由配置
│   │   └── index.ts
│   ├── stores/             # Pinia 狀態管理
│   │   ├── auth.ts         # 認證狀態
│   │   └── books.ts        # 書籍狀態
│   ├── types/              # TypeScript 類型定義
│   │   └── index.ts
│   ├── views/              # 頁面組件
│   │   ├── HomeView.vue    # 首頁 (書籍列表)
│   │   └── LoginView.vue   # 登入頁面
│   ├── App.vue             # 根組件
│   ├── main.ts             # 應用入口
│   └── style.css           # 全域樣式 (Tailwind)
├── index.html              # HTML 模板
├── vite.config.ts          # Vite 配置
├── tailwind.config.js      # Tailwind 配置
├── postcss.config.js       # PostCSS 配置
├── tsconfig.json           # TypeScript 配置
└── package.json
```

## 功能特性

### 已實現功能

- ✅ 書籍列表展示
- ✅ 即時搜尋（書名、作者、出版社、ID）
- ✅ 多種排序方式（ID、書名、作者、可借閱優先）
- ✅ 統計資訊顯示
- ✅ 使用者登入/登出
- ✅ 借書功能（需登入）
- ✅ 還書功能（需登入）
- ✅ 響應式設計（手機、平板、桌面）
- ✅ 載入狀態提示
- ✅ 成功/失敗訊息提示
- ✅ 空狀態友善提示

### API 端點

後端 API 基礎 URL: `http://localhost:7070/api`

| 端點 | 方法 | 說明 | 認證 |
|------|------|------|------|
| `/books` | GET | 取得所有書籍 | 否 |
| `/stats` | GET | 取得統計資訊 | 否 |
| `/auth/login` | POST | 使用者登入 | 否 |
| `/auth/logout` | POST | 使用者登出 | 是 |
| `/auth/whoami` | GET | 取得當前使用者 | 是 |
| `/books/borrow` | POST | 借書 | 是 |
| `/books/return` | POST | 還書 | 是 |

## 測試帳號

- **館長**: 0001 / 1111
- **員工**: 0002 / 2222

## 設計系統

### 組件命名規範

- **PascalCase** - 所有 Vue 組件名稱
- **camelCase** - 變數、函數名稱
- **kebab-case** - CSS class 名稱

### 樣式規範

1. **優先使用 Tailwind CSS 工具類**
2. **避免自訂 CSS**（除非必要）
3. **使用 Naive UI 組件的預設樣式**
4. **保持簡約、扁平的視覺風格**

### 狀態管理

使用 Pinia stores:
- `useAuthStore()` - 認證狀態
- `useBooksStore()` - 書籍狀態

### 類型安全

所有 API 回應和資料結構都有 TypeScript 類型定義，確保類型安全。

## 開發指南

### 添加新頁面

1. 在 `src/views/` 建立新的 Vue 組件
2. 在 `src/router/index.ts` 添加路由
3. 更新導航連結（如需要）

### 添加新 API

1. 在 `src/types/index.ts` 定義 TypeScript 介面
2. 在 `src/api/index.ts` 添加 API 方法
3. 在相關 store 中呼叫 API

### 添加新組件

1. 在 `src/components/` 建立 Vue 組件
2. 使用 Naive UI 組件和 Tailwind CSS
3. 遵循極簡設計原則

## 瀏覽器支援

- Chrome (最新)
- Firefox (最新)
- Safari (最新)
- Edge (最新)

## License

此專案為中央大學資管系期末專題。

## 開發團隊

中央大學資管系

---

**建立日期**: 2025-10-31
**版本**: 1.0.0
**基於**: Vue 3 + Vite + Tailwind + Naive UI
