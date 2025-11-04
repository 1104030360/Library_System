<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { NRate, NSpin } from 'naive-ui'
import { bookApi } from '@/api'
import type { Book } from '@/types'

const loading = ref(true)
const topBooksData = ref<Book[]>([])

// 載入熱門書籍數據
async function loadTopBooks() {
  loading.value = true
  try {
    topBooksData.value = await bookApi.getTopBooks(10)
  } catch (error) {
    console.error('Failed to load top books:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadTopBooks()
})

// 格式化熱門書籍數據為表格顯示格式
const topBooks = computed(() => {
  return topBooksData.value.map((book, index) => ({
    rank: index + 1,
    id: book.id,
    title: book.title,
    borrowCount: book.borrowCount || 0,
    rating: book.averageRating || 0,
    reviewCount: book.reviewCount || 0
  }))
})
</script>

<template>
  <div class="relative overflow-hidden max-h-80">
    <div class="overflow-auto max-h-80">
      <NSpin :show="loading">
        <table class="w-full text-sm">
        <thead class="bg-slate-200 sticky top-0 z-10">
          <tr>
            <th class="px-3 py-2 text-left font-bold text-slate-700">排名</th>
            <th class="px-3 py-2 text-left font-bold text-slate-700">書名</th>
            <th class="px-3 py-2 text-center font-bold text-slate-700">借閱次數</th>
            <th class="px-3 py-2 text-center font-bold text-slate-700">評分</th>
            <th class="px-3 py-2 text-center font-bold text-slate-700">評論數</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="book in topBooks"
            :key="book.rank"
            class="border-b border-slate-200 hover:bg-slate-50 transition-colors"
          >
            <td class="px-3 py-3 text-center">
              <span
                class="inline-flex items-center justify-center w-8 h-8 rounded-full font-bold text-white"
                :class="{
                  'bg-yellow-500': book.rank === 1,
                  'bg-slate-400': book.rank === 2,
                  'bg-orange-600': book.rank === 3,
                  'bg-slate-300 text-slate-700': book.rank > 3
                }"
              >
                {{ book.rank }}
              </span>
            </td>
            <td class="px-3 py-3 font-semibold text-slate-800">{{ book.title }}</td>
            <td class="px-3 py-3 text-center font-bold text-blue-600">{{ book.borrowCount }} 次</td>
            <td class="px-3 py-3 text-center">
              <div class="flex items-center justify-center gap-2">
                <NRate :value="book.rating" readonly size="small" :allow-half="false" />
                <span class="inline-block w-8 text-right font-bold text-slate-700">
                  {{ book.rating > 0 ? book.rating.toFixed(1) : '-' }}
                </span>
              </div>
            </td>
            <td class="px-3 py-3 text-center text-slate-600">{{ book.reviewCount }} 則</td>
          </tr>
        </tbody>
        </table>
        <div v-if="topBooks.length === 0 && !loading" class="text-center py-8 text-slate-400">
          暫無數據
        </div>
      </NSpin>
    </div>
  </div>
</template>
