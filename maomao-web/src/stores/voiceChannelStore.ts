import { defineStore } from 'pinia'
import type { VoiceChannel } from '@/types/voiceChannel.ts'
import { getCurrentVoiceChannelState } from '@/services/voiceChannelService.ts'

export const useVoiceChannelStore = defineStore('voiceChannel', {
    state: () => ({
        currentVoiceChannel: null as VoiceChannel | null,
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

        async fetchCurrentVoiceChannel(guildId: string) {
            const res = await this.withLoading(() => getCurrentVoiceChannelState(guildId))
            if (res && res.channel) {
                this.currentVoiceChannel = {
                    id: res.channel.id,
                    name: res.channel.name,
                    guildId: res.guildId,
                    currentlyConnectedUsers: res.currentlyConnectedUsers,
                    botUser: res.botUser,
                }
            } else {
                this.currentVoiceChannel = null
            }
        }
    }
})
