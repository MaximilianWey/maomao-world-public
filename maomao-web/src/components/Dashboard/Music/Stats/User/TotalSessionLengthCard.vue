<script setup lang="ts">
import { watch, onMounted, ref } from 'vue';
import StatCard from '@/components/Dashboard/Music/Stats/StatCard.vue';
import { useStatsStore } from '@/stores/statsStore.ts';
import { useUserStore } from '@/stores/userStore.ts';
import type { Range } from '@/types/stats.ts';
import { RANGES } from '@/types/stats.ts';

const props = defineProps<{
  selectedRange: Range;
  from?: number;
  to?: number;
}>();

const statsStore = useStatsStore();
const userStore = useUserStore();
const userId = userStore.discordAccount?.externalId;

const allSessionLengths = ref<Record<Range, number>>({} as Record<Range, number>);

async function fetchAllRanges() {
  if (!userId) return;
  const sessionLengthsMap: Record<Range, number> = {} as Record<Range, number>;

  for (const range of RANGES) {
    await statsStore.fetchUserTotalSessionLength(userId, range);
    sessionLengthsMap[range] = statsStore.userTotalSessionLength?.value ?? 0;
  }

  allSessionLengths.value = sessionLengthsMap;
}

onMounted(() => {
  fetchAllRanges();
});

watch(
    () => [props.from, props.to],
    () => {
      fetchAllRanges();
    }
);
</script>

<template>
  <StatCard
      title="Total Session Length"
      :value="allSessionLengths"
      :currentRange="selectedRange"
      unit="ms"
  />
</template>

<style scoped></style>
