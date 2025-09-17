package net.maomaocloud.maomaomusic.music.repositories;

import net.maomaocloud.maomaomusic.music.model.TrackPlaybackSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TrackPlaybackSessionRepository extends JpaRepository<TrackPlaybackSession, UUID> {

    List<TrackPlaybackSession> findByRequesterId(String requesterId);

    List<TrackPlaybackSession> findByGuildIdAndStartedAtBetween(String guildId, long from, long to);

    List<TrackPlaybackSession> findByStartedAtBetween(long from, long to);

    List<TrackPlaybackSession> findAllByStartedAtBetween(long epochMilli, long epochMilli1);

    List<TrackPlaybackSession> findAllByStartedAtIsNull();

    List<TrackPlaybackSession> findAllByEndedAtIsNull();

    List<TrackPlaybackSession> findByGuildIdAndStartedAtIsNull(String guildId);
}
