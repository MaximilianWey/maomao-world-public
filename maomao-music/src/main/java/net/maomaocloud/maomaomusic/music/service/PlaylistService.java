package net.maomaocloud.maomaomusic.music.service;

import net.maomaocloud.maomaomusic.discord.model.DiscordUser;
import net.maomaocloud.maomaomusic.discord.service.DiscordUserService;
import net.maomaocloud.maomaomusic.music.model.SimplePlaylist;
import net.maomaocloud.maomaomusic.music.model.SimpleSong;
import net.maomaocloud.maomaomusic.music.model.SimplePlaylist.Visibility;
import net.maomaocloud.maomaomusic.music.repositories.SimplePlaylistRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PlaylistService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlaylistService.class);

    private final DiscordUserService discordUserService;
    private final SongService songService;
    private final SimplePlaylistRepository simplePlaylistRepository;

    @Autowired
    public PlaylistService(DiscordUserService discordUserService,
                           SongService songService,
                           SimplePlaylistRepository simplePlaylistRepository) {
        this.discordUserService = discordUserService;
        this.songService = songService;
        this.simplePlaylistRepository = simplePlaylistRepository;
    }

    public Optional<SimplePlaylist> createPlaylist(String name, Long creatorId, String visibilityStr, List<String> songIds) {
        DiscordUser creator = getUserOrThrow(creatorId);
        Visibility visibility = Visibility.fromString(visibilityStr);

        List<SimpleSong> songs = songIds.stream()
                .map(songService::getSongById)
                .peek(opt -> {
                    if (opt.isEmpty()) {
                        LOGGER.warn("Song not found for ID: {}", opt);
                    }
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        if (!isValidPlaylist(name, creator, visibility)) return Optional.empty();

        SimplePlaylist playlist = new SimplePlaylist(name, creator, songs, visibility);
        simplePlaylistRepository.save(playlist);
        return Optional.of(playlist);
    }

    public SimplePlaylist getPlaylistById(Long userId, UUID playlistId) {
        DiscordUser user = getUserOrThrow(userId);
        SimplePlaylist playlist = getPlaylistOrThrow(playlistId);
        requireViewPermission(user, playlist);
        return playlist;
    }

    public List<SimplePlaylist> getPlaylistsByCreator(Long userId) {
        DiscordUser creator = getUserOrThrow(userId);
        return simplePlaylistRepository.findAllByCreator(creator);
    }

    public Optional<SimplePlaylist> getPlaylistByName(Long userId, String name) {
        DiscordUser user = getUserOrThrow(userId);
        List<SimplePlaylist> ownPlaylists =
                simplePlaylistRepository.findByCreatorAndNameContainingIgnoreCase(user, name);

        if (!ownPlaylists.isEmpty()) {
            return ownPlaylists.stream().findFirst();
        }

        List<SimplePlaylist> subscribedPlaylists =
                simplePlaylistRepository.findBySubscriberAndNameContainingIgnoreCase(user, name);

        if (!subscribedPlaylists.isEmpty()) {
            return subscribedPlaylists.stream().findFirst();
        }

        List<SimplePlaylist> publicPlaylists =
                simplePlaylistRepository.findPublicPlaylistsByNameContainingIgnoreCase(name);

        return publicPlaylists.stream().findFirst();
    }

    public SimplePlaylist addSongToPlaylist(Long userId, UUID playlistId, String songId) {
        DiscordUser user = getUserOrThrow(userId);
        SimplePlaylist playlist = getPlaylistOrThrow(playlistId);
        requireCreatorOwnership(user, playlist);

        SimpleSong song = songService.getSongById(songId)
                .orElseThrow(() -> new IllegalStateException("Song not found"));

        playlist.addSong(song);
        return simplePlaylistRepository.save(playlist);
    }

    public SimplePlaylist removeSongFromPlaylist(Long userId, UUID playlistId, int index) {
        DiscordUser user = getUserOrThrow(userId);
        SimplePlaylist playlist = getPlaylistOrThrow(playlistId);
        requireCreatorOwnership(user, playlist);

        if (index < 0 || index >= playlist.getSongs().size()) {
            throw new IndexOutOfBoundsException("Invalid song index");
        }

        playlist.removeSongAtIndex(index);
        return simplePlaylistRepository.save(playlist);
    }

    public SimplePlaylist updatePlaylistVisibility(Long userId, UUID playlistId, String visibilityStr) {
        DiscordUser user = getUserOrThrow(userId);
        SimplePlaylist playlist = getPlaylistOrThrow(playlistId);
        requireCreatorOwnership(user, playlist);

        playlist.setVisibility(Visibility.fromString(visibilityStr));
        return simplePlaylistRepository.save(playlist);
    }

    public void deletePlaylist(Long userId, UUID playlistId) {
        DiscordUser user = getUserOrThrow(userId);
        SimplePlaylist playlist = getPlaylistOrThrow(playlistId);
        requireCreatorOwnership(user, playlist);

        simplePlaylistRepository.delete(playlist);
    }

    public SimplePlaylist updatePlaylistName(Long userId, UUID playlistId, String newName) {
        DiscordUser user = getUserOrThrow(userId);
        SimplePlaylist playlist = getPlaylistOrThrow(playlistId);
        requireCreatorOwnership(user, playlist);

        playlist.setName(newName);
        return simplePlaylistRepository.save(playlist);
    }

    public SimplePlaylist subscribeToPlaylist(Long userId, UUID playlistId) {
        DiscordUser user = getUserOrThrow(userId);
        SimplePlaylist playlist = getPlaylistOrThrow(playlistId);
        requireViewPermission(user, playlist);

        playlist.addSubscriber(user);
        return simplePlaylistRepository.save(playlist);
    }

    public SimplePlaylist unsubscribeFromPlaylist(Long userId, UUID playlistId) {
        DiscordUser user = getUserOrThrow(userId);
        SimplePlaylist playlist = getPlaylistOrThrow(playlistId);

        if (!playlist.getSubscribers().contains(user)) {
            throw new IllegalStateException("You are not subscribed to this playlist");
        }

        playlist.removeSubscriber(user);
        return simplePlaylistRepository.save(playlist);
    }

    public List<SimplePlaylist> getSubscribedPlaylists(Long userId) {
        DiscordUser user = getUserOrThrow(userId);
        return simplePlaylistRepository.findAllBySubscribersContains(user).stream()
                .filter(playlist -> playlist.getVisibility() != Visibility.PRIVATE
                        || playlist.getCreator().equals(user))
                .toList();
    }

    public List<SimplePlaylist> getPublicPlaylists() {
        return simplePlaylistRepository.findAllByVisibility(Visibility.PUBLIC);
    }

    // ---------- Private Helpers ----------

    private DiscordUser getUserOrThrow(Long userId) {
        return discordUserService.getDiscordUser(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }

    private SimplePlaylist getPlaylistOrThrow(UUID playlistId) {
        return simplePlaylistRepository.findById(playlistId)
                .orElseThrow(() -> new IllegalStateException("Playlist not found"));
    }

    private void requireCreatorOwnership(DiscordUser user, SimplePlaylist playlist) {
        if (!playlist.getCreator().equals(user)) {
            throw new IllegalStateException("You do not have access to this playlist");
        }
    }

    private void requireViewPermission(DiscordUser user, SimplePlaylist playlist) {
        if (playlist.getVisibility() == Visibility.PRIVATE
                && !playlist.getCreator().equals(user)) {
            throw new IllegalStateException("You do not have access to this playlist");
        }
    }

    private boolean isValidPlaylist(String name, DiscordUser creator, Visibility visibility) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Playlist name cannot be null or empty");
        }
        if (simplePlaylistRepository.existsByCreatorAndName(creator, name)) {
            throw new IllegalArgumentException("Playlist with this name already exists for this creator");
        }
        // name must be globally unique
        if (visibility == Visibility.PUBLIC && simplePlaylistRepository.existsByNameAndVisibility(name, Visibility.PUBLIC)) {
            throw new IllegalArgumentException("A public playlist with this name already exists");
        }
        return true;
    }
}
