<script setup lang="ts">
import type { Song } from '@/stores/songStore.ts';

const props = defineProps<{ song: Song; isCurrent: boolean }>();
</script>

<template>
  <div class="song-info-container flex items-center gap-4" tabindex="-1">
    <!-- Song Image -->
    <a :href="props.song.url" target="_blank" class="song-image" tabindex="-1">
      <img :src="props.song.thumbnail"
           alt="Song Thumbnail" class="rounded-lg border-accent-2"
           tabindex="-1"
      />
    </a>

    <!-- Song Details -->
    <div class="song-details flex flex-col overflow-hidden" tabindex="-1">
      <div class="title-container flex items-center gap-2" tabindex="-1">
        <!-- Title -->
        <a
            :href="props.song.url"
            target="_blank"
            class="song-title truncate"
            :class="{ 'current-title': props.isCurrent }"
            tabindex="-1"
        >
          {{ props.song.title }}
        </a>
      </div>
      <div class="song-artist text-secondary truncate" tabindex="-1">
        {{ props.song.artist }}
      </div>
    </div>
  </div>
</template>

<style scoped>
.song-info-container {
  width: 100%;
  overflow: hidden;
}

.song-title.current-title {
  color: var(--accent-success);
}

.song-image img {
  width: 3.5rem;
  height: 3.5rem;
  object-fit: cover;
  border: 2px solid var(--accent-2);
}

.song-details {
  flex: 1;
  overflow: hidden;
}

.title-container,
.song-artist {
  max-width: 100%;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
</style>