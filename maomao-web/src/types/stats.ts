import type {Song} from "@/stores/songStore.ts";

export interface SongStatDTO {
    song: Song;
    playCount: number;
}

export interface TrackCountDTO<T> {
    item: T;
    count: number;
}

export interface CountStatDTO<T> {
    subject: T;
    count: number;
}

export interface PreciseStatDTO<T> {
    subject: T;
    value: number;
}

export interface DailyStatDTO {
    date: string;
    value: number;
}

export interface HourStatDTO {
    hour: number;
    value: number;
}

export interface StatsQueryParam {
    guildId: string;
    userId?: string | null;
    channelId?: string | null;
    date?: string | null;
    range?: Range;
}

export interface ListeningTimeDTO {
    requestedParams: StatsQueryParam;
    totalListeningTimeMS: number | null;
}

export interface ListeningTimeDTO {
    requestedParams: StatsQueryParam;
    totalListeningTimeMS: number | null;
}

export type Range = 'today' | 'week' | 'month' | 'year';
export const RANGES: Range[] = ['today', 'week', 'month', 'year'];
export const RANGE_LABELS: Record<Range, string> = {
    today: 'Today',
    week: 'This Week',
    month: 'This Month',
    year: 'This Year',
};