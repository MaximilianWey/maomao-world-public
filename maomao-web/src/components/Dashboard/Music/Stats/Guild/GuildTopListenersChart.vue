<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useStatsStore } from '@/stores/statsStore.ts';
import { useGuildStore } from "@/stores/guildStore.ts";
import type { Range, CountStatDTO } from '@/types/stats.ts';
import { RANGES } from '@/types/stats.ts';
import type { DiscordUser } from "@/types/user.ts";

import PreciseStatDTOUserChart from "@/components/Dashboard/Music/Stats/PreciseStatDTOUserChart.vue";

const props = defineProps<{
  selectedRange: Range;
  from?: number;
  to?: number;
}>();

const guildStore = useGuildStore();
const statsStore = useStatsStore();

const guild = computed(() => guildStore.getCurrentGuild);

const allUsersByRange = ref<Record<Range, CountStatDTO<DiscordUser>[]>>({
  today: [],
  week: [],
  month: [],
  year: [],
});

async function fetchAllRanges() {
  if (!guild.value) return;

  const usersMap: Record<Range, CountStatDTO<DiscordUser>[]> = {} as Record<Range, CountStatDTO<DiscordUser>[]>;
  for (const range of RANGES) {
    await statsStore.fetchGuildTopListeners(guild.value.id, range, props.from ?? 0, props.to ?? 10);
    usersMap[range] = [...statsStore.guildTopListeners];
  }
  allUsersByRange.value = usersMap;
}

watch(
    () => [props.from, props.to, guild.value?.id],
    () => {
      fetchAllRanges();
    },
    { immediate: true }
);

const title = computed(() => `Top Listeners for ${guild.value?.name || 'Guild'}`);
</script>

<template>
  <PreciseStatDTOUserChart
      :title="title"
      :range="selectedRange"
      :all-users-by-range="allUsersByRange"
  />
</template>

<style scoped>
</style>
