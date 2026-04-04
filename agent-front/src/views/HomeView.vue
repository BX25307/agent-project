<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const entries = [
  {
    title: 'AI\u604b\u7231\u5e94\u7528\u5927\u5e08',
    description:
      '\u6a21\u62df\u5fae\u4fe1\u5bf9\u8bdd\u4f53\u9a8c\uff0c\u652f\u6301\u57fa\u4e8e chatId \u7684\u6d41\u5f0f\u804a\u5929\u3002',
    action: () => router.push('/love-app')
  },
  {
    title: 'EternityManus',
    description:
      '\u7edf\u4e00\u98ce\u683c\u7684\u6d41\u5f0f\u804a\u5929\u7a97\u53e3\uff0c\u5b9e\u65f6\u5c55\u793a AI \u8fd4\u56de\u5185\u5bb9\u3002',
    action: () => router.push('/eternity-manus')
  }
]

const pageText = {
  heading: 'New World',
  subheading: '\u9009\u62e9\u4e00\u4e2a\u80fd\u529b\u5165\u53e3\uff0c\u8fdb\u5165\u5bf9\u5e94\u7684 AI \u804a\u5929\u754c\u9762\u3002',
  action: '\u8fdb\u5165\u804a\u5929'
}
</script>

<template>
  <div class="home-page">
    <div class="home-light home-light-left"></div>
    <div class="home-light home-light-right"></div>
    <div class="home-scanline"></div>
    <div class="home-orbit orbit-left"></div>
    <div class="home-orbit orbit-right"></div>
    <section class="hero-section">
      <h1 class="hero-title">{{ pageText.heading }}</h1>
      <p class="hero-text">{{ pageText.subheading }}</p>
    </section>

    <section class="card-grid">
      <article
        v-for="entry in entries"
        :key="entry.title"
        class="entry-card"
        @click="entry.action"
      >
        <div class="card-glow"></div>
        <h2>{{ entry.title }}</h2>
        <p>{{ entry.description }}</p>
        <button class="card-button" type="button">{{ pageText.action }}</button>
      </article>
    </section>
  </div>
</template>
