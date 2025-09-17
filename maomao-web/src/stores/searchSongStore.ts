// src/stores/search.ts
import {defineStore} from 'pinia'
import {searchTracks} from '@/services/musicService.ts'
import type {Song} from '@/stores/songStore.ts'
import {useGuildStore} from '@/stores/guildStore.ts'

export const useSearchStore = defineStore('search', {
    state: () => ({
        query: '',
        results: [] as Song[],
        loading: false,
        error: null as string | null,
    }),

    actions: {
        async performSearch(query: string) {
            const guildStore = useGuildStore()
            const guild = guildStore.getCurrentGuild

            if (!guild || !guild.id) {
                this.error = 'No guild selected'
                return
            }

            this.loading = true
            this.query = query
            this.error = null

            try {
                this.results = await searchTracks(guild.id, query)
            } catch (err) {
                this.error = 'Failed to search'
                this.results = []
                console.error(err)
            } finally {
                this.loading = false
            }
        },

        clear() {
            this.query = ''
            this.results = []
            this.error = null
        }
    }
})
