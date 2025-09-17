<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useUserStore } from '@/stores/userStore.ts';
import { getCookie } from '@/services/authService';

const userStore = useUserStore();
const currentDate = ref(new Date());
const userTimeZone = ref('Europe/Berlin'); // Default timezone

// Greeting based on time of day with refined time ranges
const greeting = computed(() => {
  const hour = currentDate.value.getHours();

  if (hour >= 5 && hour < 12) return 'Good morning';
  if (hour >= 12 && hour < 18) return 'Good afternoon';
  if (hour >= 18 && hour < 24) return 'Good evening';
  // Between midnight (0) and 5 AM
  return 'Hello'; // Neutral greeting for late night hours
});

// Format date as "Day of week, Month Day, Year"
const formattedDate = computed(() => {
  return new Intl.DateTimeFormat('en-US', {
    weekday: 'long',
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    timeZone: userTimeZone.value
  }).format(currentDate.value);
});

onMounted(() => {
  // Try to get timezone from cookies if available
  const tzCookie = getCookie('USER_TIMEZONE');
  if (tzCookie) {
    userTimeZone.value = tzCookie;
  } else {
    // Fallback to browser's timezone if available
    try {
      const browserTimeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;
      if (browserTimeZone) {
        userTimeZone.value = browserTimeZone;
      }
    } catch (e) {
      console.warn("Could not detect browser timezone, using default", e);
    }
  }

  // Update the date every minute
  setInterval(() => {
    currentDate.value = new Date();
  }, 60000);
});
</script>

<template>
  <div>
    <span class="text-sm font-bold block">{{ greeting }}, {{ userStore.displayName }}!</span>
    <span class="text-xs block text-stone-500">{{ formattedDate }}</span>
  </div>
</template>