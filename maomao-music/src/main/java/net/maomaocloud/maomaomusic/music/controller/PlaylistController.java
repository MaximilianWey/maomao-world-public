package net.maomaocloud.maomaomusic.music.controller;

import net.maomaocloud.maomaomusic.discord.service.DiscordInfoService;
import net.maomaocloud.maomaomusic.music.dto.playlists.CreatePlaylistDTO;
import net.maomaocloud.maomaomusic.music.model.SimplePlaylist;
import net.maomaocloud.maomaomusic.music.service.PlaylistService;
import net.maomaocloud.maomaomusic.utils.JwtUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/playlist")
public class PlaylistController {

    private final PlaylistService playlistService;
    private final DiscordInfoService discordInfoService;

    public PlaylistController(PlaylistService playlistService, DiscordInfoService discordInfoService) {
        this.playlistService = playlistService;
        this.discordInfoService = discordInfoService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createPlaylist(@RequestBody CreatePlaylistDTO playlistDTO,
                                            @AuthenticationPrincipal Jwt jwt) {
        try {
            Long discordId = JwtUtils.getDiscordId(jwt);


            Optional<SimplePlaylist> playlist = playlistService.createPlaylist(playlistDTO.name(),
                    discordId,
                    playlistDTO.visibility(),
                    playlistDTO.songIds());
            if (playlist.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to create playlist");
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(playlist.get());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @GetMapping("/list-public")
    public ResponseEntity<?> getPublicPlaylists(@AuthenticationPrincipal Jwt jwt) {
        try {
            Long _ = JwtUtils.getDiscordId(jwt);
            return ResponseEntity.ok(playlistService.getPublicPlaylists());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @GetMapping("/list-own")
    public ResponseEntity<?> getOwnPlaylists(@AuthenticationPrincipal Jwt jwt) {
        try {
            Long discordId = JwtUtils.getDiscordId(jwt);

            return ResponseEntity.ok(playlistService.getPlaylistsByCreator(discordId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @GetMapping("/list-subscribed")
    public ResponseEntity<?> getSubscribedPlaylists(@AuthenticationPrincipal Jwt jwt) {
        try {
            Long discordId = JwtUtils.getDiscordId(jwt);


            return ResponseEntity.ok(playlistService.getSubscribedPlaylists(discordId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @GetMapping("/{playlistId}")
    public ResponseEntity<?> getPlaylist(@PathVariable UUID playlistId,
                                         @AuthenticationPrincipal Jwt jwt) {
        try {
            Long discordId = JwtUtils.getDiscordId(jwt);

            SimplePlaylist playlist = playlistService.getPlaylistById(discordId, playlistId);
            return ResponseEntity.ok(playlist);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @DeleteMapping("/{playlistId}")
    public ResponseEntity<?> deletePlaylist(@PathVariable UUID playlistId,
                                            @AuthenticationPrincipal Jwt jwt) {
        try {
            Long discordId = JwtUtils.getDiscordId(jwt);

            playlistService.deletePlaylist(discordId, playlistId);
            return ResponseEntity.ok("Playlist deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping("/{playlistId}/add-song")
    public ResponseEntity<?> addSongToPlaylist(@PathVariable UUID playlistId,
                                               @RequestParam String songId,
                                               @AuthenticationPrincipal Jwt jwt) {
        try {
            Long discordId = JwtUtils.getDiscordId(jwt);

            SimplePlaylist playlist = playlistService.addSongToPlaylist(discordId, playlistId, songId);
            return ResponseEntity.ok(playlist);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping("/{playlistId}/remove-song")
    public ResponseEntity<?> removeSongFromPlaylist(@PathVariable UUID playlistId,
                                                    @RequestParam int index,
                                                    @AuthenticationPrincipal Jwt jwt) {
        try {
            Long discordId = JwtUtils.getDiscordId(jwt);

            SimplePlaylist playlist = playlistService.removeSongFromPlaylist(discordId, playlistId, index);
            return ResponseEntity.ok(playlist);
        } catch (IndexOutOfBoundsException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping("/{playlistId}/update-visibility")
    public ResponseEntity<?> updatePlaylistVisibility(@PathVariable UUID playlistId,
                                                       @RequestParam String visibility,
                                                       @AuthenticationPrincipal Jwt jwt) {
        try {
            Long discordId = JwtUtils.getDiscordId(jwt);

            SimplePlaylist playlist = playlistService.updatePlaylistVisibility(discordId, playlistId, visibility);
            return ResponseEntity.ok(playlist);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping("/{playlistId}/update-name")
    public ResponseEntity<?> updatePlaylistName(@PathVariable UUID playlistId,
                                                @RequestParam String name,
                                                @AuthenticationPrincipal Jwt jwt) {
        try {
            Long discordId = JwtUtils.getDiscordId(jwt);

            SimplePlaylist playlist = playlistService.updatePlaylistName(discordId, playlistId, name);
            return ResponseEntity.ok(playlist);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping("/{playlistId}/subscribe")
    public ResponseEntity<?> subscribeToPlaylist(@PathVariable UUID playlistId,
                                                 @AuthenticationPrincipal Jwt jwt) {
        try {
            Long discordId = JwtUtils.getDiscordId(jwt);

            SimplePlaylist playlist = playlistService.subscribeToPlaylist(discordId, playlistId);
            return ResponseEntity.ok(playlist);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping("/{playlistId}/unsubscribe")
    public ResponseEntity<?> unsubscribeFromPlaylist(@PathVariable UUID playlistId,
                                                     @AuthenticationPrincipal Jwt jwt) {
        try {
            Long discordId = JwtUtils.getDiscordId(jwt);

            SimplePlaylist playlist = playlistService.unsubscribeFromPlaylist(discordId, playlistId);
            return ResponseEntity.ok(playlist);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}
