<script setup lang="ts">
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useBooksStore } from '@/stores/books'
import { useRouter } from 'vue-router'
import { RouterLink } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import StatsBar from '@/components/StatsBar.vue'
import SearchBar from '@/components/SearchBar.vue'
import BookList from '@/components/BookList.vue'
import LiquidBackground from '@/components/LiquidBackground.vue'

const authStore = useAuthStore()
const booksStore = useBooksStore()
const router = useRouter()

onMounted(async () => {
  // 檢查登入狀態
  await authStore.checkAuth()

  // 如果已登入，根據角色重定向到對應頁面
  if (authStore.isLoggedIn) {
    if (authStore.isAdmin) {
      router.push('/admin')      // 館長 → /admin
    } else if (authStore.isStaff) {
      router.push('/staff')      // 館員 → /staff
    } else {
      router.push('/user')       // 使用者 → /user
    }
    return
  }

  // 未登入用戶：載入書籍和統計資訊（公開瀏覽模式）
  await Promise.all([booksStore.loadBooks(), booksStore.loadStats()])
})
</script>

<template>
  <div class="relative min-h-screen">
    <!-- 流動背景 -->
    <LiquidBackground />

    <div class="relative z-10">
      <AppHeader />

      <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <!-- 公開首頁歡迎區 -->
        <div class="mb-8 text-center">
          <h1 class="text-6xl font-bold text-slate-800 text-overlay mb-4" style="letter-spacing: -0.02em;">
            中大圖書館借還系統
          </h1>
          <p class="text-2xl text-slate-700 font-semibold text-overlay">
            探索豐富的館藏圖書資源
          </p>
        </div>

        <!-- 統計資訊 -->
        <div class="mb-6">
          <StatsBar mode="guest" />
        </div>

        <!-- 搜尋和排序 -->
        <div class="mb-6">
          <SearchBar />
        </div>

        <!-- 書籍列表 - 訪客模式 -->
        <BookList mode="guest" />
      </main>
    </div>
  </div>
</template>
