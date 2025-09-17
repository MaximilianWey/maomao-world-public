<template>
  <p class="text-center text-sm mb-4 text-auth-text">Sign in with</p>
  <div class="space-y-2">
    <button
        v-for="provider in filteredProviders"
        :key="provider.id"
        @click="onProviderClick(provider)"
        class="auth-provider-button"
    >
      <img
          v-if="provider.logoUrl"
          :src="provider.logoUrl"
          alt=""
          class="h-5 w-5 mr-2"
      />
      <span>Sign in with {{ provider.providerName }}</span>
    </button>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import type { AuthProviderMetadata } from '@/types/authProvider';
import {
  handleTokenRedirect, loginWithOauth2Provider,
  loginWithOidcProvider,
  loginWithSamlProvider
} from '@/services/authProviderService';

const router = useRouter();
const authStore = useAuthStore();

onMounted(async () => {
  const token = handleTokenRedirect();
  if (token) {
    await router.push('/login');
  }
});

onMounted(async () => {
  await authStore.loadProviders();
});

const orderedProviders = computed(() : AuthProviderMetadata[] => {
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
            provider.enabled && provider.type !== 'LOCAL' && provider.type !== 'LDAP'
    )
);

function onProviderClick(provider: AuthProviderMetadata) {
  if (provider.type === 'OIDC') {
    loginWithOidcProvider(provider.id);
  } else if (provider.type === 'OAUTH2') {
    loginWithOauth2Provider(provider.id);
  } else if (provider.type === 'SAML') {
    loginWithSamlProvider(provider.id);
  } else {
    console.warn(`Unsupported provider type: ${provider.type}`);
  }
}
</script>

<style scoped>
</style>