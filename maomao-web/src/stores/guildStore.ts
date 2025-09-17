import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { DISCORD_URLS } from '@/config.ts'
import { apiGet } from '@/services/authService'

export interface Guild {
    id: string
    name: string
    iconUrl?: string
}

export const useGuildStore = defineStore('guild', () => {
    const guilds = ref<Guild[]>([])
    const currentGuildId = ref<string | null>(null)
    const isLoading = ref(false)
    const error = ref<string | null>(null)

    // Fetch the guilds from the backend
    const fetchGuilds = async () => {
        isLoading.value = true
        error.value = null

        try {
            const response = await apiGet(DISCORD_URLS.getGuilds())
            guilds.value = response.data

            if (!currentGuildId.value && guilds.value.length > 0) {
                currentGuildId.value = guilds.value[0].id
            }

        } catch (err) {
            console.error('Failed to fetch guilds:', err)
            error.value = 'Failed to load guilds'
        } finally {
            isLoading.value = false
        }
    }

    const setCurrentGuild = (guildId: string) => {
        if (guilds.value.find(g => g.id === guildId)) {
            currentGuildId.value = guildId
        } else {
            console.warn(`Guild with ID ${guildId} not found.`)
        }
    }

    const getCurrentGuild = computed(() =>
        guilds.value.find(g => g.id === currentGuildId.value) || null
    )
    return {
        guilds,
        isLoading,
        error,
        fetchGuilds,
        setCurrentGuild,
        getCurrentGuild,
    }
}, {
    persist: true,
})
