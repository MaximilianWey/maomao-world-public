import { apiGet, apiPost, apiDelete } from './authService'
import { DISCORD_URLS } from '@/config'

export const createPlaylist = async (data: {
    name: string,
    visibility: 'PUBLIC' | 'FRIENDS' | 'PRIVATE',
    songIds: string[]
}) => {
    return await apiPost(DISCORD_URLS.playlist.create(), data)
}

export const getPublicPlaylists = async () => {
    return await apiGet(DISCORD_URLS.playlist.listPublic())
}

export const getOwnPlaylists = async () => {
    return await apiGet(DISCORD_URLS.playlist.listOwn())
}

export const getSubscribedPlaylists = async () => {
    return await apiGet(DISCORD_URLS.playlist.listSubscribed())
}

export const getPlaylist = async (playlistId: string) => {
    return await apiGet(DISCORD_URLS.playlist.get(playlistId))
}

export const deletePlaylist = async (playlistId: string) => {
    return await apiDelete(DISCORD_URLS.playlist.delete(playlistId))
}

export const addSongToPlaylist = async (playlistId: string, songId: string) => {
    return await apiPost(DISCORD_URLS.playlist.addSong(playlistId, songId), null)
}

export const removeSongFromPlaylist = async (playlistId: string, index: number) => {
    return await apiPost(DISCORD_URLS.playlist.removeSong(playlistId, index), null)
}

export const updatePlaylistVisibility = async (playlistId: string, visibility: 'PUBLIC' | 'FRIENDS' | 'PRIVATE') => {
    return await apiPost(DISCORD_URLS.playlist.updateVisibility(playlistId, visibility), null)
}

export const updatePlaylistName = async (playlistId: string, name: string) => {
    return await apiPost(DISCORD_URLS.playlist.updateName(playlistId, name), null)
}

export const subscribeToPlaylist = async (playlistId: string) => {
    return await apiPost(DISCORD_URLS.playlist.subscribe(playlistId), null)
}

export const unsubscribeFromPlaylist = async (playlistId: string) => {
    return await apiPost(DISCORD_URLS.playlist.unsubscribe(playlistId), null)
}
