package net.maomaocloud.maomaomusic.bootstrap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import net.maomaocloud.maomaomusic.discord.service.DiscordUserService;
import net.maomaocloud.maomaomusic.music.dto.playlists.PremadePlaylistDTO;
import net.maomaocloud.maomaomusic.music.model.SimpleSong;
import net.maomaocloud.maomaomusic.music.service.MusicService;
import net.maomaocloud.maomaomusic.music.service.PlaylistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class RegisterDefaultPlaylists {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterDefaultPlaylists.class);

    @Value("${premade.playlists.enabled:false}")
    private boolean enabled;

    @Value("${premade.playlists.file}")
    private Resource jsonFile;

    private final PlaylistService playlistService;
    private final MusicService musicService;
    private final DiscordUserService discordUserService;
    private final ObjectMapper objectMapper;

    @Autowired
    public RegisterDefaultPlaylists(PlaylistService playlistService,
                                    MusicService musicService,
                                    DiscordUserService discordUserService,
                                    ObjectMapper objectMapper) {
        this.playlistService = playlistService;
        this.musicService = musicService;
        this.discordUserService = discordUserService;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void registerDefaultPlaylists() {
        if (!enabled) {
            LOGGER.info("Premade playlists are disabled. Skipping registration.");
            return;
        }

        try {
            List<PremadePlaylistDTO> playlistDTOS = objectMapper.readValue(
                    jsonFile.getInputStream(),
                    new TypeReference<>() {}
            );

            for (PremadePlaylistDTO dto : playlistDTOS) {
                Optional<?> existing = playlistService.getPlaylistByName(Long.valueOf(discordUserService.getBotUser().getId()), dto.name());
                if (existing.isPresent()) {
                    LOGGER.info("Skipping already existing premade playlist: {}", dto.name());
                    continue;
                }

                List<SimpleSong> songs = dto.tracks().stream()
                        .map(musicService::findFirstTrack)
                        .peek(song -> LOGGER.info("Adding song '{}' to playlist '{}'", song.getTitle(), dto.name()))
                        .toList();

                playlistService.createPlaylist(dto.name(),
                        Long.valueOf(discordUserService.getBotUser().getId()),
                        dto.visibility(),
                        songs.stream().map(SimpleSong::getIdentifier).toList()
                );

                LOGGER.info("Registered premade playlist: {} with {} songs", dto.name(), songs.size());
            }
        } catch (Exception e) {
            LOGGER.error("Failed to register premade playlists", e);
        }
    }
}
