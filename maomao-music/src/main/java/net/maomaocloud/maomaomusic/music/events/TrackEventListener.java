package net.maomaocloud.maomaomusic.music.events;

import net.dv8tion.jda.api.JDA;
import net.maomaocloud.maomaomusic.music.model.TrackPlaybackSession;
import net.maomaocloud.maomaomusic.music.model.TrackUserDetail;
import net.maomaocloud.maomaomusic.music.service.StatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class TrackEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrackEventListener.class);

    private final StatsService statsService;
    private final JDA jda;

    @Autowired
    public TrackEventListener(StatsService statsService, JDA jda) {
        this.statsService = statsService;
        this.jda = jda;
    }

    @EventListener
    public void onTrackStarted(TrackStartedEvent event) {
        Optional<TrackUserDetail> trackDataOpt = statsService.extractTrackUserDetail(event.getTrack());
        if (trackDataOpt.isPresent()) {
            TrackUserDetail trackData = trackDataOpt.get();
            UUID sessionId = trackData.sessionId();
            Optional<TrackPlaybackSession> session = statsService.getPlaybackSessionById(sessionId);
            if (session.isPresent()) {
                String guildId = session.get().getGuildId();
                long channelId = session.get().getVoiceChannelId();
                statsService.markSessionStarted(sessionId, getCurrentVoiceChannelParticipants(guildId, channelId));
            }
        } else {
            LOGGER.warn("Track started without session data: {}", event.getTrack().getInfo().getTitle());
        }
    }

    @EventListener
    public void onTrackEnded(TrackEndedEvent event) {
        Optional<TrackUserDetail> trackDataOpt = statsService.extractTrackUserDetail(event.getTrack());
        if (trackDataOpt.isPresent()) {
            TrackUserDetail trackData = trackDataOpt.get();
            UUID sessionId = trackData.sessionId();
            Optional<TrackPlaybackSession> session = statsService.getPlaybackSessionById(sessionId);
            if (session.isPresent()) {
                statsService.markSessionEnded(sessionId);
            }
        } else {
            LOGGER.warn("Track ended without session data: {}", event.getTrack().getInfo().getTitle());
        }
    }

    @EventListener
    public void onQueueCleared(QueueClearedEvent event) {
        statsService.clearUnstartedSessions(event.getGuildId());
    }

    private List<String> getCurrentVoiceChannelParticipants(String guildId, Long channelId) {
        return jda.getGuildById(guildId).getVoiceChannels().stream()
                .filter(channel -> channel.getIdLong() == channelId)
                .flatMap(channel -> channel.getMembers().stream()
                        .map(member -> member.getUser().getId()))
                .toList();
    }
}
