package net.maomaocloud.maomaomusic.music.dto.playlists;

import java.util.List;

public record CreatePlaylistDTO(String name,
                                String visibility,
                                List<String> songIds) {
}
