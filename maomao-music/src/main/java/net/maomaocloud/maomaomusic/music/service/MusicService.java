package net.maomaocloud.maomaomusic.music.service;

import dev.arbjerg.lavalink.client.player.Track;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.maomaocloud.maomaomusic.discord.model.DiscordUser;
import net.maomaocloud.maomaomusic.discord.service.DiscordUserService;
import net.maomaocloud.maomaomusic.music.model.*;
import net.maomaocloud.maomaomusic.music.lavalink.LavalinkManager;
import net.maomaocloud.maomaomusic.music.manager.GuildMusicManager;
import net.maomaocloud.maomaomusic.music.manager.TrackScheduler;
import net.maomaocloud.maomaomusic.utils.VoiceChannelUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MusicService {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MusicService.class);

    private final LavalinkManager lavalinkManager;
    private final DiscordUserService discordUserService;
    private final SongService songService;
    private final PlaylistService playlistService;
    private final StatsService statsService;

    @Autowired
    public MusicService(LavalinkManager lavalinkManager,
                        DiscordUserService discordUserService,
                        SongService songService,
                        PlaylistService playlistService,
                        StatsService statsService) {
        this.lavalinkManager = lavalinkManager;
        this.discordUserService = discordUserService;
        this.songService = songService;
        this.playlistService = playlistService;
        this.statsService = statsService;
    }

    public DiscordUser createOrUpdateUser(Member member) {
        return discordUserService.createOrUpdateDiscordUser(member);
    }

    public Optional<SimpleSong> enqueueAndGetSong(Long guildId, Long channelId, String requesterId, String query) {
        GuildMusicManager musicManager = lavalinkManager.getGuildMusicManager(guildId);
        TrackScheduler scheduler = musicManager.getScheduler();
        Optional<Track> trackOpt;

        try {
            trackOpt = Optional.ofNullable(lavalinkManager.findFirstTrack(query));
        } catch (Exception e) {
            return Optional.empty();
        }
        if (trackOpt.isEmpty()) {
            return Optional.empty();
        }
        Track track = trackOpt.get();
        SimpleSong song = songService.getOrCreateSong(track);

        TrackPlaybackSession session = statsService.createPlaybackSession(
                song.getIdentifier(),
                requesterId,
                guildId.toString(),
                channelId
        );

        scheduler.enqueue(statsService.attachSessionIdToTrack(track, session.getId()));
        return Optional.ofNullable(song);
    }

    public SimpleQueue enqueue(Long guildId, Long channelId, String requesterId, String query) {
        GuildMusicManager musicManager = lavalinkManager.getGuildMusicManager(guildId);
        TrackScheduler scheduler = musicManager.getScheduler();
        enqueueAndGetSong(guildId, channelId, requesterId, query);
        return scheduler.getQueuedTracks();
    }

    public SimpleQueue enqueue(JDA jda, Long guildId, Long channelId, String requesterId, String query) {
        GuildMusicManager musicManager = lavalinkManager.getGuildMusicManager(guildId);
        TrackScheduler scheduler = musicManager.getScheduler();
        VoiceChannelUtil.checkAndJoinVoiceChannel(jda, guildId, Long.valueOf(requesterId),
                () -> enqueue(guildId, channelId, requesterId, query),
                reason -> {
                    LOGGER.error("Failed to join voice channel: {}", reason);
                }
        );
        return scheduler.getQueuedTracks();

    }

    public Optional<SimplePlaylist> enqueuePlaylist(JDA jda,
                                                    Long guildId,
                                                    Long channelId,
                                                    Long requesterId,
                                                    UUID playlistId,
                                                    boolean shuffle) {
        SimplePlaylist playlist = playlistService.getPlaylistById(requesterId, playlistId);
        if (playlist == null) {
            return Optional.empty();
        }

        if (shuffle) {
            Collections.shuffle(playlist.getSongs());
        }

        VoiceChannelUtil.checkAndJoinVoiceChannel(jda, guildId, requesterId,
                () -> playlist.getSongs().forEach(song -> {
                    try {
                        enqueue(guildId, channelId, requesterId.toString(), song.getUrl());
                    } catch (Exception e) {
                        LOGGER.error("Failed to enqueue song from playlist: {}", song.getTitle(), e);
                    }
                }),
                reason -> {
                    LOGGER.error("Failed to join voice channel: {}", reason);
                }
        );
        return Optional.of(playlist);
    }

    public Optional<SimplePlaylist> enqueuePlaylist(Long guildId, Long channelId, Long requesterId, String playlistName) {
        Optional<SimplePlaylist> playlist = playlistService.getPlaylistByName(requesterId, playlistName);
        if (playlist.isEmpty()) {
            return Optional.empty();
        }
        playlist.get().getSongs().forEach(song -> {
            try {
                enqueue(guildId, channelId, requesterId.toString(), song.getUrl());
            } catch (Exception e) {
                LOGGER.error("Failed to enqueue song from playlist: {}", song.getTitle(), e);
            }
        });
        return playlist;
    }

    public void skipTrack(Long guildId) {
        GuildMusicManager musicManager = lavalinkManager.getGuildMusicManager(guildId);
        musicManager.getScheduler().skipTrack();
    }

    public void stop(Long guildId) {
        GuildMusicManager musicManager = lavalinkManager.getGuildMusicManager(guildId);
        musicManager.getScheduler().clearQueue();

        lavalinkManager.getLinkForGuild(guildId).destroy();
    }

    public void clearQueue(Long guildId) {
        GuildMusicManager musicManager = lavalinkManager.getGuildMusicManager(guildId);
        musicManager.getScheduler().clearQueue();
    }

    // todo : move into info command for better embed creation with pictures
    public List<String> getQueueInfo(Long guildId) {
        GuildMusicManager musicManager = lavalinkManager.getGuildMusicManager(guildId);
        TrackScheduler scheduler = musicManager.getScheduler();

        Optional<SimpleSong> currentTrackOpt = scheduler.getCurrentTrack();
        SimpleQueue queuedTracks = scheduler.getQueuedTracks();
        int currentTrackIndex = queuedTracks.currentIndex();
        List<SimpleSong> songs = queuedTracks.songs();
        TrackScheduler.Mode mode = scheduler.getMode();
        Boolean isPaused = scheduler.isPaused();

        List<String> pages = new ArrayList<>();
        int totalSongs = songs.size();
        int pageSize = 10;
        int totalPages = (int) Math.ceil((double) totalSongs / pageSize);

        for (int page = 0; page < totalPages; page++) {
            StringBuilder sb = new StringBuilder();

            // First page includes "Currently Playing" section
            if (page == 0) {
                appendNowPlayingSection(sb, currentTrackOpt, currentTrackIndex, mode, isPaused);

                if (totalSongs == 0) {
                    sb.append("📭 **The queue is empty.**");
                    pages.add(sb.toString());
                    break;
                } else {
                    sb.append("📜 **Up Next:**\n");
                }
            }

            int start = page * pageSize;
            int end = Math.min(start + pageSize, totalSongs);
            for (int i = start; i < end; i++) {
                SimpleSong song = songs.get(i);
                boolean isCurrent = i == currentTrackIndex;
                sb.append(formatQueueEntry(i, song, isCurrent));

                // Only add footnote on last entry of the page
                if (i == end - 1) {
                    sb.append("   ").append(String.format("`[%d/%d]`", page + 1, totalPages));
                }
                sb.append("\n");
            }

            pages.add(sb.toString().trim());
        }

        if (pages.isEmpty()) {
            pages.add("🎶 **Currently Playing:** _(Mode: `" + mode.name() + "\"`)_\n　 Nothing is playing\n\n📭 **The queue is empty.**");
        }

        return pages;
    }

    private void appendNowPlayingSection(StringBuilder sb, Optional<SimpleSong> currentTrackOpt, int currentTrackIndex, TrackScheduler.Mode mode, Boolean isPaused) {
        sb.append("🎶 **Currently Playing** _(Mode: `").append(mode.name()).append("`)_");
        sb.append(" _(Paused: `").append(isPaused).append("`)_\n");
        if (currentTrackOpt.isEmpty() || currentTrackIndex == -1) {
            sb.append("　 Nothing is playing\n\n");
        } else {
            SimpleSong track = currentTrackOpt.get();
            String titleWithUrl = "[" + track.getTitle() + "](" + track.getUrl() + ")";
            sb.append("＞ ").append(currentTrackIndex + 1).append(". ")
                    .append(titleWithUrl).append(" by ").append(track.getArtist())
                    .append("\n\n");
        }
    }

    private String formatQueueEntry(int index, SimpleSong song, boolean isCurrent) {
        String marker = isCurrent ? "＞" : "　";
        String titleWithUrl = "[" + song.getTitle() + "](" + song.getUrl() + ")";
        return marker + " " + (index + 1) + ". " + titleWithUrl + " by " + song.getArtist();
    }

    public Optional<SimpleSong> getCurrentTrack(Long guildId) {
        return lavalinkManager.getCurrentTrack(guildId);
    }

    public SimpleQueue getQueuedTracks(Long guildId) {
        return lavalinkManager.getQueuedTracks(guildId);
    }

    public Optional<GuildMusicManager> getGuildMusicManager(Long guildId) {
        return Optional.of(lavalinkManager.getGuildMusicManager(guildId));
    }

    public SimpleQueue reorderQueue(Long guildId, SimpleQueue newQueue) throws IllegalArgumentException {
        GuildMusicManager manager = lavalinkManager.getGuildMusicManager(guildId);
        if (manager == null) {
            throw new IllegalArgumentException("No GuildMusicManger found for guild ID: " + guildId);
        }
        TrackScheduler scheduler = manager.getScheduler();

        return scheduler.updateQueue(newQueue);
    }

    public boolean isPaused(Long guildId) {
        GuildMusicManager musicManager = lavalinkManager.getGuildMusicManager(guildId);
        return musicManager.getScheduler().isPaused();
    }

    public void pausePlayback(Long guildId) {
        GuildMusicManager musicManager = lavalinkManager.getGuildMusicManager(guildId);
        musicManager.getScheduler().pause();
    }

    public void resumePlayback(Long guildId) {
        GuildMusicManager musicManager = lavalinkManager.getGuildMusicManager(guildId);
        musicManager.getScheduler().resume();
    }

    public Optional<SimpleSong> playPreviousTrack(Long guildId) {
        GuildMusicManager musicManager = lavalinkManager.getGuildMusicManager(guildId);
        return musicManager.getScheduler().previousTrack();
    }

    public void setMode(Long guildId, TrackScheduler.Mode mode) {
        GuildMusicManager musicManager = lavalinkManager.getGuildMusicManager(guildId);
        musicManager.getScheduler().setMode(mode);
    }

    public TrackScheduler.Mode getMode(Long guildId) {
        GuildMusicManager musicManager = lavalinkManager.getGuildMusicManager(guildId);
        return musicManager.getScheduler().getMode();
    }

    public void setIndex(Long guildId, int index) throws IllegalArgumentException {
        GuildMusicManager musicManager = lavalinkManager.getGuildMusicManager(guildId);
        musicManager.getScheduler().setIndex(index);
    }

    public int getIndex(Long guildId) {
        GuildMusicManager musicManager = lavalinkManager.getGuildMusicManager(guildId);
        return musicManager.getScheduler().getIndex();
    }

    public void shuffleQueue(Long guildId) {
        GuildMusicManager musicManager = lavalinkManager.getGuildMusicManager(guildId);
        musicManager.getScheduler().shuffleQueue();
    }

    public SimpleSong findFirstTrack(String query) {
        Track track = lavalinkManager.findFirstTrack(query);
        if (track == null) {
            return null;
        }
        return songService.getOrCreateSong(track);
    }

    // todo : check if broken
    public List<SimpleSong> searchTracks(String query) {
        return lavalinkManager.searchTracks(query).stream()
                .map(songService::getOrCreateSong)
                .toList();
    }

    public List<SimpleSong> searchYoutubeTracks(String query) {
        return lavalinkManager.searchYoutubeTracks(query).stream()
                .map(songService::getOrCreateSong)
                .toList();
    }

    public List<SimpleSong> searchSoundcloudTracks(String query) {
        return lavalinkManager.searchSoundcloudTracks(query).stream()
                .map(songService::getOrCreateSong)
                .toList();
    }

    public SimpleSong playTrackNextAndGetSong(Long guildId, Long channelId, Long requesterId, String query) {
        GuildMusicManager musicManager = lavalinkManager.getGuildMusicManager(guildId);
        TrackScheduler scheduler = musicManager.getScheduler();
        Optional<Track> track;

        try {
            track = Optional.ofNullable(lavalinkManager.findFirstTrack(query));
        } catch (Exception e) {
            return null;
        }
        if (track.isEmpty()) {
            return null;
        }

        SimpleSong song = songService.getOrCreateSong(track.get());

        TrackPlaybackSession session = statsService.createPlaybackSession(
                song.getIdentifier(),
                requesterId.toString(),
                guildId.toString(),
                channelId
        );

        scheduler.playTrackNext(statsService.attachSessionIdToTrack(track.get(), session.getId()));
        return song;
    }

    public SimpleQueue playTrackNext(JDA jda, Long guildId, Long channelId, Long requesterId, String query) {
        GuildMusicManager musicManager = lavalinkManager.getGuildMusicManager(guildId);
        TrackScheduler scheduler = musicManager.getScheduler();
        VoiceChannelUtil.checkAndJoinVoiceChannel(jda, guildId, requesterId,
                () -> playTrackNextAndGetSong(guildId, channelId, requesterId, query),
                reason -> {
                    LOGGER.error("Failed to join voice channel: {}", reason);
                }
        );
        return scheduler.getQueuedTracks();
    }

    public void removeTrack(Long guildId, int index) throws IndexOutOfBoundsException{
        GuildMusicManager musicManager = lavalinkManager.getGuildMusicManager(guildId);
        TrackScheduler scheduler = musicManager.getScheduler();
        scheduler.removeTrack(index);
    }

}
