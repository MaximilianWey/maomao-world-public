<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import SearchBar from "@/components/Dashboard/Music/Banner/SongSearch/SearchBar.vue"
import SearchResultDropdownList from "@/components/Dashboard/Music/Banner/SongSearch/SearchResultDropdownList.vue"

const query = ref('')
const isFocused = ref(false)
const wrapperRef = ref<HTMLElement | null>(null)

const handleClickOutside = (event: MouseEvent) => {
  if (wrapperRef.value && !wrapperRef.value.contains(event.target as Node)) {
    isFocused.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<template>
  <div ref="wrapperRef" class="relative w-full">
    <SearchBar
        v-model:query="query"
        @focus="isFocused = true"
    />
    <SearchResultDropdownList v-if="query && isFocused" :query="query" />
  </div>
</template>
