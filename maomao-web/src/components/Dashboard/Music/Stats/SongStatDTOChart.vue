<template>
  <div>
    <div class="chart-wrapper bg-bg-secondary rounded-lg p-6 shadow-md max-w-4xl mx-auto">
      <canvas ref="chartRef"></canvas>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, watch } from 'vue';
import { Chart } from 'chart.js/auto';
import type { SongStatDTO, Range } from "@/types/stats.ts";
import type {CoreScaleOptions, Scale, Tick} from "chart.js/auto";

const props = defineProps<{
  range: Range;
  allSongsByRange: Record<Range, SongStatDTO[]>;
  title: string;
}>();

const chartRef = ref<HTMLCanvasElement | null>(null);
let chartInstance: Chart<"bar", (number | undefined)[], string> | null = null;

const sourceColors: Record<string, { base: string; hover: string }> = {
  youtube: {
    base: 'rgba(255, 102, 102, 0.6)',       // soft red
    hover: 'rgba(255, 102, 102, 0.85)',     // slightly more opaque on hover
  },
  spotify: {
    base: 'rgba(144, 238, 144, 0.6)',       // light green
    hover: 'rgba(144, 238, 144, 0.85)',
  },
  soundcloud: {
    base: 'rgba(255, 179, 102, 0.6)',       // soft orange
    hover: 'rgba(255, 179, 102, 0.85)',
  }
};

function padArray<T>(arr: T[], length: number, defaultValue: T): T[] {
  const copy = [...arr];
  while (copy.length < length) {
    copy.push(defaultValue);
  }
  return copy;
}


function prepareDataMaps(songsByRange: Record<Range, SongStatDTO[]>) {
  const playCountsMap = new Map<Range, number[]>();
  const labelsMap = new Map<Range, string[]>();
  const sourcesMap = new Map<Range, string[]>();

  const maxLength = Math.max(...Object.values(songsByRange).map(arr => arr.length));

  for (const [range, songs] of Object.entries(songsByRange) as [Range, SongStatDTO[]][]) {
    const playCounts = songs.map(s => s.playCount || 0);
    const labels = songs.map(s => s.song.title || 'Unknown');
    const sources = songs.map(s => s.song.source || 'youtube');

    playCountsMap.set(range, padArray(playCounts, maxLength, 0));
    labelsMap.set(range, padArray(labels, maxLength, ''));
    sourcesMap.set(range, padArray(sources, maxLength, 'youtube'));
  }

  return { playCountsMap, labelsMap, sourcesMap };
}

let playCountsMap = new Map<Range, number[]>();
let labelsMap = new Map<Range, string[]>();
let sourcesMap = new Map<Range, string[]>();

let currentSongs: SongStatDTO[] = [];

function createChart() {
  if (!chartRef.value) return;
  const ctx = chartRef.value.getContext('2d');
  if (!ctx) return;

  ({ playCountsMap, labelsMap, sourcesMap } = prepareDataMaps(props.allSongsByRange));

  currentSongs = props.allSongsByRange[props.range] || [];

  const sources = sourcesMap.get(props.range) || [];
  const backgroundColors = sources.map(source => sourceColors[source]?.base || 'rgba(100, 100, 100, 0.6)');
  const hoverColors = sources.map(source => sourceColors[source]?.hover || 'rgba(100, 100, 100, 0.9)');

  const data = {
    labels: labelsMap.get(props.range),
    datasets: [{
      label: props.range,
      data: playCountsMap.get(props.range),
      backgroundColor: backgroundColors,
      hoverBackgroundColor: hoverColors,
      borderWidth: 1
    }]
  };

  if (chartInstance) {
    chartInstance.destroy();
  }

  chartInstance = new Chart(ctx, {
    type: 'bar',
    data,
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
            title: () => props.range,
            label: (tooltipItem) => {
              const songTitle = tooltipItem.label;
              const playCount = tooltipItem.parsed.x;
              return `${songTitle}: ${playCount} plays`;
            }
          }
        }
      },
      scales: {
        x: {
          beginAtZero: true,
          max: getNiceMax(Math.max(...(playCountsMap.get(props.range) || [0])))
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

        if (!currentSongs[index]) return;

        const songUrl = currentSongs[index].song.url;
        if (songUrl) {
          window.open(songUrl, '_blank');
        }
      }
    }
  });
}

function updateChart() {
  if (!chartInstance) return;

  ({ playCountsMap, labelsMap, sourcesMap } = prepareDataMaps(props.allSongsByRange));

  currentSongs = props.allSongsByRange[props.range] || [];

  chartInstance.data.labels = labelsMap.get(props.range) || [];
  chartInstance.data.datasets[0].label = props.range;
  chartInstance.data.datasets[0].data = playCountsMap.get(props.range) || [];

  const sources = sourcesMap.get(props.range) || [];
  chartInstance.data.datasets[0].backgroundColor = sources.map(source => sourceColors[source]?.base || 'rgba(100, 100, 100, 0.6)');
  chartInstance.data.datasets[0].hoverBackgroundColor = sources.map(source => sourceColors[source]?.hover || 'rgba(100, 100, 100, 0.9)');

  chartInstance.options.scales!.x!.max = getNiceMax(Math.max(...(playCountsMap.get(props.range) || [0])));

  chartInstance.update();
}


function getNiceMax(value: number): number {
  if (value <= 10) return 10;
  if (value <= 50) return Math.ceil(value / 10) * 10;
  if (value <= 100) return Math.ceil(value / 25) * 25;
  if (value <= 1000) return Math.ceil(value / 100) * 100;
  return Math.ceil(value / 500) * 500;
}

onMounted(() => {
  createChart();
});

watch(
    () => [props.range, props.allSongsByRange],
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
