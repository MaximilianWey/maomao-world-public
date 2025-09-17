<script setup lang="ts">
import { computed, ref, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import PlaylistNavbar from '@/components/Dashboard/Music/Playlists/PlaylistNavbar.vue'
import SongBackground from '@/components/Dashboard/Music/SongBackground.vue'
import type { Song } from '@/stores/songStore.ts'

const route = useRoute()
const router = useRouter()

// Determine current tab based on route
const tabFromRoute = computed<'Own' | 'Subscribed' | 'Public'>(() => {
  const lastSegment = route.path.split('/').pop()
  if (lastSegment === 'subscribed') return 'Subscribed'
  if (lastSegment === 'public') return 'Public'
  return 'Own'
})

const goToTab = (tab: 'Own' | 'Subscribed' | 'Public') => {
  const path = {
    Own: '/dash/music/playlists/own',
    Subscribed: '/dash/music/playlists/subscribed',
    Public: '/dash/music/playlists/public',
  }[tab]
  router.push(path)
}

// 🎵 Background logic
const songPool = ref<Song[]>([])
const backgroundSong = ref<Song | null>(null)

function pickRandomSong() {
  if (songPool.value.length === 0) return
  const randomIndex = Math.floor(Math.random() * songPool.value.length)
  backgroundSong.value = songPool.value[randomIndex]
}

let interval: ReturnType<typeof setInterval> | null = null

function setupInterval() {
  clearInterval(interval!)
  interval = setInterval(() => {
    pickRandomSong()
  }, 60000)
}

// 🎧 When child emits new song pool (e.g., own playlists, public etc)
function handleSongPoolUpdate(songs: Song[]) {
  songPool.value = songs
  pickRandomSong()
  setupInterval()
}

onBeforeUnmount(() => {
  clearInterval(interval!)
})
</script>

<template>
  <div class="relative w-full min-h-screen overflow-hidden">
    <!-- 🎨 Background -->
    <div class="absolute inset-0 z-0 pointer-events-none mask-gradient">
      <transition name="fade" mode="in-out">
        <SongBackground
            v-if="backgroundSong"
            :key="backgroundSong.identifier"
            :song="backgroundSong"
            class="absolute inset-0 z-0 transition-opacity duration-1000"
        />
      </transition>
    </div>

    <!-- 📦 Foreground -->
    <div class="relative z-10 playlist-page p-4">
      <PlaylistNavbar :activeTab="tabFromRoute" @change-tab="goToTab" />

      <!-- 🧩 Subpage injects song pool to us -->
      <router-view @set-background-songs="handleSongPoolUpdate" />
    </div>
  </div>
</template>

<style scoped>
.playlist-page {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.mask-gradient {
  -webkit-mask-image: linear-gradient(to bottom, rgba(0, 0, 0, 1), rgba(0, 0, 0, 0.8), transparent);
  mask-image: linear-gradient(to bottom, rgba(0, 0, 0, 1), rgba(0, 0, 0, 0.8), transparent);
  mask-repeat: no-repeat;
  mask-size: cover;
  pointer-events: none;
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 1s ease;
  position: absolute;
  inset: 0;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

</style>
