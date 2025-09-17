import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import {
    getGlobalTrackCount,
    getGlobalTopTracks,
    getGuildDailySessionLength,
    getGuildSessionTimeByHour,
    getUserTotalSessionLength,
    getGuildTrackCount,
    getGlobalTopListeners,
    getGlobalTopRequesters,
    getGlobalBotUptime,
    getGuildTopTracks,
    getGuildTopListeners,
    getGuildTopRequesters,
    getGuildDailySessionCount,
    getGuildUniqueTrackCount,
    getUserAverageSessionLength,
    getUserMedianSessionLength,
} from '@/services/statsService';
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

export const useStatsStore = defineStore('stats', () => {
    // --- State ---
    const isLoading = ref<boolean>(false);
    const error = ref<string | null>(null);

    // Global Stats
    const globalTrackCount = ref<TrackCountDTO<Song> | null>(null);
    const globalTopTracks = ref<SongStatDTO[]>([]);
    const globalTopListeners = ref<CountStatDTO<DiscordUser>[]>([]);
    const globalTopRequesters = ref<CountStatDTO<DiscordUser>[]>([]);
    const globalBotUptime = ref<PreciseStatDTO<Date> | null>(null); // todo: fix to return total time not average

    // Guild Stats
    const guildTrackCount = ref<TrackCountDTO<Song> | null>(null);
    const guildTopTracks = ref<SongStatDTO[]>([]);
    const guildTopListeners = ref<CountStatDTO<DiscordUser>[]>([]);
    const guildTopRequesters = ref<CountStatDTO<DiscordUser>[]>([]);
    const guildDailySessionLength = ref<DailyStatDTO | null>(null);
    const guildDailySessionCount = ref<DailyStatDTO | null>(null);
    const guildSessionTimeByHour = ref<HourStatDTO[]>([]);
    const guildUniqueTrackCount = ref<TrackCountDTO<any> | null>(null);


    // User Stats (these would typically be fetched based on a user's ID, maybe from an auth store)
    const userAverageSessionLength = ref<PreciseStatDTO<DiscordUser> | null>(null);
    const userMedianSessionLength = ref<PreciseStatDTO<DiscordUser> | null>(null);
    const userTotalSessionLength = ref<PreciseStatDTO<DiscordUser> | null>(null);


    // --- Actions ---

    // Generic error handler for actions
    const handleError = (err: any, context: string) => {
        console.error(`Failed to fetch ${context}:`, err);
        error.value = `Failed to load ${context}.`;
        isLoading.value = false;
        throw err;
    };

    // Global Stats Actions
    const fetchGlobalTrackCount = async (range: string) => {
        isLoading.value = true;
        error.value = null;
        try {
            globalTrackCount.value = await getGlobalTrackCount(range);
        } catch (err) {
            handleError(err, 'global track count');
        } finally {
            isLoading.value = false;
        }
    };

    const fetchGlobalTopTracks = async (range: string, from: number, to: number) => {
        isLoading.value = true;
        error.value = null;
        try {
            globalTopTracks.value = await getGlobalTopTracks(range, from, to);
        } catch (err) {
            handleError(err, 'global top tracks');
        } finally {
            isLoading.value = false;
        }
    };

    const fetchGlobalTopListeners = async (range: string, from: number, to: number) => {
        isLoading.value = true;
        error.value = null;
        try {
            globalTopListeners.value = await getGlobalTopListeners(range, from, to);
        } catch (err) {
            handleError(err, 'global top listeners');
        } finally {
            isLoading.value = false;
        }
    };

    const fetchGlobalTopRequesters = async (range: string, limit?: number) => {
        isLoading.value = true;
        error.value = null;
        try {
            globalTopRequesters.value = await getGlobalTopRequesters(range, limit);
        } catch (err) {
            handleError(err, 'global top requesters');
        } finally {
            isLoading.value = false;
        }
    };

    const fetchGlobalBotUptime = async (range: string) => {
        isLoading.value = true;
        error.value = null;
        try {
            globalBotUptime.value = await getGlobalBotUptime(range);
        } catch (err) {
            handleError(err, 'global bot uptime');
        } finally {
            isLoading.value = false;
        }
    };


    // Guild Stats Actions
    const fetchGuildTrackCount = async (guildId: string, range: string) => {
        isLoading.value = true;
        error.value = null;
        try {
            guildTrackCount.value = await getGuildTrackCount(guildId, range);
        } catch (err) {
            handleError(err, 'guild track count');
        } finally {
            isLoading.value = false;
        }
    };

    const fetchGuildTopTracks = async (guildId: string, range: string, from: number, to: number) => {
        isLoading.value = true;
        error.value = null;
        try {
            guildTopTracks.value = await getGuildTopTracks(guildId, range, from, to);
        } catch (err) {
            handleError(err, 'guild top tracks');
        } finally {
            isLoading.value = false;
        }
    };

    const fetchGuildTopListeners = async (guildId: string, range: string, from: number, to: number) => {
        isLoading.value = true;
        error.value = null;
        try {
            guildTopListeners.value = await getGuildTopListeners(guildId, range, from, to);
        } catch (err) {
            handleError(err, 'guild top listeners');
        } finally {
            isLoading.value = false;
        }
    };

    const fetchGuildTopRequesters = async (guildId: string, range: string, limit?: number) => {
        isLoading.value = true;
        error.value = null;
        try {
            guildTopRequesters.value = await getGuildTopRequesters(guildId, range, limit);
        } catch (err) {
            handleError(err, 'guild top requesters');
        } finally {
            isLoading.value = false;
        }
    };

    const fetchGuildDailySessionLength = async (guildId: string, date: string) => {
        isLoading.value = true;
        error.value = null;
        try {
            guildDailySessionLength.value = await getGuildDailySessionLength(guildId, date);
        } catch (err) {
            handleError(err, 'guild daily session length');
        } finally {
            isLoading.value = false;
        }
    };

    const fetchGuildDailySessionCount = async (guildId: string, date: string) => {
        isLoading.value = true;
        error.value = null;
        try {
            guildDailySessionCount.value = await getGuildDailySessionCount(guildId, date);
        } catch (err) {
            handleError(err, 'guild daily session count');
        } finally {
            isLoading.value = false;
        }
    };

    const fetchGuildSessionTimeByHour = async (guildId: string, date: string) => {
        isLoading.value = true;
        error.value = null;
        try {
            guildSessionTimeByHour.value = await getGuildSessionTimeByHour(guildId, date);
        } catch (err) {
            handleError(err, 'guild session time by hour');
        } finally {
            isLoading.value = false;
        }
    };

    const fetchGuildUniqueTrackCount = async (guildId: string, range: string) => {
        isLoading.value = true;
        error.value = null;
        try {
            guildUniqueTrackCount.value = await getGuildUniqueTrackCount(guildId, range);
        } catch (err) {
            handleError(err, 'guild unique track count');
        } finally {
            isLoading.value = false;
        }
    };

    // User Stats Actions
    const fetchUserAverageSessionLength = async (userId: string, range: string) => {
        isLoading.value = true;
        error.value = null;
        try {
            userAverageSessionLength.value = await getUserAverageSessionLength(userId, range);
        } catch (err) {
            handleError(err, 'user average session length');
        } finally {
            isLoading.value = false;
        }
    };

    const fetchUserMedianSessionLength = async (userId: string, range: string) => {
        isLoading.value = true;
        error.value = null;
        try {
            userMedianSessionLength.value = await getUserMedianSessionLength(userId, range);
        } catch (err) {
            handleError(err, 'user median session length');
        } finally {
            isLoading.value = false;
        }
    };

    const fetchUserTotalSessionLength = async (userId: string, range: string) => {
        isLoading.value = true;
        error.value = null;
        try {
            userTotalSessionLength.value = await getUserTotalSessionLength(userId, range);
        } catch (err) {
            handleError(err, 'user total session length');
        } finally {
            isLoading.value = false;
        }
    };


    // --- Computed Properties (for easy access in components, potentially transformed for Chart.js) ---

    // Example for Global Top Tracks data suitable for Chart.js
    const globalTopTracksChartData = computed(() => {
        if (!globalTopTracks.value.length) return { labels: [], datasets: [] };

        const labels = globalTopTracks.value.map(item => item.song.title);
        const data = globalTopTracks.value.map(item => item.playCount);

        return {
            labels: labels,
            datasets: [
                {
                    label: 'Play Count',
                    backgroundColor: '#f87979',
                    data: data
                }
            ]
        };
    });

    // Example for Guild Session Time By Hour data suitable for Chart.js
    const guildSessionTimeByHourChartData = computed(() => {
        if (!guildSessionTimeByHour.value.length) return { labels: [], datasets: [] };

        const labels = guildSessionTimeByHour.value.map(item => `${item.hour}:00`);
        const data = guildSessionTimeByHour.value.map(item => item.value);

        return {
            labels: labels,
            datasets: [
                {
                    label: 'Session Time (Minutes)', // Adjust unit based on API
                    backgroundColor: '#42b983',
                    data: data
                }
            ]
        };
    });


    return {
        isLoading,
        error,

        // State variables
        globalTrackCount,
        globalTopTracks,
        globalTopListeners,
        globalTopRequesters,
        globalBotUptime,

        guildTrackCount,
        guildTopTracks,
        guildTopListeners,
        guildTopRequesters,
        guildDailySessionLength,
        guildDailySessionCount,
        guildSessionTimeByHour,
        guildUniqueTrackCount,

        userAverageSessionLength,
        userMedianSessionLength,
        userTotalSessionLength,

        // Actions
        fetchGlobalTrackCount,
        fetchGlobalTopTracks,
        fetchGlobalTopListeners,
        fetchGlobalTopRequesters,
        fetchGlobalBotUptime,

        fetchGuildTrackCount,
        fetchGuildTopTracks,
        fetchGuildTopListeners,
        fetchGuildTopRequesters,
        fetchGuildDailySessionLength,
        fetchGuildDailySessionCount,
        fetchGuildSessionTimeByHour,
        fetchGuildUniqueTrackCount,

        fetchUserAverageSessionLength,
        fetchUserMedianSessionLength,
        fetchUserTotalSessionLength,

        // Computed properties for Chart.js
        globalTopTracksChartData,
        guildSessionTimeByHourChartData,
    };
});