<script setup lang="ts">
import { NInput, NSelect } from 'naive-ui'
import { useBooksStore } from '@/stores/books'
import type { SortOption } from '@/types'

const booksStore = useBooksStore()

const sortOptions = [
  { label: '按 ID 排序', value: 'id' },
  { label: '按書名排序', value: 'title' },
  { label: '按作者排序', value: 'author' },
  { label: '可借閱優先', value: 'available' },
]
</script>

<template>
  <div class="bg-white/90 backdrop-blur-xl border border-slate-200 rounded-2xl p-5 shadow-lg">
    <div class="flex flex-col sm:flex-row gap-4">
      <!-- 搜尋框 -->
      <div class="flex-1">
        <NInput
          v-model:value="booksStore.searchQuery"
          placeholder="搜尋書名、作者、出版社或 ID..."
          size="large"
          clearable
        />
      </div>

      <!-- 排序選擇器 -->
      <div class="w-full sm:w-48">
        <NSelect v-model:value="booksStore.sortBy" :options="sortOptions" size="large" />
      </div>
    </div>

    <!-- 搜尋結果提示 -->
    <div v-if="booksStore.searchQuery" class="mt-3 text-base text-slate-700 font-semibold">
      找到 {{ booksStore.filteredBooks.length }} 本書籍
    </div>
  </div>
</template>
