<script setup lang="ts">
import { ref, watch } from 'vue';
import { useStatsStore } from '@/stores/statsStore.ts';
import type { Range, CountStatDTO} from '@/types/stats.ts';
import { RANGES } from '@/types/stats.ts';
import PreciseStatDTOUserChart from "@/components/Dashboard/Music/Stats/PreciseStatDTOUserChart.vue";
import type {DiscordUser} from "@/types/user.ts";

const props = defineProps<{
  selectedRange: Range;
  from?: number;
  to?: number;
}>();

const statsStore = useStatsStore();

const allUsersByRange = ref<Record<Range, CountStatDTO<DiscordUser>[]>>({} as Record<Range, CountStatDTO<DiscordUser>[]>);

async function fetchAllRanges() {
  const usersMap: Record<Range, CountStatDTO<DiscordUser>[]> = {} as Record<Range, CountStatDTO<DiscordUser>[]>;
  for (const range of RANGES) {
    await statsStore.fetchGlobalTopListeners(range, props.from ?? 0, props.to ?? 10);
    usersMap[range] = [...statsStore.globalTopListeners];
  }
  allUsersByRange.value = usersMap;
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
  <PreciseStatDTOUserChart
      title="Global Top Listeners"
      :range="selectedRange"
      :all-users-by-range="allUsersByRange" />
</template>

<style scoped>

</style>