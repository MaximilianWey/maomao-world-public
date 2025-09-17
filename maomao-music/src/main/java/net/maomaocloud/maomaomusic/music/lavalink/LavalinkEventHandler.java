package net.maomaocloud.maomaomusic.music.lavalink;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.LavalinkNode;
import dev.arbjerg.lavalink.client.event.*;
import dev.arbjerg.lavalink.protocol.v4.Message;
import net.maomaocloud.maomaomusic.music.manager.GuildMusicManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class LavalinkEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(LavalinkEventHandler.class);

    public void registerEvents(LavalinkClient client, Map<Long, GuildMusicManager> musicManagers) {
        client.on(ReadyEvent.class).subscribe(event -> {
            LavalinkNode node = event.getNode();
            LOGGER.info("Node '{}' ready, session id is '{}'", node.getName(), event.getSessionId());
        });

        client.on(StatsEvent.class).subscribe(event -> {
            LavalinkNode node = event.getNode();
            LOGGER.info(
                    "Node '{}' has stats, current players: {}/{} (link count {})",
                    node.getName(),
                    event.getPlayingPlayers(),
                    event.getPlayers(),
                    client.getLinks().size()
                    );
        });

        client.on(TrackStartEvent.class).subscribe(event -> {
            Optional.ofNullable(musicManagers.get(event.getGuildId()))
                    .ifPresent(mng -> mng.getScheduler().onTrackStart(event.getTrack()));

            LavalinkNode node = event.getNode();
            LOGGER.trace(
                    "{}: track started: {}",
                    node.getName(),
                    event.getTrack().getInfo().getTitle()
            );
        });
        
        client.on(TrackEndEvent.class).subscribe(event -> {
            Optional.ofNullable(musicManagers.get(event.getGuildId()))
                    .ifPresent(mng -> mng.getScheduler().onTrackEnd(
                            event.getTrack(), 
                            Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason.valueOf(event.getEndReason().name())
                    ));
        });

        client.on(WebSocketClosedEvent.class).subscribe(event -> {
            LOGGER.warn("WebSocket closed for guild {}: code {}, reason: {}, byRemote: {}", 
                    event.getGuildId(), 
                    event.getCode(),
                    event.getReason(),
                    event.getByRemote());
        });
    }
}