<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useBooksStore } from '@/stores/books'
import { bookApi } from '@/api'
import type { DashboardStats } from '@/types'
import { NCard, NGrid, NGridItem, NSpin, NStatistic, NTabs, NTabPane } from 'naive-ui'
import AppHeader from '@/components/AppHeader.vue'
import LiquidBackground from '@/components/LiquidBackground.vue'
import StatsCharts from '@/components/StatsCharts.vue'
import BorrowTrendChart from '@/components/BorrowTrendChart.vue'
import CategoryPieChart from '@/components/CategoryPieChart.vue'
import TopBooksTable from '@/components/TopBooksTable.vue'

const authStore = useAuthStore()
const booksStore = useBooksStore()

const loading = ref(true)
const activeTab = ref('overview')

// Dashboard 統計數據
const dashboardStats = ref<DashboardStats | null>(null)

// 統計數據 computed
const totalBooks = computed(() => dashboardStats.value?.totalBooks ?? 0)
const totalUsers = computed(() => dashboardStats.value?.totalUsers ?? 0)
const todayBorrows = computed(() => dashboardStats.value?.todayBorrows ?? 0)
const averageRating = computed(() => dashboardStats.value?.averageRating ?? 0)

// 趨勢計算 (今日 vs 昨日)
const booksTrend = computed(() => {
  if (!dashboardStats.value) return ''
  // 總藏書不會每天變動太大，顯示為穩定狀態
  return '→ 持平 (今日)'
})

const usersTrend = computed(() => {
  if (!dashboardStats.value) return ''
  const yesterday = dashboardStats.value.yesterdayUsers
  if (yesterday === 0) return '→ 昨日無新增'
  if (yesterday > 0) return `↑ 昨日 +${yesterday} 人`
  return '→ 持平 (今日)'
})

const borrowsTrend = computed(() => {
  if (!dashboardStats.value) return ''
  const today = dashboardStats.value.todayBorrows
  const yesterday = dashboardStats.value.yesterdayBorrows

  if (yesterday === 0) {
    if (today > 0) return `↑ 今日 ${today} 次`
    return '→ 昨日無借閱'
  }

  const diff = today - yesterday
  const percent = ((diff / yesterday) * 100).toFixed(0)

  if (diff > 0) return `↑ +${percent}% (較昨日)`
  if (diff < 0) return `↓ ${percent}% (較昨日)`
  return '→ 持平 (較昨日)'
})

const ratingTrend = computed(() => {
  if (!dashboardStats.value) return ''
  const current = dashboardStats.value.averageRating
  const yesterday = dashboardStats.value.yesterdayAvgRating

  if (yesterday === 0) return '→ 昨日無評分'

  const diff = (current - yesterday).toFixed(1)

  if (parseFloat(diff) > 0) return `↑ +${diff} (較昨日)`
  if (parseFloat(diff) < 0) return `↓ ${diff} (較昨日)`
  return '→ 持平 (較昨日)'
})

onMounted(async () => {
  await authStore.checkAuth()

  // 並行載入所有數據
  await Promise.all([
    booksStore.loadBooks(),
    booksStore.loadStats(),
    loadDashboardStats()
  ])

  loading.value = false
})

// 載入儀表板統計數據
async function loadDashboardStats() {
  try {
    dashboardStats.value = await bookApi.getDashboardStats()
  } catch (error) {
    console.error('Failed to load dashboard stats:', error)
  }
}
</script>

<template>
  <div class="relative min-h-screen">
    <LiquidBackground />

    <div class="relative z-10">
      <AppHeader />

      <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <!-- 頁面標題 -->
        <div class="mb-8">
          <div class="flex items-center gap-3">
            <h1
              class="text-5xl font-bold text-slate-800 text-overlay"
              style="letter-spacing: -0.02em"
            >
              館長管理儀表板
            </h1>
            <span class="px-3 py-1 text-sm font-semibold bg-gradient-to-r from-purple-100 to-blue-100 text-purple-700 rounded-full">
              Admin Dashboard
            </span>
          </div>
          <p class="text-xl text-slate-700 font-semibold mt-2 text-overlay">
            系統數據分析與可視化
          </p>
        </div>

        <!-- 載入中 -->
        <div v-if="loading" class="flex justify-center py-20">
          <NSpin size="large" />
        </div>

        <!-- Dashboard 內容 -->
        <div v-else>
          <!-- 核心統計卡片 -->
          <NGrid :cols="4" :x-gap="16" :y-gap="16" responsive="screen" class="mb-6">
            <!-- 總藏書 -->
            <NGridItem>
              <NCard :bordered="false" class="bg-white/90 backdrop-blur-xl shadow-lg">
                <NStatistic label="總藏書" :value="totalBooks">
                  <template #suffix>本</template>
                </NStatistic>
                <div
                  v-if="booksTrend"
                  class="text-sm font-semibold mt-2"
                  :class="{
                    'text-green-600': booksTrend.startsWith('↑'),
                    'text-red-600': booksTrend.startsWith('↓'),
                    'text-slate-500': booksTrend.startsWith('→')
                  }"
                >
                  {{ booksTrend }}
                </div>
              </NCard>
            </NGridItem>

            <!-- 註冊會員 -->
            <NGridItem>
              <NCard :bordered="false" class="bg-white/90 backdrop-blur-xl shadow-lg">
                <NStatistic label="註冊會員" :value="totalUsers">
                  <template #suffix>人</template>
                </NStatistic>
                <div
                  v-if="usersTrend"
                  class="text-sm font-semibold mt-2"
                  :class="{
                    'text-green-600': usersTrend.startsWith('↑'),
                    'text-red-600': usersTrend.startsWith('↓'),
                    'text-slate-500': usersTrend.startsWith('→')
                  }"
                >
                  {{ usersTrend }}
                </div>
              </NCard>
            </NGridItem>

            <!-- 今日借閱 -->
            <NGridItem>
              <NCard :bordered="false" class="bg-white/90 backdrop-blur-xl shadow-lg">
                <NStatistic label="今日借閱" :value="todayBorrows">
                  <template #suffix>次</template>
                </NStatistic>
                <div
                  v-if="borrowsTrend"
                  class="text-sm font-semibold mt-2"
                  :class="{
                    'text-green-600': borrowsTrend.startsWith('↑'),
                    'text-red-600': borrowsTrend.startsWith('↓'),
                    'text-slate-500': borrowsTrend.startsWith('→')
                  }"
                >
                  {{ borrowsTrend }}
                </div>
              </NCard>
            </NGridItem>

            <!-- 平均評分 -->
            <NGridItem>
              <NCard :bordered="false" class="bg-white/90 backdrop-blur-xl shadow-lg">
                <NStatistic label="平均評分" :value="averageRating" :precision="1">
                  <template #suffix>/ 5.0</template>
                </NStatistic>
                <div
                  v-if="ratingTrend"
                  class="text-sm font-semibold mt-2"
                  :class="{
                    'text-green-600': ratingTrend.startsWith('↑'),
                    'text-red-600': ratingTrend.startsWith('↓'),
                    'text-slate-500': ratingTrend.startsWith('→')
                  }"
                >
                  {{ ratingTrend }}
                </div>
              </NCard>
            </NGridItem>
          </NGrid>

          <!-- 功能標籤頁 -->
          <NCard :bordered="false" class="bg-white/90 backdrop-blur-xl shadow-lg">
            <NTabs v-model:value="activeTab" type="line" size="large">
              <!-- 概覽 -->
              <NTabPane name="overview" tab="數據概覽">
                <div class="space-y-6">
                  <!-- 借閱趨勢圖 -->
                  <div class="bg-slate-50 rounded-xl p-6">
                    <h3 class="text-lg font-bold text-slate-800 mb-4">借閱趨勢分析（過去 30 天）</h3>
                    <BorrowTrendChart />
                  </div>

                  <!-- 分類統計 -->
                  <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
                    <div class="bg-slate-50 rounded-xl p-6">
                      <h3 class="text-lg font-bold text-slate-800 mb-4">書籍分類統計</h3>
                      <CategoryPieChart />
                    </div>

                    <!-- 熱門書籍 TOP 10 -->
                    <div class="bg-slate-50 rounded-xl p-6">
                      <h3 class="text-lg font-bold text-slate-800 mb-4">熱門書籍 TOP 10</h3>
                      <TopBooksTable />
                    </div>
                  </div>
                </div>
              </NTabPane>

              <!-- 詳細統計 -->
              <NTabPane name="stats" tab="詳細統計">
                <StatsCharts />
              </NTabPane>

              <!-- 系統分析 -->
              <NTabPane name="analysis" tab="系統分析">
                <div class="text-center py-12 text-slate-400">
                  系統分析功能開發中...
                </div>
              </NTabPane>
            </NTabs>
          </NCard>
        </div>
      </main>
    </div>
  </div>
</template>
