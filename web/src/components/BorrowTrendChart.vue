<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Line } from 'vue-chartjs'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler
} from 'chart.js'
import { NSpin } from 'naive-ui'
import { bookApi } from '@/api'
import type { DailyBorrowCount } from '@/types'

// 註冊 Chart.js 組件
ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler
)

const loading = ref(true)
const error = ref(false)
const chartData = ref({
  labels: [] as string[],
  datasets: [
    {
      label: '每日借閱次數',
      data: [] as number[],
      borderColor: 'rgb(59, 130, 246)',
      backgroundColor: 'rgba(59, 130, 246, 0.1)',
      tension: 0.4,
      fill: true,
      pointRadius: 4,
      pointHoverRadius: 6
    }
  ]
})

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      display: true,
      position: 'top' as const
    },
    tooltip: {
      mode: 'index' as const,
      intersect: false,
      callbacks: {
        label: function(context: any) {
          return `借閱次數: ${context.parsed.y} 次`
        }
      }
    }
  },
  scales: {
    y: {
      beginAtZero: true,
      ticks: {
        stepSize: 1,
        precision: 0
      }
    }
  }
}

// 從 API 載入真實數據
async function loadBorrowTrendData() {
  loading.value = true
  error.value = false

  try {
    const trendData: DailyBorrowCount[] = await bookApi.getBorrowTrend(30)

    if (!trendData || trendData.length === 0) {
      error.value = true
      return
    }

    const labels: string[] = []
    const data: number[] = []

    trendData.forEach(item => {
      // 直接處理 YYYY-MM-DD 格式，避免時區問題
      const dateParts = item.date.split('-')
      const month = parseInt(dateParts[1], 10)
      const day = parseInt(dateParts[2], 10)
      const label = `${month}/${day}`

      labels.push(label)
      data.push(item.count)
    })

    chartData.value.labels = labels
    chartData.value.datasets[0].data = data
  } catch (err) {
    console.error('Failed to load borrow trend data:', err)
    error.value = true
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadBorrowTrendData()
})
</script>

<template>
  <div class="h-64 relative">
    <NSpin :show="loading">
      <div v-if="error" class="h-full flex items-center justify-center text-slate-400">
        <div class="text-center">
          <p class="text-lg mb-2">📊</p>
          <p>暫無數據</p>
        </div>
      </div>
      <div v-else-if="!loading && chartData.labels.length > 0" class="h-full">
        <Line :data="chartData" :options="chartOptions" />
      </div>
    </NSpin>
  </div>
</template>
