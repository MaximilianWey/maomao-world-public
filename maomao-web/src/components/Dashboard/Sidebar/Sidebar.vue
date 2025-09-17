<script setup lang="ts">
import AccountToggle from "./AccountToggle.vue";
import RouteSelect from "./RouteSelect.vue";
import Footer from "./Footer.vue";
</script>

<template>
  <aside class="sticky top-4 h-[calc(100vh-32px)] flex flex-col w-[220px]">
    <div class="flex-1 overflow-y-auto scrollbar-hide">
      <AccountToggle />
      <RouteSelect />
    </div>
    <div>
      <Footer />
    </div>
  </aside>
</template>

<style scoped>

</style>