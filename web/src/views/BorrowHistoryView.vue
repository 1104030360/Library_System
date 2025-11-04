<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage, NCard, NButton, NTag, NEmpty, NSpin, NSpace, NDivider } from 'naive-ui'
import { historyApi } from '@/api'
import type { BorrowHistory } from '@/types'

const router = useRouter()
const message = useMessage()

const loading = ref(false)
const historyList = ref<BorrowHistory[]>([])
const filterStatus = ref<'all' | 'borrowing' | 'returned' | 'overdue'>('all')

// 載入借閱歷史
const loadHistory = async () => {
  loading.value = true
  try {
    historyList.value = await historyApi.getUserHistory()
  } catch (error: any) {
    message.error('載入借閱歷史失敗: ' + (error.message || '未知錯誤'))
  } finally {
    loading.value = false
  }
}

// 過濾後的歷史記錄
const filteredHistory = computed(() => {
  if (filterStatus.value === 'all') {
    return historyList.value
  }
  return historyList.value.filter((item) => item.status === filterStatus.value)
})

// 統計資訊
const stats = computed(() => {
  const total = historyList.value.length
  const borrowing = historyList.value.filter((h) => h.status === 'borrowing').length
  const returned = historyList.value.filter((h) => h.status === 'returned').length
  const overdue = historyList.value.filter((h) => h.status === 'overdue').length

  return { total, borrowing, returned, overdue }
})

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  return dateStr.split(' ')[0] // 只顯示日期部分
}

// 計算借閱天數
const calculateDays = (borrowDate: string, returnDate: string | null) => {
  const start = new Date(borrowDate)
  const end = returnDate ? new Date(returnDate) : new Date()
  const diffTime = Math.abs(end.getTime() - start.getTime())
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  return diffDays
}

// 取得狀態標籤類型
const getStatusType = (status: string) => {
  switch (status) {
    case 'borrowing':
      return 'info'
    case 'returned':
      return 'success'
    case 'overdue':
      return 'error'
    default:
      return 'default'
  }
}

// 取得狀態文字
const getStatusText = (status: string) => {
  switch (status) {
    case 'borrowing':
      return '借閱中'
    case 'returned':
      return '已歸還'
    case 'overdue':
      return '逾期'
    default:
      return status
  }
}

// 查看書籍詳情
const viewBookDetail = (bookId: string) => {
  router.push(`/book/${bookId}`)
}

onMounted(() => {
  loadHistory()
})
</script>

<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50 via-indigo-50 to-purple-50 p-6">
    <div class="max-w-6xl mx-auto">
      <!-- 標題 -->
      <div class="mb-6">
        <h1 class="text-4xl font-bold text-slate-800 mb-2">借閱歷史</h1>
        <p class="text-slate-600">查看您的借閱記錄與統計資訊</p>
      </div>

      <!-- 統計卡片 -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
        <NCard class="bg-white/80 backdrop-blur-xl">
          <div class="text-center">
            <div class="text-3xl font-bold text-blue-600">{{ stats.total }}</div>
            <div class="text-sm text-slate-600 mt-1">總借閱次數</div>
          </div>
        </NCard>
        <NCard class="bg-white/80 backdrop-blur-xl">
          <div class="text-center">
            <div class="text-3xl font-bold text-indigo-600">{{ stats.borrowing }}</div>
            <div class="text-sm text-slate-600 mt-1">借閱中</div>
          </div>
        </NCard>
        <NCard class="bg-white/80 backdrop-blur-xl">
          <div class="text-center">
            <div class="text-3xl font-bold text-green-600">{{ stats.returned }}</div>
            <div class="text-sm text-slate-600 mt-1">已歸還</div>
          </div>
        </NCard>
        <NCard class="bg-white/80 backdrop-blur-xl">
          <div class="text-center">
            <div class="text-3xl font-bold text-red-600">{{ stats.overdue }}</div>
            <div class="text-sm text-slate-600 mt-1">逾期</div>
          </div>
        </NCard>
      </div>

      <!-- 篩選按鈕 -->
      <div class="mb-6">
        <NCard class="bg-white/80 backdrop-blur-xl">
          <NSpace>
            <span class="text-slate-700 font-medium">篩選：</span>
            <NButton
              :type="filterStatus === 'all' ? 'primary' : 'default'"
              @click="filterStatus = 'all'"
            >
              全部 ({{ stats.total }})
            </NButton>
            <NButton
              :type="filterStatus === 'borrowing' ? 'primary' : 'default'"
              @click="filterStatus = 'borrowing'"
            >
              借閱中 ({{ stats.borrowing }})
            </NButton>
            <NButton
              :type="filterStatus === 'returned' ? 'primary' : 'default'"
              @click="filterStatus = 'returned'"
            >
              已歸還 ({{ stats.returned }})
            </NButton>
            <NButton
              :type="filterStatus === 'overdue' ? 'primary' : 'default'"
              @click="filterStatus = 'overdue'"
            >
              逾期 ({{ stats.overdue }})
            </NButton>
          </NSpace>
        </NCard>
      </div>

      <!-- 載入中 -->
      <div v-if="loading" class="flex justify-center items-center py-12">
        <NSpin size="large" />
      </div>

      <!-- 歷史記錄列表 -->
      <div v-else-if="filteredHistory.length > 0" class="space-y-4">
        <NCard
          v-for="item in filteredHistory"
          :key="item.id"
          class="bg-white/80 backdrop-blur-xl hover:shadow-lg transition-shadow"
        >
          <div class="flex items-start justify-between">
            <!-- 書籍資訊 -->
            <div class="flex-1">
              <div class="flex items-center gap-3 mb-2">
                <h3 class="text-xl font-bold text-slate-800">{{ item.bookTitle }}</h3>
                <NTag :type="getStatusType(item.status)" size="small">
                  {{ getStatusText(item.status) }}
                </NTag>
              </div>

              <div class="grid grid-cols-1 md:grid-cols-2 gap-2 text-sm text-slate-600">
                <div>
                  <span class="font-medium">書籍編號：</span>{{ item.bookId }}
                </div>
                <div>
                  <span class="font-medium">借閱日期：</span>{{ formatDate(item.borrowDate) }}
                </div>
                <div>
                  <span class="font-medium">應還日期：</span>{{ formatDate(item.dueDate) }}
                </div>
                <div v-if="item.returnDate">
                  <span class="font-medium">歸還日期：</span>{{ formatDate(item.returnDate) }}
                </div>
                <div v-if="item.status === 'returned'">
                  <span class="font-medium">借閱天數：</span
                  >{{ calculateDays(item.borrowDate, item.returnDate) }} 天
                </div>
                <div v-else-if="item.status === 'borrowing'">
                  <span class="font-medium">已借閱：</span
                  >{{ calculateDays(item.borrowDate, null) }} 天
                </div>
              </div>
            </div>

            <!-- 操作按鈕 -->
            <div class="ml-4">
              <NButton @click="viewBookDetail(item.bookId)" type="primary">
                查看書籍
              </NButton>
            </div>
          </div>
        </NCard>
      </div>

      <!-- 空狀態 -->
      <NCard v-else class="bg-white/80 backdrop-blur-xl">
        <NEmpty description="尚無借閱記錄" />
      </NCard>

      <!-- 返回按鈕 -->
      <div class="mt-6 text-center">
        <NButton @click="router.back()" size="large">返回</NButton>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 可以添加自定義樣式 */
</style>
