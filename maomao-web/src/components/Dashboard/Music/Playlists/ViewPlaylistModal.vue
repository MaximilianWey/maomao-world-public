<script setup lang="ts">
import { ref, watch } from 'vue'
import PlaylistThumbnail from './PlaylistThumbnail.vue'
import SongQueueItem from '@/components/Dashboard/Music/Queue/SongQueueItem.vue'
import UserMini from '@/components/Dashboard/Music/Playlists/UserMini.vue'
import type { Song } from '@/stores/songStore.ts'
import type { User } from '@/types/user.ts'

const props = defineProps<{
  modelValue: boolean
  name: string
  songs: Song[]
  creator?: User
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const isOpen = ref(props.modelValue)

watch(() => props.modelValue, (val) => {
  isOpen.value = val
})

function close() {
  emit('update:modelValue', false)
}
</script>

<template>
  <div v-if="isOpen" class="modal-overlay" @click.self="close">
    <div class="modal-content">
      <!-- Playlist header -->
      <div class="flex items-center gap-4 mb-6">
        <div class="thumbnail-wrapper">
          <PlaylistThumbnail :songs="songs" />
        </div>
        <div class="flex flex-col">
          <h2 class="text-2xl font-bold truncate"
              style="max-width: 500px;">
            {{name}}
          </h2>
          <UserMini
              v-if="creator"
              :user="creator"
              style="max-width: 500px"/>
        </div>
      </div>

      <!-- Songs list -->
      <div v-if="songs.length > 0" class="songs-wrapper">
        <div
            v-for="song in songs"
            :key="song.url"
            class="spaced-0-5"
        >
          <SongQueueItem :song="song" :is-current="false" />
        </div>
      </div>

      <!-- Close button -->
      <div class="mt-6 flex justify-end">
        <button @click="close" class="btn-secondary">Close</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 50;
}

.modal-content {
  background: var(--bg-primary);
  padding: 2rem;
  border-radius: 0.75rem;
  width: 100%;
  max-width: 700px;
  overflow-y: auto;
  max-height: 80vh;
}

.songs-wrapper {
  border: 1px solid var(--auth-input-border);
  border-radius: 0.5rem;
  padding: 1rem;
  max-height: 500px;
  overflow-y: auto;
  background: var(--bg-tertiary);
}

.spaced-0-5 {
  margin-top: 0.5rem;
}

.btn-secondary {
  background-color: var(--accent-2);
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 0.5rem;
}

.btn-secondary:hover {
  background-color: var(--accent-2-hover);
}

.thumbnail-wrapper {
  width: 96px;
  height: 96px;
  flex-shrink: 0;
}

.thumbnail-wrapper :deep(*) {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 0.5rem;
}

</style>
