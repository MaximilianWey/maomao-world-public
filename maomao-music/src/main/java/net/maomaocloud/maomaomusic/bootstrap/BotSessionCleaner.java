package net.maomaocloud.maomaomusic.bootstrap;

import jakarta.annotation.PostConstruct;
import net.maomaocloud.maomaomusic.music.model.BotSession;
import net.maomaocloud.maomaomusic.music.service.StatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BotSessionCleaner {

    private static final Logger LOGGER = LoggerFactory.getLogger(BotSessionCleaner.class);
    private final StatsService statsService;

    @Autowired
    public BotSessionCleaner(StatsService statsService) {
        this.statsService = statsService;
    }

    @PostConstruct
    public void cleanBotSessions() {
        LOGGER.info("Cleaning bot sessions...");
        List<BotSession> orphans = statsService.removeOrphanedBotSessions();
        int size = orphans.size();
        if (size > 0) {
            LOGGER.warn("{} Orphaned sessions were found! Removing...", size);
        } else {
            LOGGER.info("No orphaned bot sessions found");
        }
    }

}
