<script setup lang="ts">
import type { Guild } from '@/stores/guildStore.ts'

const props = defineProps<{
  guild?: Guild | null
}>()
</script>

<template>
  <div class="logo-card">
    <img
        v-if="props.guild?.iconUrl"
        :src="props.guild?.iconUrl"
        :alt="props.guild?.name"
        class="logo-image"
        draggable="false"
        style="pointer-events: none"
    />
    <div v-else class="no-logo">No Logo</div>
    <div class="guild-name">{{ props.guild?.name || 'Unknown Guild' }}</div>
  </div>
</template>

<style scoped>
.logo-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 12px;
  gap: 8px;
  text-align: center;
}

.logo-image {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  object-fit: contain;
  -webkit-user-drag: none;
  pointer-events: none;
  cursor: grab;
}

.no-logo {
  color: var(--text-muted);
  font-size: 14px;
}

.guild-name {
  font-size: 14px;
  font-weight: bold;
  color: var(--accent-1);
}

.logo-image,
.guild-name,
.no-logo {
  pointer-events: none;
  -webkit-user-drag: none;
  user-select: none;
  -moz-user-select: none;
  -webkit-user-select: none;
  -ms-user-select: none;
}

</style>
