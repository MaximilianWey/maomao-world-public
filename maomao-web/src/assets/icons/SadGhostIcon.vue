<script setup lang="ts">
</script>

<template>
  <svg
      xmlns="http://www.w3.org/2000/svg"
      viewBox="0 0 64 64"
      fill="none"
      class="animate-float w-32 h-32 mx-auto mb-6 text-primary"
      aria-hidden="true"
  >
    <!-- Ghost body -->
    <path
        d="M32 6c-12.15 0-22 9.85-22 22v20c0 2.21 2.69 3.3 4.24 1.76L18 48l3.76 3.76c1.56 1.56 4.1 1.56 5.66 0L32 48l4.58 3.76c1.56 1.56 4.1 1.56 5.66 0L46 48l3.76 1.76C51.31 51.3 54 50.21 54 48V28c0-12.15-9.85-22-22-22z"
        class="ghost-body"
    />
    <!-- Eyes -->
    <circle cx="24" cy="28" r="2.2" class="ghost-face" />
    <circle cx="40" cy="28" r="2.2" class="ghost-face" />
    <!-- Sad mouth -->
    <path d="M24 40c2 -2 6 -2 8 0" class="ghost-face" stroke-width="2" stroke-linecap="round"/>

    <ellipse
        cx="32"
        cy="58"
        rx="12"
        ry="4"
        class="ghost-hover-elipse opacity-20"
    >
      <animate attributeName="opacity" values="0.2;0.05;0.2" dur="3s" repeatCount="indefinite" />
    </ellipse>

  </svg>
</template>

<script lang="ts">
export default {
  name: 'SadGhostIcon'
}
</script>

<style scoped>
@keyframes float {
  0% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
  100% { transform: translateY(0); }
}

.animate-float {
  animation: float 3s ease-in-out infinite;
}

.ghost-face {
  fill: var(--ghost-face);
}

.ghost-body {
  fill: var(--ghost-body);
}

.ghost-hover-elipse {
  fill: var(--ghost-hover-elipse);
}

.dark .ghost-face {
  fill: var(--ghost-face);
}

.dark .ghost-body {
  fill: var(--ghost-body);
}

.dark .ghost-hover-elipse {
  fill: var(--ghost-hover-elipse);
}



</style>
