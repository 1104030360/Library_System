<script setup lang="ts">
import { NConfigProvider, NMessageProvider, NDialogProvider } from 'naive-ui'
import { RouterView } from 'vue-router'
import ChatWidget from './components/ChatWidget.vue'

// Naive UI 主題配置（專業淺色風格）
const themeOverrides = {
  common: {
    primaryColor: '#2563eb',
    primaryColorHover: '#1d4ed8',
    primaryColorPressed: '#1e40af',
    borderRadius: '8px',
    fontFamily:
      '-apple-system, BlinkMacSystemFont, "Segoe UI", "Roboto", "Oxygen", "Ubuntu", "Cantarell", "Fira Sans", "Droid Sans", "Helvetica Neue", sans-serif',
  },
  Button: {
    borderRadiusMedium: '8px',
  },
  Input: {
    borderRadius: '8px',
  },
  Card: {
    borderRadius: '8px',
  },
  Tag: {
    borderRadius: '6px',
  },
}
</script>

<template>
  <NConfigProvider :theme-overrides="themeOverrides">
    <NMessageProvider>
      <NDialogProvider>
        <RouterView />
        <!-- AI Chatbot - Phase 11 -->
        <ChatWidget />
      </NDialogProvider>
    </NMessageProvider>
  </NConfigProvider>
</template>
