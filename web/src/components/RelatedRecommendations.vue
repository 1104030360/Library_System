<template>
  <div v-if="!loading && recommendations.length > 0" class="related-recommendations">
    <h3 class="section-title">
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
      相關推薦
    </h3>
    <div class="recommendations-grid">
      <RecommendationCard
        v-for="recommendation in recommendations"
        :key="recommendation.book.id"
        :recommendation="recommendation"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { recommendationApi } from '@/api'
import type { RecommendationWithBook } from '@/types'
import RecommendationCard from './RecommendationCard.vue'

const props = defineProps<{
  bookId: string
}>()

const loading = ref(false)
const recommendations = ref<RecommendationWithBook[]>([])

const loadRecommendations = async () => {
  if (!props.bookId) return

  loading.value = true

  try {
    recommendations.value = await recommendationApi.getRelatedRecommendations(props.bookId)
  } catch (err) {
    console.error('Failed to load related recommendations:', err)
    recommendations.value = []
  } finally {
    loading.value = false
  }
}

// Watch for bookId changes
watch(() => props.bookId, () => {
  loadRecommendations()
}, { immediate: true })
</script>

<style scoped>
.related-recommendations {
  margin-top: 48px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 20px 0;
}

.title-icon {
  width: 28px;
  height: 28px;
  color: #667eea;
}

.recommendations-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

@media (max-width: 768px) {
  .section-title {
    font-size: 20px;
  }

  .recommendations-grid {
    grid-template-columns: 1fr;
  }
}
</style>
