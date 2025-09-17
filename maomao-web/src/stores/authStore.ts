// stores/auth.ts
import { ref } from 'vue'
import { defineStore } from 'pinia'
import type { AuthProviderMetadata } from '@/types/authProvider'
import { fetchAuthProviders } from '@/services/authProviderService'

export const useAuthStore = defineStore('auth', () => {
    const providers = ref<AuthProviderMetadata[]>([])
    const activeProviders = ref<AuthProviderMetadata[]>([])
    const isLoading = ref(false)
    const error = ref<string | null>(null)

    async function withLoading<T>(fn: () => Promise<T>): Promise<T | undefined> {
        isLoading.value = true
        try {
            const result = await fn()
            error.value = null
            return result
        } catch (err: any) {
            error.value = err.message || 'Something went wrong'
        } finally {
            isLoading.value = false
        }
    }

    async function loadProviders() {
        const res = await withLoading(() => fetchAuthProviders())
        if (res) {
            providers.value = res.data || res
            activeProviders.value = providers.value.filter(p => p.enabled)
        }
    }

    function getProviderByName(name: string) {
        return activeProviders.value.find(p =>
            p.providerName.toLowerCase().includes(name.toLowerCase())
        )
    }

    return {
        providers,
        activeProviders,
        isLoading,
        error,
        withLoading,
        loadProviders,
        getProviderByName,
    }
})
