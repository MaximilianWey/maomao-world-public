import type { DiscordUser } from "@/types/user.ts";

export interface VoiceChannel {
    id: string
    name: string
    guildId: string
    currentlyConnectedUsers: DiscordUser[]
    botUser: DiscordUser
}