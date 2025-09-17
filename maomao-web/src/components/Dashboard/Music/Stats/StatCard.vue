<template>
  <div class="stat-card">
    <div class="stat-title">{{ title }}</div>
    <div class="stat-value">
      {{ formattedValue }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { formatDuration } from '@/utils/statsUtils.ts';
import type { Range } from '@/types/stats.ts';

const props = defineProps<{
  title: string;
  value: Record<Range, number>;
  currentRange: Range;
  unit: 'ms' | 's' | 'min' | 'h' | 'count' | 'custom';
  customUnit?: string;
}>();

function formatCount(n: number): string {
  return n.toString().replace(/\B(?=(\d{3})+(?!\d))/g, "'");
}

const formattedValue = computed(() => {
  const val = props.value[props.currentRange] ?? 0;

  if (props.unit === 'count') return formatCount(val);
  if (props.unit === 'custom') return `${val} ${props.customUnit ?? ''}`;
  if (props.unit === 'ms') return formatDuration(val);
  if (props.unit === 's') return formatDuration(val * 1000);
  if (props.unit === 'min') return formatDuration(val * 60 * 1000);
  if (props.unit === 'h') return formatDuration(val * 60 * 60 * 1000);

  return val.toString();
});
</script>

<style scoped>
.stat-card {
  background-color: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 1rem;
  padding: 1.25rem 1.5rem;
  box-shadow: 0 4px 10px rgba(var(--bg-primary-rgb), 0.1);
  display: flex;
  flex-direction: column;
  justify-content: center;
  transition: background-color 0.3s ease;
}

.stat-title {
  font-size: 0.875rem;
  color: var(--text-muted);
  margin-bottom: 0.5rem;
  font-weight: 500;
  text-transform: uppercase;
}

.stat-value {
  font-size: 1.875rem;
  font-weight: 700;
  color: var(--text-primary);
}
</style>
