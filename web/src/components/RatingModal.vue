<script setup lang="ts">
import { ref, watch } from 'vue'
import { useMessage, NModal, NCard, NButton, NRate, NSpin } from 'naive-ui'
import { ratingApi } from '@/api'
import type { BookRating } from '@/types'

interface Props {
  show: boolean
  bookId: string
  bookTitle: string
  currentRating?: BookRating
}

interface Emits {
  (e: 'update:show', value: boolean): void
  (e: 'rated', rating: BookRating): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const message = useMessage()
const loading = ref(false)
const userRating = ref(0)

// 當彈窗打開時，設定使用者當前評分
watch(
  () => props.show,
  (newVal) => {
    if (newVal && props.currentRating?.userRating) {
      userRating.value = props.currentRating.userRating
    } else {
      userRating.value = 0
    }
  }
)

// 提交評分
const submitRating = async () => {
  if (userRating.value === 0) {
    message.warning('請選擇評分')
    return
  }

  loading.value = true
  try {
    const result = await ratingApi.rateBook({
      bookId: props.bookId,
      rating: userRating.value,
    })

    message.success('評分成功！')
    emit('rated', result)
    emit('update:show', false)
  } catch (error: any) {
    message.error('評分失敗: ' + (error.message || '未知錯誤'))
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
    title="為書籍評分"
    :style="{ width: '500px' }"
    :mask-closable="!loading"
    :closable="!loading"
  >
    <div class="space-y-6">
      <!-- 書籍標題 -->
      <div class="text-center">
        <h3 class="text-xl font-bold text-slate-800 mb-2">{{ bookTitle }}</h3>
        <p class="text-sm text-slate-600">請為這本書評分</p>
      </div>

      <!-- 評分控制 -->
      <div class="flex flex-col items-center space-y-4">
        <div class="text-center">
          <div class="text-sm text-slate-600 mb-2">您的評分：</div>
          <NRate v-model:value="userRating" :count="5" size="large" allow-half clearable />
          <div class="mt-2 text-2xl font-bold text-blue-600">
            {{ userRating > 0 ? userRating.toFixed(1) : '-' }} / 5.0
          </div>
        </div>

        <!-- 平均評分資訊 -->
        <div
          v-if="currentRating && currentRating.ratingCount > 0"
          class="w-full bg-slate-50 rounded-lg p-4"
        >
          <div class="text-center">
            <div class="text-sm text-slate-600 mb-1">目前平均評分</div>
            <div class="flex items-center justify-center gap-2">
              <NRate :value="currentRating.averageRating" :count="5" readonly size="small" />
              <span class="text-lg font-bold text-slate-800"
                >{{ currentRating.averageRating.toFixed(1) }}</span
              >
              <span class="text-sm text-slate-500">({{ currentRating.ratingCount }} 人評分)</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 按鈕 -->
      <div class="flex gap-3 justify-end">
        <NButton @click="handleClose" :disabled="loading">取消</NButton>
        <NButton type="primary" @click="submitRating" :loading="loading">提交評分</NButton>
      </div>
    </div>
  </NModal>
</template>

<style scoped>
/* 自定義樣式 */
</style>
