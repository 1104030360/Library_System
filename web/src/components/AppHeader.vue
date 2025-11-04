<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { storeToRefs } from 'pinia'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useNotificationsStore } from '@/stores/notifications'
import NotificationCenter from './NotificationCenter.vue'

const authStore = useAuthStore()
const notificationsStore = useNotificationsStore()
const route = useRoute()
const router = useRouter()

const showNotifications = ref(false)

// 使用 storeToRefs 保持響應式
const { unreadCount } = storeToRefs(notificationsStore)

const handleLogout = async () => {
  await authStore.logout()
  // 停止輪詢
  notificationsStore.stopPolling()
  // 登出後直接跳轉到登入頁面
  router.push('/login')
}

// 生命週期：啟動輪詢
onMounted(() => {
  if (authStore.isLoggedIn) {
    notificationsStore.startPolling()
  }
})

onUnmounted(() => {
  notificationsStore.stopPolling()
})
</script>

<template>
  <header class="bg-white/90 backdrop-blur-xl border-b border-slate-200 shadow-md">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex justify-between items-center h-16">
        <!-- 左側 - Logo 和導航 -->
        <div class="flex items-center space-x-8">
          <RouterLink to="/" class="text-2xl font-bold text-slate-800 hover:text-slate-900" style="letter-spacing: -0.01em;">
            中大圖書館借還系統
          </RouterLink>
          <nav class="hidden md:flex space-x-4">
            <!-- 管理員專用：頁面切換導航 -->
            <template v-if="authStore.isLoggedIn && (authStore.isAdmin || authStore.isStaff)">
              <RouterLink
                to="/user"
                class="px-3 py-2 text-sm font-semibold rounded-lg transition-colors"
                :class="route.path === '/user' ? 'bg-blue-100 text-blue-700' : 'text-slate-700 hover:bg-slate-100'"
              >
                使用者模式
              </RouterLink>
              <RouterLink
                to="/dashboard"
                class="px-3 py-2 text-sm font-semibold rounded-lg transition-colors"
                :class="route.path === '/dashboard' ? 'bg-indigo-100 text-indigo-700' : 'text-slate-700 hover:bg-slate-100'"
              >
                數據儀表板
              </RouterLink>
              <!-- 動態導航：館長顯示管理後台(/admin)，館員顯示館員後台(/staff) -->
              <RouterLink
                v-if="authStore.isAdmin"
                to="/admin"
                class="px-3 py-2 text-sm font-semibold rounded-lg transition-colors"
                :class="route.path === '/admin' ? 'bg-purple-100 text-purple-700' : 'text-slate-700 hover:bg-slate-100'"
              >
                管理後台
              </RouterLink>
              <RouterLink
                v-else-if="authStore.isStaff"
                to="/staff"
                class="px-3 py-2 text-sm font-semibold rounded-lg transition-colors"
                :class="route.path === '/staff' ? 'bg-blue-100 text-blue-700' : 'text-slate-700 hover:bg-slate-100'"
              >
                館員後台
              </RouterLink>
              <RouterLink
                to="/add-book"
                class="px-3 py-2 text-sm font-semibold rounded-lg transition-colors"
                :class="route.path === '/add-book' ? 'bg-green-100 text-green-700' : 'text-slate-700 hover:bg-slate-100'"
              >
                新增書籍
              </RouterLink>
            </template>
          </nav>
        </div>

        <!-- 右側 - 使用者資訊 -->
        <div class="flex items-center space-x-4">
          <template v-if="authStore.isLoggedIn && authStore.user">
            <!-- 通知按鈕 -->
            <div class="relative">
              <button
                @click="showNotifications = true"
                class="px-4 py-2 rounded-lg font-semibold text-slate-700 hover:bg-slate-100 transition-colors relative"
              >
                通知中心
                <span
                  v-if="unreadCount > 0"
                  class="absolute -top-1 -right-1 min-w-[20px] h-5 px-1.5 bg-red-500 text-white text-xs font-bold rounded-full flex items-center justify-center"
                >
                  {{ unreadCount > 99 ? '99+' : unreadCount }}
                </span>
              </button>
            </div>

            <div class="text-base text-slate-700 font-semibold">
              <span class="font-bold">{{ authStore.user.userType }}</span>
              <span class="mx-2">·</span>
              <span>{{ authStore.user.username }}</span>
            </div>
            <button
              @click="handleLogout"
              class="text-base text-slate-700 hover:text-slate-900 font-semibold"
            >
              登出
            </button>
          </template>
          <template v-else>
            <div class="flex items-center gap-3">
              <RouterLink
                to="/register"
                class="px-5 py-2 text-base font-bold text-slate-700 hover:text-slate-900 transition-colors"
              >
                註冊
              </RouterLink>
              <RouterLink
                to="/login"
                class="px-5 py-2 bg-blue-600 text-white text-base font-bold rounded-xl hover:bg-blue-700 transition-colors"
              >
                登入
              </RouterLink>
            </div>
          </template>
        </div>
      </div>
    </div>

    <!-- 通知中心側邊欄 -->
    <NotificationCenter v-model:show="showNotifications" />
  </header>
</template>
