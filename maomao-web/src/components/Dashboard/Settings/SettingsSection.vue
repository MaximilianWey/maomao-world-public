<script setup lang="ts">
defineProps<{
  id: string
  title: string;
  description: string;
}>();
</script>

<template>
  <section :id="id" class="card p-6 w-full mb-8">
    <div>
      <h2 class="text-xl font-semibold text-primary">{{ title }}</h2>
      <p class="text-muted text-sm -mt-1">{{ description }}</p>
    </div>
    <hr class="my-4 border-mute" />
    <div>
      <slot />
    </div>
  </section>
</template>