<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { Doughnut } from 'vue-chartjs'
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from 'chart.js'
import { useBooksStore } from '@/stores/books'

// 註冊 Chart.js 組件
ChartJS.register(ArcElement, Tooltip, Legend)

const booksStore = useBooksStore()

const chartData = computed(() => {
  const books = booksStore.books
  const total = books.length

  // 計算各分類數量（根據書籍 ID 數字範圍判斷分類）
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

  const labels = Object.keys(categories)
  const data = Object.values(categories)
  const percentages = data.map(count => total > 0 ? Math.round((count / total) * 100) : 0)

  return {
    labels: labels.map((label, index) => `${label} (${percentages[index]}%)`),
    datasets: [
      {
        data: data,
        backgroundColor: [
          'rgba(59, 130, 246, 0.8)',   // 藍色 - 計算機類
          'rgba(16, 185, 129, 0.8)',   // 綠色 - 商管類
          'rgba(245, 158, 11, 0.8)',   // 橙色 - 文學類
          'rgba(239, 68, 68, 0.8)',    // 紅色 - 科學類
          'rgba(139, 92, 246, 0.8)'    // 紫色 - 語言學習
        ],
        borderColor: [
          'rgb(59, 130, 246)',
          'rgb(16, 185, 129)',
          'rgb(245, 158, 11)',
          'rgb(239, 68, 68)',
          'rgb(139, 92, 246)'
        ],
        borderWidth: 2
      }
    ]
  }
})

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'bottom' as const,
      labels: {
        padding: 15,
        font: {
          size: 12
        }
      }
    },
    tooltip: {
      callbacks: {
        label: function(context: any) {
          const label = context.label || ''
          const value = context.parsed || 0
          return `${label}: ${value} 本`
        }
      }
    }
  }
}
</script>

<template>
  <div class="h-64">
    <Doughnut :data="chartData" :options="chartOptions" />
  </div>
</template>
