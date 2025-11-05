<template>
  <div class="fixed bottom-24 right-6 w-96 h-[600px] bg-white rounded-lg shadow-2xl flex flex-col z-50 border border-gray-200">
    <!-- Header -->
    <div class="flex items-center justify-between p-4 border-b bg-blue-600 text-white rounded-t-lg">
      <div class="flex items-center gap-2">
        <span class="text-xl">💬</span>
        <h3 class="font-semibold">中大圖書館 AI 助手</h3>
      </div>
      <button
        @click="$emit('close')"
        class="hover:bg-blue-700 rounded px-2 py-1 transition-colors"
        title="關閉"
      >
        ✕
      </button>
    </div>

    <!-- Messages -->
    <div ref="messagesContainer" class="flex-1 overflow-y-auto p-4 space-y-3 bg-gray-50">
      <!-- Welcome message -->
      <div v-if="messages.length === 0" class="text-center text-gray-400 mt-8">
        <div class="text-4xl mb-2">👋</div>
        <div class="text-sm">有什麼可以幫您的嗎？</div>
        <div class="text-xs mt-2 text-gray-300">
          您可以問我關於借還書、圖書館規則等問題
        </div>
      </div>

      <!-- Message list -->
      <div
        v-for="(msg, index) in messages"
        :key="index"
        :class="['flex', msg.role === 'user' ? 'justify-end' : 'justify-start']"
      >
        <div
          :class="[
            'max-w-[80%] px-4 py-2 rounded-lg whitespace-pre-wrap break-words',
            msg.role === 'user'
              ? 'bg-blue-600 text-white rounded-br-none'
              : 'bg-white text-gray-800 border border-gray-200 rounded-bl-none'
          ]"
        >
          {{ msg.content }}
        </div>
      </div>

      <!-- Loading -->
      <div v-if="isLoading" class="flex justify-start">
        <div class="bg-white border border-gray-200 px-4 py-2 rounded-lg rounded-bl-none">
          <span class="text-gray-400">AI 思考中</span>
          <span class="animate-pulse">...</span>
        </div>
      </div>

      <!-- Error -->
      <div v-if="error" class="flex justify-center">
        <div class="bg-red-50 border border-red-200 text-red-700 px-4 py-2 rounded-lg text-sm max-w-full">
          ⚠️ {{ error }}
        </div>
      </div>
    </div>

    <!-- Input area -->
    <div class="border-t p-4 bg-white rounded-b-lg">
      <form @submit.prevent="sendMessage" class="flex gap-2">
        <input
          v-model="inputMessage"
          type="text"
          placeholder="輸入訊息..."
          class="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 focus:border-transparent"
          :disabled="isLoading"
          maxlength="500"
        />
        <button
          type="submit"
          :disabled="!inputMessage.trim() || isLoading"
          class="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors font-medium"
        >
          發送
        </button>
      </form>
      <div class="text-xs text-gray-400 mt-2 text-center">
        由 Ollama AI 提供支援
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import axios from 'axios'

interface Message {
  role: 'user' | 'assistant'
  content: string
}

interface ChatResponse {
  success: boolean
  message: string
}

defineEmits(['close'])

const messages = ref<Message[]>([])
const inputMessage = ref('')
const isLoading = ref(false)
const error = ref('')
const messagesContainer = ref<HTMLDivElement>()

// Send message - Linus: simple and direct
async function sendMessage() {
  if (!inputMessage.value.trim() || isLoading.value) return

  const userMessage = inputMessage.value.trim()
  inputMessage.value = ''
  error.value = ''

  // Add user message
  messages.value.push({
    role: 'user',
    content: userMessage
  })

  // Scroll to bottom
  await nextTick()
  scrollToBottom()

  // Call API - Linus: no fancy state management, just axios
  isLoading.value = true

  try {
    const response = await axios.post<ChatResponse>('/api/chat', {
      message: userMessage,
      history: messages.value.slice(0, -1) // All messages except the one we just added
    })

    if (!response.data.success) {
      throw new Error(response.data.message || 'AI 回應失敗')
    }

    // Add AI response
    messages.value.push({
      role: 'assistant',
      content: response.data.message
    })

    // Scroll to bottom
    await nextTick()
    scrollToBottom()

  } catch (e: any) {
    // Handle errors - always show something to user (Linus: never fail silently)
    console.error('Chat error:', e)

    if (e.code === 'ERR_NETWORK') {
      error.value = '無法連線到伺服器，請確認服務是否啟動'
    } else if (e.response?.status === 503) {
      error.value = 'AI 服務暫時無法使用，請稍後再試'
    } else if (e.response?.status === 504) {
      error.value = 'AI 回應超時，請稍後再試'
    } else {
      error.value = e.response?.data?.message || e.message || 'AI 服務發生錯誤'
    }

    // Remove the user message if we failed
    messages.value.pop()

  } finally {
    isLoading.value = false
  }
}

// Scroll to bottom - Linus: simple DOM manipulation
function scrollToBottom() {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}
</script>
