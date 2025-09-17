<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { watch } from 'vue'
import { usePlaylistStore } from '@/stores/playlistsStore.ts'
import PlaylistOverview from '@/components/Dashboard/Music/Playlists/PlaylistOverview.vue'
import type { Song } from '@/stores/songStore.ts'

const emit = defineEmits<{
  (e: 'set-background-songs', songs: Song[]): void
}>()

const playlistStore = usePlaylistStore()
playlistStore.fetchOwnPlaylists()
const { ownPlaylists } = storeToRefs(playlistStore)

watch(ownPlaylists, (newPlaylists) => {
  if (newPlaylists.length > 0) {
    const songs = newPlaylists.flatMap(p => p.songs)
    emit('set-background-songs', songs)
  }
}, { immediate: true })
</script>

<template>
  <PlaylistOverview :playlists="ownPlaylists" :show-create-card="true" :editable="true"/>
</template>
