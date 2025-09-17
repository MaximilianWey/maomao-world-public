<script setup lang="ts">
import SongInfo from '@/components/Dashboard/Music/Banner/SongInfo.vue';
import SongOptions from '@/components/Dashboard/Music/SongOptions/SongOptions.vue';
import type { Song } from '@/stores/songStore.ts';


const props = defineProps<{
  song: Song;
  index?: number;
  isCurrent: boolean
}>();
</script>

<template>
  <div class="queue-item" :class="{ 'current-song': props.isCurrent }">
    <div class="song-info">
      <SongInfo :song="props.song" :is-current="props.isCurrent" />
    </div>
    <div class="song-options">
      <SongOptions :song="props.song" :index="props.index"/>
    </div>
  </div>
</template>

<style scoped>
.queue-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.5rem;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  height: 5rem;
  width: 100%;   /* use relative width, or fixed like 100% of banner */
}

.queue-item.current-song {
  border-color: var(--accent-success);
}

.song-info {
  flex-grow: 1;
  margin-right: 1rem;
  overflow: hidden;
}

.song-options {
  flex-shrink: 0;
}
</style>
