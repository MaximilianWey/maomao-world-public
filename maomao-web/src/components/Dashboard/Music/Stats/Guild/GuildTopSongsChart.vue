<script setup lang="ts">
import { ref, watch, computed } from 'vue';
import { useStatsStore } from '@/stores/statsStore.ts';
import { useGuildStore } from '@/stores/guildStore.ts';
import SongStatDTOChart from "@/components/Dashboard/Music/Stats/SongStatDTOChart.vue";
import type { SongStatDTO, Range } from '@/types/stats.ts';
import { RANGES } from '@/types/stats.ts';

const props = defineProps<{
  selectedRange: Range;
  from?: number;
  to?: number;
}>();

const statsStore = useStatsStore();
const guildStore = useGuildStore();
const guild = computed(() => guildStore.getCurrentGuild);

const allSongsByRange = ref<Record<Range, SongStatDTO[]>>({
  today: [],
  week: [],
  month: [],
  year: [],
});

async function fetchAllRanges() {
  if (!guild.value) return;

  const songsMap: Record<Range, SongStatDTO[]> = {
    today: [],
    week: [],
    month: [],
    year: [],
  };
  for (const range of RANGES) {
    await statsStore.fetchGuildTopTracks(guild.value.id, range, props.from ?? 0, props.to ?? 10);
    songsMap[range] = [...statsStore.guildTopTracks];
  }
  allSongsByRange.value = songsMap;
}

const title = computed(() => `Top Songs for ${guild.value?.name || 'Guild'}`);

watch(
    () => [props.from, props.to, guild.value?.id],
    () => {
      fetchAllRanges();
    },
    { immediate: true }
);
</script>

<template>
  <SongStatDTOChart
      :range="selectedRange"
      :all-songs-by-range="allSongsByRange"
      :title="title"
  />
</template>
