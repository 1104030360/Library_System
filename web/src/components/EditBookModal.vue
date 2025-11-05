<script setup lang="ts">
import { ref, watch } from 'vue'
import { useMessage, NModal, NForm, NFormItem, NInput, NButton } from 'naive-ui'
import { useBooksStore } from '@/stores/books'
import type { Book } from '@/types'

interface Props {
  show: boolean
  book: Book | null
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:show': [value: boolean]
  success: []
}>()

const message = useMessage()
const booksStore = useBooksStore()

const formData = ref({
  id: '',
  title: '',
  author: '',
  publisher: '',
  description: ''
})

const loading = ref(false)

// 當 book prop 變化時,更新表單
watch(
  () => props.book,
  (newBook) => {
    if (newBook) {
      formData.value = {
        id: newBook.id,
        title: newBook.title,
        author: newBook.author,
        publisher: newBook.publisher,
        description: newBook.description || ''
      }
    }
  },
  { immediate: true }
)

const handleSubmit = async () => {
  // 表單驗證
  if (!formData.value.title.trim()) {
    message.error('書名不可為空')
    return
  }
  if (!formData.value.author.trim()) {
    message.error('作者不可為空')
    return
  }
  if (!formData.value.publisher.trim()) {
    message.error('出版社不可為空')
    return
  }

  loading.value = true

  try {
    const result = await booksStore.updateBook(formData.value)

    if (result.success) {
      message.success('書籍更新成功！')
      emit('update:show', false)
      emit('success')
    } else {
      message.error(result.message || '更新失敗')
    }
  } catch (error) {
    message.error('更新失敗,請稍後再試')
  } finally {
    loading.value = false
  }
}

const handleClose = () => {
  if (!loading.value) {
    emit('update:show', false)
  }
}
</script>

<template>
  <NModal
    :show="show"
    @update:show="handleClose"
    preset="card"
    title="編輯書籍"
    :style="{ width: '600px' }"
    :mask-closable="!loading"
    :closable="!loading"
  >
    <NForm :model="formData" label-placement="left" label-width="80">
      <NFormItem label="書籍編號">
        <NInput v-model:value="formData.id" disabled />
      </NFormItem>

      <NFormItem label="書名" required>
        <NInput v-model:value="formData.title" placeholder="請輸入書名" :disabled="loading" />
      </NFormItem>

      <NFormItem label="作者" required>
        <NInput v-model:value="formData.author" placeholder="請輸入作者" :disabled="loading" />
      </NFormItem>

      <NFormItem label="出版社" required>
        <NInput
          v-model:value="formData.publisher"
          placeholder="請輸入出版社"
          :disabled="loading"
        />
      </NFormItem>

      <NFormItem label="書籍簡介">
        <NInput
          v-model:value="formData.description"
          type="textarea"
          :rows="5"
          :maxlength="500"
          show-count
          placeholder="請輸入書籍簡介（選填）"
          :disabled="loading"
        />
      </NFormItem>
    </NForm>

    <template #footer>
      <div class="flex justify-end gap-2">
        <NButton @click="handleClose" :disabled="loading">取消</NButton>
        <NButton type="primary" @click="handleSubmit" :loading="loading">儲存</NButton>
      </div>
    </template>
  </NModal>
</template>

<style scoped>
/* 編輯對話框樣式 */
</style>
