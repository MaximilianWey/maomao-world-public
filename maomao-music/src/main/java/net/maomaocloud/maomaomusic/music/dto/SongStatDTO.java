package net.maomaocloud.maomaomusic.music.dto;

import net.maomaocloud.maomaomusic.music.model.SimpleSong;

public record SongStatDTO(SimpleSong song, long playCount) {
}
