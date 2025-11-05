<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { NButton, NSpin, NTag, NRate, NDivider } from 'naive-ui'
import { useBooksStore } from '@/stores/books'
import { useAuthStore } from '@/stores/auth'
import { ratingApi } from '@/api'
import AppHeader from '@/components/AppHeader.vue'
import LiquidBackground from '@/components/LiquidBackground.vue'
import RatingModal from '@/components/RatingModal.vue'
import ReviewsSection from '@/components/ReviewsSection.vue'
import ReviewForm from '@/components/ReviewForm.vue'
import RelatedRecommendations from '@/components/RelatedRecommendations.vue'
import type { BookRating, BookReview } from '@/types'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const booksStore = useBooksStore()
const authStore = useAuthStore()

const bookId = route.params.id as string
const loading = ref(true)
const operating = ref(false)

// 評分相關
const bookRating = ref<BookRating | null>(null)
const showRatingModal = ref(false)

// 評論相關
const showReviewForm = ref(false)
const editingReview = ref<BookReview | null>(null)
const reviewsSection = ref<InstanceType<typeof ReviewsSection> | null>(null)

// Get book from store
const book = computed(() => {
  return booksStore.books.find(b => b.id === bookId)
})
const isBorrowedByMe = computed(() => booksStore.isBookBorrowedByCurrentUser(bookId))
const isBorrowedByOthers = computed(() => !!book.value && !book.value.isAvailable && !isBorrowedByMe.value)

// 載入評分
const loadRating = async () => {
  try {
    bookRating.value = await ratingApi.getBookRating(bookId)
  } catch (error) {
    // 如果沒有評分，不顯示錯誤
    console.log('No rating yet')
  }
}

onMounted(async () => {
  loading.value = true
  try {
    // Load books if not loaded
    if (booksStore.books.length === 0) {
      await booksStore.loadBooks()
    }

    await booksStore.loadMyBorrowings()

    // Check if book exists
    if (!book.value) {
      message.error('找不到此書籍')
      router.push('/')
      return
    }

    // Load rating
    await loadRating()
  } catch (error) {
    message.error('載入書籍資料失敗')
    router.push('/')
  } finally {
    loading.value = false
  }
})

// Handle borrow
const handleBorrow = async () => {
  if (!authStore.isLoggedIn) {
    message.warning('請先登入')
    router.push('/login')
    return
  }

  operating.value = true
  try {
    const result = await booksStore.borrowBook(bookId)
    if (result.success) {
      message.success('借閱成功！')
    } else {
      message.error(result.message)
    }
  } catch (error) {
    message.error('借閱失敗')
  } finally {
    operating.value = false
  }
}

// Handle return
const handleReturn = async () => {
  if (!authStore.isLoggedIn) {
    message.warning('請先登入')
    router.push('/login')
    return
  }

  operating.value = true
  try {
    const result = await booksStore.returnBook(bookId)
    if (result.success) {
      message.success('歸還成功！')
    } else {
      message.error(result.message)
    }
  } catch (error) {
    message.error('歸還失敗')
  } finally {
    operating.value = false
  }
}

const goBack = () => {
  router.push('/')
}

// 評分處理
const handleRated = (rating: BookRating) => {
  bookRating.value = rating
}

// 撰寫評論
const handleWriteReview = () => {
  editingReview.value = null
  showReviewForm.value = true
}

// 編輯評論
const handleEditReview = (review: BookReview) => {
  editingReview.value = review
  showReviewForm.value = true
}

// 評論提交後
const handleReviewSubmitted = () => {
  reviewsSection.value?.refresh()
}
</script>

<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50">
    <LiquidBackground />
    <AppHeader />

    <main class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8 relative z-10">
      <!-- Back button -->
      <button
        @click="goBack"
        class="mb-6 flex items-center space-x-2 text-slate-600 hover:text-slate-900 font-semibold transition-colors"
      >
        <span>←</span>
        <span>返回書籍列表</span>
      </button>

      <!-- Loading state -->
      <div v-if="loading" class="flex justify-center items-center py-20">
        <NSpin size="large" />
      </div>

      <!-- Book details -->
      <div v-else-if="book" class="bg-white/90 backdrop-blur-xl rounded-3xl shadow-2xl p-8 sm:p-12">
        <!-- Title -->
        <div class="mb-8">
          <h1 class="text-4xl font-bold text-slate-800 mb-4">{{ book.title }}</h1>
          <div class="flex items-center space-x-3">
            <NTag :type="book.isAvailable ? 'success' : 'error'" size="large" :bordered="false">
              {{ book.isAvailable ? '✅ 可借閱' : '❌ 已借出' }}
            </NTag>
          </div>
        </div>

        <!-- Book info -->
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-6 mb-8">
          <div class="space-y-4">
            <div>
              <div class="text-sm font-semibold text-slate-500 mb-1">書籍編號</div>
              <div class="text-lg font-bold text-slate-800">{{ book.id }}</div>
            </div>
            <div>
              <div class="text-sm font-semibold text-slate-500 mb-1">作者</div>
              <div class="text-lg font-bold text-slate-800">{{ book.author }}</div>
            </div>
          </div>
          <div class="space-y-4">
            <div>
              <div class="text-sm font-semibold text-slate-500 mb-1">出版社</div>
              <div class="text-lg font-bold text-slate-800">{{ book.publisher }}</div>
            </div>
          </div>
        </div>

        <!-- Book description -->
        <div v-if="book.description" class="mb-8">
          <div class="text-sm font-semibold text-slate-500 mb-3">📝 書籍簡介</div>
          <div class="bg-slate-50 rounded-2xl p-6">
            <p class="text-slate-700 leading-relaxed whitespace-pre-wrap">{{ book.description }}</p>
          </div>
        </div>
        <div v-else class="mb-8">
          <div class="text-sm font-semibold text-slate-500 mb-3">📝 書籍簡介</div>
          <div class="bg-slate-50 rounded-2xl p-6">
            <p class="text-slate-400 italic">暫無簡介</p>
          </div>
        </div>

        <!-- Rating Section -->
        <div class="mb-8">
          <NDivider />
          <div class="flex items-center justify-between mb-4">
            <div>
              <h2 class="text-2xl font-bold text-slate-800 mb-2">書籍評分</h2>
              <div v-if="bookRating && bookRating.ratingCount > 0" class="flex items-center gap-3">
                <NRate :value="bookRating.averageRating" readonly size="large" />
                <span class="text-2xl font-bold text-slate-800">{{ bookRating.averageRating.toFixed(1) }}</span>
                <span class="text-slate-600">({{ bookRating.ratingCount }} 人評分)</span>
              </div>
              <div v-else class="text-slate-600">尚無評分</div>
            </div>
            <!-- 評分按鈕：需要登入 -->
            <NButton
              v-if="authStore.isLoggedIn"
              type="primary"
              @click="showRatingModal = true"
            >
              {{ bookRating?.userRating ? '修改評分' : '為此書評分' }}
            </NButton>
            <NButton
              v-else
              type="primary"
              @click="router.push('/login')"
            >
              登入以評分
            </NButton>
          </div>
          <!-- 使用者評分（僅登入用戶可見） -->
          <div v-if="authStore.isLoggedIn && bookRating?.userRating" class="bg-blue-50 rounded-lg p-3">
            <div class="text-sm text-slate-600">您的評分：</div>
            <div class="flex items-center gap-2">
              <NRate :value="bookRating.userRating" readonly />
              <span class="font-bold text-blue-600">{{ bookRating.userRating.toFixed(1) }}</span>
            </div>
          </div>
        </div>

        <!-- Reviews Section -->
        <div class="mb-8">
          <NDivider />
          <ReviewsSection
            ref="reviewsSection"
            :book-id="bookId"
            @write-review="handleWriteReview"
            @edit-review="handleEditReview"
          />
        </div>

        <!-- AI Related Recommendations (Phase 10) -->
        <RelatedRecommendations :book-id="bookId" />

        <!-- Action buttons -->
        <NDivider />
        <div class="flex flex-col sm:flex-row gap-4">
          <NButton
            @click="goBack"
            size="large"
            class="flex-1"
          >
            返回列表
          </NButton>
          <NButton
            v-if="book.isAvailable"
            @click="handleBorrow"
            type="primary"
            size="large"
            :loading="operating"
            class="flex-1"
          >
            借閱此書
          </NButton>
          <NButton
            v-else-if="isBorrowedByMe"
            @click="handleReturn"
            type="warning"
            size="large"
            :loading="operating"
            class="flex-1"
          >
            歸還此書
          </NButton>
          <NButton
            v-else
            type="default"
            size="large"
            disabled
            class="flex-1"
          >
            已被其他讀者借出
          </NButton>
        </div>
      </div>

      <!-- Rating Modal -->
      <RatingModal
        v-if="book"
        v-model:show="showRatingModal"
        :book-id="bookId"
        :book-title="book.title"
        :current-rating="bookRating || undefined"
        @rated="handleRated"
      />

      <!-- Review Form Modal -->
      <ReviewForm
        v-if="book"
        v-model:show="showReviewForm"
        :book-id="bookId"
        :book-title="book.title"
        :editing-review="editingReview"
        @submitted="handleReviewSubmitted"
      />

      <!-- Book not found -->
      <div v-else class="bg-white/90 backdrop-blur-xl rounded-3xl shadow-2xl p-12 text-center">
        <p class="text-xl text-slate-600 mb-6">找不到此書籍</p>
        <NButton @click="goBack" type="primary" size="large">
          返回書籍列表
        </NButton>
      </div>
    </main>
  </div>
</template>
