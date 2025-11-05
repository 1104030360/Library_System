import axios from 'axios'
import type {
  Book,
  User,
  LoginCredentials,
  RegisterData,
  Stats,
  BorrowHistory,
  BookRating,
  RateBookRequest,
  BookReview,
  AddReviewRequest,
  UpdateReviewRequest,
  RecommendationWithBook,
  RecommendationsResponse,
  AIHealthResponse,
  Notification,
  NotificationsResponse,
  UnreadCountResponse,
  MarkAllAsReadResponse,
  ClearNotificationsResponse,
  AccountInfo,
  CreateAccountRequest,
  AccountsListResponse,
  DashboardStats,
  DashboardStatsResponse,
  DailyBorrowCount,
  BorrowTrendResponse,
  SystemInfo,
  SystemInfoResponse,
  ApiResponse
} from '@/types'

// 建立 axios 實例
const api = axios.create({
  baseURL: '/api',
  timeout: 300000,  // 增加到 5 分鐘（AI 生成需要較長時間）
  withCredentials: true, // 支援 cookies
})

// API 方法
export const bookApi = {
  // 取得所有書籍
  async getBooks(): Promise<Book[]> {
    const response = await api.get<{ success: boolean; books: Book[] }>('/books')
    return response.data.books
  },

  // 取得特定書籍
  async getBookById(id: string): Promise<Book> {
    const response = await api.get<Book>(`/books?id=${id}`)
    return response.data
  },

  // 借書
  async borrowBook(bookId: string): Promise<void> {
    await api.post('/books/borrow', { bookId })
  },

  // 還書
  async returnBook(bookId: string): Promise<void> {
    await api.post('/books/return', { bookId })
  },

  // 新增書籍 (管理員專用)
  async addBook(book: Omit<Book, 'isAvailable'>): Promise<Book> {
    const response = await api.post<{
      success: boolean
      message: string
      book: Book
    }>('/books/add', book)
    return response.data.book
  },

  // 更新書籍 (管理員專用)
  async updateBook(book: Omit<Book, 'isAvailable'>): Promise<void> {
    await api.put('/books/update', book)
  },

  // 刪除書籍 (管理員專用)
  async deleteBook(bookId: string): Promise<void> {
    await api.delete('/books/delete', { data: { id: bookId } })
  },

  // ===== 帳號管理 API (Phase 12) =====

  // 獲取所有使用者（館長+館員）
  async getUsers(): Promise<AccountInfo[]> {
    const response = await api.get<AccountsListResponse>('/accounts/users')
    return response.data.users
  },

  // 創建使用者（館長+館員）
  async createUser(data: CreateAccountRequest): Promise<ApiResponse> {
    const response = await api.post('/accounts/users', data)
    return response.data
  },

  // 刪除使用者（館長+館員）
  async deleteUser(userId: string): Promise<ApiResponse> {
    const response = await api.delete('/accounts/users', { data: { userId } })
    return response.data
  },

  // 獲取所有館員（僅館長）
  async getStaff(): Promise<AccountInfo[]> {
    const response = await api.get<AccountsListResponse>('/accounts/staff')
    return response.data.users
  },

  // 創建館員（僅館長）
  async createStaff(data: CreateAccountRequest): Promise<ApiResponse> {
    const response = await api.post('/accounts/staff', data)
    return response.data
  },

  // 刪除館員（僅館長）
  async deleteStaff(userId: string): Promise<ApiResponse> {
    const response = await api.delete('/accounts/staff', { data: { userId } })
    return response.data
  },

  // ===== 儀表板統計 API (Phase 12 Enhancement) =====

  // 獲取儀表板統計數據（館長+館員）
  async getDashboardStats(): Promise<DashboardStats> {
    const response = await api.get<DashboardStatsResponse>('/dashboard/stats')
    return response.data.stats
  },

  // 獲取借閱趨勢數據（館長+館員）
  async getBorrowTrend(days: number = 30): Promise<DailyBorrowCount[]> {
    const response = await api.get<BorrowTrendResponse>(`/dashboard/borrow-trend?days=${days}`)
    return response.data.data
  },

  // 獲取系統資訊（館長+館員）
  async getSystemInfo(): Promise<SystemInfo> {
    const response = await api.get<SystemInfoResponse>('/dashboard/system-info')
    return response.data.data
  },

  // 獲取熱門書籍（館長+館員）
  async getTopBooks(limit: number = 10): Promise<Book[]> {
    const response = await api.get<{ success: boolean; books: Book[] }>(`/dashboard/top-books?limit=${limit}`)
    return response.data.books
  },
}

export const authApi = {
  // 註冊
  async register(data: RegisterData): Promise<User> {
    const response = await api.post<{
      success: boolean
      userId: string
      name: string
      sessionId: string
    }>('/auth/register', data)
    return {
      username: response.data.userId,
      name: response.data.name,
      userType: 'user',
    }
  },

  // 登入
  async login(credentials: LoginCredentials): Promise<User> {
    const response = await api.post<{
      success: boolean
      username: string
      name?: string
      userType: string
    }>('/auth/login', credentials)
    return {
      username: response.data.username,
      name: response.data.name,
      userType: response.data.userType,
    }
  },

  // 登出
  async logout(): Promise<void> {
    await api.post('/auth/logout')
  },

  // 取得當前使用者
  async whoami(): Promise<User> {
    const response = await api.get<{
      success: boolean
      username: string
      userType: string
    }>('/auth/whoami')
    return {
      username: response.data.username,
      userType: response.data.userType,
    }
  },
}

export const statsApi = {
  // 取得統計資訊
  async getStats(): Promise<Stats> {
    const response = await api.get<{ success: boolean; statistics: string }>('/stats')
    // 解析字符串格式: "Total: 5 books | Available: 0 | Borrowed: 5"
    const stats = response.data.statistics
    const totalMatch = stats.match(/Total:\s*(\d+)/)
    const availableMatch = stats.match(/Available:\s*(\d+)/)
    const borrowedMatch = stats.match(/Borrowed:\s*(\d+)/)

    return {
      totalBooks: totalMatch ? parseInt(totalMatch[1]) : 0,
      availableBooks: availableMatch ? parseInt(availableMatch[1]) : 0,
      borrowedBooks: borrowedMatch ? parseInt(borrowedMatch[1]) : 0,
    }
  },

  // 取得伺服器狀態
  async getStatus(): Promise<any> {
    const response = await api.get('/status')
    return response.data
  },
}

// 借閱歷史 API
export const historyApi = {
  // 取得使用者借閱歷史
  async getUserHistory(): Promise<BorrowHistory[]> {
    const response = await api.get<{
      success: boolean
      history: BorrowHistory[]
    }>('/history/user')
    return response.data.history
  },

  // 取得當前借閱中的書籍
  async getCurrentBorrowings(): Promise<BorrowHistory[]> {
    const response = await api.get<{
      success: boolean
      current: BorrowHistory[]
    }>('/history/current')
    return response.data.current
  },

  // 取得特定書籍的借閱歷史
  async getBookHistory(bookId: string): Promise<BorrowHistory[]> {
    const response = await api.get<{
      success: boolean
      history: BorrowHistory[]
    }>(`/history/book?bookId=${bookId}`)
    return response.data.history
  },

  // 取得所有借閱記錄（管理員專用）
  async getAllHistory(): Promise<BorrowHistory[]> {
    const response = await api.get<{
      success: boolean
      history: BorrowHistory[]
    }>('/history/all')
    return response.data.history
  },
}

// 評分 API
export const ratingApi = {
  // 評分/更新評分
  async rateBook(data: RateBookRequest): Promise<BookRating> {
    const response = await api.post<{
      success: boolean
      averageRating: number
      ratingCount: number
    }>('/ratings/rate', data)
    return {
      bookId: data.bookId,
      averageRating: response.data.averageRating,
      ratingCount: response.data.ratingCount,
    }
  },

  // 取得書籍評分統計
  async getBookRating(bookId: string): Promise<BookRating> {
    const response = await api.get<{
      success: boolean
      averageRating: number
      ratingCount: number
      userRating?: number
    }>(`/ratings/book?bookId=${bookId}`)
    return {
      bookId,
      averageRating: response.data.averageRating,
      ratingCount: response.data.ratingCount,
      userRating: response.data.userRating,
    }
  },

  // 取得使用者所有評分
  async getUserRatings(): Promise<Array<{ bookId: string; rating: number }>> {
    const response = await api.get<{
      success: boolean
      ratings: Array<{ bookId: string; rating: number }>
    }>('/ratings/user')
    return response.data.ratings
  },

  // 取得高分書籍推薦
  async getTopRatedBooks(limit: number = 10, minRating: number = 4.0): Promise<Array<{
    bookId: string
    bookTitle: string
    averageRating: number
    ratingCount: number
  }>> {
    const response = await api.get<{
      success: boolean
      topBooks: Array<{
        bookId: string
        bookTitle: string
        averageRating: number
        ratingCount: number
      }>
    }>(`/ratings/top?limit=${limit}&minRating=${minRating}`)
    return response.data.topBooks
  },
}

// 評論 API
export const reviewApi = {
  // 新增評論
  async addReview(data: AddReviewRequest): Promise<{ reviewId: number }> {
    const response = await api.post<{
      success: boolean
      message: string
      reviewId: number
    }>('/reviews/add', data)
    return { reviewId: response.data.reviewId }
  },

  // 取得書籍評論
  async getBookReviews(bookId: string): Promise<{
    reviews: BookReview[]
    reviewCount: number
  }> {
    const response = await api.get<{
      success: boolean
      reviews: BookReview[]
      reviewCount: number
    }>(`/reviews/book?bookId=${bookId}`)
    return {
      reviews: response.data.reviews,
      reviewCount: response.data.reviewCount,
    }
  },

  // 取得使用者評論
  async getUserReviews(): Promise<BookReview[]> {
    const response = await api.get<{
      success: boolean
      reviews: BookReview[]
    }>('/reviews/user')
    return response.data.reviews
  },

  // 更新評論
  async updateReview(data: UpdateReviewRequest): Promise<void> {
    await api.put('/reviews/update', data)
  },

  // 刪除評論
  async deleteReview(reviewId: number): Promise<void> {
    await api.delete(`/reviews/delete?reviewId=${reviewId}`)
  },

  // 取得最新評論
  async getLatestReviews(limit: number = 10): Promise<BookReview[]> {
    const response = await api.get<{
      success: boolean
      reviews: BookReview[]
    }>(`/reviews/latest?limit=${limit}`)
    return response.data.reviews
  },
}

// AI 推薦 API (Phase 10)
export const recommendationApi = {
  // 取得個人化推薦
  async getPersonalRecommendations(): Promise<RecommendationWithBook[]> {
    // Step 1: Create recommendation task (returns immediately with taskId)
    const taskResponse = await api.post<{ success: boolean; taskId: string; status: string }>(
      '/recommendations/personal'
    )

    const taskId = taskResponse.data.taskId

    // Step 2: Connect to WebSocket and wait for result
    return new Promise(async (resolve, reject) => {
      const ws = new WebSocket(`ws://localhost:7071`)

      // Set timeout (60 seconds)
      const timeout = setTimeout(() => {
        ws.close()
        reject(new Error('Recommendation timeout'))
      }, 60000)

      ws.onopen = () => {
        console.log('→ WebSocket connected for task:', taskId)
        // Subscribe to task updates
        ws.send(JSON.stringify({ action: 'subscribe', taskId }))
      }

      ws.onmessage = async (event) => {
        try {
          const data = JSON.parse(event.data)

          if (data.status === 'completed') {
            clearTimeout(timeout)
            ws.close()

            // Parse recommendations from JSON string
            const backendRecommendations = JSON.parse(data.recommendations)
            console.log('📥 Backend recommendations:', backendRecommendations)

            // Transform backend format to frontend format
            // Backend: { bookId, reason, score }
            // Frontend: { book: Book, reason, score }
            const transformedRecommendations: RecommendationWithBook[] = []

            for (const rec of backendRecommendations) {
              try {
                // Fetch full book details
                const book = await bookApi.getBookById(rec.bookId)
                transformedRecommendations.push({
                  book: book,
                  reason: rec.reason,
                  score: rec.score
                })
              } catch (error) {
                console.error(`Failed to fetch book ${rec.bookId}:`, error)
                // Skip this recommendation if book fetch fails
              }
            }

            console.log('✅ Transformed recommendations:', transformedRecommendations)
            resolve(transformedRecommendations)
          } else if (data.status === 'failed') {
            clearTimeout(timeout)
            ws.close()
            reject(new Error(data.error || 'Recommendation failed'))
          }
        } catch (error) {
          console.error('Error parsing WebSocket message:', error)
          clearTimeout(timeout)
          ws.close()
          reject(error)
        }
      }

      ws.onerror = (error) => {
        clearTimeout(timeout)
        ws.close()
        reject(new Error('WebSocket connection error'))
      }

      ws.onclose = () => {
        clearTimeout(timeout)
      }
    })
  },

  // 取得相關書籍推薦
  async getRelatedRecommendations(bookId: string): Promise<RecommendationWithBook[]> {
    const response = await api.get<RecommendationsResponse>(
      `/recommendations/related?bookId=${bookId}`,
      {
        timeout: 300000, // AI generation may take longer (5 minutes for CPU-based llama3.2)
      }
    )
    return response.data.recommendations
  },

  // 檢查 AI 服務健康狀態
  async checkAIHealth(): Promise<boolean> {
    try {
      const response = await api.get<AIHealthResponse>('/recommendations/health')
      return response.data.healthy
    } catch (error) {
      console.error('Failed to check AI health:', error)
      return false
    }
  },
}

// 通知 API (Phase 13)
export const notificationApi = {
  // 獲取通知列表
  async getNotifications(params?: {
    unreadOnly?: boolean
    type?: string
    limit?: number
    offset?: number
  }): Promise<NotificationsResponse> {
    const queryParams = new URLSearchParams()
    if (params?.unreadOnly) queryParams.append('unreadOnly', 'true')
    if (params?.type) queryParams.append('type', params.type)
    if (params?.limit) queryParams.append('limit', params.limit.toString())
    if (params?.offset) queryParams.append('offset', params.offset.toString())

    const response = await api.get<NotificationsResponse>(
      `/notifications?${queryParams.toString()}`
    )
    return response.data
  },

  // 獲取未讀數量
  async getUnreadCount(): Promise<UnreadCountResponse> {
    const response = await api.get<UnreadCountResponse>('/notifications/unread-count')
    return response.data
  },

  // 標記單個通知為已讀
  async markAsRead(notificationId: number): Promise<void> {
    await api.post(`/notifications/${notificationId}/read`)
  },

  // 標記全部為已讀
  async markAllAsRead(): Promise<MarkAllAsReadResponse> {
    const response = await api.post<MarkAllAsReadResponse>('/notifications/read-all')
    return response.data
  },

  // 清空所有通知
  async clearNotifications(): Promise<ClearNotificationsResponse> {
    const response = await api.delete<ClearNotificationsResponse>('/notifications/clear')
    return response.data
  },
}

// 錯誤處理攔截器
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // 未授權 - 靜默處理，不顯示錯誤（checkAuth 會處理）
      // console.error('未授權，請重新登入')
    }
    return Promise.reject(error)
  }
)

export default api
