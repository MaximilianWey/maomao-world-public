package net.maomaocloud.maomaomusic.music.dto;

public record ListeningTimeDTO(StatsQueryParam requestedParams,
                               Long totalListeningTimeMS) {
}
