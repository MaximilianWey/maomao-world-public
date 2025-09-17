package net.maomaocloud.maomaomusic.bot.listeners;


import jakarta.annotation.Nonnull;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.maomaocloud.maomaomusic.music.lavalink.LavalinkManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Component
public class VoiceStateListener extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(VoiceStateListener.class);

    private static final Long TIME_OUT_IN_MINUTES = 5L;

    private final Map<Long, ScheduledFuture<?>> disconnectTasks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler;
    private final LavalinkManager lavalinkManager;

    @Autowired
    public VoiceStateListener(ScheduledExecutorService scheduler, LavalinkManager lavalinkManager) {
        this.scheduler = scheduler;
        this.lavalinkManager = lavalinkManager;
    }

    @Override
    public void onGuildVoiceUpdate(@Nonnull GuildVoiceUpdateEvent event) {
        final var guild = event.getGuild();
        final var selfMember = guild.getSelfMember();
        final var selfChannel = selfMember.getVoiceState().getChannel();

        if (selfChannel == null) {
            cancelDisconnectTask(guild.getIdLong());
            return;
        }

        boolean alone = selfChannel.getMembers().stream()
                .allMatch(member -> member.getUser().isBot());

        if (alone) {
            ScheduledFuture<?> existingTask = disconnectTasks.get(guild.getIdLong());
            if (existingTask != null) {
                long remainingSeconds = existingTask.getDelay(TimeUnit.SECONDS);
                LOGGER.info("Disconnect already scheduled for guild {}. Remaining time: {} seconds.", guild.getId(), remainingSeconds);
            } else {
                LOGGER.info("Bot is alone in channel {}. Scheduling disconnect in {} minutes.", selfChannel.getName(), TIME_OUT_IN_MINUTES);
                scheduleDisconnect(guild.getIdLong(), () -> {
                    LOGGER.info("{} minutes passed with no listeners. Disconnecting from {}", TIME_OUT_IN_MINUTES, selfChannel.getName());
                    lavalinkManager.getLinkForGuild(guild.getIdLong()).destroy();
                    selfMember.getVoiceState().getChannel().getGuild().getAudioManager().closeAudioConnection();
                    disconnectTasks.remove(guild.getIdLong());
                });
            }
        } else {
            LOGGER.info("Bot is not alone in channel {}. Cancelling any existing disconnect task.", selfChannel.getName());
            cancelDisconnectTask(guild.getIdLong());
        }
    }

    private void scheduleDisconnect(long guildId, Runnable task) {
        cancelDisconnectTask(guildId);
        ScheduledFuture<?> future = scheduler.schedule(task, TIME_OUT_IN_MINUTES, TimeUnit.MINUTES);
        disconnectTasks.put(guildId, future);
    }

    private void cancelDisconnectTask(long guildId) {
        ScheduledFuture<?> existing = disconnectTasks.remove(guildId);
        if (existing != null && !existing.isDone()) {
            existing.cancel(false);
        }
    }

}
