<script setup lang="ts">
import { ref, watch } from 'vue';
import SongStatDTOChart from "@/components/Dashboard/Music/Stats/SongStatDTOChart.vue";
import { useStatsStore } from '@/stores/statsStore.ts';
import type { SongStatDTO, Range } from '@/types/stats.ts';
import { RANGES } from '@/types/stats.ts';

const props = defineProps<{
  selectedRange: Range;
  from?: number;
  to?: number;
}>();

const statsStore = useStatsStore();

const allSongsByRange = ref<Record<Range, SongStatDTO[]>>({} as Record<Range, SongStatDTO[]>);

async function fetchAllRanges() {
  const songsMap: Record<Range, SongStatDTO[]> = {} as Record<Range, SongStatDTO[]>;
  for (const range of RANGES) {
    await statsStore.fetchGlobalTopTracks(range, props.from ?? 0, props.to ?? 10);
    songsMap[range] = [...statsStore.globalTopTracks];
  }
  allSongsByRange.value = songsMap;
}

watch(
    () => [props.from, props.to],
    () => {
      fetchAllRanges();
    },
    { immediate: true }
);

</script>

<template>
  <SongStatDTOChart
      title="Global Top Songs"
      :range="selectedRange"
      :allSongsByRange="allSongsByRange" />
</template>
