<template>
  <div class="personal-recommendations">
    <!-- Header -->
    <div class="section-header">
      <div class="header-left">
        <h2 class="section-title">
          <svg
            class="title-icon"
            viewBox="0 0 24 24"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              d="M12 2L2 7L12 12L22 7L12 2Z"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
            <path
              d="M2 17L12 22L22 17"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
            <path
              d="M2 12L12 17L22 12"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
          為您推薦
        </h2>
        <p class="section-subtitle">基於您的閱讀歷史，AI 為您精選以下書籍</p>
      </div>
      <button v-if="!loading && recommendations.length > 0" class="refresh-btn" @click="loadRecommendations">
        <svg
          class="refresh-icon"
          :class="{ spinning: loading }"
          viewBox="0 0 24 24"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            d="M1 4V10H7"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
          <path
            d="M3.51 15C4.15 17.49 6.17 19.51 8.66 20.15C13.16 21.21 17.37 18.43 18.43 13.93C19.49 9.43 16.71 5.22 12.21 4.16C9.62 3.51 7.04 4.15 5.12 5.64L1 10"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
        重新整理
      </button>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p class="loading-text">AI 正在為您生成推薦...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="error-state">
      <svg
        class="error-icon"
        viewBox="0 0 24 24"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
      >
        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2" />
        <path d="M12 8V12" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
        <circle cx="12" cy="16" r="1" fill="currentColor" />
      </svg>
      <p class="error-message">{{ error }}</p>
      <button class="retry-btn" @click="loadRecommendations">重試</button>
    </div>

    <!-- Empty State -->
    <div v-else-if="recommendations.length === 0" class="empty-state">
      <svg
        class="empty-icon"
        viewBox="0 0 24 24"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
      >
        <path
          d="M4 19.5C4 18.837 4.26339 18.2011 4.73223 17.7322C5.20107 17.2634 5.83696 17 6.5 17H20"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M6.5 2H20V22H6.5C5.83696 22 5.20107 21.7366 4.73223 21.2678C4.26339 20.7989 4 20.163 4 19.5V4.5C4 3.83696 4.26339 3.20107 4.73223 2.73223C5.20107 2.26339 5.83696 2 6.5 2Z"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
      </svg>
      <p class="empty-message">暫時沒有推薦書籍</p>
      <p class="empty-hint">借閱更多書籍後，AI 將為您生成個人化推薦</p>
    </div>

    <!-- Recommendations Grid -->
    <div v-else class="recommendations-grid">
      <RecommendationCard
        v-for="recommendation in recommendations"
        :key="recommendation.book.id"
        :recommendation="recommendation"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { recommendationApi } from '@/api'
import type { RecommendationWithBook } from '@/types'
import RecommendationCard from './RecommendationCard.vue'

const loading = ref(false)
const error = ref<string | null>(null)
const recommendations = ref<RecommendationWithBook[]>([])

const loadRecommendations = async () => {
  loading.value = true
  error.value = null

  try {
    recommendations.value = await recommendationApi.getPersonalRecommendations()
  } catch (err: any) {
    console.error('Failed to load recommendations:', err)
    if (err.response?.status === 401) {
      error.value = '請先登入以獲取個人化推薦'
    } else {
      error.value = '無法載入推薦內容，請稍後再試'
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadRecommendations()
})
</script>

<style scoped>
.personal-recommendations {
  margin: 32px 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  gap: 16px;
}

.header-left {
  flex: 1;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.title-icon {
  width: 32px;
  height: 32px;
  color: #667eea;
}

.section-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(102, 126, 234, 0.2);
}

.refresh-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(102, 126, 234, 0.3);
}

.refresh-icon {
  width: 18px;
  height: 18px;
  transition: transform 0.5s ease;
}

.refresh-icon.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.loading-state,
.error-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 64px 20px;
  text-align: center;
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 4px solid #e5e7eb;
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.loading-text {
  margin-top: 20px;
  font-size: 16px;
  color: #6b7280;
}

.error-icon,
.empty-icon {
  width: 64px;
  height: 64px;
  color: #9ca3af;
  margin-bottom: 16px;
}

.error-message,
.empty-message {
  font-size: 18px;
  font-weight: 600;
  color: #374151;
  margin: 0 0 8px 0;
}

.empty-hint {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.retry-btn {
  margin-top: 20px;
  padding: 10px 24px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.retry-btn:hover {
  background: #5568d3;
  transform: translateY(-2px);
}

.recommendations-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

@media (max-width: 768px) {
  .section-header {
    flex-direction: column;
  }

  .section-title {
    font-size: 24px;
  }

  .recommendations-grid {
    grid-template-columns: 1fr;
  }
}
</style>
