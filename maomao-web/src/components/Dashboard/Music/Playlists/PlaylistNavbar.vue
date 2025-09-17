<script setup lang="ts">
import { useRoute } from 'vue-router'

const tabs = [
  { name: 'Own', path: '/dash/music/playlists/own' },
  { name: 'Subscribed', path: '/dash/music/playlists/subscribed' },
  { name: 'Public', path: '/dash/music/playlists/public' }
]

const route = useRoute()

function isActive(tabPath: string) {
  return route.path === tabPath
}
</script>

<template>
  <nav class="top-bar flex justify-between px-4 py-2  rounded-md">
    <router-link
        v-for="tab in tabs"
        :key="tab.name"
        :to="tab.path"
        class="tab-button"
        :class="{ active: isActive(tab.path) }"
    >
      {{ tab.name }}
    </router-link>
  </nav>
</template>

<style scoped>
.top-bar {
  /* add any container styles if you want */
}

.tab-button {
  flex: 1;
  text-align: center;
  padding: 0.5rem 0;
  color: white;
  background: var(--accent-2);
  border-radius: 0.5rem;
  text-decoration: none;
  margin: 0 0.25rem;
  transition: background-color 0.3s ease;
}

.tab-button.active {
  background: var(--accent-1);
  font-weight: 600;
}

/* Optional: add hover effect */
.tab-button:hover {
  background: var(--accent-3);
}
</style>
