<script lang="ts" setup>
import { ref } from 'vue'
import { MagnifyingGlassIcon } from '@heroicons/vue/24/solid'

const query = ref('')
</script>

<template>
  <div class="w-full max-w-md">
    <div class="flex items-center w-full border-b-2 px-2 py-1" :style="{ borderColor: 'var(--accent-1)' }">
      <input
          id="search"
          type="text"
          v-model="query"
          @focus="$emit('focus')"
          @input="$emit('update:query', query)"
          placeholder="Type a song..."
          autocomplete="off"
          class="flex-1 focus:outline-none text-sm text-secondary"
      />
      <MagnifyingGlassIcon class="w-5 h-5 ml-2" :style="{ color: 'var(--accent-1)' }" />
    </div>
  </div>
</template>

<style scoped>

input {
  background-color: transparent;
  border: none;
}

</style>
