<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { NGrid, NGridItem, NCard, NProgress, NSpin } from 'naive-ui'
import { useBooksStore } from '@/stores/books'
import { bookApi } from '@/api'
import type { SystemInfo } from '@/types'

const booksStore = useBooksStore()

// 系統資訊
const systemInfo = ref<SystemInfo | null>(null)
const loadingSystemInfo = ref(false)

// 載入系統資訊
async function loadSystemInfo() {
  loadingSystemInfo.value = true
  try {
    systemInfo.value = await bookApi.getSystemInfo()
  } catch (error) {
    console.error('Failed to load system info:', error)
  } finally {
    loadingSystemInfo.value = false
  }
}

onMounted(() => {
  loadSystemInfo()
})

// 分類詳細統計
const categoryStats = computed(() => {
  const books = booksStore.books
  const total = books.length

  const categories = {
    '計算機類': books.filter((b: any) => {
      const id = parseInt(b.id)
      return id >= 1 && id <= 5
    }).length,
    '商管類': books.filter((b: any) => {
      const id = parseInt(b.id)
      return id >= 6 && id <= 10
    }).length,
    '文學類': books.filter((b: any) => {
      const id = parseInt(b.id)
      return id >= 11 && id <= 14
    }).length,
    '科學類': books.filter((b: any) => {
      const id = parseInt(b.id)
      return id >= 15 && id <= 17
    }).length,
    '語言學習': books.filter((b: any) => {
      const id = parseInt(b.id)
      return id >= 18 && id <= 20
    }).length
  }

  return Object.entries(categories)
    .filter(([_, count]) => count > 0)
    .map(([category, count]) => ({
      category,
      count,
      percentage: total > 0 ? Math.round((count / total) * 100) : 0
    }))
    .sort((a, b) => b.count - a.count)
})

// 狀態統計
const statusStats = computed(() => {
  const available = booksStore.stats.availableBooks
  const borrowed = booksStore.stats.borrowedBooks
  const total = booksStore.stats.totalBooks

  return [
    {
      label: '可借閱',
      count: available,
      percentage: total > 0 ? Math.round((available / total) * 100) : 0,
      color: 'success'
    },
    {
      label: '已借出',
      count: borrowed,
      percentage: total > 0 ? Math.round((borrowed / total) * 100) : 0,
      color: 'warning'
    }
  ]
})
</script>

<template>
  <div class="space-y-6">
    <!-- 分類統計 -->
    <NCard title="書籍分類統計" :bordered="false" class="bg-slate-50">
      <div class="space-y-4">
        <div
          v-for="stat in categoryStats"
          :key="stat.category"
          class="flex items-center gap-4"
        >
          <span class="text-sm font-bold text-slate-700 w-24">{{ stat.category }}</span>
          <div class="flex-1">
            <NProgress
              :percentage="stat.percentage"
              :show-indicator="false"
              :height="24"
              :border-radius="12"
              type="line"
            />
          </div>
          <span class="text-sm font-bold text-slate-600 w-28 text-right">
            {{ stat.count }} 本 ({{ stat.percentage }}%)
          </span>
        </div>
      </div>
    </NCard>

    <!-- 借閱狀態統計 -->
    <NCard title="借閱狀態統計" :bordered="false" class="bg-slate-50">
      <div class="space-y-4">
        <div
          v-for="stat in statusStats"
          :key="stat.label"
          class="flex items-center gap-4"
        >
          <span class="text-sm font-bold text-slate-700 w-24">{{ stat.label }}</span>
          <div class="flex-1">
            <NProgress
              :percentage="stat.percentage"
              :show-indicator="false"
              :height="24"
              :border-radius="12"
              :type="stat.color as any"
            />
          </div>
          <span class="text-sm font-bold text-slate-600 w-28 text-right">
            {{ stat.count }} 本 ({{ stat.percentage }}%)
          </span>
        </div>
      </div>
    </NCard>

    <!-- 系統資訊 -->
    <NCard title="系統資訊" :bordered="false" class="bg-slate-50">
      <NSpin :show="loadingSystemInfo">
        <div v-if="systemInfo" class="space-y-3">
          <div class="flex justify-between items-center py-2 border-b border-slate-200">
            <span class="text-sm font-medium text-slate-600">版本號</span>
            <span class="text-sm font-bold text-slate-800">{{ systemInfo.version }}</span>
          </div>
          <div class="flex justify-between items-center py-2 border-b border-slate-200">
            <span class="text-sm font-medium text-slate-600">資料庫大小</span>
            <span class="text-sm font-bold text-slate-800">{{ systemInfo.databaseSize }}</span>
          </div>
          <div class="flex justify-between items-center py-2 border-b border-slate-200">
            <span class="text-sm font-medium text-slate-600">總記錄數</span>
            <span class="text-sm font-bold text-slate-800">{{ systemInfo.totalRecords.toLocaleString() }} 筆</span>
          </div>
          <div class="flex justify-between items-center py-2">
            <span class="text-sm font-medium text-slate-600">系統運行時間</span>
            <span class="text-sm font-bold text-slate-800">{{ systemInfo.uptime }}</span>
          </div>
        </div>
        <div v-else class="text-center text-slate-400 py-4">
          載入中...
        </div>
      </NSpin>
    </NCard>
  </div>
</template>
