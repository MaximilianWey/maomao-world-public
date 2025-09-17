<script setup lang="ts">
import { ref, computed } from 'vue';
import { useGuildStore } from "@/stores/guildStore.ts";
import type { Range } from "@/types/stats";
import RangeSelector from "@/components/Dashboard/Music/Stats/RangeSelector.vue";
import TopSongsChart from "@/components/Dashboard/Music/Stats/Global/GlobalTopSongsChart.vue";
import GlobalTopListenersChart from "@/components/Dashboard/Music/Stats/Global/GlobalTopListenersChart.vue";
import GuildTopSongsChart from "@/components/Dashboard/Music/Stats/Guild/GuildTopSongsChart.vue";
import GuildTopListenersChart from "@/components/Dashboard/Music/Stats/Guild/GuildTopListenersChart.vue";
import AverageSessionLengthCard from "@/components/Dashboard/Music/Stats/User/AverageSessionLengthCard.vue";
import MedianSessionLengthCard from "@/components/Dashboard/Music/Stats/User/MedianSessionLengthCard.vue";
import TotalSessionLengthCard from "@/components/Dashboard/Music/Stats/User/TotalSessionLengthCard.vue";

const selectedRange = ref<Range>('week');
const guildStore = useGuildStore();
const guildId = computed(() => guildStore.getCurrentGuild?.id);
</script>

<template>
  <!-- Outer Container -->
  <div class="max-w-[1000px] mx-auto px-4 pb-6">

    <!-- Range Selector -->
    <RangeSelector v-model="selectedRange" />

    <!-- User Stats Section -->
    <section class="mt-6">
      <h2 class="text-xl font-semibold mb-4">Your Stats</h2>
      <div class="grid grid-cols-3 gap-2">
        <AverageSessionLengthCard
            class="w-full"
            :selected-range="selectedRange"
        />
        <MedianSessionLengthCard
            class="w-full"
            :selected-range="selectedRange"
        />
        <TotalSessionLengthCard
            class="w-full"
            :selected-range="selectedRange"
        />
      </div>
    </section>

    <!-- Global Stats Section -->
    <section class="mt-10">
      <h2 class="text-xl font-semibold mb-4">Global Stats</h2>
      <TopSongsChart :selected-range="selectedRange" />
      <div class="mt-6">
        <GlobalTopListenersChart :selected-range="selectedRange" />
      </div>
    </section>

    <!-- Guild Stats Section -->
    <section class="mt-10">
      <h2 class="text-xl font-semibold mb-4">Guild Stats</h2>
      <GuildTopSongsChart :selected-range="selectedRange" />
      <div class="mt-6">
        <GuildTopListenersChart :selected-range="selectedRange" />
      </div>
    </section>

  </div>
</template>
