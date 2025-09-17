package net.maomaocloud.maomaomusic.music.events;

import jakarta.annotation.Nonnull;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.maomaocloud.maomaomusic.music.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserLeaveVoiceChatListener extends ListenerAdapter {

    private final StatsService statsService;

    @Autowired
    public UserLeaveVoiceChatListener(StatsService statsService) {
        this.statsService = statsService;
    }

    @Override
    public void onGuildVoiceUpdate(@Nonnull GuildVoiceUpdateEvent event) {
        final var guild = event.getGuild();
        final var member = event.getMember();
        final var channel = event.getChannelLeft();

        if (channel == null) {
            return;
        }
        statsService.handleUserLeftVoiceChannel(member.getId(), guild.getId(), channel.getId());
    }
}
