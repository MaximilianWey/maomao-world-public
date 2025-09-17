<script setup lang="ts">
import {computed, ref, watch} from 'vue';

const props = defineProps<{
  count: number
  modelValue?: number
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', index: number): void
}>()

const currentIndex = ref(props.modelValue ?? 0)

watch(() => props.modelValue, (val) => {
  if (val !== undefined) currentIndex.value = val
})

const next = () => {
  currentIndex.value = (currentIndex.value + 1) % props.count
  emit('update:modelValue', currentIndex.value)
}

const prev = () => {
  currentIndex.value = (currentIndex.value - 1 + props.count) % props.count
  emit('update:modelValue', currentIndex.value)
}

let isDragging = false
let dragStartX = 0

const startDrag = (e: MouseEvent) => {
  isDragging = true
  dragStartX = e.clientX
}

const onDrag = (e: MouseEvent) => {
  if (!isDragging) return
  const deltaX = e.clientX - dragStartX
  if (Math.abs(deltaX) > 50) {
    deltaX > 0 ? prev() : next()
    isDragging = false
  }
}

const endDrag = () => {
  isDragging = false
}

const hasPrev = computed(() => currentIndex.value > 0)
const hasNext = computed(() => currentIndex.value < props.count - 1)

</script>

<template>
  <div class="carousel">
    <!-- Always render buttons, but hide/inactivate based on computed state -->
    <button
        class="arrow left"
        :class="{ hidden: !hasPrev }"
        @click="prev"
    >&lt;</button>

    <div
        class="carousel-container"
        @mousedown="startDrag"
        @mousemove="onDrag"
        @mouseup="endDrag"
        @mouseleave="endDrag"
    >
      <div class="carousel-track" :style="{ transform: `translateX(-${currentIndex * 100}%)` }">
        <div v-for="i in count" :key="i" class="carousel-item">
          <slot :index="i - 1" />
        </div>
      </div>
    </div>

    <button
        class="arrow right"
        :class="{ hidden: !hasNext }"
        @click="next"
    >&gt;</button>
  </div>
</template>


<style scoped>
.carousel {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.carousel-container {
  overflow: hidden;
  position: relative;
}

.carousel-track {
  display: flex;
  transition: transform 0.3s ease;
}

.carousel-item {
  min-width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.arrow {
  border: none;
  border-radius: 50%;
  cursor: pointer;
  user-select: none;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  color: var(--nav-btn-text-unselected);
  transition: background-color 0.3s ease, color 0.3s ease;
  padding: 22px;
}

.arrow:hover {
  color: var(--nav-btn-text-selected);
  background-color: var(--nav-btn-bg-hover);
}

.arrow.hidden {
  visibility: hidden;
  pointer-events: none;
}

</style>