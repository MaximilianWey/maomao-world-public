package net.maomaocloud.maomaomusic.music.controller;

import net.dv8tion.jda.api.JDA;
import net.maomaocloud.maomaomusic.bot.commands.text.SearchCommand;
import net.maomaocloud.maomaomusic.music.model.SimpleQueue;
import net.maomaocloud.maomaomusic.music.model.SimpleSong;
import net.maomaocloud.maomaomusic.music.manager.TrackScheduler;
import net.maomaocloud.maomaomusic.music.service.MusicService;
import net.maomaocloud.maomaomusic.discord.service.DiscordInfoService;
import net.maomaocloud.maomaomusic.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/discord/music")
public class DiscordMusicController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscordMusicController.class);

    private final JDA jda;
    private final MusicService musicService;
    private final DiscordInfoService discordInfoService;

    public DiscordMusicController(JDA jda, MusicService musicService, DiscordInfoService discordInfoService) {
        this.jda = jda;
        this.musicService = musicService;
        this.discordInfoService = discordInfoService;
    }

    @GetMapping("/{guildId}/now-playing")
    public ResponseEntity<?> getPlaying(@PathVariable Long guildId, @AuthenticationPrincipal Jwt jwt) {
        try {
            Long discordId = JwtUtils.getDiscordId(jwt);

            discordInfoService.verifyAccess(guildId, discordId, jwt);

            Optional<SimpleSong> track = musicService.getCurrentTrack(guildId);
            return track.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NO_CONTENT).build());

        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        }
    }

    @GetMapping("/{guildId}/queue")
    public ResponseEntity<?> getQueue(@PathVariable Long guildId, @AuthenticationPrincipal Jwt jwt) {
        try {
            Long discordId = JwtUtils.getDiscordId(jwt);

            discordInfoService.verifyAccess(guildId, discordId, jwt);

            SimpleQueue queue = musicService.getQueuedTracks(guildId);
            return ResponseEntity.ok(queue);

        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        }
    }

    @PostMapping("/{guildId}/play")
    public ResponseEntity<?> playTrack(@PathVariable Long guildId,
                                       @RequestParam Long channelId,
                                       @RequestParam String query,
                                       @AuthenticationPrincipal Jwt jwt) {
        try {
            Long discordId = JwtUtils.getDiscordId(jwt);

            discordInfoService.verifyAccess(guildId, discordId, jwt);

            if (query.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Query cannot be empty");
            }

            return ResponseEntity.ok(musicService.enqueue(jda, guildId, channelId, discordId.toString(), query));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        }
    }

    @PostMapping("/{guildId}/play-next")
    public ResponseEntity<?> playNextTrack(@PathVariable Long guildId,
                                           @RequestParam String query,
                                           @RequestParam Long channelId,
                                           @AuthenticationPrincipal Jwt jwt) {
        try {
            Long discordId = JwtUtils.getDiscordId(jwt);

            discordInfoService.verifyAccess(guildId, discordId, jwt);

            if (query.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Query cannot be empty");
            }

            return ResponseEntity.ok(musicService.playTrackNext(jda, guildId, channelId, discordId, query));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        }
    }

    @PostMapping("/{guildId}/skip")
    public ResponseEntity<?> skipTrack(@PathVariable Long guildId, @AuthenticationPrincipal Jwt jwt) {
        try {
            Long discordId = JwtUtils.getDiscordId(jwt);

            discordInfoService.verifyAccess(guildId, discordId, jwt);

            musicService.skipTrack(guildId);
            return ResponseEntity.ok().build();

        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        }
    }

    @PostMapping("/{guildId}/previous")
    public ResponseEntity<?> previousTrack(@PathVariable Long guildId, @AuthenticationPrincipal Jwt jwt) {
        try {
            Long discordId = JwtUtils.getDiscordId(jwt);

            discordInfoService.verifyAccess(guildId, discordId, jwt);

            musicService.playPreviousTrack(guildId);
            return ResponseEntity.ok().build();

        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        }
    }

    @PostMapping("/{guildId}/stop")
    public ResponseEntity<?> stop(@PathVariable Long guildId, @AuthenticationPrincipal Jwt jwt) {
        try {
            Long discordId = JwtUtils.getDiscordId(jwt);

            discordInfoService.verifyAccess(guildId, discordId, jwt);

            musicService.stop(guildId);
            return ResponseEntity.noContent().build();

        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        }
    }

    @PostMapping("/{guildId}/reorder-queue")
    public ResponseEntity<?> updateQueue(@PathVariable Long guildId,
                                            @RequestBody SimpleQueue queue,
                                         @AuthenticationPrincipal Jwt jwt) {
        try {
            Long discordId = JwtUtils.getDiscordId(jwt);

            discordInfoService.verifyAccess(guildId, discordId, jwt);

            LOGGER.debug("Received queue update request for guild {}: {}", guildId, queue);

            SimpleQueue updatedQueue = musicService.reorderQueue(guildId, queue);

            return ResponseEntity.ok(updatedQueue);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping("/{guildId}/shuffle")
    public ResponseEntity<?> shuffleQueue(@PathVariable Long guildId, @AuthenticationPrincipal Jwt jwt) {
        try {
            Long discordId = JwtUtils.getDiscordId(jwt);

            discordInfoService.verifyAccess(guildId, discordId, jwt);

            musicService.shuffleQueue(guildId);
            SimpleQueue shuffledQueue =  musicService.getQueuedTracks(guildId);
            return ResponseEntity.ok(shuffledQueue);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        }
    }

    @PostMapping("/{guildId}/set-index/{index}")
    public ResponseEntity<?> setIndex(@PathVariable Long guildId,
                                       @PathVariable int index,
                                       @AuthenticationPrincipal Jwt jwt) {
        try {
            Long discordId = JwtUtils.getDiscordId(jwt);

            discordInfoService.verifyAccess(guildId, discordId, jwt);

            musicService.setIndex(guildId, index);

            var updatedQueue = musicService.getQueuedTracks(guildId);
            return ResponseEntity.ok(updatedQueue);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping("/{guildId}/pause")
    public ResponseEntity<?> pause(@PathVariable Long guildId, @AuthenticationPrincipal Jwt jwt) {
        try {
            Long discordId = JwtUtils.getDiscordId(jwt);

            discordInfoService.verifyAccess(guildId, discordId, jwt);

            musicService.pausePlayback(guildId);
            return ResponseEntity.noContent().build();

        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        }
    }

    @PostMapping("/{guildId}/resume")
    public ResponseEntity<?> resume(@PathVariable Long guildId, @AuthenticationPrincipal Jwt jwt) {
        try {
            Long discordId = JwtUtils.getDiscordId(jwt);

            discordInfoService.verifyAccess(guildId, discordId, jwt);

            musicService.resumePlayback(guildId);
            return ResponseEntity.noContent().build();

        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        }
    }

    @GetMapping("/{guildId}/is-paused")
    public ResponseEntity<?> isPaused(@PathVariable Long guildId, @AuthenticationPrincipal Jwt jwt) {
        try {
            Long discordId = JwtUtils.getDiscordId(jwt);

            discordInfoService.verifyAccess(guildId, discordId, jwt);

            boolean isPaused = musicService.isPaused(guildId);
            return ResponseEntity.ok(isPaused);

        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        }
    }

    @PostMapping("/{guildId}/set-mode/{mode}")
    public ResponseEntity<?> setMode(@PathVariable Long guildId, @PathVariable String mode, @AuthenticationPrincipal Jwt jwt) {
        try {
            Long discordId = JwtUtils.getDiscordId(jwt);

            discordInfoService.verifyAccess(guildId, discordId, jwt);
            TrackScheduler.Mode trackMode = TrackScheduler.Mode.valueOf(mode.toUpperCase());

            musicService.setMode(guildId, trackMode);
            SimpleQueue queue = musicService.getQueuedTracks(guildId);
            return ResponseEntity.ok(queue);

        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        }
    }

    @GetMapping("/{guildId}/search")
    public ResponseEntity<?> search(@PathVariable Long guildId,
                                    @RequestParam String query,
                                    @RequestParam(required = false) String source,
                                    @AuthenticationPrincipal Jwt jwt) {
        try {
            Long discordId = JwtUtils.getDiscordId(jwt);

            discordInfoService.verifyAccess(guildId, discordId, jwt);

            if (query.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Query cannot be empty");
            }

            if (source == null || source.isBlank()) {
                return ResponseEntity.ok(musicService.searchTracks(query));
            }

            SearchCommand.Source sourceType = SearchCommand.Source.fromString(source);
            return switch (sourceType) {
                case YOUTUBE -> ResponseEntity.ok(musicService.searchYoutubeTracks(query));
                case SOUNDCLOUD -> ResponseEntity.ok(musicService.searchSoundcloudTracks(query));
                default -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid source type");
            };
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        }
    }

    @PostMapping("{guildId}/enqueue-playlist")
    public ResponseEntity<?> enqueuePlaylist(@PathVariable Long guildId,
                                             @RequestParam UUID playlistId,
                                             @RequestParam String channelId,
                                             @RequestParam(required = false) boolean shuffle,
                                             @AuthenticationPrincipal Jwt jwt) {
        try {
            Long discordId = JwtUtils.getDiscordId(jwt);

            discordInfoService.verifyAccess(guildId, discordId, jwt);

            var playlistOpt = musicService.enqueuePlaylist(jda,
                    guildId,
                    Long.valueOf(channelId),
                    discordId,
                    playlistId,
                    shuffle
            );

            if (playlistOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Playlist not found");
            }

            return ResponseEntity.ok(musicService.getQueuedTracks(guildId));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        }
    }

    @PostMapping("/{guildId}/remove-track")
    public ResponseEntity<?> removeTrack(@PathVariable Long guildId,
                                         @RequestParam int index,
                                         @AuthenticationPrincipal Jwt jwt) {
        try {
            Long discordId = JwtUtils.getDiscordId(jwt);

            discordInfoService.verifyAccess(guildId, discordId, jwt);

            musicService.removeTrack(guildId, index);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        }
    }

    @PostMapping("/{guildId}/clear-queue")
    public ResponseEntity<?> clearQueue(@PathVariable Long guildId, @AuthenticationPrincipal Jwt jwt) {
        try {
            Long discordId = JwtUtils.getDiscordId(jwt);

            discordInfoService.verifyAccess(guildId, discordId, jwt);

            musicService.clearQueue(guildId);
            return ResponseEntity.noContent().build();

        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        }
    }

}
