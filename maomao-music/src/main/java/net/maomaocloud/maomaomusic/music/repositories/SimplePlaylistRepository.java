package net.maomaocloud.maomaomusic.music.repositories;

import net.maomaocloud.maomaomusic.discord.model.DiscordUser;
import net.maomaocloud.maomaomusic.music.model.SimplePlaylist;
import net.maomaocloud.maomaomusic.music.model.SimplePlaylist.Visibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SimplePlaylistRepository extends JpaRepository<SimplePlaylist, UUID> {

    boolean existsByCreatorAndName(DiscordUser creator, String name);

    Optional<SimplePlaylist> findByCreatorAndName(DiscordUser creator, String name);

    List<SimplePlaylist> findAllByCreator(DiscordUser creator);

    List<SimplePlaylist> findAllBySubscribersContains(DiscordUser user);

    List<SimplePlaylist> findAllByVisibility(Visibility visibility);

    @Query("SELECT p FROM SimplePlaylist p WHERE p.creator = :user AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<SimplePlaylist> findByCreatorAndNameContainingIgnoreCase(@Param("user") DiscordUser user, @Param("name") String name);

    @Query("SELECT p FROM SimplePlaylist p JOIN p.subscribers s WHERE s = :user AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<SimplePlaylist> findBySubscriberAndNameContainingIgnoreCase(@Param("user") DiscordUser user, @Param("name") String name);

    @Query("SELECT p FROM SimplePlaylist p WHERE p.visibility = 'PUBLIC' AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<SimplePlaylist> findPublicPlaylistsByNameContainingIgnoreCase(@Param("name") String name);

    boolean existsByNameAndVisibility(String name, Visibility visibility);
}
