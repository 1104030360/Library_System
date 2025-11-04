<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { useMessage, NModal, NInput, NButton } from 'naive-ui'
import { reviewApi } from '@/api'
import type { BookReview } from '@/types'

interface Props {
  show: boolean
  bookId: string
  bookTitle: string
  editingReview?: BookReview | null
}

interface Emits {
  (e: 'update:show', value: boolean): void
  (e: 'submitted'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const message = useMessage()
const loading = ref(false)
const reviewText = ref('')

const MAX_LENGTH = 500

// 計算剩餘字數
const remainingChars = computed(() => MAX_LENGTH - reviewText.value.length)

// 當彈窗打開時，如果是編輯模式，載入現有評論
watch(
  () => props.show,
  (newVal) => {
    if (newVal) {
      if (props.editingReview) {
        reviewText.value = props.editingReview.reviewText
      } else {
        reviewText.value = ''
      }
    }
  }
)

// 提交評論
const submitReview = async () => {
  if (!reviewText.value.trim()) {
    message.warning('請輸入評論內容')
    return
  }

  if (reviewText.value.length > MAX_LENGTH) {
    message.warning(`評論內容不能超過 ${MAX_LENGTH} 字`)
    return
  }

  loading.value = true
  try {
    if (props.editingReview) {
      // 更新評論
      await reviewApi.updateReview({
        reviewId: props.editingReview.id,
        reviewText: reviewText.value,
      })
      message.success('評論已更新！')
    } else {
      // 新增評論
      await reviewApi.addReview({
        bookId: props.bookId,
        reviewText: reviewText.value,
      })
      message.success('評論已發表！')
    }

    emit('submitted')
    emit('update:show', false)
    reviewText.value = ''
  } catch (error: any) {
    message.error('操作失敗: ' + (error.message || '未知錯誤'))
  } finally {
    loading.value = false
  }
}

// 關閉彈窗
const handleClose = () => {
  emit('update:show', false)
}
</script>

<template>
  <NModal
    :show="show"
    @update:show="(val) => emit('update:show', val)"
    preset="card"
    :title="editingReview ? '編輯評論' : '撰寫評論'"
    :style="{ width: '600px' }"
    :mask-closable="!loading"
    :closable="!loading"
  >
    <div class="space-y-4">
      <!-- 書籍標題 -->
      <div class="bg-slate-50 rounded-lg p-3">
        <div class="text-sm text-slate-600">書籍</div>
        <div class="text-lg font-bold text-slate-800">{{ bookTitle }}</div>
      </div>

      <!-- 評論輸入框 -->
      <div>
        <div class="text-sm text-slate-600 mb-2">評論內容</div>
        <NInput
          v-model:value="reviewText"
          type="textarea"
          placeholder="分享您對這本書的想法..."
          :rows="8"
          :maxlength="MAX_LENGTH"
          show-count
          :disabled="loading"
        />
        <div
          class="text-xs mt-1"
          :class="remainingChars < 50 ? 'text-red-500' : 'text-slate-500'"
        >
          還可輸入 {{ remainingChars }} 字
        </div>
      </div>

      <!-- 提示 -->
      <div class="text-xs text-slate-500 bg-blue-50 rounded p-2">
        提示：評論將公開顯示，請保持友善與尊重
      </div>

      <!-- 按鈕 -->
      <div class="flex gap-3 justify-end pt-2">
        <NButton @click="handleClose" :disabled="loading">取消</NButton>
        <NButton type="primary" @click="submitReview" :loading="loading">
          {{ editingReview ? '更新評論' : '發表評論' }}
        </NButton>
      </div>
    </div>
  </NModal>
</template>

<style scoped>
/* 自定義樣式 */
</style>
