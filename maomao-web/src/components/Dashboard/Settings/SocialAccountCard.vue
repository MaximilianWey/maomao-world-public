<script setup lang="ts">
import LocalProviderIcon from "@/assets/icons/LocalProviderIcon.vue";
import type { AuthProviderMetadata } from "@/types/authProvider.ts";

const props = defineProps<{
  provider: AuthProviderMetadata;
  isConnected: boolean;
  isMain: boolean;
}>();

const emit = defineEmits(['connect', 'disconnect', 'info']);

function handleConnect() {
  if (props.provider.type !== 'LOCAL') {
    emit('connect', props.provider.id);
  }
}

function handleInfo() {
  if (props.provider.type !== 'LOCAL') {
    emit('info', props.provider.id);
  }
}
</script>

<template>
  <div class="card">
    <!-- Connection Status Badge -->
    <span
        class="status-badge"
        :class="{
        'status-primary': isMain,
        'status-connected': !isMain && isConnected,
        'status-disconnected': !isMain && !isConnected
      }"
    >
      {{ isMain ? 'Primary' : isConnected ? 'Connected' : 'Not Connected' }}
    </span>

    <!-- Content block -->
    <div
        class="content"
        :class="{ clickable: provider.type !== 'LOCAL' && isConnected }"
        @click="handleInfo"
    >
      <template v-if="provider.logoUrl">
        <img
            :src="props.provider.logoUrl"
            :alt="props.provider.providerName"
            class="provider-logo"
        />
      </template>
      <template v-else-if="provider.type === 'LOCAL'">
        <LocalProviderIcon class="provider-logo" />
      </template>
      <h3 class="provider-name">{{ provider.providerName }}</h3>
    </div>

    <!-- Actions (always rendered) -->
    <div class="actions">
      <template v-if="!isMain">
        <button
            v-if="!isConnected"
            :disabled="props.provider.type === 'LOCAL'"
            @click="handleConnect"
            class="btn btn-connect"
            :title="props.provider.type === 'LOCAL' ? 'Cannot connect to local provider' : ''"
        >
          Connect
        </button>
      </template>
    </div>
  </div>
</template>

<style scoped>
.card {
  position: relative;
  width: 256px;
  height: 256px;
  border: 1px solid var(--border-color-darker);
  background-color: var(--bg-primary);
  border-radius: 1rem;
  padding: 1.25rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  transition: box-shadow 0.2s ease;
}

.card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.status-badge {
  position: absolute;
  top: 0.75rem;
  right: 0.75rem;
  font-size: 0.75rem;
  padding: 0.25rem 0.5rem;
  border-radius: 9999px;
  font-weight: 600;
}

.status-primary {
  background-color: #3b82f6;
  color: white;
}

.status-connected {
  background-color: #d1fae5;
  color: #065f46;
}

.status-disconnected {
  background-color: #fef2f2;
  color: #991b1b;
}

.content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  margin-top: 1rem;
}

.clickable {
  cursor: pointer;
  transition: transform 0.3s ease;
}

.clickable:hover {
  transform: scale(1.08);
}

.provider-logo {
  width: 64px;
  height: 64px;
  object-fit: contain;
  margin-bottom: 0.75rem;
}

.provider-name {
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 0.25rem;
}

.actions {
  height: 2.25rem;
  display: flex;
  align-items: center;
}

.btn {
  padding: 0.35rem 0.75rem;
  border-radius: 0.5rem;
  font-size: 0.8125rem;
  font-weight: 500;
  border: none;
  cursor: pointer;
  transition: background-color 0.2s ease;
  min-width: 100px;
}

.btn-connect {
  background-color: #7c8dfb;
  color: white;
}

.btn-connect:hover:not(:disabled) {
  background-color: #5d6ef9;
}

.btn-connect:disabled {
  background-color: #5d6ef9;
  cursor: not-allowed;
  opacity: 0.7;
}
</style>
