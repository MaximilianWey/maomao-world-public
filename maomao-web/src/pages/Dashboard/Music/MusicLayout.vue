<script setup lang="ts">
import { computed, onMounted, onUnmounted, watch, nextTick, ref } from "vue";
import DiscordLogoBox from "@/components/Dashboard/Music/DiscordUsers/DiscordLogoBox.vue";
import VoiceUserCard from "@/components/Dashboard/Music/DiscordUsers/VoiceUserCard.vue";
import MusicSubSidebar from "@/components/Dashboard/Music/MusicSubSidebar.vue";
import Banner from "@/components/Dashboard/Music/Banner/Banner.vue";
import { useSongStore } from "@/stores/songStore.ts";
import { useGuildStore } from "@/stores/guildStore.ts";
import { useVoiceChannelStore } from "@/stores/voiceChannelStore.ts";
import Carousel from "@/components/Dashboard/Music/DiscordUsers/Carousel.vue";

const songStore = useSongStore();
const guildStore = useGuildStore();
const voiceChannelStore = useVoiceChannelStore();

let queueIntervalId: number | undefined;
let voiceIntervalId: number | undefined;

const fetchForGuild = (guildId: string) => {
  songStore.fetchQueue(guildId);
  voiceChannelStore.fetchCurrentVoiceChannel(guildId);

  // Clear previous intervals
  if (queueIntervalId) clearInterval(queueIntervalId);
  if (voiceIntervalId) clearInterval(voiceIntervalId);

  // Start polling for new queue
  queueIntervalId = setInterval(() => {
    songStore.fetchQueue(guildId);
  }, 3000);

  // Start polling for voice channel
  voiceIntervalId = setInterval(() => {
    voiceChannelStore.fetchCurrentVoiceChannel(guildId);
  }, 5000);
};

onMounted(() => {
  const currentGuildId = guildStore.getCurrentGuild?.id;
  if (currentGuildId) {
    fetchForGuild(currentGuildId);
  }
});

onUnmounted(() => {
  if (queueIntervalId) clearInterval(queueIntervalId);
  if (voiceIntervalId) clearInterval(voiceIntervalId);
});

const hasUsers = computed(() => {
  const vc = voiceChannelStore.currentVoiceChannel;
  return vc !== null && Array.isArray(vc.currentlyConnectedUsers) && vc.currentlyConnectedUsers.length > 0;
});

const lastUserListHeight = ref(0);

watch(hasUsers, async (newVal) => {
  await nextTick();

  const mainContent = document.querySelector('.main-content') as HTMLElement | null;
  const userList = document.querySelector('.user-list') as HTMLElement | null;

  if (!mainContent) return;

  if (newVal) {
    lastUserListHeight.value = userList ? userList.clientHeight : 200;
    mainContent.style.scrollBehavior = 'smooth';
    mainContent.scrollBy({ top: lastUserListHeight.value, behavior: 'smooth' });
  } else {
    mainContent.style.scrollBehavior = 'smooth';
    mainContent.scrollBy({ top: -lastUserListHeight.value, behavior: 'smooth' });
    lastUserListHeight.value = 200;
  }
});

const currentIndex = ref(0);

watch(currentIndex, (index) => {
  const guild = guildStore.guilds[index];
  if (guild) {
    guildStore.setCurrentGuild(guild.id);
  }
});

watch(
    () => guildStore.getCurrentGuild?.id,
    (guildId) => {
      if (guildId) {
        const i = guildStore.guilds.findIndex(g => g.id === guildId);
        if (i !== -1 && i !== currentIndex.value) {
          currentIndex.value = i;
        }

        fetchForGuild(guildId);
      }
    },
    { immediate: true }
);
</script>

<template>
  <div class="music-grid" :class="{ 'no-voice-channel': !hasUsers }">
    <!-- Top Left: Logo -->
    <div class="box logo" v-if="guildStore.guilds.length">
      <Carousel
          v-model="currentIndex"
          :count="guildStore.guilds.length"
      >
        <template #default="{ index }">
          <DiscordLogoBox :guild="guildStore.guilds[index]" />
        </template>
      </Carousel>
    </div>
    <!-- fallback if no guilds -->
    <div class="box logo" v-else>
      <DiscordLogoBox :guild="guildStore.getCurrentGuild"/>
    </div>

    <!-- Top Right: Banner -->
    <div class="box banner">
      <Banner />
    </div>

    <!-- Middle Left: User List -->
    <transition name="fade-slide">
      <div
          class="box user-list"
          :class="{ 'hidden': !hasUsers }"
      >
        <VoiceUserCard />
      </div>
    </transition>

    <!-- Bottom Left: Sidebar (moves up if no voice channel) -->
    <transition name="fade-slide">
      <div class="box sidebar" :class="{ 'expanded': !hasUsers }">
        <MusicSubSidebar />
      </div>
    </transition>

    <!-- Right: Main Content -->
    <div class="box main-content" tabindex="0">
      <router-view />
    </div>
  </div>
</template>

<style scoped>
.music-grid {
  display: grid;
  grid-template-areas:
    "logo banner"
    "user-list main"
    "sidebar main";
  grid-template-columns: 180px 1fr;
  grid-template-rows: 120px 200px 1fr;
  height: 100%;
  gap: 10px;
  box-sizing: border-box;
  transition: grid-template-rows 1s ease; /* smooth height change */
}

.music-grid.no-voice-channel {
  grid-template-areas:
    "logo banner"
    "sidebar main"
    "sidebar main";
  grid-template-rows: 120px 0 1fr; /* shrink user-list row smoothly */
}

.box {
  border: 2px solid var(--border-color);
  border-radius: 8px;
  box-sizing: border-box;
  background-color: var(--bg-tertiary);
}

.logo {
  grid-area: logo;
}

.banner {
  grid-area: banner;
}

.user-list {
  grid-area: user-list;
  overflow-y: auto;
  max-height: 200px;
  opacity: 1;
  transition: opacity 0.3s ease, max-height 0.3s ease;
}

.user-list.hidden {
  max-height: 0;
  opacity: 0;
  overflow: hidden;
  pointer-events: none;
}


.sidebar {
  grid-area: sidebar;
  overflow-y: auto;
  transition: max-height 0.3s ease;
}

.main-content {
  grid-area: main;
  overflow-y: auto;
  scroll-behavior: smooth; /* fallback smooth scroll */
}

/* Animations */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}
.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
