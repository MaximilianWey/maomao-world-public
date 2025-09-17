<script setup lang="ts">
import type { User } from '@/types/user.ts'

defineProps<{
  user: User;
}>()
</script>

<template>
  <div class="flex items-center gap-2 mt-1">
    <img
        :src="user.avatarUrl ?? ''"
        class="w-5 h-5 rounded-full object-cover"
        alt="Avatar"
    />
    <span class="text-sm text-muted truncate">{{ user.username }}</span>
  </div>
</template>

<style scoped>

</style>