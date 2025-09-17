<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/userStore.ts'
import { useGuildStore } from '@/stores/guildStore.ts'
import DiscordNotConnected from './DiscordNotConnected.vue'
import NoGuilds from './NoGuilds.vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from "@/stores/authStore.ts";
import MusicLayout from "@/pages/Dashboard/Music/MusicLayout.vue";

const authStore = useAuthStore()
const userStore = useUserStore()
const guildStore = useGuildStore()
const route = useRoute()
const router = useRouter()

const discordLinked = computed(() => userStore.hasDiscordLinked)
const hasGuilds = computed(() => guildStore.guilds.length > 0)

onMounted(async () => {
  await authStore.loadProviders();

  if (!userStore.currentUser) {
    await userStore.fetchUserData();
  }

  if (!discordLinked.value) return

  await guildStore.fetchGuilds()
  console.log(guildStore.guilds); // Add this to check the state of guilds

  if (hasGuilds.value && route.path === '/dash/music') {
    await router.push('/dash/music/overview')
  }
})
</script>

<template>
  <div v-if="guildStore.isLoading" class="text-center mt-10">
    Loading...
  </div>
  <DiscordNotConnected v-else-if="!discordLinked" />
  <NoGuilds v-else-if="!hasGuilds" />
  <MusicLayout v-else />
</template>
