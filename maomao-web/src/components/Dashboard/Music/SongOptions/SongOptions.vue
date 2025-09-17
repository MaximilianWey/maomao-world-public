<script setup lang="ts">
import { ref } from "vue";
import OptionsIcon from "@/assets/icons/OptionsIcon.vue";
import SongOptionsMenu from "@/components/Dashboard/Music/SongOptions/SongOptionsMenu.vue";
import { useSongStore } from '@/stores/songStore.ts';
import { usePlaylistStore } from '@/stores/playlistsStore.ts';
import type { Song } from "@/stores/songStore.ts";

const songStore = useSongStore();
const playlistStore = usePlaylistStore();
const buttonRef = ref<HTMLElement | null>(null)
const showMenu = ref(false)

const toggleMenu = (event: MouseEvent) => {
  event.stopPropagation()
  showMenu.value = !showMenu.value
}

const handlePlayNext = () => {
  songStore.playTrackNext(props.song);
};

const handleAddToQueue = () => {
  songStore.addSongToQueue(props.song);
};

const handleRemoveFromQueue = () => {
  if (props.index === undefined) {
    console.error("Index is not provided");
    return;
  }
  console.log("Removing song index {} from queue", props.index);
  songStore.removeSong(props.index);
}

const handleAddToPlaylist = (Song : Song, playlistId: string) => {
  playlistStore.addSongToPlaylist(playlistId, Song.identifier);
};

const props = defineProps<{
  song: Song;
  index?: number;
  position?: 'bottom' | 'right';
}>();
</script>

<template>
  <div>
    <button
        ref="buttonRef"
        @click="toggleMenu"
        class="button-container group text-primary"
    >
      <OptionsIcon />
    </button>

    <teleport to="body">
      <SongOptionsMenu
          v-if="showMenu"
          :parent-button="buttonRef"
          :position="props.position"
          :song="props.song"
          :index="props.index"
          @close="showMenu = false"
          @play-next="handlePlayNext"
          @add-to-queue="handleAddToQueue"
          @remove-from-queue="handleRemoveFromQueue"
          @add-to-playlist="handleAddToPlaylist"
      />
    </teleport>
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
</style>