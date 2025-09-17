<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  avatarUrl: string;
  showRing?: boolean;
  clickable?: boolean;
}>();

const hasRing = computed(() => props.showRing);
const isClickable = computed(() => props.clickable);
</script>

<template>
  <div
      class="avatar-ring"
      :class="{ 'has-ring': hasRing, clickable: isClickable }"
      role="button"
      tabindex="0"
      aria-label="Edit avatar"
  >
    <div class="avatar-inner">
      <img :src="avatarUrl" alt="User Avatar" class="avatar" draggable="false" />
    </div>
  </div>
</template>

<style scoped>
.avatar-ring {
  position: relative;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  box-sizing: border-box;
  cursor: default;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* The ring border */
.avatar-ring.has-ring {
  border: 4px solid var(--accent-1, #3b82f6);
  /* add a subtle shadow so ring stands out */
  box-shadow: 0 0 4px var(--accent-1, #3b82f6);
}

.avatar-ring.clickable {
  cursor: pointer;
}

/* Inner container that will scale */
.avatar-inner {
  width: calc(100% - 8px); /* full size minus ring thickness */
  height: calc(100% - 8px);
  border-radius: 50%;
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* Hover scaling only when clickable */
.avatar-ring.clickable:hover .avatar-inner {
  transform: scale(1.05);
  box-shadow: 0 0 10px var(--accent-1, #3b82f6);
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
  user-select: none;
  pointer-events: none;
}
</style>
