package net.maomaocloud.maomaomusic.music.model;

import net.maomaocloud.maomaomusic.music.manager.TrackScheduler;

import java.util.List;

public record SimpleQueue(List<SimpleSong> songs,
                          int currentIndex,
                          TrackScheduler.Mode mode) {
    public SimpleQueue {
        if (currentIndex < -1 || currentIndex >= songs.size()) {
            throw new IllegalArgumentException("Invalid current index: " + currentIndex);
        }
    }
}
