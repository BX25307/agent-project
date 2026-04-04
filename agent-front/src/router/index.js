import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import LoveAppChatView from '../views/LoveAppChatView.vue'
import EternityManusChatView from '../views/EternityManusChatView.vue'
import WhisperNestChatView from '../views/WhisperNestChatView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/love-app',
      name: 'love-app',
      component: LoveAppChatView
    },
    {
      path: '/eternity-manus',
      name: 'eternity-manus',
      component: EternityManusChatView
    },
    {
      path: '/whisper-nest',
      name: 'whisper-nest',
      component: WhisperNestChatView
    }
  ]
})

export default router
