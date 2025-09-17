package net.maomaocloud.maomaomusic.discord.model;

import java.util.List;

public record DiscordVoiceChannelStateDTO(DiscordVoiceChannel channel,
                                          String guildId,
                                          List<DiscordUser> currentlyConnectedUsers,
                                          DiscordUser botUser) {
}
