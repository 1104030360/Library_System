<script setup lang="ts">
import { NSpin, NEmpty } from 'naive-ui'
import { useBooksStore } from '@/stores/books'
import BookCard from './BookCard.vue'
import type { Book } from '@/types'

interface Props {
  mode?: 'guest' | 'user' | 'admin'
}

const props = withDefaults(defineProps<Props>(), {
  mode: 'user'
})

const emit = defineEmits<{
  edit: [book: Book]
  delete: [book: Book]
}>()

const booksStore = useBooksStore()
</script>

<template>
  <div>
    <!-- 載入中 -->
    <div v-if="booksStore.loading" class="flex justify-center py-12">
      <NSpin size="large" />
    </div>

    <!-- 空狀態 -->
    <div v-else-if="booksStore.filteredBooks.length === 0" class="py-12">
      <NEmpty description="沒有找到書籍" />
    </div>

    <!-- 書籍網格 -->
    <div
      v-else
      class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4"
    >
      <BookCard
        v-for="book in booksStore.filteredBooks"
        :key="book.id"
        :book="book"
        :mode="mode"
        :show-rating="true"
        @edit="emit('edit', $event)"
        @delete="emit('delete', $event)"
      />
    </div>
  </div>
</template>
