<script setup>
import { ref } from 'vue'
import ChatShell from '../components/ChatShell.vue'
import { createChatId } from '../utils/chat'

const chatId = ref(createChatId())

function extraParams() {
  return {
    chatId: chatId.value
  }
}

const title = 'AI\u604b\u7231\u5e94\u7528\u5927\u5e08'
const welcomeText =
  '\u4f60\u597d\uff0c\u6211\u53ef\u4ee5\u5e2e\u4f60\u5206\u6790\u604b\u7231\u6c9f\u901a\u3001\u8868\u8fbe\u65b9\u5f0f\u548c\u4e92\u52a8\u7b56\u7565\u3002'
</script>

<template>
  <ChatShell
    :title="title"
    endpoint="/ai/love_app/chat/emitter"
    :extra-params="extraParams"
    :welcome-text="welcomeText"
  />
</template>
