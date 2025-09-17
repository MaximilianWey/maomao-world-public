<script setup lang="ts">
import { ref, onMounted } from 'vue';

interface RssItem {
  title: string;
  date: Date;
  link: string;
}

const rssItems = ref<RssItem[]>([]);
const isLoading = ref(true);
const error = ref<string | null>(null);

// Sample data to use during development
const sampleRssItems: RssItem[] = [
  { title: 'Welcome to MaoMao Cloud', date: new Date(), link: '#' },
  { title: 'New features in our latest update', date: new Date(), link: '#' },
  { title: 'Tips for getting the most out of our platform', date: new Date(), link: '#' },
  { title: 'Upcoming system maintenance this weekend', date: new Date(), link: '#' },
  { title: 'How to set up your profile for better collaboration', date: new Date(), link: '#' },
  { title: 'Join our community forum to share ideas', date: new Date(), link: '#' },
  { title: 'Introducing our new dashboard features', date: new Date(), link: '#' }
];

async function fetchRssFeed() {
  isLoading.value = true;
  error.value = null;
  
  try {
    // For development, we'll use sample data with a simulated delay
    await new Promise(resolve => setTimeout(resolve, 500));
    
    // Set the sample data as our feed items
    rssItems.value = sampleRssItems;
  } catch (err) {
    console.error('Failed to fetch RSS feed:', err);
    error.value = 'Could not load feed';
    
    // Fallback to static data if fetch fails
    rssItems.value = sampleRssItems;
  } finally {
    isLoading.value = false;
  }
}

onMounted(() => {
  fetchRssFeed();
});
</script>

<template>
  <div class="rss-container">
    <div v-if="isLoading" class="rss-loading">
      <div class="animate-pulse">Loading feed...</div>
    </div>
    <div v-else-if="error" class="rss-error">
      {{ error }}
    </div>
    <div v-else class="overflow-hidden relative h-full flex items-center">
      <div class="rss-gradient-left">
        <div class="rss-gradient-left-inner"></div>
      </div>
      
      <div class="animate-marquee whitespace-nowrap pl-8 pr-8">
        <template v-for="(item, index) in rssItems" :key="index">
          <a :href="item.link" class="rss-item">
            <span class="rss-item-indicator"></span>
            {{ item.title }}
          </a>
          <span v-if="index < rssItems.length - 1" class="rss-separator">•</span>
        </template>
      </div>
      
      <div class="rss-gradient-right">
        <div class="rss-gradient-right-inner"></div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.animate-marquee {
  animation: marquee 30s linear infinite;
}

@keyframes marquee {
  0% { transform: translateX(5%); }
  100% { transform: translateX(-100%); }
}

/* Pause animation on hover */
.animate-marquee:hover {
  animation-play-state: paused;
}
</style>