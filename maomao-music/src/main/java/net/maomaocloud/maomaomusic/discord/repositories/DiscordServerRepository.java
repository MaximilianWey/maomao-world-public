package net.maomaocloud.maomaomusic.discord.repositories;

import net.maomaocloud.maomaomusic.discord.model.DiscordServer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscordServerRepository extends JpaRepository<DiscordServer, String> {
}
