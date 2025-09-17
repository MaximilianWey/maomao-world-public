<script setup lang="ts">

</script>

<template>
  <div class="no-song-placeholder">
    <p>No song is currently playing. Add something to the queue!</p>
  </div>
</template>

<style scoped>
.no-song-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  width: 100%;
  max-width: 600px;
  min-height: 120px;

  padding: 1rem;
  border-radius: 0.5rem;
  font-style: italic;
  color: var(--text-secondary, #666);
  text-align: center;
}

</style>
