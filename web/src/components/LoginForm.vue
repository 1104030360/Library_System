<script setup lang="ts">
import { ref } from 'vue'
import { NForm, NFormItem, NInput, NButton, NCard, useMessage } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import LiquidBackground from './LiquidBackground.vue'

const authStore = useAuthStore()
const router = useRouter()
const message = useMessage()

const formValue = ref({
  username: '',
  password: '',
})

const loading = ref(false)

const handleLogin = async () => {
  if (!formValue.value.username || !formValue.value.password) {
    message.warning('請輸入帳號和密碼')
    return
  }

  loading.value = true
  try {
    const result = await authStore.login({
      username: formValue.value.username,
      password: formValue.value.password,
    })

    if (result.success) {
      message.success('登入成功')
      // 根據角色跳轉到對應頁面
      if (authStore.isAdmin) {
        router.push({ name: 'admin' })      // 館長 → /admin
      } else if (authStore.isStaff) {
        router.push({ name: 'staff' })      // 館員 → /staff
      } else {
        router.push({ name: 'user' })       // 使用者 → /user
      }
    } else {
      message.error(result.message || '登入失敗')
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="relative min-h-screen flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
    <!-- 流動背景 -->
    <LiquidBackground />

    <div class="relative z-10 max-w-md w-full">
      <!-- 標題 -->
      <div class="text-center mb-8">
        <h2 class="text-6xl font-bold text-slate-800 mb-4 text-overlay" style="letter-spacing: -0.02em;">登入系統</h2>
        <p class="text-2xl text-slate-700 font-semibold text-overlay">中大圖書館借還系統</p>
      </div>

      <!-- 登入表單 -->
      <NCard class="!border-slate-200 !bg-white/90 backdrop-blur-xl shadow-2xl">
        <NForm @submit.prevent="handleLogin">
          <NFormItem label="帳號">
            <NInput
              v-model:value="formValue.username"
              placeholder="請輸入帳號"
              size="large"
              @keyup.enter="handleLogin"
            />
          </NFormItem>

          <NFormItem label="密碼">
            <NInput
              v-model:value="formValue.password"
              type="password"
              placeholder="請輸入密碼"
              size="large"
              show-password-on="click"
              @keyup.enter="handleLogin"
            />
          </NFormItem>

          <NButton
            type="primary"
            size="large"
            block
            :loading="loading"
            @click="handleLogin"
            style="font-weight: 600; font-size: 16px;"
          >
            登入
          </NButton>
        </NForm>
      </NCard>

      <!-- 測試帳號提示 -->
      <NCard class="mt-4 !border-slate-200 !bg-white/90 backdrop-blur-xl shadow-xl">
        <div class="text-sm">
          <div class="font-bold text-slate-800 mb-2" style="font-size: 15px;">測試帳號</div>
          <div class="space-y-1 text-slate-700 font-medium">
            <div>館長：0001 / 1111</div>
            <div>員工：0002 / 2222</div>
            <div>使用者：1001 / 1234</div>
          </div>
        </div>
      </NCard>

      <!-- 註冊連結 -->
      <div class="mt-4 text-center">
        <div class="text-sm text-slate-600">
          還沒有帳號？
          <router-link
            to="/register"
            class="text-blue-600 hover:text-blue-700 font-semibold transition-colors ml-1"
          >
            立即註冊
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>
