package net.maomaocloud.maomaomusic.music.dto;

import org.jetbrains.annotations.NotNull;

import java.util.Date;

public record StatsQueryParam(@NotNull String guildId,
                              String userId,
                              String channelId,
                              Date date,
                              String range) {
}
