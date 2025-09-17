<script setup lang="ts">
import {computed} from "vue";
import { useSongStore } from "@/stores/songStore.ts";
import SongContainer from "@/components/Dashboard/Music/Banner/SongContainer.vue";
import SongSearch from "@/components/Dashboard/Music/Banner/SongSearch/SongSearch.vue";
import NoSongPlaceholder from "@/components/Dashboard/Music/Banner/NoSongPlaceholder.vue";

const songStore = useSongStore();

const isQueueEmpty = computed(() => songStore.queue.length === 0);
</script>

<template>
  <div class="banner">
    <div class="song-search">
      <SongSearch />
    </div>

    <SongContainer v-if="!isQueueEmpty" class="song-container" />
    <NoSongPlaceholder v-else class="song-container" />
  </div>
</template>

<style scoped>
.banner {
  height: auto;
  display: flex;
  justify-content: space-between;
  padding-left: 2rem;
  gap: 1rem;
}

.song-search {
  height: 100px;
  flex: 2;
  max-width: 100%;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: center;
}

.song-container {
  flex-shrink: 0;
}

/* Hide SongContainer on small screens */
@media (max-width: 1400px) {
  .song-container {
    display: none;
  }
}

</style>