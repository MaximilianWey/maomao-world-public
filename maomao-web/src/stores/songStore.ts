import { defineStore } from 'pinia'
import {
    clearQueue,
    enqueuePlaylist,
    enqueueTrack,
    getQueue,
    pausePlayback, playTrackNext,
    previousTrack, removeTrackFromQueue,
    reorderQueue,
    resumePlayback, setMode,
    shuffleQueue,
    skipTrack
} from '@/services/musicService.ts'
import { useGuildStore } from "@/stores/guildStore.ts";
import { useVoiceChannelStore } from "@/stores/voiceChannelStore.ts";

export interface Song {
    identifier: string
    title: string
    artist: string
    url: string
    thumbnail: string
    source: 'youtube' | 'soundcloud' | 'spotify'
}

type Mode = 'NORMAL' | 'REPEAT_SONG' | 'REPEAT_QUEUE'

export const useSongStore = defineStore('song', {
    state: () => ({
        queue: [] as Song[],
        currentIndex: -1,
        mode: 'NORMAL' as Mode,
        isPaused: false,
        lastPlayedSong: null as Song | null,
    }),
    getters: {
        currentSong(state): Song | null {
            return state.queue[state.currentIndex] || null
        },
    },
    actions: {
        async fetchQueue(guildId: string) {
            try {
                const queue = await getQueue(guildId)

                this.queue = mapQueueToSongs(queue)
                this.currentIndex = queue.currentIndex
                this.mode = queue.mode
                console.debug('Fetched queue from server:', queue)
            } catch (error) {
                console.error('Failed to fetch queue from server:', error)
            }
        },

        startTrack(index: number) {
            if (index < 0 || index >= this.queue.length) return
            this.currentIndex = index
            this.lastPlayedSong = this.queue[index]
            this.isPaused = false
            console.debug(`Start Track: ${this.queue[index].title}`)
        },

        async enqueue(song: Song) {
            const guildStore = useGuildStore()
            const voiceChannelStore = useVoiceChannelStore()
            const guild = guildStore.getCurrentGuild

            if (!guild || !guild.id) {
                console.error("No guild found or guild ID is not available")
                return
            }
            const voiceChannel = voiceChannelStore.currentVoiceChannel
            if (!voiceChannel) {
                console.error("No voice channel found")
                return
            }
            this.queue.push(song)
            const updatedQueue = await enqueueTrack(guild.id, song.url, voiceChannel.id)
            this.queue = mapQueueToSongs(updatedQueue)
            this.currentIndex = updatedQueue.currentIndex
            this.mode = updatedQueue.mode
        },

        async playTrackNext(song: Song) {
            const guildStore = useGuildStore()
            const voiceChannelStore = useVoiceChannelStore()
            const guild = guildStore.getCurrentGuild

            if (!guild || !guild.id) {
                console.error("No guild found or guild ID is not available")
                return
            }
            const voiceChannel = voiceChannelStore.currentVoiceChannel
            if (!voiceChannel) {
                console.error("No voice channel found")
                return
            }
            const updatedQueue = await playTrackNext(guild.id, song.url, voiceChannel.id)
            this.queue = mapQueueToSongs(updatedQueue)
            this.currentIndex = updatedQueue.currentIndex
            this.mode = updatedQueue.mode
        },

        async enqueuePlaylist(playlistId: string, shuffle: boolean = false) {
            const guildStore = useGuildStore()
            const voiceChannelStore = useVoiceChannelStore()

            const guild = guildStore.getCurrentGuild
            if (!guild || !guild.id) {
                console.error("No guild found or guild ID is not available")
                return
            }

            const voiceChannel = voiceChannelStore.currentVoiceChannel
            if (!voiceChannel) {
                console.error("No voice channel found")
                return
            }
            const res = await enqueuePlaylist(guild.id, playlistId, voiceChannel.id, shuffle)
            if (isSuccessResponse(res)) {
                this.queue = mapQueueToSongs(res)
                this.currentIndex = res.currentIndex
                this.mode = res.mode
            }
        },

        async skipSong() {
            if (this.queue.length === 0) return

            const guildStore = useGuildStore()
            const guild = guildStore.getCurrentGuild

            if (!guild || !guild.id) {
                console.error("No guild found or guild ID is not available")
                return
            }

            try {
                const response = await skipTrack(guild.id)

                if (isSuccessResponse(response)) {
                    console.debug('Skipped song')

                    switch (this.mode) {
                        case 'REPEAT_SONG':
                            this.startTrack(this.currentIndex)
                            break
                        case 'REPEAT_QUEUE':
                            this.currentIndex = (this.currentIndex + 1) % this.queue.length
                            this.startTrack(this.currentIndex)
                            break
                        case 'NORMAL':
                            if (this.currentIndex + 1 < this.queue.length) {
                                this.currentIndex++
                                this.startTrack(this.currentIndex)
                            } else {
                                this.stop()
                            }
                            break
                    }
                } else {
                    console.error('Backend rejected skip request:', response)
                }
            } catch (error) {
                console.error('Failed to skip song on backend:', error)
            }
        },

        async previousSong() {
            if (this.queue.length === 0) return

            const guildStore = useGuildStore()
            const guild = guildStore.getCurrentGuild

            if (!guild || !guild.id) {
                console.error("No guild found or guild ID is not available")
                return
            }

            try {
                const response = await previousTrack(guild.id)

                if (isSuccessResponse(response)) {
                    console.debug('Playing previous song')

                    if (this.currentIndex > 0) {
                        this.startTrack(this.currentIndex - 1)
                    } else {
                        this.startTrack(this.queue.length - 1)
                    }
                } else {
                    console.error('Backend rejected previousTrack request:', response)
                }

            } catch (error) {
                console.error('Failed to play previous song on backend:', error)
            }
        },

        async pause() {
            if (this.queue.length === 0) return

            const guildStore = useGuildStore()
            const guild = guildStore.getCurrentGuild

            if (!guild || !guild.id) {
                console.error("No guild found or guild ID is not available")
                return
            }

            try {
                const response = await pausePlayback(guild.id)

                if (isSuccessResponse(response)) {
                    this.isPaused = true
                    console.debug('Paused playback')
                } else {
                    console.error('Backend rejected pause request:', response)
                }
            } catch (error) {
                console.error('Failed to pause playback on backend:', error)
            }
        },

        async resume() {
            if (this.queue.length === 0) return

            const guildStore = useGuildStore()
            const guild = guildStore.getCurrentGuild

            if (!guild || !guild.id) {
                console.error("No guild found or guild ID is not available")
                return
            }

            try {
                const response = await resumePlayback(guild.id)

                if (isSuccessResponse(response)) {
                    this.isPaused = false
                    console.debug('Resumed playback')
                } else {
                    console.error('Backend rejected resume request:', response)
                }
            } catch (error) {
                console.error('Failed to resume playback on backend:', error)
            }
        },

        stop() {
            this.currentIndex = -1
            this.lastPlayedSong = null
            this.isPaused = false
            console.debug('Stopped playback')
        },

        async clearQueue() {

            const guildStore = useGuildStore()
            const guild = guildStore.getCurrentGuild

            if (!guild || !guild.id) {
                console.error("No guild found or guild ID is not available")
                return
            }

            this.queue = []
            this.currentIndex = -1

            await clearQueue(guild.id)
        },

        async removeTrackFromQueue(index: number) {
            if (index < 0 || index >= this.queue.length) return

            const guildStore = useGuildStore()
            const guild = guildStore.getCurrentGuild

            if (!guild || !guild.id) {
                console.error("No guild found or guild ID is not available")
                return
            }

            this.queue.splice(index, 1)

            const res = await removeTrackFromQueue(guild.id, index)
            if (isSuccessResponse(res)) {
                console.debug('Removed track from queue:', index)
            } else {
                console.error('Failed to remove track from queue:', res)
            }
        },

        reset() {
            this.currentIndex = -1
            this.lastPlayedSong = null
        },

        async shuffleQueue() {

            const guildStore = useGuildStore()
            const guild = guildStore.getCurrentGuild

            if (!guild || !guild.id) {
                console.error("No guild found or guild ID is not available")
                return
            }

            try {
                const queue = await shuffleQueue(guild.id)

                this.queue = mapQueueToSongs(queue)
                this.currentIndex = queue.currentIndex
                this.isPaused = false
                console.debug('Fetched queue from server:', queue)
            } catch (error) {
                console.error('Failed to fetch queue from server:', error)
            }
        },

        async setMode(mode: Mode) {
            const guildStore = useGuildStore()
            const guild = guildStore.getCurrentGuild

            if (!guild || !guild.id) {
                console.error("No guild found or guild ID is not available")
                return
            }
            this.mode = mode // Update the mode in the local state to feel the change immediately
            try {
                const queue = await setMode(guild.id, mode)
                this.queue = mapQueueToSongs(queue)
                this.currentIndex = queue.currentIndex
                this.mode = queue.mode
                console.debug('Set mode on server:', mode)
            } catch (error) {
                console.error('Failed to set mode on backend:', error)
            }
        },

        setIndex(i: number) {
            if (i >= 0 && i < this.queue.length) {
                this.startTrack(i)
            } else {
                console.warn('Invalid index:', i)
            }
        },

        addSongToQueue(song: Song) {
            this.enqueue(song)
        },

        async removeSong(index: number) {
            if (index < 0 || index >= this.queue.length) return

            this.queue.splice(index, 1)

            if (index < this.currentIndex) {
                this.currentIndex--
            } else if (index === this.currentIndex) {
                if (this.queue.length === 0) {
                    this.stop()
                } else {
                    this.currentIndex = Math.min(this.currentIndex, this.queue.length - 1)
                    this.startTrack(this.currentIndex)
                }
            }
            const guildStore = useGuildStore()
            const guild = guildStore.getCurrentGuild
            if (!guild) {
                console.error("No guild found");
                return;
            }
            await removeTrackFromQueue(guild.id, index)
        },

        async moveSong(fromIndex: number, toIndex: number) {
            if (fromIndex < 0 || toIndex < 0 || fromIndex >= this.queue.length || toIndex >= this.queue.length) {
                return
            }

            if (fromIndex === this.currentIndex) {
                this.currentIndex = toIndex
            } else if (fromIndex < this.currentIndex && toIndex >= this.currentIndex) {
                this.currentIndex--
            } else if (fromIndex > this.currentIndex && toIndex <= this.currentIndex) {
                this.currentIndex++
            }

            const guildStore = useGuildStore();
            const guild = guildStore.getCurrentGuild;

            if (!guild) {
                console.error("No guild found");
                return;
            }

            const guildId = guildStore.getCurrentGuild.id;

            if (guildId) {
                try {
                    await getPayloadAndReorderQueue(guildId,
                        this.queue,
                        this.currentIndex,
                        this.mode)
                } catch (err) {
                    console.error("Failed to sync reordered queue to backend", err);
                }
            } else {
                console.error("Guild ID is not available");
            }
        },
    },
})

const getPayloadAndReorderQueue = async (
    guildId: string,
    songs: Song[],
    currentIndex: number,
    mode: Mode
) => {
    try {
        const payload = {
            songs,
            currentIndex,
            mode
        };

        const response = await reorderQueue(guildId, payload);
        return response.data;
    } catch (err) {
        console.error('Failed to reorder queue:', err);
        throw new Error('Failed to reorder queue');
    }
};

const isSuccessResponse = (response: any) => {
    return response.status >= 200 && response.status < 300
}

const mapQueueToSongs = (queue: any): Song[] => {
    return queue.songs.map((simpleSong: any) => ({
        identifier: simpleSong.identifier,
        title: simpleSong.title,
        artist: simpleSong.artist,
        duration: simpleSong.duration,
        url: simpleSong.url,
        thumbnail: simpleSong.thumbnail,
        source: simpleSong.source as 'youtube' | 'soundcloud' | 'spotify',
    }))
}
