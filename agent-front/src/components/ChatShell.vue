<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
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
  },
  streamTimeoutEnabled: {
    type: Boolean,
    default: true
  },
  theme: {
    type: String,
    default: 'default'
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

const FIRST_CHUNK_TIMEOUT_MS = 20000
const STREAM_IDLE_TIMEOUT_MS = 30000
const TYPEWRITER_INTERVAL_MS = 18

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
let streamTimer = null
let typewriterTimer = null

if (props.welcomeText) {
  messages.value.push(createMessage('assistant', props.welcomeText))
}

const canSend = computed(() => !sending.value && inputValue.value.trim().length > 0)

function escapeHtml(value) {
  return value
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;')
}

function escapeAttribute(value) {
  return escapeHtml(value).replaceAll('`', '&#96;')
}

function renderMessageContent(content) {
  if (!content) {
    return ''
  }

  let html = escapeHtml(content)
  const placeholders = []

  function stash(fragment) {
    const token = `__HTML_TOKEN_${placeholders.length}__`
    placeholders.push({ token, fragment })
    return token
  }

  html = html.replace(
    /!\[([^\]]*)\]\((https?:\/\/[^\s)]+)\)/g,
    (_, alt, src) =>
      stash(
        `<img class="message-image" src="${escapeAttribute(src)}" alt="${escapeAttribute(alt)}" loading="lazy" />`
      )
  )

  html = html.replace(
    /\[([^\]]+)\]\((https?:\/\/[^\s)]+)\)/g,
    (_, text, href) =>
      stash(
        `<a class="message-link" href="${escapeAttribute(href)}" target="_blank" rel="noopener noreferrer">${text}</a>`
      )
  )

  html = html.replace(
    /(^|[\s(])((https?:\/\/[^\s<]+?\.(?:png|jpe?g|gif|webp|bmp|svg)(?:\?[^\s<]*)?))/gi,
    (_, prefix, src) =>
      `${prefix}${stash(
        `<img class="message-image" src="${escapeAttribute(src)}" alt="image" loading="lazy" />`
      )}`
  )

  html = html.replace(
    /(^|[\s(])(https?:\/\/[^\s<]+)/g,
    (_, prefix, href) =>
      `${prefix}<a class="message-link" href="${escapeAttribute(href)}" target="_blank" rel="noopener noreferrer">${href}</a>`
  )

  html = html.replace(/\n/g, '<br />')

  placeholders.forEach(({ token, fragment }) => {
    html = html.replaceAll(token, fragment)
  })

  return html
}

async function scrollToBottom() {
  await nextTick()
  if (chatBodyRef.value) {
    chatBodyRef.value.scrollTop = chatBodyRef.value.scrollHeight
  }
}

watch(
  messages,
  () => {
    requestAnimationFrame(() => {
      scrollToBottom()
    })
  },
  { deep: true }
)

function appendToAssistantMessage(messageId, chunk) {
  const target = messages.value.find((item) => item.id === messageId)
  if (target) {
    target.content += chunk
    requestAnimationFrame(() => {
      scrollToBottom()
    })
  }
}

function clearTypewriterTimer() {
  if (typewriterTimer) {
    clearTimeout(typewriterTimer)
    typewriterTimer = null
  }
}

function flushPendingChunks() {
  if (!currentStreamState || !currentStreamState.pendingChunks.length) {
    clearTypewriterTimer()
    return
  }

  const chunk = currentStreamState.pendingChunks[0]
  if (!chunk) {
    currentStreamState.pendingChunks.shift()
    flushPendingChunks()
    return
  }

  appendToAssistantMessage(currentStreamState.messageIds[0], chunk[0])
  currentStreamState.pendingChunks[0] = chunk.slice(1)

  if (!currentStreamState.pendingChunks[0]) {
    currentStreamState.pendingChunks.shift()
  }

  typewriterTimer = setTimeout(flushPendingChunks, TYPEWRITER_INTERVAL_MS)
}

function enqueueAssistantChunk(chunk) {
  if (!currentStreamState) {
    return
  }

  currentStreamState.pendingChunks.push(chunk)
  if (!typewriterTimer) {
    flushPendingChunks()
  }
}

function flushPendingChunksImmediately() {
  if (!currentStreamState || props.assistantStreamMode === 'steps') {
    clearTypewriterTimer()
    return
  }

  clearTypewriterTimer()

  while (currentStreamState.pendingChunks.length) {
    appendToAssistantMessage(currentStreamState.messageIds[0], currentStreamState.pendingChunks.shift())
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

  enqueueAssistantChunk(chunk)
}

function closeSource() {
  if (currentSource) {
    currentSource.close()
    currentSource = null
  }
}

function clearStreamTimer() {
  if (streamTimer) {
    clearTimeout(streamTimer)
    streamTimer = null
  }
}

function scheduleStreamTimeout(type) {
  if (!props.streamTimeoutEnabled) {
    return
  }

  clearStreamTimer()
  const duration = type === 'first' ? FIRST_CHUNK_TIMEOUT_MS : STREAM_IDLE_TIMEOUT_MS

  streamTimer = setTimeout(() => {
    if (!currentStreamState) {
      return
    }

    const fallbackText =
      type === 'first'
        ? '\u670d\u52a1\u54cd\u5e94\u8f83\u6162\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5\u3002'
        : '\u5bf9\u8bdd\u5df2\u4e2d\u65ad\uff0c\u8bf7\u91cd\u65b0\u53d1\u9001\u6216\u7a0d\u540e\u518d\u8bd5\u3002'

    markFailed(currentStreamState.messageIds[0], fallbackText)
    finishSending()
    currentStreamState = null
  }, duration)
}

function finishSending() {
  sending.value = false
  clearStreamTimer()
  closeSource()
}

function completeStream() {
  flushPendingChunksImmediately()
  finishSending()
  currentStreamState = null
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

  closeSource()
  clearStreamTimer()
  clearTypewriterTimer()

  const userMessage = createMessage('user', content)
  messages.value.push(userMessage)
  inputValue.value = ''
  sending.value = true
  scrollToBottom()

  if (props.assistantStreamMode === 'steps') {
    const initialAssistantMessage = createMessage('assistant', '')
    messages.value.push(initialAssistantMessage)
    currentStreamState = {
      buffer: '',
      messageIds: [initialAssistantMessage.id],
      pendingChunks: [],
      connectionOpened: false
    }
  } else {
    const assistantMessage = createMessage('assistant', '')
    messages.value.push(assistantMessage)
    currentStreamState = {
      buffer: '',
      messageIds: [assistantMessage.id],
      pendingChunks: [],
      connectionOpened: false
    }
  }

  const url = buildApiUrl(props.endpoint, {
    message: content,
    ...props.extraParams()
  })

  let receivedChunk = false
  currentSource = new EventSource(url)
  scheduleStreamTimeout('first')

  currentSource.onopen = () => {
    if (currentStreamState) {
      currentStreamState.connectionOpened = true
    }
  }

  currentSource.onmessage = (event) => {
    receivedChunk = true
    updateAssistantStream(event.data || '')
    scheduleStreamTimeout('stream')
  }

  currentSource.onerror = () => {
    const readyState = currentSource?.readyState
    const opened = currentStreamState?.connectionOpened

    // Spring's finite SSE responses often end with an EventSource error callback.
    // If we already received content, treat it as a normal completion instead.
    if (receivedChunk) {
      completeStream()
      return
    }

    // Connection handshake succeeded but the first token has not arrived yet.
    // Keep waiting for the timeout instead of failing immediately.
    if (opened && readyState === EventSource.CONNECTING) {
      return
    }

    if (currentStreamState) {
      markFailed(currentStreamState.messageIds[0], labels.unavailable)
    }
    finishSending()
    if (readyState !== EventSource.CLOSED && currentStreamState) {
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
  clearTypewriterTimer()
  clearStreamTimer()
  closeSource()
})

onMounted(() => {
  scrollToBottom()
})
</script>

<template>
  <div class="chat-page" :class="`theme-${props.theme}`">
    <button class="ghost-button chat-return-button" type="button" @click="router.push('/')">
      {{ labels.backHome }}
    </button>

    <header class="chat-header">
      <h1>{{ title }}</h1>
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
        <div
          class="message-bubble"
          v-html="renderMessageContent(message.content || (message.role === 'assistant' && sending ? labels.thinking : ''))"
        ></div>
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
