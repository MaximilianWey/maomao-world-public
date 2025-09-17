package net.maomaocloud.maomaomusic.music.service;

import dev.arbjerg.lavalink.client.player.Track;
import jakarta.transaction.Transactional;
import net.maomaocloud.maomaomusic.music.model.SimpleSong;
import net.maomaocloud.maomaomusic.music.repositories.SimpleSongRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SongService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SongService.class);

    private final SimpleSongRepository simpleSongRepository;

    @Autowired
    public SongService(SimpleSongRepository simpleSongRepository) {
        this.simpleSongRepository = simpleSongRepository;
    }

    public void saveSong(SimpleSong song) {
        simpleSongRepository.save(song);
    }

    public Optional<SimpleSong> getSongById(String id) {
        return simpleSongRepository.findById(id);
    }

    @Transactional
    public SimpleSong getOrCreateSong(Track track) {
        LOGGER.info("Getting or creating song for track: {}", track.getInfo().getIdentifier());
        return getSongById(track.getInfo().getIdentifier())
                .orElseGet(() -> this.simpleSongRepository.save(new SimpleSong(track)));
    }

}
