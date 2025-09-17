package net.maomaocloud.maomaomusic.music.model;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record TrackUserDetail(@NotNull UUID sessionId) {

    @JsonCreator
    public TrackUserDetail(@JsonProperty("sessionId") UUID sessionId) {
        this.sessionId = sessionId;
    }
}
