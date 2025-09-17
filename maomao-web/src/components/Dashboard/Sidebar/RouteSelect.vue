<script setup lang="ts">
import { RouterLink } from 'vue-router'
import { useUserStore } from '@/stores/userStore.ts'

// icon imports
import {
  HomeIcon,
  Cog6ToothIcon as SettingsIcon,
  // ClockIcon as TimeTrackerIcon,
  // PuzzlePieceIcon as MaoMaoGamesIcon,
  UsersIcon as UserManagementIcon,
  KeyIcon as AuthProviderManagementIcon,
  Cog6ToothIcon as GeneralIcon,
  RectangleStackIcon as UIIcon,
} from '@heroicons/vue/24/outline'

import DiscordIcon from '@/assets/icons/DiscordIcon.vue'
import RouteItem from './RouteItem.vue'

// Get user store to check admin status
const userStore = useUserStore()

const navItems = [
  { title: 'Home',              path: '/dash/home',                 icon: HomeIcon        },
  { title: 'MaoMao Music',      path: '/dash/music',                icon: DiscordIcon     },
 // { title: 'MaoMao Games',      path: '/dash/maomao-games',         icon: MaoMaoGamesIcon },
 // { title: 'Time Tracker',      path: '/dash/time-tracker',         icon: TimeTrackerIcon },
 { title: 'Settings',          path: '/dash/settings',             icon: SettingsIcon    },
]

const adminNavItems = [
  { title: 'User Management',           path: '/dash/admin/users',            icon: UserManagementIcon          },
  { title: 'Auth Management',           path: '/dash/admin/auth-providers',   icon: AuthProviderManagementIcon  },
  { title: 'UI',                        path: '/dash/admin/ui',               icon: UIIcon                      },
  { title: 'General',                   path: '/dash/admin/general',          icon: GeneralIcon                 },
]

</script>

<template>
<div class="space-y-1">
  <RouterLink
      v-for="item in navItems"
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

  <template v-if="userStore.isAdmin">
    <div class="border-t my-4 pt-3 sidebar-divider">
      <RouterLink
        v-for="item in adminNavItems"
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
</div>
</template>

<style scoped>
.sidebar-divider {
  border-color: var(--border-color);
}
</style>