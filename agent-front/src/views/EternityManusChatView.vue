<script setup>
import ChatShell from '../components/ChatShell.vue'

const welcomeText =
  '\u4f60\u597d\uff0c\u6211\u662f EternityManus\uff0c\u53ef\u4ee5\u76f4\u63a5\u5f00\u59cb\u5bf9\u8bdd\u3002'
</script>

<template>
  <ChatShell
    title="EternityManus"
    endpoint="/ai/eternity_manus/emitter"
    assistant-stream-mode="steps"
    :stream-timeout-enabled="false"
    :welcome-text="welcomeText"
  />
</template>
