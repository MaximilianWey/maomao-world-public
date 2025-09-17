<script setup lang="ts">
import Sidebar from '@/components/Dashboard/Sidebar/Sidebar.vue'
import TopBar from "@/components/Dashboard/TopBar/TopBar.vue";
</script>

<template>
  <main class="grid gap-4 p-4 grid-cols-[220px_1fr]">
    <Sidebar />
    <div class="card flex flex-col h-[calc(100vh-32px)]">
      <div class="sticky top-0 z-10 rounded-t-lg bg-inherit">
        <TopBar />
      </div>
      <div class="flex-1 px-4 pb-4 overflow-y-auto">
        <router-view />
      </div>
    </div>
  </main>
</template>

<style scoped>
</style>