<script setup lang="ts">
import type { Song } from '@/stores/songStore.ts'

const props = defineProps<{
  song: Song
}>()
</script>

<template>
  <div
      class="absolute inset-0 bg-cover bg-center blur-background"
      :style="{ backgroundImage: `url('${props.song.thumbnail}')` }"
  />
</template>

<style scoped>
.blur-background {
  filter: blur(22px);
  transform: scale(1.1);
  transition: background-image 1s ease-in-out;
  background-repeat: no-repeat;
  background-size: cover;
}
</style>
