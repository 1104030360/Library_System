<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { NDrawer, NDrawerContent, NButton, NEmpty, NTabs, NTabPane, NBadge, NSpin } from 'naive-ui'
import { useNotificationsStore } from '@/stores/notifications'
import { useRouter } from 'vue-router'

const props = defineProps<{
  show: boolean
}>()

const emit = defineEmits<{
  'update:show': [value: boolean]
}>()

const router = useRouter()
const notificationStore = useNotificationsStore()
const { notifications, unreadCount, loading } = storeToRefs(notificationStore)

const activeTab = ref('all')

// 篩選後的通知
const filteredNotifications = computed(() => {
  if (activeTab.value === 'all') {
    return notifications.value
  } else if (activeTab.value === 'unread') {
    return notifications.value.filter(n => !n.read)
  } else {
    return notifications.value.filter(n => n.type === activeTab.value)
  }
})

// 通知類型圖標
function getNotificationIcon(type: string) {
  const icons: Record<string, string> = {
    due: '[到期]',
    review: '[評論]',
    system: '[系統]',
    borrow: '[借閱]',
    return: '[歸還]'
  }
  return icons[type] || '[通知]'
}

// 格式化時間
function formatTime(dateString: string) {
  const date = new Date(dateString)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffMins = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMs / 3600000)
  const diffDays = Math.floor(diffMs / 86400000)

  if (diffMins < 1) return '剛剛'
  if (diffMins < 60) return `${diffMins} 分鐘前`
  if (diffHours < 24) return `${diffHours} 小時前`
  if (diffDays < 7) return `${diffDays} 天前`

  return date.toLocaleDateString('zh-TW', { month: 'numeric', day: 'numeric' })
}

// 標記單個通知為已讀並導航
async function handleNotificationClick(notification: any) {
  // 標記為已讀
  if (!notification.read) {
    await notificationStore.markAsRead(notification.id)
  }

  // 如果有連結，導航過去
  if (notification.link) {
    emit('update:show', false)
    router.push(notification.link)
  }
}

// 標記全部為已讀
async function markAllAsRead() {
  await notificationStore.markAllAsRead()
}

// 清空所有通知
async function clearAll() {
  await notificationStore.clearAllNotifications()
}

// 重新加載通知
async function refreshNotifications() {
  await notificationStore.fetchNotifications()
}

// 當通知中心打開時，獲取最新通知
const handleOpen = () => {
  if (props.show) {
    notificationStore.fetchNotifications()
  }
}

// 監聽 show 變化
watch(() => props.show, (newVal) => {
  if (newVal) {
    handleOpen()
  }
})
</script>

<template>
  <NDrawer
    :show="props.show"
    @update:show="emit('update:show', $event)"
    :width="420"
    placement="right"
  >
    <NDrawerContent title="通知中心" closable>
      <template #header>
        <div class="flex items-center gap-2">
          <span class="text-xl font-bold">通知中心</span>
          <NBadge v-if="unreadCount > 0" :value="unreadCount" :max="99" type="error" />
          <NButton
            text
            @click="refreshNotifications"
            :loading="loading"
            class="ml-auto"
          >
            刷新
          </NButton>
        </div>
      </template>

      <!-- 標籤頁 -->
      <NTabs v-model:value="activeTab" type="line" size="medium" class="mb-4">
        <NTabPane name="all" tab="全部" />
        <NTabPane name="unread" :tab="`未讀 (${unreadCount})`" />
        <NTabPane name="system" tab="系統" />
        <NTabPane name="borrow" tab="借閱" />
        <NTabPane name="return" tab="歸還" />
        <NTabPane name="due" tab="到期" />
        <NTabPane name="review" tab="評論" />
      </NTabs>

      <!-- 載入中 -->
      <div v-if="loading && notifications.length === 0" class="py-12 flex justify-center">
        <NSpin size="medium" />
      </div>

      <!-- 通知列表 -->
      <div v-else-if="filteredNotifications.length > 0" class="space-y-3">
        <div
          v-for="notification in filteredNotifications"
          :key="notification.id"
          class="p-4 rounded-xl border transition-all cursor-pointer"
          :class="notification.read
            ? 'bg-white border-slate-200 hover:border-slate-300'
            : 'bg-blue-50 border-blue-200 hover:border-blue-300'"
          @click="handleNotificationClick(notification)"
        >
          <div class="flex items-start gap-3">
            <!-- 類型標記 -->
            <div class="text-sm font-bold text-blue-600 flex-shrink-0">
              {{ getNotificationIcon(notification.type) }}
            </div>

            <!-- 內容 -->
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2 mb-1">
                <h4 class="font-bold text-slate-800">{{ notification.title }}</h4>
                <span
                  v-if="!notification.read"
                  class="w-2 h-2 bg-blue-500 rounded-full flex-shrink-0"
                />
              </div>
              <p class="text-sm text-slate-600 mb-2 whitespace-pre-wrap">{{ notification.message }}</p>
              <div class="text-xs text-slate-400">{{ formatTime(notification.createdAt) }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 空狀態 -->
      <div v-else class="py-12">
        <NEmpty description="沒有通知" />
      </div>

      <!-- 底部操作按鈕 -->
      <template #footer>
        <div class="flex flex-col gap-2">
          <NButton
            @click="markAllAsRead"
            :disabled="unreadCount === 0 || loading"
            block
            size="medium"
          >
            全部標為已讀
          </NButton>
          <NButton
            @click="clearAll"
            type="error"
            ghost
            :disabled="notifications.length === 0 || loading"
            block
            size="medium"
          >
            清空通知
          </NButton>
        </div>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>
