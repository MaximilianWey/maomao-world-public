<script setup lang="ts">
import ThemeToggle from '@/components/ThemeToggle.vue';

</script>

<template>
  <div class="auth-layout">
    <div class="flex flex-col">
      <!-- Theme toggle on top right -->
      <div class="absolute top-4 right-4">
        <ThemeToggle />
      </div>
      
      <div>
        <router-view />
      </div>
    </div>
  </div>
</template>

<style scoped>
</style>