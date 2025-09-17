import { defineStore } from 'pinia'
import type { Visibility } from '@/types/visibility'
import type { Playlist } from '@/types/playlist'
import {
    getOwnPlaylists,
    getPublicPlaylists,
    getSubscribedPlaylists,
    getPlaylist,
    createPlaylist,
    deletePlaylist,
    addSongToPlaylist,
    removeSongFromPlaylist,
    updatePlaylistName,
    updatePlaylistVisibility,
    subscribeToPlaylist,
    unsubscribeFromPlaylist,
} from '@/services/playlistService'

export const usePlaylistStore = defineStore('playlist', {
    state: () => ({
        ownPlaylists: [] as Playlist[],
        publicPlaylists: [] as Playlist[],
        subscribedPlaylists: [] as Playlist[],
        currentPlaylist: null as Playlist | null,
        isLoading: false,
        error: null as string | null,
    }),

    actions: {
        async withLoading<T>(fn: () => Promise<T>): Promise<T | undefined> {
            this.isLoading = true
            try {
                const result = await fn()
                this.error = null
                return result
            } catch (err: any) {
                this.error = err.message || 'Something went wrong'
            } finally {
                this.isLoading = false
            }
        },

        async fetchPlaylistById(playlistId: string) {
            const res = await this.withLoading(() => getPlaylist(playlistId))
            if (res) this.currentPlaylist = res.data
        },

        async fetchOwnPlaylists() {
            const res = await this.withLoading(() => getOwnPlaylists())
            if (res) this.ownPlaylists = res.data
        },

        async fetchPublicPlaylists() {
            const res = await this.withLoading(() => getPublicPlaylists())
            if (res) this.publicPlaylists = res.data
        },

        async fetchSubscribedPlaylists() {
            const res = await this.withLoading(() => getSubscribedPlaylists())
            if (res) this.subscribedPlaylists = res.data
        },

        async createNewPlaylist(name: string, visibility: Visibility, songIds: string[]) {
            const res = await this.withLoading(() =>
                createPlaylist({ name, visibility, songIds })
            )
            if (res) {
                this.ownPlaylists.push(res.data)
                return res.data
            }
        },

        async deletePlaylistById(playlistId: string) {
            await this.withLoading(async () => {
                await deletePlaylist(playlistId)
                this.ownPlaylists = this.ownPlaylists.filter(p => p.id !== playlistId)
            })
        },

        async addSongToPlaylist(playlistId: string, songId: string) {
            const res = await this.withLoading(() => addSongToPlaylist(playlistId, songId))
            if (res) {
                const index = this.ownPlaylists.findIndex(p => p.id === playlistId)
                if (index !== -1) this.ownPlaylists[index] = res.data
            }
        },

        async removeSongFromPlaylist(playlistId: string, index: number) {
            const res = await this.withLoading(() => removeSongFromPlaylist(playlistId, index))
            if (res) {
                const idx = this.ownPlaylists.findIndex(p => p.id === playlistId)
                if (idx !== -1) this.ownPlaylists[idx] = res.data
            }
        },

        async updatePlaylistName(playlistId: string, name: string) {
            const res = await this.withLoading(() => updatePlaylistName(playlistId, name))
            if (res) {
                const index = this.ownPlaylists.findIndex(p => p.id === playlistId)
                if (index !== -1) this.ownPlaylists[index] = res.data
            }
        },

        async updatePlaylistVisibility(playlistId: string, visibility: Visibility) {
            const res = await this.withLoading(() => updatePlaylistVisibility(playlistId, visibility))
            if (res) {
                const index = this.ownPlaylists.findIndex(p => p.id === playlistId)
                if (index !== -1) this.ownPlaylists[index] = res.data
            }
        },

        async subscribe(playlistId: string) {
            await this.withLoading(async () => {
                await subscribeToPlaylist(playlistId)
                await this.fetchSubscribedPlaylists()
            })
        },

        async unsubscribe(playlistId: string) {
            await this.withLoading(async () => {
                await unsubscribeFromPlaylist(playlistId)
                await this.fetchSubscribedPlaylists()
            })
        }
    }
})
