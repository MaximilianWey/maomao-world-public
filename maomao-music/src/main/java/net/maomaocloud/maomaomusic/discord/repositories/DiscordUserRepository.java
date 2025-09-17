package net.maomaocloud.maomaomusic.discord.repositories;

import net.maomaocloud.maomaomusic.discord.model.DiscordUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscordUserRepository extends JpaRepository<DiscordUser, String> {

}
