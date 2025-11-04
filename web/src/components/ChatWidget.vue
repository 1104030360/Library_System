<template>
  <!-- Chat Widget - Linus style: simple floating button -->
  <div>
    <!-- Chat Window -->
    <Transition name="chat">
      <ChatWindow
        v-if="isOpen"
        @close="isOpen = false"
      />
    </Transition>

    <!-- Floating Button -->
    <Transition name="button">
      <button
        v-if="!isOpen"
        @click="isOpen = true"
        class="fixed bottom-6 right-6 w-16 h-16 bg-blue-600 hover:bg-blue-700 rounded-full shadow-lg flex items-center justify-center text-white text-2xl z-50 transition-all hover:scale-110 active:scale-95"
        title="開啟 AI 助手"
      >
        💬
      </button>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import ChatWindow from './ChatWindow.vue'

const isOpen = ref(false)
</script>

<style scoped>
/* Transitions - simple but smooth */
.chat-enter-active,
.chat-leave-active {
  transition: all 0.3s ease;
}

.chat-enter-from {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}

.chat-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}

.button-enter-active,
.button-leave-active {
  transition: all 0.2s ease;
}

.button-enter-from,
.button-leave-to {
  opacity: 0;
  transform: scale(0);
}
</style>
