import {apiGet, apiPost} from './authService'
import {DISCORD_URLS} from "@/config.ts";

export const getQueue = async (guildId: string) => {
    try {
        const response = await apiGet(DISCORD_URLS.getQueue(guildId))
        return response.data
    } catch (err) {
        console.error('Failed to fetch queue:', err)
        throw new Error('Failed to load queue')
    }
}

export const skipTrack = async (guildId: string) => {
    try {
        return await apiPost(DISCORD_URLS.skipTrack(guildId), null)
    } catch (err) {
        console.error('Failed to skip track:', err)
        throw new Error('Failed to skip track')
    }
}

export const previousTrack = async (guildId: string) => {
    try {
        return await apiPost(DISCORD_URLS.previousTrack(guildId), null)
    } catch (err) {
        console.error('Failed to play previous track:', err)
        throw new Error('Failed to play previous track')
    }
}

export const reorderQueue = async (guildId: string, queue: any) => {
    try {
        const response = await apiPost(DISCORD_URLS.reorderQueue(guildId), queue)
        return response.data
    } catch (err) {
        console.error('Failed to reorder queue:', err)
        throw new Error('Failed to reorder queue')
    }
}

export const shuffleQueue = async (guildId: string) => {
    try {
        const response = await apiPost(DISCORD_URLS.shuffleQueue(guildId), null)
        return response.data
    } catch (err) {
        console.error('Failed to shuffle queue:', err)
        throw new Error('Failed to shuffle queue')
    }
}

export const pausePlayback = async (guildId: string) => {
    try {
        return await apiPost(DISCORD_URLS.pausePlayback(guildId), null)
    } catch (err) {
        console.error('Failed to pause playback:', err)
        throw new Error('Failed to pause playback')
    }
}

export const resumePlayback = async (guildId: string) => {
    try {
        return await apiPost(DISCORD_URLS.resumePlayback(guildId), null)
    } catch (err) {
        console.error('Failed to resume playback:', err)
        throw new Error('Failed to resume playback')
    }
}

export const setMode = async (guildId: string, mode: string) => {
    try {
        const response = await apiPost(DISCORD_URLS.setMode(guildId, mode), null)
        return response.data
    } catch (err) {
        console.error('Failed to set mode:', err)
        throw new Error('Failed to set mode')
    }
}

export const searchTracks = async (guildId: string, query: string, source?: string) => {
    try {
        const response = await apiGet(DISCORD_URLS.searchTracks(guildId, query, source))
        return response.data
    } catch (err) {
        console.error('Failed to search tracks:', err)
        throw new Error('Failed to search tracks')
    }
}

export const playTrackNext = async (guildId: string, query: string, channelId: string) => {
    try {
        const response = await apiPost(DISCORD_URLS.playTrackNext(guildId, query, channelId), null)
        return response.data
    } catch (err) {
        console.error('Failed to play track next:', err)
        throw new Error('Failed to play track next')
    }
}

export const enqueueTrack = async (guildId: string, trackId: string, channelId: string) => {
    try {
        const response = await apiPost(DISCORD_URLS.enqueueTrack(guildId, trackId, channelId), null)
        return response.data
    } catch (err) {
        console.error('Failed to enqueue track:', err)
        throw new Error('Failed to enqueue track')
    }
}

export const removeTrackFromQueue = async (guildId: string, index: number) => {
    try {
        const response = await apiPost(DISCORD_URLS.removeTrackFromQueue(guildId, index), null)
        return response.data
    } catch (err) {
        console.error('Failed to remove track from queue:', err)
        throw new Error('Failed to remove track from queue')
    }
}

export const enqueuePlaylist = async (guildId: string, playlistId: string, channelId: string, shuffle: boolean) => {
    try {
        const response = await apiPost(DISCORD_URLS.enqueuePlaylist(guildId, playlistId, channelId, shuffle), null)
        return response.data
    } catch (err) {
        console.error('Failed to enqueue playlist:', err)
        throw new Error('Failed to enqueue playlist')
    }
}

export const clearQueue = async (guildId: string) => {
    try {
        const response = await apiPost(DISCORD_URLS.clearQueue(guildId), null)
        return response.data
    } catch (err) {
        console.error('Failed to clear queue:', err)
        throw new Error('Failed to clear queue')
    }
}