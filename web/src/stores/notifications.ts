import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { notificationApi } from '@/api'
import { useAuthStore } from './auth'
import type { Notification } from '@/types'

export const useNotificationsStore = defineStore('notifications', () => {
  // 狀態
  const notifications = ref<Notification[]>([])
  const unreadCount = ref(0)
  const loading = ref(false)
  const error = ref<string | null>(null)

  // 計算屬性
  const hasUnread = computed(() => unreadCount.value > 0)

  // 獲取通知列表
  async function fetchNotifications(params?: {
    unreadOnly?: boolean
    type?: string
    limit?: number
    offset?: number
  }) {
    const authStore = useAuthStore()
    if (!authStore.isLoggedIn) {
      notifications.value = []
      unreadCount.value = 0
      return { success: false, message: '未登入' }
    }
    loading.value = true
    error.value = null

    try {
      const data = await notificationApi.getNotifications(params)
      notifications.value = data.notifications
      unreadCount.value = data.unreadCount
      return { success: true }
    } catch (err: any) {
      error.value = err.response?.data?.error || '獲取通知失敗'
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  // 獲取未讀數量
  async function fetchUnreadCount() {
    const authStore = useAuthStore()
    if (!authStore.isLoggedIn) {
      unreadCount.value = 0
      return { success: false }
    }
    try {
      const data = await notificationApi.getUnreadCount()
      unreadCount.value = data.unreadCount
      return { success: true }
    } catch (err: any) {
      console.error('獲取未讀數量失敗:', err)
      return { success: false }
    }
  }

  // 標記單個通知為已讀
  async function markAsRead(notificationId: number) {
    const authStore = useAuthStore()
    if (!authStore.isLoggedIn) {
      return { success: false, message: '未登入' }
    }
    try {
      await notificationApi.markAsRead(notificationId)

      // 更新本地狀態
      const notification = notifications.value.find(n => n.id === notificationId)
      if (notification && !notification.read) {
        notification.read = true
        unreadCount.value = Math.max(0, unreadCount.value - 1)
      }

      return { success: true }
    } catch (err: any) {
      error.value = err.response?.data?.error || '標記已讀失敗'
      return { success: false, message: error.value }
    }
  }

  // 標記全部為已讀
  async function markAllAsRead() {
    const authStore = useAuthStore()
    if (!authStore.isLoggedIn) {
      return { success: false, message: '未登入' }
    }
    try {
      const data = await notificationApi.markAllAsRead()

      // 更新本地狀態
      notifications.value.forEach(n => n.read = true)
      unreadCount.value = 0

      return { success: true, updatedCount: data.updatedCount }
    } catch (err: any) {
      error.value = err.response?.data?.error || '標記全部已讀失敗'
      return { success: false, message: error.value }
    }
  }

  // 清空所有通知
  async function clearAllNotifications() {
    const authStore = useAuthStore()
    if (!authStore.isLoggedIn) {
      return { success: false, message: '未登入' }
    }
    try {
      const data = await notificationApi.clearNotifications()

      // 更新本地狀態
      notifications.value = []
      unreadCount.value = 0

      return { success: true, deletedCount: data.deletedCount }
    } catch (err: any) {
      error.value = err.response?.data?.error || '清空通知失敗'
      return { success: false, message: error.value }
    }
  }

  // 輪詢未讀數量 (每 30 秒)
  let pollingInterval: number | null = null

  function startPolling() {
    // 清除現有的輪詢
    if (pollingInterval) {
      clearInterval(pollingInterval)
    }

    // 立即執行一次
    fetchUnreadCount()

    // 設置輪詢 (30 秒)
    pollingInterval = setInterval(() => {
      fetchUnreadCount()
    }, 30000)
  }

  function stopPolling() {
    if (pollingInterval) {
      clearInterval(pollingInterval)
      pollingInterval = null
    }
  }

  return {
    // 狀態
    notifications,
    unreadCount,
    loading,
    error,

    // 計算屬性
    hasUnread,

    // 方法
    fetchNotifications,
    fetchUnreadCount,
    markAsRead,
    markAllAsRead,
    clearAllNotifications,
    startPolling,
    stopPolling,
  }
})
