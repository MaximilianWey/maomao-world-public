<script setup lang="ts">
import { defineProps } from 'vue'
import type { LucideIcon } from 'lucide-vue-next'

const props = defineProps<{
  icon: LucideIcon
  label?: string
}>()
</script>

<template>
  <button
    class="flex items-center justify-center p-2 rounded-full transition-all duration-150 hover:scale-120 icon-component"
    :aria-label="props.label"
  >
    <component
        :is="props.icon"
        class="w-5 h-5"
    />
  </button>
</template>

<style scoped>
.icon-component {
  color: white;
}

</style>