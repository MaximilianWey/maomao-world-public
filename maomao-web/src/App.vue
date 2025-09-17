<script setup lang="ts">
import { onMounted } from 'vue';
import { useUserStore } from '@/stores/userStore.ts';
import { getAuthToken } from '@/services/authService'
import ErrorLog from "@/components/ErrorLog.vue";

const userStore = useUserStore();

onMounted(async () => {
  const token = getAuthToken();
  
  if (token) {
    await userStore.fetchUserData();
  }
});
</script>

<template>
  <ErrorLog />
  <router-view />
</template>

<style scoped>
</style>