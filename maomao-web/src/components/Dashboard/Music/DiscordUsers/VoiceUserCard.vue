<script setup lang="ts">
import { computed } from 'vue';
import { useVoiceChannelStore } from '@/stores/voiceChannelStore.ts';
import { useUserStore } from '@/stores/userStore.ts';
import VoiceUser from './VoiceUser.vue';

const voiceChannelStore = useVoiceChannelStore();
const userStore = useUserStore();

const users = computed(() => {
  const channel = voiceChannelStore.currentVoiceChannel;
  if (!channel) return [];
  const discordId = userStore.discordAccount?.externalId;
  console.log('Discord ID:', discordId);
  const discordAccount = userStore.discordAccount;
  console.log('Discord Account:', discordAccount);

  return channel.currentlyConnectedUsers.map(user => ({
    data: user,
    self: user.id === discordId,
    musicbot: user.id === channel.botUser.id,
  }));
});

const voiceChannelName = computed(() => voiceChannelStore.currentVoiceChannel?.name);
</script>

<template>
  <div v-if="users.length > 0" class="user-list-container">
    <div class="header">
      <div class="channel-name">{{ voiceChannelName }}</div>
    </div>
    <div class="user-list">
      <VoiceUser v-for="user in users" :key="user.data.id" :user="user" />
    </div>
  </div>
</template>

<style scoped>
.user-list-container {
  padding: 8px;
}

.header {
  font-weight: bold;
  padding: 6px 0;
  border-bottom: 1px solid var(--border-color);
  margin-bottom: 6px;
  font-size: 13px;
}

.channel-name {
  padding-left: 2px;
  color: var(--text-primary);
}

.user-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
</style>
