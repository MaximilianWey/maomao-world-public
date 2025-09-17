<template>
  <div>
    <div class="chart-wrapper bg-bg-secondary rounded-lg p-6 shadow-md max-w-4xl mx-auto">
      <canvas ref="chartRef"></canvas>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, watch } from 'vue';
import {Chart, type CoreScaleOptions, type Scale, type Tick} from 'chart.js/auto';
import { getAverageColor, formatDuration } from '@/utils/statsUtils.ts';
import type { CountStatDTO, Range } from '@/types/stats';
import type { DiscordUser } from "@/types/user.ts";

const props = defineProps<{
  range: Range;
  allUsersByRange: Record<Range, CountStatDTO<DiscordUser>[]>;
  title: string;
}>();

const chartRef = ref<HTMLCanvasElement | null>(null);
let chartInstance: Chart | null = null;
let currentUsers: CountStatDTO<DiscordUser>[] = [];

function getNiceMax(value: number): number {
  if (value <= 10) return 10;
  if (value <= 50) return Math.ceil(value / 10) * 10;
  if (value <= 100) return Math.ceil(value / 25) * 25;
  if (value <= 1000) return Math.ceil(value / 100) * 100;
  return Math.ceil(value / 500) * 500;
}

async function createChart() {
  if (!chartRef.value) return;
  const ctx = chartRef.value.getContext('2d');
  if (!ctx) return;

  currentUsers = (props.allUsersByRange[props.range] || []).filter(
      d => d && d.subject && typeof d.subject === 'object'
  );

  const labels = currentUsers.map(d =>
      d?.subject?.username || d?.subject?.uniqueName || 'Unknown'
  );
  const values = currentUsers.map(d => d.count / 3600000); // ms → hours

  const backgroundColors = await Promise.all(
      currentUsers.map(d =>
          d.subject.avatarUrl
              ? getAverageColor(d.subject.avatarUrl)
              : Promise.resolve('rgba(100, 100, 100, 0.6)')
      )
  );

  if (chartInstance) chartInstance.destroy();

  chartInstance = new Chart(ctx, {
    type: 'bar',
    data: {
      labels,
      datasets: [{
        label: 'Users',
        data: values,
        backgroundColor: backgroundColors,
        borderWidth: 1,
      }]
    },
    options: {
      indexAxis: 'y',
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
        title: {
          display: true,
          text: props.title,
          font: {
            size: 18,
            weight: 'bold'
          },
          padding: {
            top: 10,
            bottom: 20
          }
        },
        tooltip: {
          callbacks: {
            title: (tooltipItems) => tooltipItems[0]?.label || '',
            label: (tooltipItem) => {
              const rawMs = currentUsers[tooltipItem.dataIndex]?.count ?? 0;
              return formatDuration(rawMs);
            }
          }
        }
      },
      scales: {
        x: {
          title: {
            display: true,
            text: 'Listening Time (hours)'
          },
          beginAtZero: true,
          max: getNiceMax(Math.max(...values, 0))
        },
        y: {
          type: 'category',
          ticks: {
            callback: function (
                this: Scale<CoreScaleOptions>,
                tickValue: string | number,
                index: number,
                ticks: Tick[]
            ): string {
              const label = String(tickValue);
              return label.length > 25 ? label.slice(0, 22) + '…' : label;
            },
            maxRotation: 0,
            autoSkip: false
          }
        }
      },
      onClick: (event, elements) => {
        if (!elements.length) return;
        const element = elements[0];
        const index = element.index;
        const user = currentUsers[index]?.subject;
        if (user) window.open(`/users/${user.id}`, '_blank');
      }
    }
  });
}

async function updateChart() {
  if (!chartInstance) return;

  currentUsers = (props.allUsersByRange[props.range] || []);

  const labels = currentUsers.map(d =>
      d?.subject?.username || d?.subject?.uniqueName || 'Unknown'
  );
  const values = currentUsers.map(d => d.count / 3600000); // ms → hours

  const backgroundColors = await Promise.all(
      currentUsers.map(d =>
          d.subject.avatarUrl
              ? getAverageColor(d.subject.avatarUrl)
              : Promise.resolve('rgba(100, 100, 100, 0.6)')
      )
  );

  console.log(backgroundColors);

  chartInstance.data.labels = labels;
  chartInstance.data.datasets[0].data = values;
  chartInstance.data.datasets[0].backgroundColor = backgroundColors;
  chartInstance.options.scales!.x!.max = getNiceMax(Math.max(...values, 0));

  chartInstance.update();
}

onMounted(() => {
  createChart();
});

watch(
    () => [props.range, props.allUsersByRange],
    () => {
      updateChart();
    },
    { deep: true }
);

watch(
    () => props.title,
    (newTitle) => {
      if (chartInstance) {
        chartInstance.options.plugins!.title!.text = newTitle;
        chartInstance.update();
      }
    }
);
</script>

<style scoped>
canvas {
  max-width: 100%;
}

.chart-wrapper {
  height: 500px;
  width: 100%;
  position: relative;
  background-color: var(--bg-secondary);
  border-radius: 0.5rem;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgb(0 0 0 / 0.1);
  max-width: 64rem;
  margin-left: auto;
  margin-right: auto;
}
</style>
