package net.maomaocloud.maomaomusic.music.repositories;

import net.maomaocloud.maomaomusic.music.model.BotSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BotSessionRepository extends JpaRepository<BotSession, UUID> {

    List<BotSession> findByStartedAtBetween(long from, long to);

    List<BotSession> findByStoppedAtIsNull();
}
