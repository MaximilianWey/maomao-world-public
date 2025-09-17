<script setup lang="ts">
import { ref } from 'vue';
import { useUserStore } from '@/stores/userStore';
import SettingsSection from "@/components/Dashboard/Settings/SettingsSection.vue";
import EditableField from "@/components/Dashboard/Settings/EditableField.vue";
import AvatarEditor from "@/components/Dashboard/Settings/AvatarEditor.vue";

const userStore = useUserStore();

const user = ref(userStore.currentUser);
const displayName = ref(user.value?.displayName || 'not set');
const saveStatusName = ref<'idle' | 'saving' | 'saved' | 'error'>('idle');
const email = ref(user.value?.email || 'not set');

async function saveDisplayName(value: string) {
  saveStatusName.value = 'saving';
  console.log('Saving display name:', displayName.value);
  await userStore.updateProfile(undefined, undefined, value, undefined);
  saveStatusName.value = 'saved'
}

function uploadAvatar(file: File) {
  console.log('Avatar upload triggered with file:', file.name);
  //userStore.uploadAvatar(file);
}

</script>

<template>
  <SettingsSection
      id="profile"
      title="Profile"
      description="Update your personal details and avatar."
  >
    <div class="grid grid-cols-1 md:grid-cols-4 gap-8">
      <!-- Left side: Input fields (3/4) -->
      <div class="md:col-span-3">
        <EditableField
            v-model="displayName"
            label="Display Name"
            :placeholder="displayName"
            :editable="true"
            :save-status="saveStatusName"
            @save="saveDisplayName"
        />
        <EditableField
            v-model="email"
            label="Email"
            :placeholder="email"
            :editable="false"
        />
        <p class="text-xs text-muted mt-1">
          For username or email changes, try changing them in your corresponding social provider settings or contact an administrator.
        </p>
      </div>

      <!-- Right side: Avatar editor (1/4) -->
      <div class="flex items-center justify-center md:col-span-1">
        <AvatarEditor
            :avatar-url="userStore.avatarUrl"
            :show-ring="true"
            @upload="uploadAvatar"
        />
      </div>
    </div>
  </SettingsSection>
</template>

<style scoped>

</style>