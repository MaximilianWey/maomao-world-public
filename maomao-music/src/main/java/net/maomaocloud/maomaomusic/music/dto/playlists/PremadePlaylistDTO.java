package net.maomaocloud.maomaomusic.music.dto.playlists;

import java.util.List;

public record PremadePlaylistDTO(String name,
                                 String visibility,
                                 List<String> tracks) {
}
