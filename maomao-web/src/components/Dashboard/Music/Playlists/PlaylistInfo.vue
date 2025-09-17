<script setup lang="ts">
import type { User } from '@/types/user.ts'
import UserMini from './UserMini.vue'

defineProps<{
  name: string
  creator: User
}>()
</script>

<template>
  <div class="p-2">
    <div class="font-medium truncate text-white">{{ name }}</div>
    <UserMini :user="creator" />
  </div>
</template>


<style scoped>

</style>