<script setup lang="ts">
import type { Component } from 'vue'

const { selected, title, icon } = defineProps<{
  selected: boolean
  title:    string
  icon:     Component
}>()
</script>

<template>
  <button
    :class="['nav-button', selected ? 'selected' : '']"
  >
    <component
      :is="icon"
      class="icon"
    />
    <span>{{ title }}</span>
  </button>
</template>

<style scoped>
</style>