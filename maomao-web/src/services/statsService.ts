// src/services/statsService.ts

// Change import to use apiGetTyped
import { apiGetTyped } from './authService' // <-- Changed this
import { DISCORD_URLS } from '@/config'
import type {
    SongStatDTO,
    TrackCountDTO,
    CountStatDTO,
    DailyStatDTO,
    HourStatDTO,
    PreciseStatDTO
} from '@/types/stats';
import type { Song } from '@/stores/songStore.ts';
import type { DiscordUser } from "@/types/user.ts";

// Global Stats
export const getGlobalTrackCount = async (range: string): Promise<TrackCountDTO<Song>> => {
    // Use apiGetTyped and specify the expected response type
    return await apiGetTyped<TrackCountDTO<Song>>(DISCORD_URLS.stats.global.trackCount(range));
}

export const getGlobalTopTracks = async (range: string, from: number, to: number): Promise<SongStatDTO[]> => {
    return await apiGetTyped<SongStatDTO[]>(DISCORD_URLS.stats.global.topTracks(range, from, to));
}

export const getGlobalTopListeners = async (range: string, from: number, to: number): Promise<CountStatDTO<DiscordUser>[]> => {
    return await apiGetTyped<CountStatDTO<DiscordUser>[]>(DISCORD_URLS.stats.global.topListeners(range, from, to));
}

export const getGlobalTopRequesters = async (range: string, limit?: number): Promise<CountStatDTO<DiscordUser>[]> => {
    return await apiGetTyped<CountStatDTO<DiscordUser>[]>(DISCORD_URLS.stats.global.topRequesters(range, limit));
}

export const getGlobalBotUptime = async (range: string): Promise<PreciseStatDTO<Date>> => {
    return await apiGetTyped<PreciseStatDTO<Date>>(DISCORD_URLS.stats.global.botUptime(range));
}

// Guild Stats
export const getGuildTrackCount = async (guildId: string, range: string): Promise<TrackCountDTO<Song>> => {
    return await apiGetTyped<TrackCountDTO<Song>>(DISCORD_URLS.stats.guild.trackCount(guildId, range));
}

export const getGuildTopTracks = async (guildId: string, range: string, from: number, to: number): Promise<SongStatDTO[]> => {
    return await apiGetTyped<SongStatDTO[]>(DISCORD_URLS.stats.guild.topTracks(guildId, range, from, to));
}

export const getGuildTopListeners = async (guildId: string, range: string, from: number, to: number): Promise<CountStatDTO<DiscordUser>[]> => {
    return await apiGetTyped<CountStatDTO<DiscordUser>[]>(DISCORD_URLS.stats.guild.topListeners(guildId, range, from, to));
}

export const getGuildTopRequesters = async (guildId: string, range: string, limit?: number): Promise<CountStatDTO<DiscordUser>[]> => {
    return await apiGetTyped<CountStatDTO<DiscordUser>[]>(DISCORD_URLS.stats.guild.topRequesters(guildId, range, limit));
}

export const getGuildDailySessionLength = async (guildId: string, date: string): Promise<DailyStatDTO> => {
    return await apiGetTyped<DailyStatDTO>(DISCORD_URLS.stats.guild.dailySessionLength(guildId, date));
}

export const getGuildDailySessionCount = async (guildId: string, date: string): Promise<DailyStatDTO> => {
    return await apiGetTyped<DailyStatDTO>(DISCORD_URLS.stats.guild.dailySessionCount(guildId, date));
}

export const getGuildSessionTimeByHour = async (guildId: string, date: string): Promise<HourStatDTO[]> => {
    return await apiGetTyped<HourStatDTO[]>(DISCORD_URLS.stats.guild.sessionTimeByHour(guildId, date));
}

export const getGuildUniqueTrackCount = async (guildId: string, range: string): Promise<TrackCountDTO<any>> => {
    return await apiGetTyped<TrackCountDTO<any>>(DISCORD_URLS.stats.guild.uniqueTrackCount(guildId, range));
}

// User Stats
export const getUserAverageSessionLength = async (userId: string, range: string): Promise<PreciseStatDTO<DiscordUser>> => {
    return await apiGetTyped<PreciseStatDTO<DiscordUser>>(DISCORD_URLS.stats.user.averageSessionLength(userId, range));
}

export const getUserMedianSessionLength = async (userId: string, range: string): Promise<PreciseStatDTO<DiscordUser>> => {
    return await apiGetTyped<PreciseStatDTO<DiscordUser>>(DISCORD_URLS.stats.user.medianSessionLength(userId, range));
}

export const getUserTotalSessionLength = async (userId: string, range: string): Promise<PreciseStatDTO<DiscordUser>> => {
    return await apiGetTyped<PreciseStatDTO<DiscordUser>>(DISCORD_URLS.stats.user.totalSessionLength(userId, range));
}