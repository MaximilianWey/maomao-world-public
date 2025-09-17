import {createRouter, createWebHistory, type RouteRecordRaw} from 'vue-router'

// layouts
import DashboardLayout from './layouts/DashboardLayout.vue'
import AuthLayout from './layouts/AuthLayout.vue'
import PublicLayout from './layouts/PublicLayout.vue'

// dashboard pages
import DashboardIndex from './pages/Dashboard/Index.vue'
import MaoMaoGames from './pages/Dashboard/MaoMaoGames.vue'
import TimeTracker from './pages/Dashboard/TimeTracker.vue'
import Settings from './pages/Dashboard/Settings.vue'

// music subpages
import MusicOverviewPage from './pages/Dashboard/Music/SubPages/OverviewPage.vue'
import MusicSearchPage from './pages/Dashboard/Music/SubPages/SearchPage.vue'
import MusicQueuePage from './pages/Dashboard/Music/SubPages/QueuePage.vue'
import MusicPlaylistsPage from './pages/Dashboard/Music/SubPages/PlaylistsPage.vue'
import OwnPlaylistsPage from './pages/Dashboard/Music/SubPages/Playlists/OwnPlaylists.vue'
import SubscribedPlaylistsPage from './pages/Dashboard/Music/SubPages/Playlists/SubscribedPlaylists.vue'
import PublicPlaylistsPage from './pages/Dashboard/Music/SubPages/Playlists/PublicPlaylists.vue'

import MusicStatsPage from './pages/Dashboard/Music/SubPages/StatsPage.vue'

// admin pages
import UserManagement from './pages/Dashboard/Admin/UserManagement.vue'
import AuthProviders from './pages/Dashboard/Admin/AuthProviders.vue'
import GeneralSettings from './pages/Dashboard/Admin/GeneralSettings.vue'
import UISettings from './pages/Dashboard/Admin/UISettings.vue'

// auth + public
import LoginPage from './pages/Auth/Login.vue'
import PublicHome from './pages/Public/Home.vue'
import { getAuthToken } from '@/services/authService.ts'
import MusicGate from "@/pages/Dashboard/Music/MusicGate.vue";

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    component: AuthLayout,
    children: [
      { path: '', component: LoginPage },
    ]
  },
  {
    path: '/dash',
    component: DashboardLayout,
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/dash/home' },
      { path: 'home', component: DashboardIndex },

      // Music section with MusicGate and MusicLayout
      {
        path: 'music',
        component: MusicGate,  // MusicGate first, it checks if Discord is linked
        children: [
          { path: 'music', redirect: 'overview' },
          { path: 'overview', component: MusicOverviewPage },
          { path: 'search', component: MusicSearchPage },
          { path: 'queue', component: MusicQueuePage },
          {
            path: 'playlists',
            component: MusicPlaylistsPage,
            children: [
              { path: 'playlists', redirect: 'own' },
              { path: 'own', component: OwnPlaylistsPage },
              { path: 'subscribed', component: SubscribedPlaylistsPage },
              { path: 'public', component: PublicPlaylistsPage },
            ]
          },
          { path: 'stats', component: MusicStatsPage },
        ]
      },
      { path: 'maomao-games', component: MaoMaoGames },
      { path: 'time-tracker', component: TimeTracker },
      { path: 'settings', component: Settings },

      // Admin routes
      { path: 'admin/users', component: UserManagement },
      { path: 'admin/auth-providers', component: AuthProviders },
      { path: 'admin/general', component: GeneralSettings },
      { path: 'admin/ui', component: UISettings },
    ]
  },
  {
    path: '/',
    component: PublicLayout,
    children: [
      { path: '', component: PublicHome },
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes: routes as RouteRecordRaw[]
})

router.beforeEach((to, _, next) => {
  const token = getAuthToken()

  if (to.path === '/login' && token) {
    return next('/dash/home')
  }

  if (to.meta.requiresAuth && !token) {
    return next('/login')
  }

  next()
})

export default router
