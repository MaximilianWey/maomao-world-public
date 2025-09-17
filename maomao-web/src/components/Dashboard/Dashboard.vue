<script setup lang="ts">

</script>

<template>
  <div class="dashboard">
    <div class="status-message">
      <p><strong>Alpha Notice:</strong> This project is currently in <strong>alpha</strong>.</p>
      <p>Expect bugs, instability, and potential data loss. Features are actively being developed and may change at any time.</p>
      <p>Thanks for your patience!</p>
    </div>
  </div>
</template>

<style scoped>
.dashboard {
  padding: 2rem;
  font-family: sans-serif;
}

.status-message {
  margin-top: 1rem;
  background-color: #fffbe6;
  border: 1px solid #ffe58f;
  padding: 1rem;
  border-radius: 8px;
  color: #ad6800;
  line-height: 1.5;
}

.status-message strong {
  color: #d48806;
}
</style>