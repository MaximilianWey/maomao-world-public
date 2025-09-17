import { apiGet } from './authService'
import { DISCORD_URLS } from '@/config'

export const getCurrentVoiceChannelState = async (guildId: string) => {
    return (await apiGet(DISCORD_URLS.getCurrentVoiceChannelState(guildId))).data
}
