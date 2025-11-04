import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { bookApi, statsApi } from '@/api'
import type { Book, Stats, SortOption } from '@/types'

export const useBooksStore = defineStore('books', () => {
  // 狀態
  const books = ref<Book[]>([])
  const stats = ref<Stats>({
    totalBooks: 0,
    availableBooks: 0,
    borrowedBooks: 0,
  })
  const loading = ref(false)
  const searchQuery = ref('')
  const sortBy = ref<SortOption>('id')

  // 計算屬性 - 篩選和排序後的書籍
  const filteredBooks = computed(() => {
    let result = [...books.value]

    // 搜尋篩選
    if (searchQuery.value) {
      const query = searchQuery.value.toLowerCase()
      result = result.filter(
        (book) =>
          book.title.toLowerCase().includes(query) ||
          book.author.toLowerCase().includes(query) ||
          book.publisher.toLowerCase().includes(query) ||
          book.id.toLowerCase().includes(query)
      )
    }

    // 排序
    result.sort((a, b) => {
      switch (sortBy.value) {
        case 'id':
          return a.id.localeCompare(b.id)
        case 'title':
          return a.title.localeCompare(b.title)
        case 'author':
          return a.author.localeCompare(b.author)
        case 'available':
          if (a.isAvailable === b.isAvailable) {
            return a.id.localeCompare(b.id)
          }
          return a.isAvailable ? -1 : 1
        default:
          return 0
      }
    })

    return result
  })

  // 載入所有書籍
  async function loadBooks() {
    loading.value = true
    try {
      books.value = await bookApi.getBooks()
    } catch (error) {
      console.error('載入書籍失敗:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 載入統計資訊
  async function loadStats() {
    try {
      stats.value = await statsApi.getStats()
    } catch (error) {
      console.error('載入統計資訊失敗:', error)
    }
  }

  // 借書
  async function borrowBook(bookId: string) {
    try {
      await bookApi.borrowBook(bookId)
      // 重新載入書籍列表
      await loadBooks()
      await loadStats()

      // 立即更新未讀通知數量（通知已經由後端創建）
      const { useNotificationsStore } = await import('./notifications')
      const notificationsStore = useNotificationsStore()
      await notificationsStore.fetchUnreadCount()

      return { success: true, message: '借書成功' }
    } catch (error: any) {
      return {
        success: false,
        message: error.response?.data?.message || '借書失敗',
      }
    }
  }

  // 還書
  async function returnBook(bookId: string) {
    try {
      await bookApi.returnBook(bookId)
      // 重新載入書籍列表
      await loadBooks()
      await loadStats()

      // 立即更新未讀通知數量（通知已經由後端創建）
      const { useNotificationsStore } = await import('./notifications')
      const notificationsStore = useNotificationsStore()
      await notificationsStore.fetchUnreadCount()

      return { success: true, message: '還書成功' }
    } catch (error: any) {
      return {
        success: false,
        message: error.response?.data?.message || '還書失敗',
      }
    }
  }

  // 新增書籍 (管理員專用)
  async function addBook(book: Omit<Book, 'isAvailable'>) {
    try {
      await bookApi.addBook(book)
      // 重新載入書籍列表和統計資訊
      await loadBooks()
      await loadStats()
      return { success: true, message: '新增書籍成功' }
    } catch (error: any) {
      return {
        success: false,
        message: error.response?.data?.error || error.response?.data?.message || '新增書籍失敗',
      }
    }
  }

  // 更新書籍 (管理員專用)
  async function updateBook(book: Omit<Book, 'isAvailable'>) {
    try {
      await bookApi.updateBook(book)
      // 重新載入書籍列表
      await loadBooks()
      return { success: true, message: '更新書籍成功' }
    } catch (error: any) {
      return {
        success: false,
        message: error.response?.data?.error || error.response?.data?.message || '更新書籍失敗',
      }
    }
  }

  // 刪除書籍 (管理員專用)
  async function deleteBook(bookId: string) {
    try {
      await bookApi.deleteBook(bookId)
      // 重新載入書籍列表和統計資訊
      await loadBooks()
      await loadStats()
      return { success: true, message: '刪除書籍成功' }
    } catch (error: any) {
      return {
        success: false,
        message: error.response?.data?.error || error.response?.data?.message || '刪除書籍失敗',
      }
    }
  }

  return {
    books,
    stats,
    loading,
    searchQuery,
    sortBy,
    filteredBooks,
    loadBooks,
    loadStats,
    borrowBook,
    returnBook,
    addBook,
    updateBook,
    deleteBook,
  }
})
