<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'

// 流動速度設定
const MIN_SPEED = 0.5
const MAX_SPEED = 2

// 隨機數生成函數
function randomNumber(min: number, max: number) {
  return Math.random() * (max - min) + min
}

// Blob 類別
class Blob {
  el: HTMLElement
  size: number
  initialX: number
  initialY: number
  x: number
  y: number
  vx: number
  vy: number

  constructor(el: HTMLElement) {
    this.el = el

    // 獲取 blob 寬度和高度
    const boundingRect = this.el.getBoundingClientRect()
    this.size = boundingRect.width

    // 隨機 blob 初始位置
    this.initialX = randomNumber(0, window.innerWidth - this.size)
    this.initialY = randomNumber(0, window.innerHeight - this.size)
    this.el.style.top = `${this.initialY}px`
    this.el.style.left = `${this.initialX}px`

    // 隨機速度（可正可負）
    this.vx = randomNumber(MIN_SPEED, MAX_SPEED) * (Math.random() > 0.5 ? 1 : -1)
    this.vy = randomNumber(MIN_SPEED, MAX_SPEED) * (Math.random() > 0.5 ? 1 : -1)

    // 設定初始位置
    this.x = this.initialX
    this.y = this.initialY
  }

  update() {
    // 當下位置會隨著速度變化
    this.x += this.vx
    this.y += this.vy

    // 判斷 blob 是否超過螢幕的四個邊緣
    if (this.x >= window.innerWidth - this.size) {
      this.x = window.innerWidth - this.size
      this.vx *= -1
    }
    if (this.y >= window.innerHeight - this.size) {
      this.y = window.innerHeight - this.size
      this.vy *= -1
    }
    if (this.x <= 0) {
      this.x = 0
      this.vx *= -1
    }
    if (this.y <= 0) {
      this.y = 0
      this.vy *= -1
    }

    // 更新 translate
    // 因為 translate 是根據相對位置增加或減少
    // 所以要 this.x - this.initialX 來找到相對位置
    this.el.style.transform = `translate(${this.x - this.initialX}px, ${this.y - this.initialY}px)`
  }
}

const containerRef = ref<HTMLElement | null>(null)
const blobs: Blob[] = []
let animationId: number | null = null

function initBlobs() {
  if (!containerRef.value) return

  // 定義 7 個顏色（按照教程）
  const colors = [
    '#2ac9de',  // 青色
    '#f087f4',  // 粉色
    'color-mix(in srgb, #2ac9de, #f087f4 20%)',
    'whitesmoke',
    'color-mix(in srgb, #2ac9de, #f087f4 50%)',
    'color-mix(in srgb, #2ac9de, #f087f4 65%)',
    'color-mix(in srgb, #2ac9de, #f087f4 35%)'
  ]

  // 創建 7 個 blob 元素（按照教程）
  for (let i = 0; i < 7; i++) {
    const blobEl = document.createElement('div')
    blobEl.className = 'blob'
    blobEl.style.background = colors[i]  // 直接設置背景色
    containerRef.value.appendChild(blobEl)
  }

  // 獲取所有 blob 元素並建立 Blob 物件
  const blobEls = containerRef.value.querySelectorAll('.blob')
  blobs.push(...Array.from(blobEls).map((blobEl) => new Blob(blobEl as HTMLElement)))

  // 動畫更新函數
  function update() {
    animationId = requestAnimationFrame(update)
    blobs.forEach((blob) => blob.update())
  }

  requestAnimationFrame(update)
}

onMounted(() => {
  initBlobs()
})

onUnmounted(() => {
  if (animationId !== null) {
    cancelAnimationFrame(animationId)
  }
})
</script>

<template>
  <div ref="containerRef" class="blobs"></div>
</template>

<style>
/* 移除 scoped 讓動態創建的元素也能獲得樣式 */
.blobs {
  position: fixed;
  z-index: -1;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  filter: blur(140px);
}

.blobs .blob {
  width: max(240px, 28vw);
  aspect-ratio: 1;
  border-radius: 50%;
  position: absolute;
  top: 0;
  left: 0;
}

/* 使用 color-mix 混合主色創建 7 個 blob（按照教程）*/
.blobs .blob:nth-child(1) {
  background: #2ac9de;
}

.blobs .blob:nth-child(2) {
  background: #f087f4;
}

.blobs .blob:nth-child(3) {
  background: color-mix(in srgb, #2ac9de, #f087f4 20%);
}

.blobs .blob:nth-child(4) {
  background: whitesmoke;
}

.blobs .blob:nth-child(5) {
  background: color-mix(in srgb, #2ac9de, #f087f4 50%);
}

.blobs .blob:nth-child(6) {
  background: color-mix(in srgb, #2ac9de, #f087f4 65%);
}

.blobs .blob:nth-child(7) {
  background: color-mix(in srgb, #2ac9de, #f087f4 35%);
}
</style>
