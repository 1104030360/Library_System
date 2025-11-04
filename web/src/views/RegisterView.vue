<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage, NForm, NFormItem, NInput, NButton, NCard } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'
import LiquidBackground from '@/components/LiquidBackground.vue'

const router = useRouter()
const message = useMessage()
const authStore = useAuthStore()

// Form data
const formData = ref({
  id: '',
  name: '',
  password: '',
  confirmPassword: '',
  email: ''
})

const loading = ref(false)

// Handle registration
async function handleRegister() {
  // Validate inputs
  if (!formData.value.id.trim()) {
    message.error('請輸入學號/工號')
    return
  }

  if (!formData.value.name.trim()) {
    message.error('請輸入姓名')
    return
  }

  if (formData.value.password.length < 6) {
    message.error('密碼至少需要 6 個字元')
    return
  }

  if (formData.value.password !== formData.value.confirmPassword) {
    message.error('兩次密碼輸入不一致')
    return
  }

  loading.value = true

  try {
    const result = await authStore.register({
      id: formData.value.id,
      name: formData.value.name,
      password: formData.value.password,
      email: formData.value.email || undefined
    })

    if (result.success) {
      message.success('註冊成功！正在跳轉...')
      // 註冊成功後跳轉到使用者頁面
      setTimeout(() => {
        router.push('/user')
      }, 1000)
    } else {
      message.error(result.message || '註冊失敗')
    }
  } catch (error: any) {
    message.error(error.response?.data?.error || '註冊失敗，請稍後再試')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center px-4">
    <LiquidBackground />

    <div class="relative z-10 w-full max-w-md">
      <n-card
        title="註冊新帳號"
        class="glass-card shadow-2xl"
        :bordered="false"
      >
        <n-form :model="formData" label-placement="top" size="large">
          <!-- Name Field -->
          <n-form-item label="姓名" required>
            <n-input
              v-model:value="formData.name"
              placeholder="請輸入您的姓名"
              :disabled="loading"
            />
          </n-form-item>

          <!-- ID Field -->
          <n-form-item label="學號/工號" required>
            <n-input
              v-model:value="formData.id"
              placeholder="請輸入學號或工號"
              :disabled="loading"
            />
          </n-form-item>

          <!-- Password Field -->
          <n-form-item label="密碼" required>
            <n-input
              v-model:value="formData.password"
              type="password"
              placeholder="至少 6 個字元"
              :disabled="loading"
              show-password-on="click"
            />
          </n-form-item>

          <!-- Confirm Password Field -->
          <n-form-item label="確認密碼" required>
            <n-input
              v-model:value="formData.confirmPassword"
              type="password"
              placeholder="請再次輸入密碼"
              :disabled="loading"
              show-password-on="click"
            />
          </n-form-item>

          <!-- Email Field (Optional) -->
          <n-form-item label="Email（選填）">
            <n-input
              v-model:value="formData.email"
              type="text"
              placeholder="your.email@example.com"
              :disabled="loading"
            />
          </n-form-item>

          <!-- Submit Buttons -->
          <div class="grid grid-cols-2 gap-3 mt-6">
            <n-button
              @click="router.push('/login')"
              :disabled="loading"
              size="large"
            >
              返回登入
            </n-button>
            <n-button
              type="primary"
              @click="handleRegister"
              :loading="loading"
              size="large"
            >
              註冊
            </n-button>
          </div>
        </n-form>

        <!-- Login Link -->
        <div class="text-center mt-4 text-sm text-slate-600">
          已有帳號？
          <router-link
            to="/login"
            class="text-blue-600 hover:text-blue-700 font-semibold transition-colors"
          >
            立即登入
          </router-link>
        </div>
      </n-card>
    </div>
  </div>
</template>

<style scoped>
.glass-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
}
</style>
