<template>
  <nav class="sticky top-0 z-10 w-full bg-bg-secondary/80 backdrop-blur-md shadow-md">
    <div class="max-w-md mx-auto flex space-x-6 p-4">
      <button
          v-for="range in ranges"
          :key="range"
          @click="$emit('update:modelValue', range)"
          :class="[
          'py-2 px-4 text-sm font-semibold rounded-t-md transition-colors duration-200 whitespace-nowrap',
          props.modelValue === range
            ? 'bg-nav-btn-bg-selected text-nav-btn-text-selected border-b-4 border-accent-1 cursor-default'
            : 'text-nav-btn-text-unselected hover:bg-nav-btn-bg-hover cursor-pointer'
        ]"
          type="button"
      >
        {{ rangeLabels[range] }}
      </button>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { RANGES, RANGE_LABELS } from "@/types/stats";
import type { Range } from "@/types/stats";

const props = defineProps<{
  modelValue: Range;
}>();
const emit = defineEmits<{
  (e: 'update:modelValue', value: Range): void;
}>();

const ranges = RANGES;
const rangeLabels = RANGE_LABELS;
</script>

<style scoped>

</style>
