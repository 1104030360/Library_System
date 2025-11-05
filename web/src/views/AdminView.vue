<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useBooksStore } from '@/stores/books'
import { useMessage, useDialog, NCard, NTabs, NTabPane } from 'naive-ui'
import AppHeader from '@/components/AppHeader.vue'
import StatsBar from '@/components/StatsBar.vue'
import BookManagementTable from '@/components/BookManagementTable.vue'
import BorrowRecordsTable from '@/components/BorrowRecordsTable.vue'
import AccountManagement from '@/components/AccountManagement.vue'
import EditBookModal from '@/components/EditBookModal.vue'
import LiquidBackground from '@/components/LiquidBackground.vue'
import type { Book } from '@/types'

const authStore = useAuthStore()
const booksStore = useBooksStore()
const message = useMessage()
const dialog = useDialog()

const activeTab = ref('books')
const showEditModal = ref(false)
const editingBook = ref<Book | null>(null)

onMounted(async () => {
  await authStore.checkAuth()
  await Promise.all([
    booksStore.loadBooks(),
    booksStore.loadStats(),
    booksStore.loadMyBorrowings()
  ])
})

const handleEdit = (book: Book) => {
  editingBook.value = book
  showEditModal.value = true
}

const handleDelete = (book: Book) => {
  dialog.warning({
    title: '確認刪除',
    content: `確定要刪除書籍《${book.title}》嗎？此操作無法復原。`,
    positiveText: '確認刪除',
    negativeText: '取消',
    onPositiveClick: async () => {
      const result = await booksStore.deleteBook(book.id)
      if (result.success) {
        message.success('書籍已刪除')
      } else {
        message.error(result.message || '刪除失敗')
      }
    }
  })
}

const handleEditSuccess = () => {
  booksStore.loadBooks()
  booksStore.loadStats()
}
</script>

<template>
  <div class="relative min-h-screen">
    <LiquidBackground />

    <div class="relative z-10">
      <AppHeader />

      <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <!-- 頁面標題 - 管理員模式 -->
        <div class="mb-6">
          <div class="flex items-center gap-3">
            <h1
              class="text-5xl font-bold text-slate-800 text-overlay"
              style="letter-spacing: -0.02em"
            >
              圖書管理後台
            </h1>
            <span class="px-3 py-1 text-sm font-semibold bg-purple-100 text-purple-700 rounded-full">
              管理員模式
            </span>
          </div>
          <p class="text-xl text-slate-700 font-semibold mt-2 text-overlay">
            管理和查詢館藏圖書
          </p>
        </div>

        <!-- 統計資訊 - 管理員模式 -->
        <div class="mb-6">
          <StatsBar mode="admin" />
        </div>

        <!-- 功能標籤頁 -->
        <NCard :bordered="false" class="bg-white/90 backdrop-blur-xl shadow-lg">
          <NTabs v-model:value="activeTab" type="line" size="large">
            <!-- Tab 1: 書籍管理 -->
            <NTabPane name="books" tab="書籍管理">
              <BookManagementTable @edit="handleEdit" @delete="handleDelete" />
            </NTabPane>

            <!-- Tab 2: 借閱記錄 -->
            <NTabPane name="records" tab="借閱記錄">
              <BorrowRecordsTable />
            </NTabPane>

            <!-- Tab 3: 帳號管理 -->
            <NTabPane name="accounts" tab="帳號管理">
              <AccountManagement />
            </NTabPane>

            <!-- Tab 4: 統計分析 -->
            <NTabPane name="analytics" tab="統計分析">
              <div class="text-center py-12 text-slate-400">統計分析功能開發中...</div>
            </NTabPane>
          </NTabs>
        </NCard>
      </main>
    </div>

    <!-- 編輯書籍對話框 -->
    <EditBookModal v-model:show="showEditModal" :book="editingBook" @success="handleEditSuccess" />
  </div>
</template>
