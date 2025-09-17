<script setup lang="ts">
import SongInfo from "@/components/Dashboard/Music/Banner/SongInfo.vue";
import PlaybackOptions from "@/components/Dashboard/Music/Banner/PlaybackOptions.vue";
import SongOptions from "@/components/Dashboard/Music/SongOptions/SongOptions.vue";
import SongBackground from "@/components/Dashboard/Music/SongBackground.vue";
import { ref, watch } from "vue";
import { useSongStore } from '@/stores/songStore.ts';
import type { Song } from '@/stores/songStore.ts';

const songStore = useSongStore();

const backgroundSong = ref<Song | null>(null);

watch(
    () => songStore.currentSong,
    (newSong, oldSong) => {
      if (oldSong?.identifier !== newSong?.identifier) {
        backgroundSong.value = newSong ?? null;
      }
    },
    { immediate: true }
);
</script>

<template>
  <div class="song-container relative overflow-hidden">
    <!-- Background layer -->
    <div
        class="absolute inset-0 z-0 pointer-events-none mask-gradient bg-black bg-opacity-30"
        style="filter: brightness(0.6) blur(6px);"
    >
      <transition name="fade" mode="in-out">
        <SongBackground
            v-if="backgroundSong"
            :key="backgroundSong.identifier"
            :song="backgroundSong"
            class="absolute inset-0 z-0 transition-opacity duration-1000"
        />
      </transition>
    </div>

    <!-- Actual content on top -->
    <div class="relative z-10 flex flex-col justify-between h-full">
      <!-- Top Row -->
      <div class="top-row">
        <div class="song-info">
          <SongInfo v-if="songStore.currentSong" :song="songStore.currentSong" :is-current="true" />
        </div>
        <div class="song-options">
          <SongOptions v-if="songStore.currentSong" :song="songStore.currentSong" />
        </div>
      </div>

      <!-- Bottom Row -->
      <div class="bottom-row">
        <PlaybackOptions />
      </div>
    </div>
  </div>
</template>


<style scoped>
.song-container {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 115px;
  width: 100%;
  max-width: 600px;
  padding: 1rem;
  box-sizing: border-box;
  overflow: hidden;
  border-radius: 1rem;
}

.top-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  height: 60%;
}

.song-info {
  flex-grow: 1;
  min-width: 0;
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

.bottom-row {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 40%;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 1s ease;
  position: absolute;
  inset: 0;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
