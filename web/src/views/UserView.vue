<script setup lang="ts">
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useBooksStore } from '@/stores/books'
import AppHeader from '@/components/AppHeader.vue'
import StatsBar from '@/components/StatsBar.vue'
import SearchBar from '@/components/SearchBar.vue'
import BookList from '@/components/BookList.vue'
import LiquidBackground from '@/components/LiquidBackground.vue'
import PersonalRecommendations from '@/components/PersonalRecommendations.vue'

const authStore = useAuthStore()
const booksStore = useBooksStore()

onMounted(async () => {
  // 檢查登入狀態
  await authStore.checkAuth()

  // 載入書籍和統計資訊
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
      <!-- 頁面標題 - 使用者模式 -->
      <div class="mb-6">
        <h1 class="text-5xl font-bold text-slate-800 text-overlay" style="letter-spacing: -0.02em;">圖書借閱系統</h1>
        <p class="text-xl text-slate-700 font-semibold mt-2 text-overlay">探索和借閱館藏圖書</p>
      </div>

      <!-- 統計資訊 -->
      <div class="mb-6">
        <StatsBar />
      </div>

      <!-- 搜尋和排序 -->
      <div class="mb-6">
        <SearchBar />
      </div>

      <!-- AI 個人化推薦 (Phase 10) -->
      <PersonalRecommendations />

      <!-- 書籍列表 -->
      <BookList />
    </main>
    </div>
  </div>
</template>
