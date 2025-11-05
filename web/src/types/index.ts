// 書籍介面
export interface Book {
  id: string
  title: string
  author: string
  publisher: string
  description?: string
  isAvailable: boolean
  // 統計數據 (Phase 12 Enhancement)
  borrowCount?: number      // 借閱次數
  averageRating?: number    // 平均評分
  reviewCount?: number      // 評論數量
}

// 使用者介面
export interface User {
  username: string
  name?: string  // 使用者姓名
  userType: string
}

// 登入憑證介面
export interface LoginCredentials {
  username: string
  password: string
}

// 註冊資料介面
export interface RegisterData {
  id: string
  name: string
  password: string
  email?: string
}

// 統計資訊介面
export interface Stats {
  totalBooks: number
  availableBooks: number
  borrowedBooks: number
}

// API 回應介面
export interface ApiResponse<T = any> {
  success: boolean
  data?: T
  message?: string
}

// 排序選項
export type SortOption = 'id' | 'title' | 'author' | 'available'

// 借閱歷史介面
export interface BorrowHistory {
  id: number
  userId: string
  userName: string
  bookId: string
  bookTitle: string
  borrowDate: string
  dueDate: string
  returnDate: string | null
  status: 'borrowing' | 'returned' | 'overdue'
}

// 書籍評分介面
export interface BookRating {
  bookId: string
  averageRating: number
  ratingCount: number
  userRating?: number
}

// 評分請求介面
export interface RateBookRequest {
  bookId: string
  rating: number
}

// 書籍評論介面
export interface BookReview {
  id: number
  userId: string
  userName: string
  bookId: string
  bookTitle: string
  reviewText: string
  createdAt: string
  updatedAt: string | null
}

// 評論請求介面
export interface AddReviewRequest {
  bookId: string
  reviewText: string
}

export interface UpdateReviewRequest {
  reviewId: number
  reviewText: string
}

// AI 推薦介面 (Phase 10)
export interface Recommendation {
  bookId: string
  reason: string
  score: number
}

export interface RecommendationWithBook {
  book: Book
  reason: string
  score: number
}

export interface RecommendationsResponse {
  success: boolean
  recommendations: RecommendationWithBook[]
}

export interface AIHealthResponse {
  healthy: boolean
}

// 通知系統介面 (Phase 13)
export interface Notification {
  id: number
  userId: string
  type: 'system' | 'borrow' | 'return' | 'review' | 'due'
  title: string
  message: string
  link: string | null
  read: boolean
  deleted: boolean
  createdAt: string
  readAt: string | null
}

export interface NotificationsResponse {
  success: boolean
  notifications: Notification[]
  unreadCount: number
  totalCount: number
}

export interface UnreadCountResponse {
  success: boolean
  unreadCount: number
}

export interface MarkAllAsReadResponse {
  success: boolean
  message: string
  updatedCount: number
}

export interface ClearNotificationsResponse {
  success: boolean
  message: string
  deletedCount: number
}

// 帳號管理介面 (Phase 12)
export interface AccountInfo {
  id: string
  name: string
  email: string
  userType: string
  createdAt: string
  lastLogin: string | null
}

// 儀表板統計介面 (Phase 12 Enhancement)
export interface DashboardStats {
  totalBooks: number
  totalUsers: number
  todayBorrows: number
  averageRating: number
  yesterdayUsers: number
  yesterdayBorrows: number
  yesterdayAvgRating: number
}

export interface DashboardStatsResponse {
  success: boolean
  stats: DashboardStats
}

// 借閱趨勢介面
export interface DailyBorrowCount {
  date: string
  count: number
}

export interface BorrowTrendResponse {
  success: boolean
  data: DailyBorrowCount[]
}

// 系統資訊介面
export interface SystemInfo {
  version: string
  databaseSize: string
  totalRecords: number
  uptime: string
}

export interface SystemInfoResponse {
  success: boolean
  data: SystemInfo
}

export interface CreateAccountRequest {
  id: string
  name: string
  password: string
  email?: string
}

export interface DeleteAccountRequest {
  userId: string
}

export interface AccountsListResponse {
  success: boolean
  users: AccountInfo[]
}
