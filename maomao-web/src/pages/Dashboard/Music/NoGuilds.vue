<script setup lang="ts">
import SadGhostIcon from '@/assets/icons/SadGhostIcon.vue'
</script>

<template>
  <div class="flex flex-col items-center justify-center h-full text-center px-4">
    <div class="max-w-md">
      <SadGhostIcon class="w-32 h-32 mx-auto mb-6 opacity-80 text-primary" />
      <h1 class="text-2xl font-bold text-primary mb-2">No Guilds Available</h1>
      <p class="text-muted mb-6">
        It looks like you don’t have any servers connected to your music bot yet.
      </p>
    </div>
  </div>
</template>

<style scoped>
</style>
