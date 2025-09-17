<script setup lang="ts">
import EditableField from '@/components/Dashboard/Settings/EditableField.vue';
import { computed } from 'vue';
import type { AuthProviderMetadata } from '@/types/authProvider.ts';
import type { LinkedAccount } from '@/types/user.ts';
import AvatarWithAction from "@/components/AvatarWithAction.vue";


const props = defineProps<{
  visible: boolean;
  account: LinkedAccount;
  provider: AuthProviderMetadata;
}>()

const emit = defineEmits(['close', 'disconnect', 'update-avatar']);

function onMakeDefault(url: string) {
  emit('update-avatar', url);
}

function onDisconnect() {
  emit('disconnect', props.account.id);
  emit('close');
}

const accountFields = computed(() => [
  {
    label: 'Preferred Name',
    value: props.account.preferredName,
  },
  {
    label: 'Email',
    value: props.account.email || '',
  },
  {
    label: 'External ID',
    value: props.account.externalId,
  },
  {
    label: 'Linked At',
    value: new Date(props.account.linkedAt).toLocaleString(),
  },
]);
</script>

<template>
  <div v-if="visible" class="modal-overlay" @click.self="emit('close')">
    <div class="modal-content relative">

      <!-- Close Button -->
      <button
          class="absolute top-3 right-3 text-muted hover:text-primary transition-colors exit-button"
          @click="emit('close')"
          aria-label="Close"
      >
        ✕
      </button>

      <!-- Provider Info -->
      <div class="flex items-center gap-3 mb-4">
        <img :src="provider.logoUrl" alt="Provider Logo" class="w-8 h-8 rounded" />
        <h2 class="text-lg font-semibold">{{ provider.providerName }}</h2>
      </div>

      <hr class= "border-mute mb-4" />

      <!-- Avatar (Top-right) -->
      <div class="flex justify-end mb-4 w-full max-w-md">
        <div
            v-if="account.avatarUrl"
            class="relative w-24 h-24"
        >
          <AvatarWithAction
              :avatar-url="account.avatarUrl"
              action-text="Make Default"
              @action="onMakeDefault"
          />
        </div>
      </div>

      <!-- Account Info Fields -->
      <div>
        <EditableField
            v-for="field in accountFields"
            :key="field.label"
            :label="field.label"
            :model-value="field.value"
            :editable="false"
        />
      </div>

      <!-- Disconnect Button -->
      <div class="flex justify-end">
        <button
            @click="onDisconnect"
            class="btn btn-disconnect"
        >
          Disconnect
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 50;
}

.modal-content {
  background: var(--bg-primary);
  padding: 2rem;
  border-radius: 0.75rem;
  width: 100%;
  max-width: 400px;
  overflow-y: auto;
  max-height: 80vh;
}

.exit-button {
  cursor: pointer;
  background: transparent;
  border: none;
  font-size: 1.25rem;
}

.btn {
  padding: 0.35rem 0.75rem;
  border-radius: 0.5rem;
  font-size: 1rem;
  font-weight: 500;
  border: none;
  cursor: pointer;
  transition: background-color 0.2s ease;
  min-width: 100px;
}

.btn-disconnect {
  background-color: #f87171;
  color: white;
}

.btn-disconnect:hover {
  background-color: #ef4444;
}

.dark .btn-disconnect {
  background-color: #f86e81;
}

.dark .btn-disconnect:hover {
  background-color: #f5485c;
}
</style>