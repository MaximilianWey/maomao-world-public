<script setup lang="ts">
import GreetingWidget from './GreetingWidget.vue';
import RssFeed from './RssFeed.vue';
import ThemeToggle from '../../ThemeToggle.vue';
</script>

<template>
  <div class="border-b px-4 mt-2 pb-4 mb-4 topbar-border">
    <div class="flex items-center p-0.5">
      <div class="grid grid-cols-[auto_1fr_auto] w-full gap-16 items-center overflow-hidden">
        <!-- Left column: Greeting - auto width to fit content -->
        <div class="flex items-center">
          <GreetingWidget />
        </div>
        
        <!-- Center column: RSS Feed - capped width with overflow handling -->
        <div class="flex items-center h-8 min-w-0 overflow-hidden">
          <RssFeed />
        </div>
        
        <!-- Right column: Theme Toggle -->
        <div class="flex items-center justify-end">
          <ThemeToggle />
        </div>
      </div>
    </div>
  </div>
</template>