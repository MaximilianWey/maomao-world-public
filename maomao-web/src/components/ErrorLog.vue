<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Clipboard } from 'lucide-vue-next'

const visible = ref(false)
const errorMessage = ref('')
const isFadingOut = ref(false)
let hideTimeout: ReturnType<typeof setTimeout> | null = null
let fadeStartTimeout: ReturnType<typeof setTimeout> | null = null

const copiedVisible = ref(false)
let copiedTimeout: ReturnType<typeof setTimeout> | null = null

const scale = ref(1)
let clickTimeout: ReturnType<typeof setTimeout> | null = null

const messageContainerRef = ref<HTMLElement | null>(null)

function clearErrorFromUrl() {
  const url = new URL(window.location.href)
  url.searchParams.delete('error')
  window.history.pushState({}, document.title, url.toString())
}

function startFadeOut() {
  isFadingOut.value = true
  hideTimeout = setTimeout(() => {
    visible.value = false
    isFadingOut.value = false
  }, 2500)
}

function startHideTimer() {
  if (fadeStartTimeout) clearTimeout(fadeStartTimeout)
  if (hideTimeout) clearTimeout(hideTimeout)

  isFadingOut.value = false
  fadeStartTimeout = setTimeout(() => {
    startFadeOut()
  }, 2500)
}

function resetTimer() {
  if (fadeStartTimeout) clearTimeout(fadeStartTimeout)
  if (hideTimeout) clearTimeout(hideTimeout)

  isFadingOut.value = false
}

function resumeTimer() {
  startHideTimer()
}

function copyToClipboard() {
  if (navigator.clipboard && navigator.clipboard.writeText) {
    navigator.clipboard.writeText(errorMessage.value).then(() => {
      copiedVisible.value = true

      scale.value = 0.9
      if (clickTimeout) clearTimeout(clickTimeout)
      clickTimeout = setTimeout(() => {
        scale.value = 1
      }, 150)

      if (copiedTimeout) clearTimeout(copiedTimeout)
      copiedTimeout = setTimeout(() => {
        copiedVisible.value = false
      }, 2000)
    }).catch(() => {
      copiedVisible.value = false
      highlightErrorMessage()
    })
  } else {
    copiedVisible.value = false
    highlightErrorMessage()
  }
}

function highlightErrorMessage() {
  if (messageContainerRef.value) {
    const selection = window.getSelection()
    const range = document.createRange()
    range.selectNodeContents(messageContainerRef.value)
    selection?.removeAllRanges()
    selection?.addRange(range)
  }
}

function onButtonHover(isHovering: boolean) {
  scale.value = isHovering ? 1.2 : 1
}

function showError(message: string) {
  errorMessage.value = message
  visible.value = true
  isFadingOut.value = false
  startHideTimer()
  clearErrorFromUrl()
}

onMounted(() => {
  const url = new URL(window.location.href)
  const error = url.searchParams.get('error')
  if (error) {
    clearErrorFromUrl()
    showError(error)
  }
})
</script>

<template>
  <transition name="fade">
    <div
        v-if="visible"
        class="auth-error-box error-log"
        :class="{ fadingOut: isFadingOut }"
        @mouseenter="resetTimer"
        @mouseleave="resumeTimer"
    >
      <div class="header">An error occurred</div>
      <hr class="divider" />
      <div class="message-container" ref="messageContainerRef">
        {{ errorMessage }}
      </div>

      <div class="copy-container">
        <button
            class="copy-btn"
            :style="{ transform: `scale(${scale})` }"
            @mouseenter="onButtonHover(true)"
            @mouseleave="onButtonHover(false)"
            @click="copyToClipboard"
            aria-label="Copy error message to clipboard"
            title="Copy error message"
            type="button"
        >
          <Clipboard />
        </button>
        <transition name="fade">
          <span v-if="copiedVisible" class="copied-text">Copied!</span>
        </transition>
      </div>
    </div>
  </transition>
</template>

<style scoped>
.auth-error-box {
  position: fixed;
  top: 1rem;
  right: 1rem;
  /* Removed bottom to prevent stretching full height */
  background-color: var(--auth-error-bg);
  border: 1px solid var(--auth-error-border);
  color: var(--auth-error-text);
  border-radius: 0.5rem;
  padding: 1rem 1rem 2.5rem 1rem;
  font-size: 0.875rem;
  font-weight: 500;
  width: 320px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  cursor: default;
  display: flex;
  flex-direction: column;
  opacity: 1;
  transition: opacity 0.3s ease-in-out;
  z-index: 9999;
  overflow: hidden;
}

.error-log.fadingOut {
  opacity: 0;
  transition: opacity 2.5s ease-out;
}

.header {
  font-weight: 700;
  font-size: 1.1rem;
  margin-bottom: 0.25rem;
  user-select: none;
}

.divider {
  border: none;
  border-top: 1px solid var(--auth-error-border);
  margin-bottom: 0.5rem;
  user-select: none;
}

.message-container {
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-word;
  flex-grow: 1;
  padding-right: 0.5rem;
  margin-bottom: 1rem;
  max-height: calc(100vh - 2rem - 2.5rem - 1.5rem);
  transition: background-color 0.3s ease, border-color 0.3s ease;
}

/* Highlight when copy fails */
.highlight-failed {
  background-color: #ffdddd;
  border: 1px solid #ff5c5c;
  padding: 0.25rem 0.5rem;
  border-radius: 0.3rem;
  user-select: all;
}

/* Copy container */
.copy-container {
  position: absolute;
  bottom: 0.5rem;
  left: 1rem;
  height: 1.5rem;
  display: flex;
  align-items: center;
}

/* Copy button styling */
.copy-btn {
  background: transparent;
  border: none;
  color: var(--auth-error-text);
  cursor: pointer;
  padding: 0;
  user-select: none;
  outline-offset: 2px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  width: 1.5rem;
  height: 1.5rem;
  transition:
      color 0.2s ease,
      transform 0.15s ease;
}

.copy-btn:hover,
.copy-btn:focus {
  color: var(--auth-error-border);
  outline: none;
}

/* Copied text next to button */
.copied-text {
  margin-left: 0.7rem;
  color: var(--auth-error-text);
  font-weight: 600;
  user-select: none;
  font-size: 0.9rem;
  white-space: nowrap;
}

/* Fade transition for copied text */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
.fade-enter-to,
.fade-leave-from {
  opacity: 1;
}

/* Transition classes for error box fade */
.fade-enter-active {
  transition: opacity 0.1s ease-in;
}
.fade-leave-active {
  transition: none;
}
.fade-enter-from {
  opacity: 0;
}
.fade-enter-to {
  opacity: 1;
}
.fade-leave-from {
  opacity: 1;
}
.fade-leave-to {
  opacity: 0;
}
</style>
