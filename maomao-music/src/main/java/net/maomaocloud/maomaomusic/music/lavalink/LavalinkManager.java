package net.maomaocloud.maomaomusic.music.lavalink;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.Track;
import jakarta.annotation.PostConstruct;
import net.maomaocloud.maomaomusic.music.model.SimpleQueue;
import net.maomaocloud.maomaomusic.music.model.SimpleSong;
import net.maomaocloud.maomaomusic.music.manager.GuildMusicManager;

import net.maomaocloud.maomaomusic.music.model.TrackUserDetail;
import net.maomaocloud.maomaomusic.utils.QueryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static net.maomaocloud.maomaomusic.music.lavalink.SearchAudioLoader.MusicSearchResult.Reason.TIMEOUT;

@Component
public class LavalinkManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(LavalinkManager.class);

    private final ApplicationEventPublisher eventPublisher;
    private final LavalinkClient lavalinkClient;
    private final LavalinkEventHandler eventHandler;
    private final Map<Long, GuildMusicManager> musicManagers = new ConcurrentHashMap<>();

    @Autowired
    public LavalinkManager(ApplicationEventPublisher eventPublisher,
                           LavalinkClient lavalinkClient,
                           LavalinkEventHandler eventHandler) {
        this.eventPublisher = eventPublisher;
        this.lavalinkClient = lavalinkClient;
        this.eventHandler = eventHandler;
    }

    @PostConstruct
    public void init() {
        eventHandler.registerEvents(lavalinkClient, musicManagers);
    }

    public boolean isAnyNodeAvailable() {
        try {
            return lavalinkClient.getNodes().stream().anyMatch(node -> node.getAvailable());
        } catch (Exception e) {
            LOGGER.error("An error occurred while checking if any node is available: {}", e.getMessage(), e);
            return false;
        }
    }

    public GuildMusicManager getGuildMusicManager(Long guildId) {
        return musicManagers.computeIfAbsent(guildId, id -> new GuildMusicManager(lavalinkClient, id, eventPublisher));
    }

    public Link getLinkForGuild(Long guildId) {
        return lavalinkClient.getLinkIfCached(guildId);
    }

    public Optional<SimpleSong> getCurrentTrack(Long guildId) {
        GuildMusicManager musicManager = getGuildMusicManager(guildId);
        return musicManager.getScheduler().getCurrentTrack();
    }
    
    public SimpleQueue getQueuedTracks(Long guildId) {
        GuildMusicManager musicManager = getGuildMusicManager(guildId);
        return musicManager.getScheduler().getQueuedTracks();
    }

    public LavalinkClient getLavalinkClient() {
        return lavalinkClient;
    }
    
    public Map<Long, GuildMusicManager> getMusicManagers() {
        return musicManagers;
    }

    public Track findFirstTrack(String query) {
        long fakeGuildID = 0L;
        Link link = lavalinkClient.getOrCreateLink(fakeGuildID);

        CompletableFuture<SearchAudioLoader.MusicSearchResult> future = searchTracks(query, link, true);

        try {
            SearchAudioLoader.MusicSearchResult result = future
                    .completeOnTimeout(new SearchAudioLoader.MusicSearchResult(List.of(), TIMEOUT), 10, TimeUnit.SECONDS)
                    .join();
            return result.tracks().isEmpty() ? null : result.tracks().get(0);
        } catch (Exception e) {
            LOGGER.error("Error while searching for tracks: {}", e.getMessage(), e);
            return null;
        }
    }

    public List<Track> searchYoutubeTracks(String query) {
        long fakeGuildID = 0L;
        Link link = lavalinkClient.getOrCreateLink(fakeGuildID);

        CompletableFuture<SearchAudioLoader.MusicSearchResult> future = youtubeQuery(query, link, false);

        try {
            SearchAudioLoader.MusicSearchResult result = future
                    .completeOnTimeout(new SearchAudioLoader.MusicSearchResult(List.of(), TIMEOUT), 10, TimeUnit.SECONDS)
                    .join();
            return result.tracks();
        } catch (Exception e) {
            LOGGER.error("Error while searching YouTube tracks: {}", e.getMessage(), e);
            return List.of();
        }
    }

    public List<Track> searchSoundcloudTracks(String query) {
        long fakeGuildID = 0L;
        Link link = lavalinkClient.getOrCreateLink(fakeGuildID);

        CompletableFuture<SearchAudioLoader.MusicSearchResult> future = soundcloudQuery(query, link, false);

        try {
            SearchAudioLoader.MusicSearchResult result = future
                    .completeOnTimeout(new SearchAudioLoader.MusicSearchResult(List.of(), TIMEOUT), 10, TimeUnit.SECONDS)
                    .join();
            return result.tracks();
        } catch (Exception e) {
            LOGGER.error("Error while searching SoundCloud tracks: {}", e.getMessage(), e);
            return List.of();
        }
    }

    public List<Track> searchTracks(String query) {
        long fakeGuildID = 0L;
        Link link = lavalinkClient.getOrCreateLink(fakeGuildID);
        CompletableFuture<SearchAudioLoader.MusicSearchResult> future = searchTracks(query, link, false);

        try {
            SearchAudioLoader.MusicSearchResult result = future
                    .completeOnTimeout(new SearchAudioLoader.MusicSearchResult(List.of(), TIMEOUT), 10, TimeUnit.SECONDS)
                    .join();
            return result.tracks();
        } catch (Exception e) {
            LOGGER.error("Error while searching for tracks: {}", e.getMessage(), e);
            return List.of();
        }
    }

    private CompletableFuture<SearchAudioLoader.MusicSearchResult> searchTracks(String query, Link link, boolean returnFirst) {
        CompletableFuture<SearchAudioLoader.MusicSearchResult> future = new CompletableFuture<>();

        youtubeQuery(query, link, returnFirst)
                .orTimeout(5, TimeUnit.SECONDS)
                .thenAccept(ytRes -> {
                    if (!ytRes.tracks().isEmpty()) {
                        future.complete(ytRes);
                    } else {
                        soundcloudQuery(query, link, returnFirst)
                                .orTimeout(5, TimeUnit.SECONDS)
                                .thenAccept(future::complete)
                                .exceptionally(ex -> {
                                    future.completeExceptionally(ex);
                                    return null;
                                });
                    }
                })
                .exceptionally(ex -> {
                    future.completeExceptionally(ex);
                    return null;
                });

        return future;
    }

    private CompletableFuture<SearchAudioLoader.MusicSearchResult> youtubeQuery(String query, Link link, boolean returnFirst) {
        String searchQuery = QueryUtils.getYoutubeSearchQuery(query);
        CompletableFuture<SearchAudioLoader.MusicSearchResult> future = new CompletableFuture<>();
        link.loadItem(searchQuery).subscribe(new SearchAudioLoader(future, returnFirst));
        return future;
    }

    private CompletableFuture<SearchAudioLoader.MusicSearchResult> soundcloudQuery(String query, Link link, boolean returnFirst) {
        String searchQuery = QueryUtils.getSoundCloudSearchQuery(query);
        CompletableFuture<SearchAudioLoader.MusicSearchResult> future = new CompletableFuture<>();
        link.loadItem(searchQuery).subscribe(new SearchAudioLoader(future, returnFirst));
        return future;
    }

}