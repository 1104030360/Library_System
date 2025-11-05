<script setup lang="ts">
import { computed } from 'vue'
import { useBooksStore } from '@/stores/books'
import { NProgress } from 'naive-ui'

interface Props {
  mode?: 'guest' | 'user' | 'admin'
}

const props = withDefaults(defineProps<Props>(), {
  mode: 'user'
})

const booksStore = useBooksStore()

// 管理員專用：書籍分類統計
const categoryStats = computed(() => {
  if (props.mode !== 'admin') return []

  const books = booksStore.books
  const total = books.length

  // 計算各分類數量（根據書籍 ID 前綴判斷分類）
  const categories = {
    '計算機類': books.filter((b: any) => b.id.startsWith('C')).length,
    '商管類': books.filter((b: any) => b.id.startsWith('B')).length,
    '文學類': books.filter((b: any) => b.id.startsWith('L')).length,
    '科學類': books.filter((b: any) => b.id.startsWith('S')).length,
    '語言學習': books.filter((b: any) => b.id.startsWith('E')).length
  }

  return Object.entries(categories)
    .filter(([_, count]) => count > 0)
    .map(([category, count]) => ({
      category,
      count,
      percentage: total > 0 ? Math.round((count / total) * 100) : 0
    }))
})
</script>

<template>
  <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
    <!-- 總書籍數 -->
    <div class="bg-white/90 backdrop-blur-xl border border-slate-200 rounded-2xl p-6 shadow-lg">
      <div class="text-sm font-semibold text-slate-600 uppercase tracking-wide mb-2">
        總書籍數
      </div>
      <div class="text-5xl font-bold text-slate-800">
        {{ booksStore.stats.totalBooks }}
      </div>
    </div>

    <!-- 可借閱 -->
    <div class="bg-white/90 backdrop-blur-xl border border-slate-200 rounded-2xl p-6 shadow-lg">
      <div class="text-sm font-semibold text-slate-600 uppercase tracking-wide mb-2">
        可借閱
      </div>
      <div class="text-5xl font-bold text-slate-800">
        {{ booksStore.stats.availableBooks }}
      </div>
    </div>

    <!-- 已借出 -->
    <div class="bg-white/90 backdrop-blur-xl border border-slate-200 rounded-2xl p-6 shadow-lg">
      <div class="text-sm font-semibold text-slate-600 uppercase tracking-wide mb-2">
        已借出
      </div>
      <div class="text-5xl font-bold text-slate-800">
        {{ booksStore.stats.borrowedBooks }}
      </div>
    </div>

    <!-- 管理員專用：分類統計 -->
    <div
      v-if="mode === 'admin' && categoryStats.length > 0"
      class="bg-white/90 backdrop-blur-xl border border-slate-200 rounded-2xl p-6 shadow-lg sm:col-span-3"
    >
      <div class="text-sm font-semibold text-slate-600 uppercase tracking-wide mb-4">
        書籍分類統計
      </div>
      <div class="space-y-3">
        <div
          v-for="stat in categoryStats"
          :key="stat.category"
          class="flex items-center gap-3"
        >
          <span class="text-sm font-semibold text-slate-700 w-20">{{ stat.category }}</span>
          <div class="flex-1">
            <NProgress
              :percentage="stat.percentage"
              :show-indicator="false"
              :height="20"
              :border-radius="10"
            />
          </div>
          <span class="text-sm font-bold text-slate-600 w-16 text-right">
            {{ stat.count }} 本 ({{ stat.percentage }}%)
          </span>
        </div>
      </div>
    </div>
  </div>
</template>
