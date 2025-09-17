package net.maomaocloud.maomaomusic.music.lavalink;

import dev.arbjerg.lavalink.protocol.v4.TrackInfo;

import java.util.List;

public record LoadResult(List<TrackInfo> tracks,
                         String message) {
}
