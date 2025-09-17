package net.maomaocloud.maomaomusic.bootstrap;

import jakarta.annotation.PostConstruct;
import net.maomaocloud.maomaomusic.music.model.TrackPlaybackSession;
import net.maomaocloud.maomaomusic.music.service.StatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TrackPlaybackSessionCleaner {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrackPlaybackSessionCleaner.class);
    private final StatsService statsService;

    @Autowired
    public TrackPlaybackSessionCleaner(StatsService statsService) {
        this.statsService = statsService;
    }

    @PostConstruct
    public void cleanPlaybackSessions() {
        LOGGER.info("Cleaning playback sessions...");
        List<TrackPlaybackSession> orphans = statsService.removeOrphanedPlaybackSessions();
        int size = orphans.size();
        if (size > 0) {
            LOGGER.warn("{} Orphaned sessions were found! Removing...", size);
        } else {
            LOGGER.info("No orphaned track playback sessions found");
        }
    }

}
