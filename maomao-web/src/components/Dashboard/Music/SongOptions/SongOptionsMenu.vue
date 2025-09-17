<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch, nextTick } from 'vue';
import type { Song } from '@/stores/songStore.ts';
import { usePlaylistStore } from '@/stores/playlistsStore.ts';
import PlaylistThumbnail from "@/components/Dashboard/Music/Playlists/PlaylistThumbnail.vue";

const props = defineProps<{
  parentButton: HTMLElement | null;
  position?: 'bottom' | 'right';
  song: Song;
  index?: number;
}>();

const emit = defineEmits<{
  (e: 'close'): void;
  (e: 'play-next', song: Song): void;
  (e: 'add-to-queue', song: Song): void;
  (e: 'remove-from-queue', song: Song): void;
  (e: 'add-to-playlist', song: Song, playlistId: string): void;
}>();

const menuRef = ref<HTMLElement | null>(null);
const playlistMenuRef = ref<HTMLElement | null>(null);
const menuStyle = ref({ top: '0px', left: '0px' });
const playlistMenuStyle = ref({ top: '0px', left: '0px' });

const showPlaylistMenu = ref(false);
const playlistStore = usePlaylistStore();

onMounted(async () => {
  document.addEventListener('click', onClickOutside);
  await playlistStore.fetchOwnPlaylists();
  positionMenu();
});

onUnmounted(() => {
  document.removeEventListener('click', onClickOutside);
});

const positionMenu = () => {
  if (props.parentButton && menuRef.value) {
    const buttonRect = props.parentButton.getBoundingClientRect();
    const menuEl = menuRef.value;

    if (props.position === 'right') {
      menuStyle.value = {
        top: `${buttonRect.top}px`,
        left: `${buttonRect.right + 24}px`,
      };
    } else {
      menuStyle.value = {
        top: `${buttonRect.bottom + 4}px`,
        left: `${buttonRect.right - menuEl.offsetWidth}px`,
      };
    }
  }
};

const positionPlaylistMenu = () => {
  if (menuRef.value && playlistMenuRef.value) {
    const rect = menuRef.value.getBoundingClientRect();
    const submenu = playlistMenuRef.value;
    playlistMenuStyle.value = {
      top: `${rect.top}px`,
      left: `${rect.left - submenu.offsetWidth - 8}px`, // ← to the left of the main menu
    };
  }
};

const togglePlaylistMenu = async () => {
  showPlaylistMenu.value = !showPlaylistMenu.value;
  if (showPlaylistMenu.value) {
    await nextTick();
    positionPlaylistMenu();
  }
};

const handlePlayNext = () => {
  emit('play-next', props.song);
  emit('close');
};

const handleAddToQueue = () => {
  emit('add-to-queue', props.song);
  emit('close');
};

const handleRemoveFromQueue = () => {
  emit('remove-from-queue', props.song);
  emit('close');
};

const handleAddToPlaylist = (playlistId: string) => {
  emit('add-to-playlist', props.song, playlistId);
  emit('close');
};

const onClickOutside = (e: MouseEvent) => {
  const target = e.target as Node;
  if (
      menuRef.value &&
      !menuRef.value.contains(target) &&
      (!playlistMenuRef.value || !playlistMenuRef.value.contains(target))
  ) {
    emit('close');
  }
};

watch(() => props.parentButton, positionMenu);
</script>

<template>
  <div
      ref="menuRef"
      class="options-menu"
      :style="menuStyle"
      @click.stop
  >
    <span @click="handlePlayNext" class="truncate">Play Next</span>
    <span @click="handleAddToQueue" class="truncate">Add to Queue</span>
    <span @click="togglePlaylistMenu" class="truncate">Add to Playlist</span>

    <span
        v-if="props.index !== undefined"
        @click="handleRemoveFromQueue"
        class="truncate"
    >Remove from Queue</span>

    <hr class="options-menu-divider" />
    <span @click="emit('close')" class="truncate">Cancel</span>
  </div>

  <!-- Playlist submenu -->
  <div
      v-if="showPlaylistMenu"
      ref="playlistMenuRef"
      class="options-menu submenu"
      :style="playlistMenuStyle"
      @click.stop
  >
    <span
        v-for="playlist in playlistStore.ownPlaylists"
        :key="playlist.id"
        @click="() => handleAddToPlaylist(playlist.id)"
        class="playlist-item"
    >
      <playlist-thumbnail :songs="playlist.songs" class="thumbnail"/>
      <span class="playlist-name">{{ playlist.name }}</span>
    </span>
  </div>
</template>

<style scoped>
.options-menu {
  position: absolute;
  background-color: var(--menu-bg);
  border: 1px solid var(--menu-border);
  border-radius: 0.5rem;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  padding: 0.5rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  z-index: 10;
  width: 200px;
  transform-origin: top center;
  transform: scaleY(0);
  animation: roll-open 0.3s ease-out forwards;
}

.options-menu.submenu {
  width: 180px;
  z-index: 11;
}

.options-menu span {
  padding: 0.5rem 1rem;
  border-radius: 0.375rem;
  color: var(--text-primary);
  cursor: pointer;
  transition: background-color 0.2s;
}

.options-menu span:hover {
  background-color: var(--menu-item-hover);
}

.options-menu-divider {
  height: 1px;
  background-color: var(--menu-divider);
  margin: 0.5rem 0;
}

.playlist-item {
  display: flex;
  align-items: center;
  gap: 0.5rem; /* space between thumbnail and text */
  padding: 0.3rem 0.5rem;
  cursor: pointer;
  border-radius: 0.375rem;
  transition: background-color 0.2s;
}

.playlist-item:hover {
  background-color: var(--menu-item-hover);
}

.thumbnail {
  width: 32px;  /* adjust size as needed */
  height: 32px; /* keep aspect ratio */
  flex-shrink: 0;
  border-radius: 4px; /* optional, to soften corners */
}

.playlist-name {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

@keyframes roll-open {
  from {
    transform: scaleY(0);
    opacity: 0;
  }
  to {
    transform: scaleY(1);
    opacity: 1;
  }
}
</style>
