<script setup lang="ts">
import type { Song } from "@/stores/songStore.ts";

const props = defineProps<{
  songs: Song[]
}>()

// const altThumbnail = 'https://cactuscancer.org/wp-content/uploads/2015/04/photo-1429105049372-8d928fd29ba1-1200x1200-cropped.jpg';
const fallbackThumbnail = 'https://picsum.photos/720/720';
const thumbnails = props.songs.slice(0, 4)
</script>

<template>
  <div
      class="playlist-thumbnail-wrapper w-full h-full aspect-square"
      :class="`variant-${thumbnails.length}`"
  >
    <template v-if="thumbnails.length === 0">
      <img
          :src="fallbackThumbnail"
          alt="Empty playlist"
          class="thumbnail-image single"
      />
    </template>

    <template v-else-if="thumbnails.length === 1">
      <img
          :src="thumbnails[0].thumbnail"
          :alt="`Thumbnail for ${thumbnails[0].title}`"
          class="thumbnail-image single"
      />
    </template>

    <template v-else-if="thumbnails.length === 2">
      <div class="two-horizontal">
        <img :src="thumbnails[0].thumbnail" alt="" class="half top" />
        <img :src="thumbnails[1].thumbnail" alt="" class="half bottom" />
      </div>
    </template>

    <template v-else-if="thumbnails.length === 3">
      <div class="grid-3">
        <img :src="thumbnails[0].thumbnail" alt="" class="grid-img top-left" />
        <img :src="thumbnails[1].thumbnail" alt="" class="grid-img top-right" />
        <img :src="thumbnails[2].thumbnail" alt="" class="grid-img bottom" />
      </div>
    </template>

    <template v-else>
      <div class="grid-4">
        <img
            v-for="(song, index) in thumbnails"
            :key="index"
            :src="song.thumbnail"
            :alt="`Thumbnail for ${song.title}`"
            class="grid-img"
        />
      </div>
    </template>
  </div>
</template>

<style scoped>
.playlist-thumbnail-wrapper {

  border: 2px solid var(--accent-2);
  border-radius: 0.5rem;
  overflow: hidden;
  position: relative;
  background-color: var(--bg-primary);
}

/* 0 or 1 thumbnail (full view) */
.thumbnail-image.single {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 2 thumbnails - horizontal split */
.two-horizontal {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
}

.half {
  width: 100%;
  height: 50%;
  object-fit: cover;
}


/* 3 thumbnails */
.grid-3 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-template-rows: 1fr 1fr;
  width: 100%;
  height: 100%;
  gap: 2px;
}

.grid-3 .top-left {
  grid-column: 1;
  grid-row: 1;
}

.grid-3 .top-right {
  grid-column: 2;
  grid-row: 1;
}

.grid-3 .bottom {
  grid-column: 1 / span 2;
  grid-row: 2;
}

.grid-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 4+ thumbnails */
.grid-4 {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: repeat(2, 1fr);
  gap: 2px;
  width: 100%;
  height: 100%;
}

/* fallback image inversion for light mode */
@media (prefers-color-scheme: light) {
  .thumbnail-image.single {
    filter: invert(1) hue-rotate(180deg);
  }
}

</style>
