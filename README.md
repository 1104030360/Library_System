<p align="center">
 <h2 align="center">中大圖書館借還系統</h2>
 <p align="center"><img style="margin-bottom:-10px; height: 30px; width:30px;  " src="https://readme-components.vercel.app/api?component=logo&logo=react&fill=linear-gradient%2862deg%2C%20%238EC5FC%200%25%2C%20%23E0C3FC%20100%25%29%3B%0A&text=false&animation=spin"/>
 中央資管期末JAVA專題 !
<img style="margin-bottom:-10px; height: 30px; width:30px;  margin-left: 10px;" src="https://readme-components.vercel.app/api?component=logo&logo=react&fill=linear-gradient%2862deg%2C%20%238EC5FC%200%25%2C%20%23E0C3FC%20100%25%29%3B%0A&text=false&animation=spin"/></p>
</p>
<hr>

*我愛知識 知識愛我！*

*想看我更多的專案可以去這👉https://github.com/1104030360*

## 系統簡介

專業的圖書館管理系統，採用 **MVC 架構設計**，具備完整的借還書功能、會員管理和統計分析。

### 技術特色
- ✅ **MVC 三層架構** - Model（業務邏輯）、View（使用者介面）、Controller（流程控制）完全分離
- ✅ **Repository 模式** - 集中化資料存取管理
- ✅ **統一認證系統** - AuthenticationHelper 提供安全的身分驗證
- ✅ **會話管理** - UserSession 追蹤當前使用者狀態
- ✅ **完整測試** - 包含單元測試和整合測試
- ✅ **SOLID 原則** - 遵循物件導向設計最佳實踐
# 內容
## 角色劃分：
### 管理者分為兩種角色：館長和館員。
## 權限與決策：
  館長有權決定圖書館的休息時間和館員的月薪。

  館員負責執行日常管理任務，但是不具備調整休息時間或者自行調整薪資的權限。
  
## 管理者任務
### 館長任務：
  設定圖書館的休息時間。
  
  設定館員的月薪。
### 館員任務：
  圖書館日常管理任務，借還書的處理、圖書館內部維護。

## 借閱者功能：
### 角色劃分：
  借閱者分為會員和非會員。
### 功能區分：
  會員可能享有一些額外的特權，如更長的借閱期限或更多的借閱數量。
### 查閱圖書功能：
  非會員也可以使用系統查詢圖書館的藏書。



## 快速開始

### 1. 編譯系統

```bash
javac UserClass3.java
```

系統會自動編譯所有相依的檔案（BookController、BookService、LibraryUI 等）。

### 2. 執行系統

```bash
java UserClass3
```

### 3. 執行測試（可選）

```bash
# 測試 MVC 架構
java TestMVC

# 測試資料結構
java TestBook2
```

## 使用步驟

### 步驟 1: 選擇使用者類型

系統啟動後會出現主選單，請選擇：
- **管理員** - 館長或館員
- **借閱者** - 會員或非會員

### 步驟 2: 管理員登入

如果選擇管理員，系統會詢問身分：

#### 館長登入
- 帳號：`0001`
- 密碼：`1111`

館長功能：
1. 決定是否休館
2. 控制員工月薪
3. 編輯書籍（新增、刪除、修改、查詢）
4. 查看統計資訊

#### 館員登入
可用帳號和密碼：
- 館員 1：`0002` / `2222`
- 館員 2：`0003` / `3333`
- 館員 3：`0004` / `4444`
- 館員 4：`0005` / `5555`
- 館員 5：`0006` / `6666`

館員功能：
1. 查看月薪
2. 查看今日班表
3. 更改書籍資訊
4. 查看統計資訊

### 步驟 3: 會員登入

選擇會員身分登入，需要先選擇身分類型：

#### 學生會員
- 預設帳號需先註冊
- 可借閱書籍

#### 教師會員
- 預設帳號需先註冊
- 可借閱書籍

#### 職員會員
- 預設帳號需先註冊
- 可借閱書籍

會員功能：
1. 查看個人資料
2. 查看借還書紀錄
3. 查書與借還書
   - 借書
   - 還書
   - 查詢書籍

### 步驟 4: 非會員使用

非會員可以：
1. **註冊成會員** - 建立學生、教師或職員帳號
2. **使用查書功能** - 瀏覽館藏和搜尋書籍

#### 註冊流程
1. 選擇「註冊成會員」
2. 輸入使用者名稱
3. 輸入密碼（純數字）
4. 選擇身分（學生/教師/職員）
5. 註冊成功後可選擇直接登入

### 步驟 5: 書籍操作

#### 查詢書籍
可透過兩種方式查詢：
- **依 ID 搜尋** - 輸入書籍 ID（如：001）
- **依書名搜尋** - 輸入書名（如：Java）

#### 借書
1. 選擇「借書」
2. 選擇搜尋方式（ID 或書名）
3. 輸入書籍資訊
4. 系統顯示借書成功訊息及應還日期（借書後 2 週）

#### 還書
1. 選擇「還書」
2. 選擇搜尋方式（ID 或書名）
3. 輸入書籍資訊
4. 系統顯示還書成功訊息

### 步驟 6: 管理員書籍管理

管理員可執行：

#### 新增書籍
1. 輸入書籍 ID
2. 輸入書名
3. 輸入作者
4. 輸入出版社

#### 刪除書籍
- 可依 ID 或書名刪除

#### 編輯書籍
1. 輸入要編輯的書籍 ID
2. 系統顯示目前資訊
3. 輸入新的資訊（留空表示保持不變）

#### 查看統計
顯示：
- 總藏書量
- 可借閱數量
- 已借出數量

### 步驟 7: 結束使用

完成操作後，系統會詢問：
- **回主頁** - 返回主選單
- **繼續使用功能** - 繼續當前功能
- **登出並關閉** - 結束系統

## 系統架構

本系統採用 **MVC (Model-View-Controller)** 三層架構設計：

```
UserClass3.java (主程式)
    ↓
BookController (Controller - 流程控制)
    ↓
┌───────────────┴────────────────┐
│                                │
LibraryUI (View)          BookService (Model)
    ↓                            ↓
使用者介面               業務邏輯處理
                                 ↓
                        BookRepository (Data)
                                 ↓
                           BookInfo[] (資料)
```

### 核心元件

| 元件 | 檔案 | 職責 |
|------|------|------|
| **View** | `LibraryUI.java` | 處理所有使用者介面互動 |
| **Controller** | `BookController.java` | 協調 UI 和業務邏輯 |
| **Model** | `BookService.java` | 處理所有業務邏輯和驗證 |
| **Data** | `BookRepository.java` | 管理書籍資料存取 |
| **Entity** | `BookInfo.java` | 書籍資料物件 |
| **Auth** | `AuthenticationHelper.java` | 統一認證處理 |
| **Session** | `UserSession.java` | 會話狀態管理 |

### 設計特色

- ✅ **職責分離** - UI、業務邏輯、資料存取完全分離
- ✅ **可測試性** - 每層可獨立進行單元測試
- ✅ **可維護性** - 清晰的程式碼結構，易於理解和修改
- ✅ **可擴充性** - 新增功能不影響現有程式碼
- ✅ **可替換性** - UI 框架可輕易替換（Swing → JavaFX → Web）

## 預設資料

### 管理員帳號

| 身分 | 帳號 | 密碼 |
|------|------|------|
| 館長 | 0001 | 1111 |
| 館員 1 | 0002 | 2222 |
| 館員 2 | 0003 | 3333 |
| 館員 3 | 0004 | 4444 |
| 館員 4 | 0005 | 5555 |
| 館員 5 | 0006 | 6666 |

### 預設館藏

系統預設包含以下書籍：

| ID | 書名 | 作者 | 出版社 |
|----|------|------|--------|
| 001 | Java | 吳柏毅 | 中央大學 |
| 002 | 管數課本 | 吳昀蓁 | 台灣大學 |
| 003 | 英文課本 | 林俊廷 | 交通大學 |
| 004 | 國文課本 | 屠安弟 | 政治大學 |
| 005 | 體育課本 | 陳重言 | 清華大學 |

## 專案結構

```
JavaProj 2/
├── UserClass3.java              # 主程式 (最新版本)
├── BookController.java          # Controller 層
├── BookService.java             # Model 層（業務邏輯）
├── LibraryUI.java               # View 層（使用者介面）
├── BookRepository.java          # 資料存取層
├── BookInfo.java                # 書籍資料物件
├── BorrowResult.java            # 統一結果處理
├── AuthenticationHelper.java   # 統一認證
├── UserSession.java             # 會話管理
├── TestMVC.java                 # MVC 測試程式
├── TestBook2.java               # 資料結構測試
├── README.md                    # 本文件
├── CLAUDE.md                    # AI 開發指南
└── Improve_list/                # 改進報告
    ├── Improve_design.md        # 改進計畫
    ├── Phase1_完成報告.md       # Bug 修正報告
    ├── Phase2_完成報告.md       # 資料結構重構
    ├── Phase3_完成報告.md       # 認證重構
    ├── Phase4_完成報告.md       # MVC 架構
    └── Phase5_完成報告.md       # 最終整合
```

## 測試

系統包含完整的測試程式：

### 執行 MVC 測試
```bash
java TestMVC
```

測試項目：
- ✅ MVC 各層初始化
- ✅ 新增書籍
- ✅ 搜尋書籍
- ✅ 借書功能
- ✅ 重複借書驗證
- ✅ 還書功能
- ✅ 統計資訊
- ✅ 編輯書籍
- ✅ 刪除書籍
- ✅ 輸入驗證

### 執行資料結構測試
```bash
java TestBook2
```

測試項目：
- ✅ 資料初始化
- ✅ ID 搜尋
- ✅ 書名搜尋
- ✅ 借書流程
- ✅ 還書流程
- ✅ 書籍管理

## 開發資訊

### 技術棧
- **語言**: Java
- **UI 框架**: Swing (JOptionPane)
- **日期處理**: Java 8+ LocalDate API
- **設計模式**: MVC、Repository、Factory、Strategy

### 程式碼品質
- ✅ 遵循 SOLID 原則
- ✅ 完整的輸入驗證
- ✅ 統一的錯誤處理
- ✅ 無程式碼重複
- ✅ 清晰的註解

### 重構歷程
本系統經過完整的 5 階段重構：
1. **Phase 1**: 修正 7 個關鍵 bug
2. **Phase 2**: 重新設計資料結構
3. **Phase 3**: 重構認證與流程控制
4. **Phase 4**: 建立 MVC 架構
5. **Phase 5**: 完整整合與測試

詳細資訊請參閱 `Improve_list/` 目錄中的報告文件。

## Language


![Java progressbar](https://readme-components.vercel.app/api?component=linearprogress&value=100&skill=Java&fill=linear-gradient%2862deg%2C%20%238EC5FC%200%25%2C%20%23E0C3FC%20100%25%29%3B%0A)

