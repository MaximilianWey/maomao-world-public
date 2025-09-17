package net.maomaocloud.maomaomusic.music.repositories;

import net.maomaocloud.maomaomusic.music.model.SimpleSong;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SimpleSongRepository extends JpaRepository<SimpleSong, String> {
}
