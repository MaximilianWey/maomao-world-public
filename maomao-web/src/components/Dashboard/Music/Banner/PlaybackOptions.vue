<script setup lang="ts">
import { useSongStore } from '@/stores/songStore.ts';
import { computed } from 'vue';
import {
  Play,
  Pause,
  SkipBack,
  SkipForward,
  Repeat,
  Repeat2
} from 'lucide-vue-next';

const songStore = useSongStore();

const isFirstSong = computed(() => songStore.currentIndex === 0);
const isPlaying = computed(() => !songStore.isPaused);
const mode = computed(() => songStore.mode);

const previousSong = () => {
  if (!isFirstSong.value) {
    songStore.previousSong();
  }
};

const togglePlayPause = () => {
  if (songStore.isPaused) {
    songStore.resume();
  } else {
    songStore.pause();
  }
};

const nextSong = () => {
  songStore.skipSong();
};

const toggleMode = () => {
  const nextMode = {
    NORMAL: 'REPEAT_QUEUE',
    REPEAT_QUEUE: 'REPEAT_SONG',
    REPEAT_SONG: 'NORMAL',
  } [songStore.mode];
  songStore.setMode(nextMode as 'NORMAL' | 'REPEAT_SONG' | 'REPEAT_QUEUE');
}
</script>

<template>
  <div class="controls flex items-center justify-center gap-4">
    <!-- Previous Button -->
    <button
        class="button-container group"
        :class="[
        isFirstSong ? 'text-secondary cursor-not-allowed' : 'text-primary',
      ]"
        :disabled="isFirstSong"
        @click="previousSong"
    >
      <SkipBack class="w-6 h-6" />
    </button>

    <!-- Play/Pause Button -->
    <button class="button-container group text-primary" @click="togglePlayPause">
      <component :is="isPlaying ? Pause : Play" class="w-6 h-6" />
    </button>

    <!-- Next Button -->
    <button class="button-container group text-primary" @click="nextSong">
      <SkipForward class="w-6 h-6" />
    </button>

    <!-- Mode Button -->
    <button class="button-container group text-primary" @click="toggleMode">
      <component
          :is="mode === 'REPEAT_SONG' ? Repeat2 : Repeat"
          class="w-6 h-6"
          :class="{
            'text-muted': mode === 'NORMAL',
            'text-primary': mode !== 'NORMAL',
            }"
      />
    </button>
  </div>
</template>

<style scoped>
.button-container {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 2.8rem;
  height: 2.8rem;
  border-radius: 50%;
  background-color: transparent;
  transition: background-color 0.2s;
  border: none;
  cursor: pointer;
}

.button-container:hover {
  background-color: var(--nav-btn-bg-hover);
}

.button-container:disabled {
  cursor: not-allowed;
}
</style>