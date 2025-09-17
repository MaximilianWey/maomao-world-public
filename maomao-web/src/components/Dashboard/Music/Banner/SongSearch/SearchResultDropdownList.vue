<script setup lang="ts">
import { computed } from 'vue'
import { useSongStore } from '@/stores/songStore.ts'
import { useRouter } from 'vue-router'
import { useSearchStore } from "@/stores/searchSongStore.ts";
import SongInfo from '@/components/Dashboard/Music/Banner/SongInfo.vue'
import SongOptions from "@/components/Dashboard/Music/SongOptions/SongOptions.vue";

const props = defineProps<{ query: string }>()
const songStore = useSongStore()
const router = useRouter()
const searchStore = useSearchStore()

const results = computed(() => {
  const q = props.query.toLowerCase().trim()
  if (!q) return []

  const seen = new Set<string>()
  return songStore.queue.filter(song => {
    const matches = song.title.toLowerCase().includes(q) || song.artist.toLowerCase().includes(q)
    if (matches && !seen.has(song.identifier)) {
      seen.add(song.identifier)
      return true
    }
    return false
  })
})

const handleSearchOnline = async () => {
  await router.push('/dash/music/search')
  await searchStore.performSearch(props.query)
}

</script>

<template>
  <div class="search-dropdown">
    <div v-for="(song, index) in results" :key="index" class="result-item">
      <div class="search-item">
        <div class="song-info">
          <SongInfo :song="song" :isCurrent="false" />
        </div>
        <div class="song-options">
          <SongOptions position="right" :song="song" />
        </div>
      </div>
    </div>
    <div class="search-online" @click="handleSearchOnline">
      Search for "<strong>{{ props.query }}</strong>"
    </div>
  </div>
</template>

<style scoped>
.search-dropdown {
  position: absolute;
  z-index: 20;
  margin-top: 0.5rem;
  width: 100%;
  background-color: var(--bg-secondary);
  border: 1px solid var(--accent-2);
  border-radius: 0.75rem;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
  max-height: 20rem;
  overflow-y: auto;
}

.search-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  height: 60%;
}

.song-info {
  flex-grow: 1;
  min-width: 0; /* required for text truncation */
  overflow: hidden;
}

.song-options {
  width: 3.5rem;
  height: 3.5rem;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.result-item {
  padding: 0.5rem 1rem;
  cursor: pointer;
}

.result-item:hover {
  background-color: var(--accent-3);
}

.search-online {
  padding: 0.75rem 1rem;
  font-size: 0.875rem;
  color: var(--accent-1);
  border-top: 1px solid var(--accent-2);
  cursor: pointer;
}
.search-online:hover {
  background-color: var(--accent-3);
}
</style>
