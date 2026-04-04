<script setup>
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { buildApiUrl } from '../services/http'
import { createMessage } from '../utils/chat'

const props = defineProps({
  title: {
    type: String,
    required: true
  },
  endpoint: {
    type: String,
    required: true
  },
  extraParams: {
    type: Function,
    default: () => ({})
  },
  welcomeText: {
    type: String,
    default: ''
  },
  assistantStreamMode: {
    type: String,
    default: 'single'
  }
})

const labels = {
  backHome: '\u8fd4\u56de\u4e3b\u9875',
  thinking: '\u6b63\u5728\u601d\u8003\u4e2d...',
  placeholder: '\u8bf7\u8f93\u5165\u5185\u5bb9...',
  sending: '\u53d1\u9001\u4e2d...',
  send: '\u53d1\u9001',
  unavailable: '\u670d\u52a1\u6682\u65f6\u4e0d\u53ef\u7528\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5\u3002',
  disconnected: '\u8fde\u63a5\u4e2d\u65ad\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5\u3002'
}

const avatars = {
  assistant: {
    label: 'AI',
    emoji: '\u{1F916}'
  },
  user: {
    label: 'ME',
    emoji: '\u{1F642}'
  }
}

const router = useRouter()
const messages = ref([])
const inputValue = ref('')
const sending = ref(false)
const chatBodyRef = ref(null)
let currentSource = null
let currentStreamState = null

if (props.welcomeText) {
  messages.value.push(createMessage('assistant', props.welcomeText))
}

const canSend = computed(() => !sending.value && inputValue.value.trim().length > 0)

watch(
  messages,
  async () => {
    await nextTick()
    if (chatBodyRef.value) {
      chatBodyRef.value.scrollTop = chatBodyRef.value.scrollHeight
    }
  },
  { deep: true }
)

function appendToAssistantMessage(messageId, chunk) {
  const target = messages.value.find((item) => item.id === messageId)
  if (target) {
    target.content += chunk
  }
}

function parseStepSegments(content) {
  const pattern = /Step\s+\d+\s*:/g
  const matches = Array.from(content.matchAll(pattern))

  if (!matches.length) {
    return content ? [content] : []
  }

  const segments = []

  if (matches[0].index > 0) {
    const prefix = content.slice(0, matches[0].index).trim()
    if (prefix) {
      segments.push(prefix)
    }
  }

  matches.forEach((match, index) => {
    const start = match.index
    const end = index + 1 < matches.length ? matches[index + 1].index : content.length
    const segment = content.slice(start, end).trim()
    if (segment) {
      segments.push(segment)
    }
  })

  return segments
}

function syncAssistantMessages(messageIds, contents) {
  while (messageIds.length < contents.length) {
    const message = createMessage('assistant', '')
    messages.value.push(message)
    messageIds.push(message.id)
  }

  messageIds.forEach((messageId, index) => {
    const target = messages.value.find((item) => item.id === messageId)
    if (target) {
      target.content = contents[index] || ''
    }
  })
}

function updateAssistantStream(chunk) {
  if (props.assistantStreamMode === 'steps') {
    currentStreamState.buffer += chunk
    const segments = parseStepSegments(currentStreamState.buffer)
    const contents = segments.length ? segments : ['']
    syncAssistantMessages(currentStreamState.messageIds, contents)
    return
  }

  appendToAssistantMessage(currentStreamState.messageIds[0], chunk)
}

function closeSource() {
  if (currentSource) {
    currentSource.close()
    currentSource = null
  }
}

function finishSending() {
  sending.value = false
  closeSource()
}

function markFailed(messageId, fallbackText) {
  const target = messages.value.find((item) => item.id === messageId)
  if (target && !target.content.trim()) {
    target.content = fallbackText
  }
}

function sendMessage() {
  const content = inputValue.value.trim()
  if (!content || sending.value) {
    return
  }

  const userMessage = createMessage('user', content)
  messages.value.push(userMessage)
  inputValue.value = ''
  sending.value = true

  if (props.assistantStreamMode === 'steps') {
    const initialAssistantMessage = createMessage('assistant', '')
    messages.value.push(initialAssistantMessage)
    currentStreamState = {
      buffer: '',
      messageIds: [initialAssistantMessage.id]
    }
  } else {
    const assistantMessage = createMessage('assistant', '')
    messages.value.push(assistantMessage)
    currentStreamState = {
      buffer: '',
      messageIds: [assistantMessage.id]
    }
  }

  const url = buildApiUrl(props.endpoint, {
    message: content,
    ...props.extraParams()
  })

  let receivedChunk = false
  currentSource = new EventSource(url)

  currentSource.onmessage = (event) => {
    receivedChunk = true
    updateAssistantStream(event.data || '')
  }

  currentSource.onerror = () => {
    const isClosed = currentSource?.readyState === EventSource.CLOSED
    if (!receivedChunk) {
      markFailed(currentStreamState.messageIds[0], labels.unavailable)
    }
    finishSending()
    if (!isClosed) {
      markFailed(currentStreamState.messageIds[0], labels.disconnected)
    }
    currentStreamState = null
  }
}

function onEnter(event) {
  if (event.shiftKey) {
    return
  }
  event.preventDefault()
  if (canSend.value) {
    sendMessage()
  }
}

onBeforeUnmount(() => {
  closeSource()
})
</script>

<template>
  <div class="chat-page">
    <header class="chat-header">
      <button class="ghost-button" type="button" @click="router.push('/')">{{ labels.backHome }}</button>
      <h1>{{ title }}</h1>
      <div class="header-placeholder"></div>
    </header>

    <main ref="chatBodyRef" class="chat-body">
      <div
        v-for="message in messages"
        :key="message.id"
        class="message-row"
        :class="message.role"
      >
        <div class="message-avatar" :class="message.role">
          <span class="avatar-emoji">{{ avatars[message.role].emoji }}</span>
          <span class="avatar-label">{{ avatars[message.role].label }}</span>
        </div>
        <div class="message-bubble">
          {{ message.content || (message.role === 'assistant' && sending ? labels.thinking : '') }}
        </div>
      </div>
    </main>

    <footer class="chat-footer">
      <textarea
        v-model="inputValue"
        class="chat-input"
        rows="1"
        :placeholder="labels.placeholder"
        @keydown.enter="onEnter"
      />
      <button class="send-button" type="button" :disabled="!canSend" @click="sendMessage">
        {{ sending ? labels.sending : labels.send }}
      </button>
    </footer>
  </div>
</template>
