package net.maomaocloud.maomaomusic.music.events;

import dev.arbjerg.lavalink.client.player.Track;
import org.springframework.context.ApplicationEvent;

public class TrackStartedEvent extends ApplicationEvent {

    private final Track track;

    public TrackStartedEvent(Object source, Track track) {
        super(source);
        this.track = track;
    }

    public Track getTrack() {
        return track;
    }

}
