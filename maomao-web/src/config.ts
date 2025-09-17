export const AUTH_API_BASE = import.meta.env.VITE_AUTH_API_BASE;
export const DISCORD_BOT_API_BASE = import.meta.env.VITE_DISCORD_BOT_API_BASE;
const STATS_BASE_PATH = `${DISCORD_BOT_API_BASE}/discord/stats`;

export const AUTH_URLS = {
    checkUser: (identifier: string) =>
        `${AUTH_API_BASE}/auth/user/${encodeURIComponent(identifier)}`,
    signIn: () => `${AUTH_API_BASE}/auth/login`,
    providers: () => `${AUTH_API_BASE}/auth/providers`,
    oidcLogin: (providerId: string, redirectUri: string) => {
        return `${AUTH_API_BASE}/auth/login/oidc/${providerId}?redirect_uri=${encodeURIComponent(redirectUri)}`
    },
    oidcLink: (providerId: string, redirectUri: string) => {
        return `${AUTH_API_BASE}/user/link/oidc/${providerId}?redirect_uri=${encodeURIComponent(redirectUri)}`
    },
    oauth2Login: (providerId: string, redirectUri: string) => {
        return `${AUTH_API_BASE}/auth/login/oauth2/${providerId}?redirect_uri=${encodeURIComponent(redirectUri)}`
    },
    oauth2Link: (providerId: string, redirectUri: string) => {
        return `${AUTH_API_BASE}/user/link/oauth2/${providerId}?redirect_uri=${encodeURIComponent(redirectUri)}`
    },
    samlLogin: (providerId: string, redirectUri: string) => {
        return `${AUTH_API_BASE}/auth/login/saml/${providerId}?redirect_uri=${encodeURIComponent(redirectUri)}`
    },
    disconnectLinkedAccount: (providerId: string) => {
        return `${AUTH_API_BASE}/user/disconnect/${providerId}`
    },
};

export const USER_URLS = {
    me: () => `${AUTH_API_BASE}/user/me`,
    updateUserProfile: () => `${AUTH_API_BASE}/user/update/profile`
}

export const DISCORD_URLS = {
    getGuilds: () => `${DISCORD_BOT_API_BASE}/discord/guilds`,
    getCurrentVoiceChannelState: (guildId: string) =>
        `${DISCORD_BOT_API_BASE}/discord/${guildId}/current-voice-channel`,
    enqueueTrack: (guildId: string, trackId: string, channelId: string) => {
        return `${DISCORD_BOT_API_BASE}/discord/music/${guildId}/play?query=${encodeURIComponent(trackId)}&channelId=${encodeURIComponent(channelId)}`
    },
    getNowPlaying: (guildId: string) =>
        `${DISCORD_BOT_API_BASE}/discord/music/${guildId}/now-playing`,
    getQueue: (guildId: string) =>
        `${DISCORD_BOT_API_BASE}/discord/music/${guildId}/queue`,
    skipTrack: (guildId: string) =>
        `${DISCORD_BOT_API_BASE}/discord/music/${guildId}/skip`,
    previousTrack: (guildId: string) =>
        `${DISCORD_BOT_API_BASE}/discord/music/${guildId}/previous`,
    setIndex: (guildId: string, index: number) =>
        `${DISCORD_BOT_API_BASE}/discord/music/${guildId}/set-index/${index}`,
    reorderQueue: (guildId: string) =>
        `${DISCORD_BOT_API_BASE}/discord/music/${guildId}/reorder-queue`,
    shuffleQueue: (guildId: string) =>
        `${DISCORD_BOT_API_BASE}/discord/music/${guildId}/shuffle-queue`,
    pausePlayback: (guildId: string) =>
        `${DISCORD_BOT_API_BASE}/discord/music/${guildId}/pause`,
    resumePlayback: (guildId: string) =>
        `${DISCORD_BOT_API_BASE}/discord/music/${guildId}/resume`,
    setMode: (guildId: string, mode: string) =>
        `${DISCORD_BOT_API_BASE}/discord/music/${guildId}/set-mode/${mode}`,
    searchTracks: (guildId: string, query: string, source?: string) => {
        const base = `${DISCORD_BOT_API_BASE}/discord/music/${guildId}/search?query=${encodeURIComponent(query)}`;
        return source ? `${base}&source=${encodeURIComponent(source)}` : base;
    },
    playTrackNext: (guildId: string, query: string, channelId: string) => {
        return `${DISCORD_BOT_API_BASE}/discord/music/${guildId}/play-next?query=${encodeURIComponent(query)}&channelId=${encodeURIComponent(channelId)}`;
    },
    removeTrackFromQueue: (guildId: string, index: number) => {
        return `${DISCORD_BOT_API_BASE}/discord/music/${guildId}/remove-track?index=${index}`;
    },
    enqueuePlaylist: (guildId: string, playlistId: string, channelId: string, shuffle: boolean) => {
        return `${DISCORD_BOT_API_BASE}/discord/music/${guildId}/enqueue-playlist?playlistId=${encodeURIComponent(playlistId)}&channelId=${encodeURIComponent(channelId)}&shuffle=${encodeURIComponent(shuffle)}`;
    },
    clearQueue: (guildId: string) => {
        return `${DISCORD_BOT_API_BASE}/discord/music/${guildId}/clear-queue`;
    },
    playlist: {
        create: () => `${DISCORD_BOT_API_BASE}/playlist/create`,
        listPublic: () => `${DISCORD_BOT_API_BASE}/playlist/list-public`,
        listOwn: () => `${DISCORD_BOT_API_BASE}/playlist/list-own`,
        listSubscribed: () => `${DISCORD_BOT_API_BASE}/playlist/list-subscribed`,
        get: (playlistId: string) => `${DISCORD_BOT_API_BASE}/playlist/${playlistId}`,
        delete: (playlistId: string) => `${DISCORD_BOT_API_BASE}/playlist/${playlistId}`,
        addSong: (playlistId: string, songId: string) => `${DISCORD_BOT_API_BASE}/playlist/${playlistId}/add-song?songId=${encodeURIComponent(songId)}`,
        removeSong: (playlistId: string, index: number) => `${DISCORD_BOT_API_BASE}/playlist/${playlistId}/remove-song?index=${index}`,
        updateVisibility: (playlistId: string, visibility: string) => `${DISCORD_BOT_API_BASE}/playlist/${playlistId}/update-visibility?visibility=${encodeURIComponent(visibility)}`,
        updateName: (playlistId: string, name: string) => `${DISCORD_BOT_API_BASE}/playlist/${playlistId}/update-name?name=${encodeURIComponent(name)}`,
        subscribe: (playlistId: string) => `${DISCORD_BOT_API_BASE}/playlist/${playlistId}/subscribe`,
        unsubscribe: (playlistId: string) => `${DISCORD_BOT_API_BASE}/playlist/${playlistId}/unsubscribe`,
    },
    stats: {
        global: {
            trackCount: (range: string) => `${STATS_BASE_PATH}/global/track-count?range=${encodeURIComponent(range)}`,
            topTracks: (range: string, from: number, to: number) => `${STATS_BASE_PATH}/global/top-tracks?range=${encodeURIComponent(range)}&from=${from}&to=${to}`,
            topListeners: (range: string, from: number, to: number) => `${STATS_BASE_PATH}/global/top-listeners?range=${encodeURIComponent(range)}&from=${from}&to=${to}`,
            topRequesters: (range: string, limit?: number) => {
                let url = `${STATS_BASE_PATH}/global/top-requesters?range=${encodeURIComponent(range)}`;
                if (limit !== undefined) {
                    url += `&limit=${limit}`;
                }
                return url;
            },
            botUptime: (range: string) => `${STATS_BASE_PATH}/global/bot-uptime?range=${encodeURIComponent(range)}`,
        },
        guild: {
            trackCount: (guildId: string, range: string) => `${STATS_BASE_PATH}/${encodeURIComponent(guildId)}/track-count?range=${encodeURIComponent(range)}`,
            topTracks: (guildId: string, range: string, from: number, to: number) => `${STATS_BASE_PATH}/${encodeURIComponent(guildId)}/top-tracks?range=${encodeURIComponent(range)}&from=${from}&to=${to}`,
            topListeners: (guildId: string, range: string, from: number, to: number) => `${STATS_BASE_PATH}/${encodeURIComponent(guildId)}/top-listeners?range=${encodeURIComponent(range)}&from=${from}&to=${to}`,
            topRequesters: (guildId: string, range: string, limit?: number) => {
                let url = `${STATS_BASE_PATH}/${encodeURIComponent(guildId)}/top-requesters?range=${encodeURIComponent(range)}`;
                if (limit !== undefined) {
                    url += `&limit=${limit}`;
                }
                return url;
            },
            dailySessionLength: (guildId: string, date: string) => `${STATS_BASE_PATH}/${encodeURIComponent(guildId)}/session-length?date=${encodeURIComponent(date)}`, // date is YYYY-MM-DD
            dailySessionCount: (guildId: string, date: string) => `${STATS_BASE_PATH}/${encodeURIComponent(guildId)}/session-count?date=${encodeURIComponent(date)}`, // date is YYYY-MM-DD
            sessionTimeByHour: (guildId: string, date: string) => `${STATS_BASE_PATH}/${encodeURIComponent(guildId)}/session-time-by-hour?date=${encodeURIComponent(date)}`, // date is YYYY-MM-DD
            uniqueTrackCount: (guildId: string, range: string) => `${STATS_BASE_PATH}/${encodeURIComponent(guildId)}/unique-track-count?range=${encodeURIComponent(range)}`,
        },
        user: {
            averageSessionLength: (userId: string, range: string) => `${STATS_BASE_PATH}/${encodeURIComponent(userId)}/avg-session-length?range=${encodeURIComponent(range)}`,
            medianSessionLength: (userId: string, range: string) => `${STATS_BASE_PATH}/${encodeURIComponent(userId)}/median-session-length?range=${encodeURIComponent(range)}`,
            totalSessionLength: (userId: string, range: string) => `${STATS_BASE_PATH}/${encodeURIComponent(userId)}/user-session-length?range=${encodeURIComponent(range)}`,
        }
    }
}