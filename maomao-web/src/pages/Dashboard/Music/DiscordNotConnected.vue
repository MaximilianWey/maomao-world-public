<script setup lang="ts">
import { useRouter } from 'vue-router'
import DiscordIcon from '@/assets/icons/PulsatingDiscordIcon.vue'

const router = useRouter()
const connectDiscord = () => {
  router.push('/dash/settings#social-accounts')
}
</script>

<template>
  <div class="flex flex-col items-center justify-center h-full text-center px-4">
    <div class="max-w-md">
      <DiscordIcon class="w-32 h-32 mx-auto mb-6 opacity-80" />

      <h1 class="text-2xl font-bold text-primary mb-2">Connect Your Discord Account</h1>
      <p class="text-muted mb-6">
        To manage your music bot and view your servers, please connect your Discord account.
      </p>

      <button
          @click="connectDiscord"
          class="auth-button hover:bg-accent-1-hover transition px-6 py-2 font-semibold rounded-lg"
      >
        Connect Discord
      </button>
    </div>
  </div>
</template>

<style scoped>

</style>
