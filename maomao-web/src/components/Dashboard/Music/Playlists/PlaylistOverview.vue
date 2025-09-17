<script setup lang="ts">
import PlaylistCard from './PlaylistCard.vue'
import CreatePlaylistCard from './CreatePlaylistCard.vue'
import EditPlaylistModal from './EditPlaylistModal.vue'
import {computed, ref} from 'vue'
import { usePlaylistStore } from '@/stores/playlistsStore.ts'
import { useSongStore } from '@/stores/songStore.ts'
import type { Playlist } from '@/types/playlist'
import type { Visibility } from '@/types/visibility'
import ViewPlaylistModal from "@/components/Dashboard/Music/Playlists/ViewPlaylistModal.vue";

const props = defineProps<{
  playlists: Playlist[]
  showCreateCard?: boolean
  editable?: boolean
}>()

const playlistStore = usePlaylistStore()
const songStore = useSongStore()

// Modal control state:
const isModalOpen = ref(false)
const editingPlaylistId = ref<string | null>(null)

// We need to compute current playlist data based on editingPlaylistId:
const currentPlaylist = computed(() => {
  if (!editingPlaylistId.value) return null
  return props.playlists.find(p => p.id === editingPlaylistId.value) ?? null
})

function openModalForPlaylist(id: string | null) {
  editingPlaylistId.value = id
  isModalOpen.value = true
}

async function onSave({ name, visibility, songs }: { name: string; visibility: string; songs: any[] }) {
  const songIds = songs.map((s: any) => s.identifier)

  if (editingPlaylistId.value) {
    await playlistStore.updatePlaylistName(editingPlaylistId.value, name)
    await playlistStore.updatePlaylistVisibility(editingPlaylistId.value, visibility as Visibility)
    // Assuming songs are handled elsewhere or ignored here as your original
  } else {
    await playlistStore.createNewPlaylist(name, visibility as Visibility, songIds)
  }

  isModalOpen.value = false
  editingPlaylistId.value = null
}

function handleEdit(playlist: Playlist) {
  openModalForPlaylist(playlist.id)
}

function handleAddToQueue(playlist: Playlist) {
  songStore.enqueuePlaylist(playlist.id)
}

function handlePlayNext(playlist: Playlist) {
  songStore.clearQueue()
  songStore.enqueuePlaylist(playlist.id)
}

function handleShufflePlay(playlist: Playlist) {
  songStore.clearQueue()
  songStore.enqueuePlaylist(playlist.id, true)
}

</script>

<template>
  <div class="flex flex-wrap gap-4 m-4">
    <PlaylistCard
        v-for="playlist in playlists"
        :key="playlist.id"
        :playlist="playlist"
        class="playlist-card"
        @edit="() => handleEdit(playlist)"
        @addToQueue="handleAddToQueue"
        @playNext="handlePlayNext"
        @shufflePlay="handleShufflePlay"
    />

    <div class="playlist-card" v-if="showCreateCard">
      <CreatePlaylistCard @create="openModalForPlaylist(null)" />
    </div>

    <template v-if="isModalOpen">
      <EditPlaylistModal
          v-if="editable"
          v-model="isModalOpen"
          :playlist-id="editingPlaylistId ?? undefined"
          :initial-name="currentPlaylist?.name"
          :initial-visibility="currentPlaylist?.visibility"
          :initial-songs="currentPlaylist?.songs"
          @save="onSave"
          @update:modelValue="val => isModalOpen = val"
      />

      <ViewPlaylistModal
          v-else
          v-model="isModalOpen"
          :name="currentPlaylist?.name ?? ''"
          :songs="currentPlaylist?.songs ?? []"
          :creator="currentPlaylist?.creator"
      />
    </template>

  </div>
</template>

<style scoped>
.playlist-card {
  width: 200px;
  max-width: 200px;
  height: 200px;
  max-height: 200px;
  aspect-ratio: 1 / 1;
  display: flex;
  flex-direction: column;
  padding: 1rem;
  gap: 0.5rem;
  border-radius: 0.5rem;
  overflow: hidden;

  /* Frosted glass style */
  background: rgba(28, 25, 23, 0.70);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(10px);

  border: 1px solid var(--accent-1);
}
</style>
