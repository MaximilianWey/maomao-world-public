<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/userStore.ts'
import AccountToggleMenu from './AccountToggleMenu.vue'
import chevronUpDown from '@/assets/icons/chevron-up-down.svg'

const userStore = useUserStore()
const buttonRef = ref<HTMLElement | null>(null)
const showMenu = ref(false)

onMounted(async () => {
  if (!userStore.isAuthenticated) {
    await userStore.fetchUserData()
  }
})

const toggleMenu = (event: MouseEvent) => { 
  event.stopPropagation()
  showMenu.value = !showMenu.value 
}
</script>

<template>
  <div class="relative border-b mb-4 mt-2 pb-4 sidebar-border">
    <button
        ref="buttonRef"
        @click="toggleMenu"
        class="account-toggle-button"
    >
      <img
          :src="userStore.avatarUrl"
          alt="avatar"
          class="w-8 h-8 rounded bg-violet-500 dark:bg-violet-600 shadow"
      />
      <div class="flex-1 text-left">
        <div class="text-sm font-bold text-primary">{{ userStore.displayName || 'User' }}</div>
        <div class="text-xs text-secondary">{{ userStore.currentUser?.email || 'user@example.com' }}</div>
      </div>
      <img
          :src="chevronUpDown"
          alt="toggle"
          class="w-4 h-4 account-toggle-icon"
      />
    </button>

    <AccountToggleMenu
        v-if="showMenu"
        class="absolute left-0 mt-1 z-10"
        :is-discord-linked="userStore.hasDiscordLinked"
        :parent-button="buttonRef"
        @close="showMenu = false"
    />
  </div>
</template>