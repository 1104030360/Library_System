<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { NButton, NTag, NRate, useMessage } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'
import { useBooksStore } from '@/stores/books'
import { useRouter } from 'vue-router'
import { ratingApi } from '@/api'
import type { Book, BookRating } from '@/types'

interface Props {
  book: Book
  mode?: 'guest' | 'user' | 'admin'
  showRating?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  mode: 'user',
  showRating: true
})

// 評分資訊
const bookRating = ref<BookRating | null>(null)

const emit = defineEmits<{
  edit: [book: Book]
  delete: [book: Book]
}>()

const authStore = useAuthStore()
const booksStore = useBooksStore()
const router = useRouter()
const message = useMessage()

const handleBorrow = async () => {
  if (!authStore.isLoggedIn) {
    message.warning('請先登入才能借書')
    router.push('/login')
    return
  }

  const result = await booksStore.borrowBook(props.book.id)
  if (result.success) {
    message.success(result.message)
  } else {
    message.error(result.message)
  }
}

const handleReturn = async () => {
  if (!authStore.isLoggedIn) {
    message.warning('請先登入才能還書')
    router.push('/login')
    return
  }

  const result = await booksStore.returnBook(props.book.id)
  if (result.success) {
    message.success(result.message)
  } else {
    message.error(result.message)
  }
}

const viewDetail = () => {
  router.push(`/book/${props.book.id}`)
}

const handleEdit = () => {
  emit('edit', props.book)
}

const handleDelete = () => {
  emit('delete', props.book)
}

// 載入評分
const loadRating = async () => {
  if (!props.showRating) return
  try {
    bookRating.value = await ratingApi.getBookRating(props.book.id)
  } catch (error) {
    // 如果沒有評分，不顯示錯誤
    console.log('No rating for this book yet')
  }
}

onMounted(() => {
  loadRating()
})
</script>

<template>
  <div
    class="bg-white/90 backdrop-blur-xl border border-slate-200 rounded-2xl p-5 hover:shadow-2xl transition-shadow"
    :class="{ 'opacity-60': !book.isAvailable }"
  >
    <!-- 書籍 ID 和狀態 -->
    <div class="flex justify-between items-start mb-3">
      <span class="text-xs font-mono text-slate-500 font-semibold">{{ book.id }}</span>
      <NTag :type="book.isAvailable ? 'success' : 'warning'" size="small" :bordered="false">
        {{ book.isAvailable ? '可借閱' : '已借出' }}
      </NTag>
    </div>

    <!-- 書名 (clickable) -->
    <h3
      @click="viewDetail"
      class="text-xl font-bold text-slate-800 mb-3 line-clamp-2 cursor-pointer hover:text-blue-600 transition-colors"
    >
      {{ book.title }}
    </h3>

    <!-- 作者和出版社 -->
    <div class="space-y-1 mb-3 text-sm text-slate-700 font-semibold">
      <div>作者：{{ book.author }}</div>
      <div>出版社：{{ book.publisher }}</div>
    </div>

    <!-- 評分資訊 -->
    <div v-if="showRating && bookRating && bookRating.ratingCount > 0" class="mb-4">
      <div class="flex items-center gap-2">
        <NRate :value="bookRating.averageRating" readonly size="small" />
        <span class="text-sm font-bold text-slate-800">{{ bookRating.averageRating.toFixed(1) }}</span>
        <span class="text-xs text-slate-500">({{ bookRating.ratingCount }})</span>
      </div>
    </div>

    <!-- 操作按鈕 -->
    <div class="pt-3 border-t border-slate-100">
      <!-- 訪客模式：只顯示查看詳情 -->
      <template v-if="mode === 'guest'">
        <NButton
          type="primary"
          size="medium"
          block
          @click="router.push('/login')"
          style="font-weight: 600"
        >
          登入以借閱
        </NButton>
      </template>

      <!-- 使用者模式：借還書按鈕 -->
      <template v-else-if="mode === 'user'">
        <NButton
          v-if="book.isAvailable"
          type="primary"
          size="medium"
          block
          @click="handleBorrow"
          style="font-weight: 600"
        >
          借書
        </NButton>
        <NButton v-else type="default" size="medium" block @click="handleReturn" style="font-weight: 600">
          還書
        </NButton>
      </template>

      <!-- 管理員模式：管理按鈕 -->
      <template v-else-if="mode === 'admin'">
        <div class="flex gap-2">
          <NButton size="small" @click="viewDetail" block> 詳情 </NButton>
          <NButton type="primary" size="small" @click="handleEdit" block> 編輯 </NButton>
          <NButton type="error" size="small" @click="handleDelete" block> 刪除 </NButton>
        </div>
      </template>
    </div>
  </div>
</template>
