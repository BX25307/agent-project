<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const pointer = ref({
  x: 50,
  y: 32
})

const entries = [
  {
    title: 'Love Assistant',
    eyebrow: 'Relationship Coach',
    action: () => router.push('/love-app')
  },
  {
    title: 'EternityManus',
    eyebrow: 'Agent Workspace',
    action: () => router.push('/eternity-manus')
  },
  {
    title: 'WhisperNest',
    eyebrow: 'Emotion Sanctuary',
    action: () => router.push('/whisper-nest')
  }
]

const pageText = {
  heading: 'New World',
  action: 'Open'
}

const sceneStyle = computed(() => ({
  '--pointer-x': `${pointer.value.x}%`,
  '--pointer-y': `${pointer.value.y}%`
}))

function updatePointer(event) {
  const bounds = event.currentTarget.getBoundingClientRect()
  pointer.value = {
    x: ((event.clientX - bounds.left) / bounds.width) * 100,
    y: ((event.clientY - bounds.top) / bounds.height) * 100
  }
}

function resetPointer() {
  pointer.value = {
    x: 50,
    y: 32
  }
}
</script>

<template>
  <div class="home-page apple-home" :style="sceneStyle" @mousemove="updatePointer" @mouseleave="resetPointer">
    <div class="hero-aura"></div>
    <div class="ambient ambient-left"></div>
    <div class="ambient ambient-right"></div>

    <section class="hero-section">
      <h1 class="hero-title">{{ pageText.heading }}</h1>
    </section>

    <section class="card-grid">
      <article
        v-for="entry in entries"
        :key="entry.title"
        class="entry-card"
        @click="entry.action"
      >
        <p class="card-eyebrow">{{ entry.eyebrow }}</p>
        <h2>{{ entry.title }}</h2>
        <button class="card-button" type="button">{{ pageText.action }}</button>
      </article>
    </section>
  </div>
</template>
