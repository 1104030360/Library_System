<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { NForm, NFormItem, NInput, NButton, NCard } from 'naive-ui'
import { useBooksStore } from '@/stores/books'
import { useAuthStore } from '@/stores/auth'
import AppHeader from '@/components/AppHeader.vue'
import LiquidBackground from '@/components/LiquidBackground.vue'

const router = useRouter()
const message = useMessage()
const booksStore = useBooksStore()
const authStore = useAuthStore()

// Form data
const formRef = ref()
const formData = ref({
  id: '',
  title: '',
  author: '',
  publisher: '',
  description: ''
})

// Loading state
const loading = ref(false)

// Form validation rules
const rules = {
  id: [
    { required: true, message: 'Please enter book ID', trigger: 'blur' },
    {
      pattern: /^[A-Za-z0-9]{3,6}$/,
      message: 'Book ID must be 3-6 alphanumeric characters',
      trigger: 'blur'
    }
  ],
  title: [
    { required: true, message: 'Please enter book title', trigger: 'blur' },
    { min: 2, max: 100, message: 'Title length should be 2-100 characters', trigger: 'blur' }
  ],
  author: [
    { required: true, message: 'Please enter author name', trigger: 'blur' },
    { min: 2, max: 50, message: 'Author name length should be 2-50 characters', trigger: 'blur' }
  ],
  publisher: [
    { required: true, message: 'Please enter publisher name', trigger: 'blur' },
    { min: 2, max: 50, message: 'Publisher name length should be 2-50 characters', trigger: 'blur' }
  ],
  description: [
    { max: 500, message: 'Description should not exceed 500 characters', trigger: 'blur' }
  ]
}

// Submit form
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    loading.value = true

    const result = await booksStore.addBook(formData.value)

    if (result.success) {
      message.success('書籍新增成功！')
      // 根據使用者角色跳轉到適當頁面
      if (authStore.isAdmin || authStore.isStaff) {
        router.push('/admin')
      } else {
        router.push('/user')
      }
    } else {
      message.error(result.message || '新增書籍失敗')
    }
  } catch (error: any) {
    console.error('Form validation failed:', error)
  } finally {
    loading.value = false
  }
}

// Cancel operation
const handleCancel = () => {
  // 根據使用者角色跳轉回適當頁面
  if (authStore.isAdmin || authStore.isStaff) {
    router.push('/admin')
  } else {
    router.push('/user')
  }
}

// Check permission on mount
onMounted(async () => {
  await authStore.checkAuth()

  if (!authStore.isLoggedIn) {
    message.warning('請先登入')
    router.push('/login')
    return
  }

  const userType = authStore.user?.userType
  if (userType !== '館長' && userType !== '館員') {
    message.error('只有管理員可以新增書籍')
    router.push('/')
  }
})
</script>

<template>
  <div class="relative min-h-screen">
    <!-- Liquid Background -->
    <LiquidBackground />

    <div class="relative z-10">
      <AppHeader />

      <main class="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <!-- Page Title -->
        <div class="mb-8">
          <h1 class="text-4xl font-bold text-slate-800 text-overlay">新增書籍</h1>
          <p class="text-lg text-slate-700 font-semibold mt-2 text-overlay">填寫以下資訊新增書籍到系統</p>
        </div>

        <!-- Add Book Form -->
        <NCard
          class="backdrop-blur-lg bg-white/90 shadow-2xl"
          :bordered="false"
        >
          <NForm
            ref="formRef"
            :model="formData"
            :rules="rules"
            label-placement="top"
            label-width="auto"
            require-mark-placement="right-hanging"
            size="large"
          >
            <!-- Book ID -->
            <NFormItem label="書籍編號" path="id">
              <NInput
                v-model:value="formData.id"
                placeholder="例如: 021, B001"
                :maxlength="6"
                clearable
              />
            </NFormItem>

            <!-- Book Title -->
            <NFormItem label="書名" path="title">
              <NInput
                v-model:value="formData.title"
                placeholder="請輸入書名"
                :maxlength="100"
                clearable
              />
            </NFormItem>

            <!-- Author -->
            <NFormItem label="作者" path="author">
              <NInput
                v-model:value="formData.author"
                placeholder="請輸入作者姓名"
                :maxlength="50"
                clearable
              />
            </NFormItem>

            <!-- Publisher -->
            <NFormItem label="出版社" path="publisher">
              <NInput
                v-model:value="formData.publisher"
                placeholder="請輸入出版社名稱"
                :maxlength="50"
                clearable
              />
            </NFormItem>

            <!-- Description (Optional) -->
            <NFormItem label="書籍簡介（選填）" path="description">
              <NInput
                v-model:value="formData.description"
                type="textarea"
                placeholder="請輸入書籍簡介，最多 500 字元"
                :rows="5"
                :maxlength="500"
                show-count
                clearable
              />
            </NFormItem>

            <!-- Buttons -->
            <div class="flex justify-end space-x-4 mt-6">
              <NButton
                @click="handleCancel"
                :disabled="loading"
                size="large"
              >
                取消
              </NButton>
              <NButton
                type="primary"
                @click="handleSubmit"
                :loading="loading"
                size="large"
              >
                新增書籍
              </NButton>
            </div>
          </NForm>
        </NCard>

        <!-- Hint -->
        <div class="mt-6 p-4 bg-blue-50/90 backdrop-blur-sm rounded-xl shadow-sm border border-blue-100">
          <p class="text-sm text-blue-800">
            💡 <strong>提示:</strong> 請確保書籍編號唯一，不要與現有書籍重複。
          </p>
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
.text-overlay {
  text-shadow: 0 2px 4px rgba(255, 255, 255, 0.5);
}
</style>
