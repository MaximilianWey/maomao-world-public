package net.maomaocloud.maomaomusic.bot.listeners;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.maomaocloud.maomaomusic.music.model.BotSession;
import net.maomaocloud.maomaomusic.discord.service.DiscordServerService;
import net.maomaocloud.maomaomusic.music.service.StatsService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BotSessionListener extends ListenerAdapter {

    private final StatsService statsService;
    private final DiscordServerService discordServerService;

    private final Map<String, BotSession> activeSessions = new ConcurrentHashMap<>();

    @Autowired
    public BotSessionListener(StatsService statsService, DiscordServerService discordServerService) {
        this.statsService = statsService;
        this.discordServerService = discordServerService;
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        Member member = event.getMember();

        if (!member.getUser().isBot()) {
            return;
        }

        String guildId = member.getGuild().getId();
        String botId = member.getUser().getId();

        AudioChannel oldChannel = event.getOldValue();
        AudioChannel newChannel = event.getNewValue();

        if (oldChannel == null && newChannel != null) {
            onBotJoin(guildId, botId, newChannel);
        }

        if (oldChannel != null && newChannel == null) {
            onBotLeave(guildId);
        }
    }

    private void onBotJoin(String guildId, String botId, AudioChannel channel) {
        var voiceChat = discordServerService.getOrCreateVoiceChannel(channel);
        BotSession session = new BotSession();
        session.setStartedAt(System.currentTimeMillis());
        session.setBotId(botId);
        session.setVoiceChannel(String.valueOf(voiceChat.getId()));

        statsService.saveBotSession(session);
        activeSessions.put(guildId, session);
    }

    private void onBotLeave(String guildId) {
        BotSession session = activeSessions.remove(guildId);
        if (session != null) {
            session.setStoppedAt(System.currentTimeMillis());
            statsService.saveBotSession(session);
        }
    }
}
