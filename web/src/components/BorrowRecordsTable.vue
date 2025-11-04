<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { NTable, NTag, NButton, NInput, NDatePicker, NSpin, NEmpty, NPagination, useMessage } from 'naive-ui'
import { historyApi } from '@/api'
import type { BorrowHistory } from '@/types'

const message = useMessage()
const loading = ref(true)
const allRecords = ref<BorrowHistory[]>([])

// 搜尋和篩選
const searchText = ref('')
const statusFilter = ref<'all' | 'borrowing' | 'returned' | 'overdue'>('all')
const dateRange = ref<[number, number] | null>(null)

// 分頁
const currentPage = ref(1)
const pageSize = ref(20)

// 載入所有借閱記錄
const loadAllRecords = async () => {
  loading.value = true
  try {
    allRecords.value = await historyApi.getAllHistory()
  } catch (error) {
    message.error('載入借閱記錄失敗')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 篩選後的記錄
const filteredRecords = computed(() => {
  let records = allRecords.value

  // 搜尋過濾（書名、用戶ID、用戶名稱）
  if (searchText.value.trim()) {
    const search = searchText.value.toLowerCase()
    records = records.filter(
      (r) =>
        r.bookTitle.toLowerCase().includes(search) ||
        r.userId.toLowerCase().includes(search) ||
        r.userName?.toLowerCase().includes(search)
    )
  }

  // 狀態過濾
  if (statusFilter.value !== 'all') {
    records = records.filter((r) => r.status === statusFilter.value)
  }

  // 日期範圍過濾
  if (dateRange.value) {
    const [startTime, endTime] = dateRange.value
    records = records.filter((r) => {
      const borrowTime = new Date(r.borrowDate).getTime()
      return borrowTime >= startTime && borrowTime <= endTime
    })
  }

  return records
})

// 分頁後的記錄
const paginatedRecords = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredRecords.value.slice(start, end)
})

// 總頁數
const totalPages = computed(() => {
  return Math.ceil(filteredRecords.value.length / pageSize.value)
})

// 判斷是否逾期
function isOverdue(record: BorrowHistory): boolean {
  if (record.status !== 'borrowing') return false
  return new Date(record.dueDate) < new Date()
}

// 取得狀態標籤類型
function getStatusType(record: BorrowHistory) {
  if (record.status === 'returned') return 'success'
  if (isOverdue(record)) return 'error'
  return 'warning'
}

// 取得狀態文字
function getStatusText(record: BorrowHistory): string {
  if (record.status === 'returned') return '已歸還'
  if (isOverdue(record)) {
    const overdueDays = Math.floor(
      (new Date().getTime() - new Date(record.dueDate).getTime()) / (1000 * 60 * 60 * 24)
    )
    return `逾期 ${overdueDays} 天`
  }
  return '借閱中'
}

// 格式化日期
function formatDate(dateString: string): string {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-TW', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  })
}

// 重置篩選
function resetFilters() {
  searchText.value = ''
  statusFilter.value = 'all'
  dateRange.value = null
  currentPage.value = 1
}

onMounted(() => {
  loadAllRecords()
})
</script>

<template>
  <div class="space-y-4">
    <!-- 搜尋和篩選區 -->
    <div class="bg-slate-50 rounded-xl p-4 space-y-4">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <!-- 搜尋框 -->
        <NInput
          v-model:value="searchText"
          placeholder="搜尋書名 / 學號 / 姓名"
          clearable
        />

        <!-- 狀態篩選 -->
        <div class="flex gap-2">
          <button
            @click="statusFilter = 'all'"
            :class="[
              'flex-1 px-4 py-2 rounded-lg font-semibold transition-colors',
              statusFilter === 'all'
                ? 'bg-blue-500 text-white'
                : 'bg-white text-slate-700 hover:bg-slate-100',
            ]"
          >
            全部
          </button>
          <button
            @click="statusFilter = 'borrowing'"
            :class="[
              'flex-1 px-4 py-2 rounded-lg font-semibold transition-colors',
              statusFilter === 'borrowing'
                ? 'bg-orange-500 text-white'
                : 'bg-white text-slate-700 hover:bg-slate-100',
            ]"
          >
            借閱中
          </button>
          <button
            @click="statusFilter = 'returned'"
            :class="[
              'flex-1 px-4 py-2 rounded-lg font-semibold transition-colors',
              statusFilter === 'returned'
                ? 'bg-green-500 text-white'
                : 'bg-white text-slate-700 hover:bg-slate-100',
            ]"
          >
            已歸還
          </button>
        </div>

        <!-- 日期範圍 -->
        <div class="flex gap-2">
          <NDatePicker
            v-model:value="dateRange"
            type="daterange"
            clearable
            placeholder="選擇日期範圍"
            class="flex-1"
          />
          <NButton @click="resetFilters">重置</NButton>
        </div>
      </div>

      <!-- 統計資訊 -->
      <div class="flex gap-4 text-sm font-semibold text-slate-600">
        <span>總記錄數: {{ allRecords.length }}</span>
        <span>篩選結果: {{ filteredRecords.length }}</span>
        <span>借閱中: {{ allRecords.filter((r) => r.status === 'borrowing').length }}</span>
        <span>已歸還: {{ allRecords.filter((r) => r.status === 'returned').length }}</span>
      </div>
    </div>

    <!-- 載入中 -->
    <div v-if="loading" class="flex justify-center py-12">
      <NSpin size="large" />
    </div>

    <!-- 空狀態 -->
    <div v-else-if="filteredRecords.length === 0" class="py-12">
      <NEmpty description="沒有找到借閱記錄" />
    </div>

    <!-- 借閱記錄表格 -->
    <div v-else class="bg-white rounded-xl overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full">
          <thead class="bg-slate-100">
            <tr>
              <th class="px-4 py-3 text-left text-sm font-bold text-slate-700">記錄 ID</th>
              <th class="px-4 py-3 text-left text-sm font-bold text-slate-700">書名</th>
              <th class="px-4 py-3 text-left text-sm font-bold text-slate-700">借閱者</th>
              <th class="px-4 py-3 text-left text-sm font-bold text-slate-700">借閱日期</th>
              <th class="px-4 py-3 text-left text-sm font-bold text-slate-700">應還日期</th>
              <th class="px-4 py-3 text-left text-sm font-bold text-slate-700">歸還日期</th>
              <th class="px-4 py-3 text-left text-sm font-bold text-slate-700">狀態</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="record in paginatedRecords"
              :key="record.id"
              class="border-b border-slate-100 hover:bg-slate-50 transition-colors"
            >
              <td class="px-4 py-3 text-sm font-mono text-slate-600">{{ record.id }}</td>
              <td class="px-4 py-3 text-sm font-semibold text-slate-800">
                {{ record.bookTitle }}
              </td>
              <td class="px-4 py-3 text-sm text-slate-700">
                <div>{{ record.userName || '-' }}</div>
                <div class="text-xs text-slate-500">{{ record.userId }}</div>
              </td>
              <td class="px-4 py-3 text-sm text-slate-700">
                {{ formatDate(record.borrowDate) }}
              </td>
              <td class="px-4 py-3 text-sm text-slate-700">
                {{ formatDate(record.dueDate) }}
              </td>
              <td class="px-4 py-3 text-sm text-slate-700">
                {{ record.returnDate ? formatDate(record.returnDate) : '-' }}
              </td>
              <td class="px-4 py-3">
                <NTag :type="getStatusType(record)" size="small" :bordered="false">
                  {{ getStatusText(record) }}
                </NTag>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 分頁 -->
      <div class="flex justify-center py-4 border-t border-slate-100">
        <NPagination
          v-model:page="currentPage"
          :page-count="totalPages"
          :page-size="pageSize"
          show-size-picker
          :page-sizes="[10, 20, 50, 100]"
          @update:page-size="pageSize = $event"
        />
      </div>
    </div>
  </div>
</template>
