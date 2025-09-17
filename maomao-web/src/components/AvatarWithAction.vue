<script setup lang="ts">
import RoundAvatar from "@/components/RoundAvatar.vue";

const props = defineProps<{
  avatarUrl: string;
  actionText?: string;
}>();

const emit = defineEmits<{
  (e: 'action', url: string): void;
}>();

const actionText = props.actionText ?? "Action";

function handleClick() {
  emit('action', props.avatarUrl);
}
</script>

<template>
  <div class="flex flex-col items-center space-y-2">
    <div class="avatar-wrapper w-full aspect-square">
      <RoundAvatar
          :avatarUrl="avatarUrl"
          :showRing="true"
          :clickable="true"
          @click="handleClick"
      />
    </div>
    <button
        class="text-xs text-accent-1 hover:underline"
        @click="handleClick"
        type="button"
    >
      {{ actionText }}
    </button>
  </div>
</template>

<style scoped>
.avatar-wrapper {
  border-radius: 50%;
  overflow: hidden;
}
</style>