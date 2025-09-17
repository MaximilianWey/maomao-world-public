<script setup lang="ts">
import { defineEmits } from 'vue'

const emit = defineEmits(['create'])

function onClick() {
  emit('create')
}
</script>

<template>
  <div
      class="w-full h-full flex items-center justify-center text-6xl text-accent-1"
      @click="onClick"
  >
    +
  </div>
</template>
