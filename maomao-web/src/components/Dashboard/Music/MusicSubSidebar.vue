<script setup lang="ts">
import { RouterLink } from 'vue-router'
import {
  Squares2X2Icon as OverviewIcon,
  MagnifyingGlassIcon as SearchIcon,
  ListBulletIcon as QueueIcon,
  RectangleGroupIcon as PlaylistsIcon,
  ChartBarIcon as StatsIcon,
} from '@heroicons/vue/24/outline'
import RouteItem from '@/components/Dashboard/Sidebar/RouteItem.vue'

const subNavItems = [
  { title: 'Overview',  path: '/dash/music/overview',  icon: OverviewIcon },
  { title: 'Search',    path: '/dash/music/search',    icon: SearchIcon },
  { title: 'Queue',     path: '/dash/music/queue',     icon: QueueIcon },
  { title: 'Playlists', path: '/dash/music/playlists', icon: PlaylistsIcon },
  { title: 'Stats',     path: '/dash/music/stats',     icon: StatsIcon}
]
</script>

<template>
  <div class="space-y-1 p-2">
    <RouterLink
        v-for="item in subNavItems"
        :key="item.path"
        :to="item.path"
        custom
        v-slot="{ navigate, isActive }"
    >
      <div @click="navigate">
        <RouteItem
            :title="item.title"
            :icon="item.icon"
            :selected="isActive"
        />
      </div>
    </RouterLink>
  </div>
</template>

<style scoped>

</style>
