<script setup lang="ts">
import PlaylistThumbnail from './PlaylistThumbnail.vue'
import PlaylistInfo from './PlaylistInfo.vue'
import IconButton from './IconButton.vue'
import { ListPlus, Forward, Shuffle } from 'lucide-vue-next'
import { defineEmits, defineProps } from 'vue'
import type { Playlist } from '@/types/playlist'

const props = defineProps<{
  playlist: Playlist
}>()

const emit = defineEmits<{
  (e: 'edit'): void
  (e: 'addToQueue', playlist: Playlist): void
  (e: 'playNext', playlist: Playlist): void
  (e: 'shufflePlay', playlist: Playlist): void
}>()

function onEditClick() {
  emit('edit')
}
</script>

<template>

  <div class="w-full h-full p-4">
    <!-- Outer Grid: Two Columns (60% / 40%) -->
    <div class="grid grid-cols-5 h-full gap-4">
      <!-- Left Section (60%) -->
      <div class="col-span-3 flex flex-col">
        <!-- Top takes only as much space as needed -->
        <div class="mb-4 cursor-pointer"
             @click="onEditClick"
        >
          <PlaylistThumbnail :songs="props.playlist.songs" class="flex-grow" />
        </div>
        <!-- Bottom takes remaining space -->
        <div class="flex-1 cursor-default">
          <PlaylistInfo :name="props.playlist.name" :creator="playlist.creator" class="p-2" />
        </div>
      </div>

      <!-- Right Section (40%) -->
      <div class="col-span-2 grid grid-rows-3 mt-2 mb-2">
        <IconButton :icon="ListPlus" label="Add to Queue" @click="emit('addToQueue', props.playlist)" />
        <IconButton :icon="Forward" label="Play Next" @click="emit('playNext', props.playlist)" />
        <IconButton :icon="Shuffle" label="Shuffle Play" @click="emit('shufflePlay', props.playlist)" />
      </div>
    </div>
  </div>
</template>
