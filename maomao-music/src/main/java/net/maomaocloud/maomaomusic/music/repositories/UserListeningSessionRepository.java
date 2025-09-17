package net.maomaocloud.maomaomusic.music.repositories;

import net.maomaocloud.maomaomusic.music.model.UserListeningSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserListeningSessionRepository extends JpaRepository<UserListeningSession, UUID> {

    List<UserListeningSession> findByUserId(String userId);

    List<UserListeningSession> findByUserIdAndLeftAtBetween(String userId, long from, long to);

    List<UserListeningSession> findByGuildIdAndLeftAtBetween(String guildId, long from, long to);

    List<UserListeningSession> findByLeftAtBetween(long from, long to);

    List<UserListeningSession> findByTrackPlaybackSessionIdAndLeftAtIsNull(UUID trackPlaybackSessionId);

    List<UserListeningSession> findByUserIdAndGuildIdAndVoiceChannelIdAndLeftAtIsNull(String userId, String guildId, String voiceChannelId);

    List<UserListeningSession> findByLeftAtIsNull();

    List<UserListeningSession> findByGuildIdAndLeftAtIsNull(String guildId);
}
