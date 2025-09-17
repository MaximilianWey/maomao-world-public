<script setup lang="ts">
import { useSongStore } from '@/stores/songStore.ts'
import SongQueueItem from './SongQueueItem.vue'
import draggable from 'vuedraggable'

const songStore = useSongStore()

function onEnd(evt: { oldIndex: number; newIndex: number }) {
  document.body.classList.remove('dragging')
  if (evt.oldIndex !== undefined && evt.newIndex !== undefined) {
    songStore.moveSong(evt.oldIndex, evt.newIndex)
  }
}

function onStart() {
  document.body.classList.add('dragging')
}

</script>

<template>
  <div class="queue-container">
    <draggable
        v-model="songStore.queue"
        item-key="url"
        animation="200"
        ghost-class="ghost"
        chosen-class="chosen"
        @start="onStart"
        @end="onEnd"
    >
      <template #item="{ element: song, index }">
        <SongQueueItem
            :song="song"
            :index="index"
            :is-current="index === songStore.currentIndex"
            class="draggable-item"
        />
      </template>
    </draggable>
  </div>
</template>

<style scoped>
body.dragging a {
  pointer-events: none;
}

.queue-container {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  padding: 1rem;
  border-radius: 8px;
  overflow-y: auto;
  max-height: 80vh;
}

.draggable-item {
  cursor: grab;
}

.draggable-item:not(:last-child) {
  margin-bottom: 0.5rem;
}
</style>