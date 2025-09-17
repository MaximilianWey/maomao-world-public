<script setup lang="ts">
import {useDark, useToggle } from '@vueuse/core'
import { SunIcon, MoonIcon } from '@heroicons/vue/24/outline';

const isDark = useDark()
const toggleDark = useToggle(isDark)
</script>

<template>
  <button
    @click="toggleDark()"
    class="theme-toggle"
    aria-label="Toggle theme"
  >
    <SunIcon v-if="isDark" class="theme-toggle-icon" />
    <MoonIcon v-else class="theme-toggle-icon" />
  </button>
</template>