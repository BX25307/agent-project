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

const welcomeText =
  '\u4f60\u597d\uff0c\u6211\u662f WhisperNest\uff0c\u53ef\u4ee5\u966a\u4f60\u68b3\u7406\u60c5\u7eea\uff0c\u4e5f\u53ef\u4ee5\u5b89\u9759\u542c\u4f60\u503e\u8bc9\u3002'
</script>

<template>
  <ChatShell
    title="WhisperNest"
    endpoint="/ai/whisper_nest/chat/emitter"
    :extra-params="extraParams"
    theme="whisper"
    :welcome-text="welcomeText"
  />
</template>
