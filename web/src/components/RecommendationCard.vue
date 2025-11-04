<template>
  <div
    class="recommendation-card"
    @click="goToBookDetail"
  >
    <!-- AI Badge -->
    <div class="ai-badge">
      <svg
        class="ai-icon"
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
      <span>AI 推薦</span>
    </div>

    <!-- Book Info -->
    <div class="book-info">
      <h3 class="book-title">{{ recommendation.book.title }}</h3>
      <p class="book-meta">
        {{ recommendation.book.author }} · {{ recommendation.book.publisher }}
      </p>

      <!-- Recommendation Reason -->
      <div class="reason-box">
        <p class="reason-text">{{ recommendation.reason }}</p>
      </div>

      <!-- Score -->
      <div class="score-bar">
        <div class="score-label">推薦度</div>
        <div class="score-progress">
          <div
            class="score-fill"
            :style="{ width: `${recommendation.score * 100}%` }"
          ></div>
        </div>
        <div class="score-value">{{ Math.round(recommendation.score * 100) }}%</div>
      </div>

      <!-- Status Badge -->
      <div class="status-badge" :class="{ available: recommendation.book.isAvailable }">
        {{ recommendation.book.isAvailable ? '可借閱' : '已借出' }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import type { RecommendationWithBook } from '@/types'

const props = defineProps<{
  recommendation: RecommendationWithBook
}>()

const router = useRouter()

const goToBookDetail = () => {
  router.push(`/book/${props.recommendation.book.id}`)
}
</script>

<style scoped>
.recommendation-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.recommendation-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(102, 126, 234, 0.3);
}

.recommendation-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
    135deg,
    rgba(255, 255, 255, 0.1) 0%,
    rgba(255, 255, 255, 0) 100%
  );
  pointer-events: none;
}

.ai-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  color: white;
  margin-bottom: 16px;
}

.ai-icon {
  width: 16px;
  height: 16px;
  color: white;
}

.book-info {
  position: relative;
  z-index: 1;
}

.book-title {
  font-size: 20px;
  font-weight: 700;
  color: white;
  margin: 0 0 8px 0;
  line-height: 1.3;
}

.book-meta {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  margin: 0 0 16px 0;
}

.reason-box {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 12px;
  margin-bottom: 16px;
}

.reason-text {
  font-size: 14px;
  line-height: 1.6;
  color: white;
  margin: 0;
}

.score-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.score-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 600;
  min-width: 50px;
}

.score-progress {
  flex: 1;
  height: 6px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 3px;
  overflow: hidden;
}

.score-fill {
  height: 100%;
  background: linear-gradient(90deg, #ffd700 0%, #ffed4e 100%);
  border-radius: 3px;
  transition: width 0.5s ease;
}

.score-value {
  font-size: 14px;
  font-weight: 700;
  color: white;
  min-width: 45px;
  text-align: right;
}

.status-badge {
  display: inline-block;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  background: rgba(239, 68, 68, 0.2);
  color: #fca5a5;
}

.status-badge.available {
  background: rgba(34, 197, 94, 0.2);
  color: #86efac;
}

@media (max-width: 768px) {
  .recommendation-card {
    padding: 16px;
  }

  .book-title {
    font-size: 18px;
  }

  .book-meta,
  .reason-text {
    font-size: 13px;
  }
}
</style>
