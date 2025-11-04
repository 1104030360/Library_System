import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api'
import type { User, LoginCredentials, RegisterData } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  // 狀態
  const user = ref<User | null>(null)
  const isLoggedIn = ref(false)

  // 計算屬性
  const userType = computed(() => user.value?.userType || '')
  const isAdmin = computed(() => userType.value === '館長')
  const isStaff = computed(() => userType.value === '館員')

  // 檢查登入狀態
  async function checkAuth() {
    try {
      const userData = await authApi.whoami()
      user.value = userData
      isLoggedIn.value = true
      return true
    } catch (error) {
      user.value = null
      isLoggedIn.value = false
      return false
    }
  }

  // 註冊
  async function register(data: RegisterData) {
    try {
      const userData = await authApi.register(data)
      user.value = userData
      isLoggedIn.value = true
      return { success: true }
    } catch (error: any) {
      return {
        success: false,
        message: error.response?.data?.error || '註冊失敗，請稍後再試',
      }
    }
  }

  // 登入
  async function login(credentials: LoginCredentials) {
    try {
      const userData = await authApi.login(credentials)
      user.value = userData
      isLoggedIn.value = true
      return { success: true }
    } catch (error: any) {
      return {
        success: false,
        message: error.response?.data?.message || '登入失敗，請檢查帳號密碼',
      }
    }
  }

  // 登出
  async function logout() {
    try {
      await authApi.logout()
    } catch (error) {
      console.error('登出錯誤:', error)
    } finally {
      user.value = null
      isLoggedIn.value = false
    }
  }

  return {
    user,
    isLoggedIn,
    userType,
    isAdmin,
    isStaff,
    checkAuth,
    register,
    login,
    logout,
  }
})
