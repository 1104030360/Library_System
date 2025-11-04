<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useMessage, NCard, NButton, NEmpty, NSpin, NSpace, NPopconfirm } from 'naive-ui'
import { reviewApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import type { BookReview } from '@/types'

interface Props {
  bookId: string
}

interface Emits {
  (e: 'write-review'): void
  (e: 'edit-review', review: BookReview): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const authStore = useAuthStore()
const message = useMessage()

const loading = ref(false)
const reviews = ref<BookReview[]>([])
const reviewCount = ref(0)

// 載入評論
const loadReviews = async () => {
  loading.value = true
  try {
    const result = await reviewApi.getBookReviews(props.bookId)
    reviews.value = result.reviews
    reviewCount.value = result.reviewCount
  } catch (error: any) {
    message.error('載入評論失敗: ' + (error.message || '未知錯誤'))
  } finally {
    loading.value = false
  }
}

// 檢查是否為評論作者
const isAuthor = (review: BookReview) => {
  return review.userId === authStore.user?.username
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  return dateStr.split(' ')[0]
}

// 刪除評論
const deleteReview = async (reviewId: number) => {
  try {
    await reviewApi.deleteReview(reviewId)
    message.success('評論已刪除')
    await loadReviews() // 重新載入評論列表
  } catch (error: any) {
    message.error('刪除失敗: ' + (error.message || '未知錯誤'))
  }
}

// 編輯評論
const editReview = (review: BookReview) => {
  emit('edit-review', review)
}

// 撰寫評論
const writeReview = () => {
  emit('write-review')
}

// 檢查使用者是否已經評論過
const hasUserReviewed = computed(() => {
  return reviews.value.some((r) => r.userId === authStore.user?.username)
})

onMounted(() => {
  loadReviews()
})

// 暴露刷新方法給父組件
defineExpose({
  refresh: loadReviews,
})
</script>

<template>
  <div class="space-y-4">
    <!-- 標題與撰寫按鈕 -->
    <div class="flex items-center justify-between">
      <h3 class="text-2xl font-bold text-slate-800">
        書籍評論
        <span v-if="reviewCount > 0" class="text-lg text-slate-600 font-normal"
          >({{ reviewCount }})</span
        >
      </h3>
      <!-- 已登入：顯示撰寫/修改評論按鈕 -->
      <NButton v-if="authStore.isLoggedIn" type="primary" @click="writeReview">
        {{ hasUserReviewed ? '修改我的評論' : '撰寫評論' }}
      </NButton>
      <!-- 未登入：顯示登入提示按鈕 -->
      <NButton v-else type="primary" @click="$router.push('/login')">
        登入以評論
      </NButton>
    </div>

    <!-- 載入中 -->
    <div v-if="loading" class="flex justify-center py-8">
      <NSpin size="large" />
    </div>

    <!-- 評論列表 -->
    <div v-else-if="reviews.length > 0" class="space-y-3">
      <NCard
        v-for="review in reviews"
        :key="review.id"
        class="bg-white/80 backdrop-blur-xl hover:shadow-md transition-shadow"
      >
        <div class="space-y-3">
          <!-- 評論頭部 -->
          <div class="flex items-start justify-between">
            <div class="flex-1">
              <div class="flex items-center gap-3">
                <span class="font-semibold text-slate-800">{{ review.userName }}</span>
                <span class="text-sm text-slate-500">{{ formatDate(review.createdAt) }}</span>
                <span
                  v-if="review.updatedAt && review.updatedAt !== review.createdAt"
                  class="text-xs text-slate-400"
                >
                  (已編輯)
                </span>
              </div>
            </div>

            <!-- 操作按鈕（僅作者可見） -->
            <NSpace v-if="isAuthor(review)" size="small">
              <NButton size="small" @click="editReview(review)">編輯</NButton>
              <NPopconfirm
                @positive-click="deleteReview(review.id)"
                positive-text="確定刪除"
                negative-text="取消"
              >
                <template #trigger>
                  <NButton size="small" type="error">刪除</NButton>
                </template>
                確定要刪除這則評論嗎？
              </NPopconfirm>
            </NSpace>
          </div>

          <!-- 評論內容 -->
          <div class="text-slate-700 leading-relaxed whitespace-pre-wrap">
            {{ review.reviewText }}
          </div>
        </div>
      </NCard>
    </div>

    <!-- 空狀態 -->
    <NCard v-else class="bg-white/80 backdrop-blur-xl">
      <NEmpty description="尚無評論，成為第一個評論的人吧！">
        <template #extra>
          <!-- 已登入：顯示撰寫評論按鈕 -->
          <NButton v-if="authStore.isLoggedIn" type="primary" @click="writeReview">
            撰寫評論
          </NButton>
          <!-- 未登入：顯示登入提示按鈕 -->
          <NButton v-else type="primary" @click="$router.push('/login')">
            登入以評論
          </NButton>
        </template>
      </NEmpty>
    </NCard>
  </div>
</template>

<style scoped>
/* 自定義樣式 */
</style>
