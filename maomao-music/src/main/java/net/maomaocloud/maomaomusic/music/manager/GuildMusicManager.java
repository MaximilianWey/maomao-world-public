package net.maomaocloud.maomaomusic.music.manager;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.LavalinkPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

public class GuildMusicManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GuildMusicManager.class);
    
    private final TrackScheduler scheduler;
    private final LavalinkClient lavalinkClient;
    private final long guildId;
    
    public GuildMusicManager(LavalinkClient lavalinkClient, Long guildId, ApplicationEventPublisher eventPublisher) {
        this.lavalinkClient = lavalinkClient;
        this.guildId = guildId;
        this.scheduler = new TrackScheduler(this, eventPublisher);
    }
    
    public void stop() {
        this.getPlayer().ifPresent(
            (player) -> player.setPaused(false)
                .setTrack(null)
                .subscribe()
        );
        this.scheduler.reset();
    }
    
    public Link getLink() {
        return this.lavalinkClient.getOrCreateLink(this.guildId);
    }
    
    public Optional<LavalinkPlayer> getPlayer() {
        return this.getLink().getPlayer().blockOptional();
    }

    public TrackScheduler getScheduler() {
        return this.scheduler;
    }

    public Long getGuildId() {
        return this.guildId;
    }

}