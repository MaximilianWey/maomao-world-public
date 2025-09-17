<script setup lang="ts">
import { UserIcon } from '@heroicons/vue/20/solid';
import type { DiscordUser } from '@/types/user.ts';

defineProps<{
  user: {
    data: DiscordUser
    self: boolean;
    musicbot: boolean;
  };
}>();

const defaultAvatar = 'https://cdn.discordapp.com/embed/avatars/0.png';
</script>

<template>
  <div class="voice-user" :class="{ self: user.self, bot: user.musicbot }">
    <div class="avatar-wrapper">
      <img :src="user.data.avatarUrl ?? defaultAvatar" alt="Avatar" class="avatar" />
    </div>

    <div class="user-info">
      <span class="display-name">
        <template v-if="user.musicbot">
          <UserIcon class="bot-icon" />
        </template>
        <span class="display-text">{{ user.data.username }}</span>
      </span>
      <span class="username">@{{ user.data.uniqueName }}</span>
    </div>
  </div>
</template>

<style scoped>
.voice-user {
  display: flex;
  align-items: center;
  padding: 6px;
  gap: 10px;
  border-radius: 6px;
  overflow: hidden;
}

.avatar-wrapper {
  position: relative;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0; /* Never shrink */
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  z-index: 1;
}

.self .avatar-wrapper::before {
  content: "";
  position: absolute;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 2px solid var(--accent-1);
  box-sizing: border-box;
  z-index: 0;
}

.user-info {
  display: flex;
  flex-direction: column;
  font-size: 12px;
  min-width: 0;
  flex-grow: 1;
}

.display-name {
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: bold;
  overflow: hidden;
}

.display-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: inline-block;
  min-width: 0;
  flex-shrink: 1;
}

.username {
  color: #888;
  font-size: 11px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}

/* Self styling */
.self .display-name {
  color: var(--accent-success);
}

/* Bot styling */
.bot-icon {
  width: 14px;
  height: 14px;
  color: var(--accent-2);
  flex-shrink: 0; /* Prevent shrinking */
}
</style>
