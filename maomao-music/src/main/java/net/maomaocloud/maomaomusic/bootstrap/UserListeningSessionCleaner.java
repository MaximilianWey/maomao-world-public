package net.maomaocloud.maomaomusic.bootstrap;

import jakarta.annotation.PostConstruct;
import net.maomaocloud.maomaomusic.music.model.UserListeningSession;
import net.maomaocloud.maomaomusic.music.service.StatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserListeningSessionCleaner {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserListeningSessionCleaner.class);
    private final StatsService statsService;

    @Autowired
    public UserListeningSessionCleaner(StatsService statsService) {
        this.statsService = statsService;
    }

    @PostConstruct
    public void cleanUserListeningSessions() {
        LOGGER.info("Cleaning user listening sessions...");
        List<UserListeningSession> orphans = statsService.removeOrphanedListeningSessions();
        int size = orphans.size();
        if (size > 0) {
            LOGGER.warn("{} Orphaned sessions were found! Removing...", size);
        } else {
            LOGGER.info("No orphaned user listening sessions found");
        }
    }

}
