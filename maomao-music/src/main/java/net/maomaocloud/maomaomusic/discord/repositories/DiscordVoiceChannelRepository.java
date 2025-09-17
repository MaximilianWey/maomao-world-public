package net.maomaocloud.maomaomusic.discord.repositories;

import net.maomaocloud.maomaomusic.discord.model.DiscordVoiceChannel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscordVoiceChannelRepository extends JpaRepository<DiscordVoiceChannel, String> {
}
