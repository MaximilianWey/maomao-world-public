<script setup lang="ts">
import { ref } from 'vue';
import RoundAvatar from "@/components/RoundAvatar.vue";

const props = defineProps<{
  avatarUrl: string;
  showRing?: boolean;
}>();

const emit = defineEmits(['upload']);

const fileInput = ref<HTMLInputElement | null>(null);

function triggerFileInput() {
  fileInput.value?.click();
}

function handleUpload(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0];
  if (file) {
    emit('upload', file);
  }
}
</script>

<template>
  <div class="flex flex-col items-center space-y-4">
    <div
        class="avatar-wrapper"
        @click="triggerFileInput"
        role="button"
        tabindex="0"
        aria-label="Edit avatar"
    >
      <RoundAvatar :avatarUrl="avatarUrl" :showRing="props.showRing ?? true" :clickable="true" />
    </div>
    <button
        @click="triggerFileInput"
        class="text-sm text-accent-1 hover:underline"
    >
      Edit Avatar
    </button>
    <input
        ref="fileInput"
        type="file"
        accept="image/jpeg,image/png,image/gif"
        class="hidden"
        @change="handleUpload"
    />
  </div>
</template>

<style scoped>
.avatar-wrapper {
  cursor: pointer;
  display: inline-flex;
  border-radius: 50%;
  width: 136px;
  height: 136px;
}
</style>
