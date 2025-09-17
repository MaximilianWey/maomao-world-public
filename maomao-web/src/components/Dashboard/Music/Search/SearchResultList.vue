<script setup lang="ts">
import { useSearchStore } from '@/stores/searchSongStore.ts'
import SongQueueItem from '@/components/Dashboard/Music/Queue/SongQueueItem.vue'

const searchStore = useSearchStore()
</script>

<template>
  <div class="results-container">
    <template v-for="(song) in searchStore.results" :key="song.url">
      <SongQueueItem
          :song="song"
          :is-current="false"
          class="result-item"
      />
    </template>

    <div v-if="searchStore.loading" class="loading">Searching…</div>
    <div v-else-if="searchStore.results.length === 0 && searchStore.query" class="empty">
      No results found.
    </div>
    <div v-if="searchStore.error" class="error">{{ searchStore.error }}</div>
  </div>
</template>

<style scoped>
.results-container {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  padding: 1rem;
  border-radius: 8px;
  overflow-y: auto;
  max-height: 80vh;
}

.result-item:not(:last-child) {
  margin-bottom: 0.5rem;
}

.loading,
.empty,
.error {
  text-align: center;
  margin-top: 1rem;
  font-weight: bold;
}

.error {
  color: red;
}
</style>
