import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import LoveAppChatView from '../views/LoveAppChatView.vue'
import EternityManusChatView from '../views/EternityManusChatView.vue'

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
    }
  ]
})

export default router
