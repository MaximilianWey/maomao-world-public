<script setup lang="ts">
import SocialAccountCard from "@/components/Dashboard/Settings/SocialAccountCard.vue";
import { useAuthStore } from "@/stores/authStore.ts";
import { useUserStore } from "@/stores/userStore.ts";
import SettingsSection from "@/components/Dashboard/Settings/SettingsSection.vue";
import { onMounted, computed, ref } from "vue";
import {
  handleTokenRedirect,
  linkOauth2Provider,
  linkOidcProvider
} from "@/services/authProviderService.ts";
import type { AuthProviderMetadata } from "@/types/authProvider.ts";
import type { LinkedAccount } from "@/types/user.ts";
import SocialAccountInfoModal from "@/components/Dashboard/Settings/SocialAccountInfoModal.vue";

const authStore = useAuthStore();
const userStore = useUserStore();

// Modal state
const showInfoModal = ref(false);
const selectedAccount = ref<LinkedAccount | null>(null);
const selectedProvider = ref<AuthProviderMetadata | null>(null);

function showProviderInfo(providerId: string) {
  const provider = getProviderById(providerId);
  const account = linkedAccounts.value.find(acc => acc.providerId === providerId);
  if (!provider || !account) return;

  selectedProvider.value = provider;
  selectedAccount.value = account;
  showInfoModal.value = true;
}

function closeModal() {
  showInfoModal.value = false;
  selectedAccount.value = null;
  selectedProvider.value = null;
}

const linkedAccounts = computed(() => userStore.linkedAccounts);
const mainProviderId = computed(() => userStore.currentUser?.providerId);

const orderedProviders = computed(() => {
  return [...authStore.providers].sort((a, b) => {
    if (a.displayOrder !== b.displayOrder) {
      return a.displayOrder - b.displayOrder;
    }
    if (a.type === 'LOCAL' && b.type !== 'LOCAL') return -1;
    if (a.type !== 'LOCAL' && b.type === 'LOCAL') return 1;
    return a.providerName.localeCompare(b.providerName);
  });
});

const filteredProviders = computed(() =>
    orderedProviders.value.filter(
        (provider) =>
            provider.enabled
    )
);

onMounted(async () => {
  await authStore.loadProviders();
  handleTokenRedirect();
})

function connectProvider(providerId: string) {
  const provider = getProviderById(providerId);
  if (!provider) {
    console.error(`Provider with ID ${providerId} not found`);
    return;
  }
  if (provider.type === 'LOCAL') {
    console.warn('Local provider cannot be connected through this interface.');
    return;
  }
  if (linkedAccounts.value.some(acc => acc.providerId === providerId)) {
    console.warn(`Already connected to provider: ${providerId}`);
    return;
  }
  if (providerId === mainProviderId.value) {
    console.warn(`Provider ${providerId} is already set as the main provider.`);
    return;
  }
  if (provider.type === 'OIDC') {
    console.log(`Connecting to OIDC provider: ${providerId}`);
    linkOidcProvider(providerId);
  }
  if (provider.type === 'OAUTH2') {
    console.log(`Connecting to OAuth2 provider: ${providerId}`);
    linkOauth2Provider(providerId);
  } else {
    console.warn(`Unsupported provider type for connection: ${provider.type}`);
  }
}

function getProviderById(providerId: string) : AuthProviderMetadata | undefined {
  return authStore.providers.find(provider => provider.id === providerId);
}

function disconnectProvider(providerId: string) {
  userStore.disconnectAccount(providerId);
}

function updateAvatar(url: string) {
  userStore.updateProfile(undefined, undefined, undefined, url);
}

</script>

<template>
  <SettingsSection
      id="social-accounts"
      title="Social Accounts"
      description="Update your linked social accounts."
  >
    <SocialAccountInfoModal
        v-if="selectedAccount && selectedProvider"
        :visible="showInfoModal"
        :account="selectedAccount"
        :provider="selectedProvider"
        @close="closeModal"
        @disconnect="disconnectProvider"
        @update-avatar="updateAvatar"
    />
    <div class="flex flex-wrap gap-6">
      <SocialAccountCard
          v-for="provider in filteredProviders"
          :key="provider.id"
          :provider="provider"
          :is-connected="linkedAccounts.some(acc => acc.providerId === provider.id)"
          :is-main="mainProviderId === provider.id"
          @connect="connectProvider"
          @disconnect="disconnectProvider"
          @info="showProviderInfo"
      />
    </div>
  </SettingsSection>
</template>

<style scoped>

</style>