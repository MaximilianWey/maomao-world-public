package net.maomaocloud.maomaomusic.music.manager;


import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.protocol.v4.Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason;
import dev.arbjerg.lavalink.protocol.v4.TrackInfo;
import net.maomaocloud.maomaomusic.music.events.QueueClearedEvent;
import net.maomaocloud.maomaomusic.music.model.SimpleQueue;
import net.maomaocloud.maomaomusic.music.model.SimpleSong;
import net.maomaocloud.maomaomusic.music.events.TrackEndedEvent;
import net.maomaocloud.maomaomusic.music.events.TrackStartedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.util.*;

public class TrackScheduler {

    public enum Mode {
        REPEAT_QUEUE,
        REPEAT_SONG,
        NORMAL
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(TrackScheduler.class);

    private final ApplicationEventPublisher eventPublisher;

    private final GuildMusicManager guildMusicManager;
    private final List<Track> queue;
    private int currentTrackIndex;
    private Track lastPlayedTrack;
    private Mode mode;
    private boolean isPaused;

    public TrackScheduler(GuildMusicManager guildMusicManager, ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;

        this.guildMusicManager = guildMusicManager;
        this.queue = new ArrayList<>();
        this.currentTrackIndex = -1;
        this.lastPlayedTrack = null;
        this.mode = Mode.NORMAL;
        this.isPaused = false;
    }

    public void enqueue(@NotNull Track track) {
        this.queue.add(track);
        if (currentTrackIndex == -1) {
            this.currentTrackIndex = queue.size() - 1;
            this.startTrack(track);
        }
    }

    public void enqueuePlaylist(List<Track> tracks) {
        this.queue.addAll(tracks);
        if (currentTrackIndex == -1) {
            this.currentTrackIndex = queue.size() - tracks.size() ;
            this.startTrack(this.queue.getFirst());
        }
    }

    public void playTrackNext(@NotNull Track track) {
        int insertIndex = currentTrackIndex + 1;
        if (currentTrackIndex == -1 || insertIndex >= queue.size()) {
            this.enqueue(track);
        } else {
            this.queue.add(insertIndex, track);
        }
    }

    private void startTrack(Track track) {
        LOGGER.info("Starting track: {} (paused={})", track.getInfo().getTitle(), isPaused);
        this.guildMusicManager.getLink()
                .createOrUpdatePlayer()
                .setTrack(track)
                .setVolume(35)
                .setPaused(isPaused)
                .subscribe();
        lastPlayedTrack = track;
    }

    public void onTrackStart(Track track) {
        eventPublisher.publishEvent(new TrackStartedEvent(this, track));
    }

    public void onTrackEnd(Track lastTrack, AudioTrackEndReason reason) {
        LOGGER.info("Track ended: {} (reason: {})", lastTrack.getInfo().getTitle(), reason);
        eventPublisher.publishEvent(new TrackEndedEvent(this, lastTrack));

        if (!reason.getMayStartNext()) return;

        switch (mode) {
            case REPEAT_SONG -> {
                if (lastPlayedTrack != null) {
                    startTrack(lastPlayedTrack);
                }
            }
            case REPEAT_QUEUE -> {
                currentTrackIndex++;
                if (currentTrackIndex >= queue.size()) {
                    currentTrackIndex = 0;
                }
                startTrack(queue.get(currentTrackIndex));
            }
            case NORMAL -> {
                currentTrackIndex++;
                if (currentTrackIndex < queue.size()) {
                    startTrack(queue.get(currentTrackIndex));
                } else {
                    guildMusicManager.stop();
                }
            }
        }
    }

    public void clearQueue() {
        this.queue.clear();
        this.currentTrackIndex = -1;
        this.lastPlayedTrack = null;
        this.guildMusicManager.stop();
        String guildID = guildMusicManager.getGuildId().toString();
        eventPublisher.publishEvent(new QueueClearedEvent(this, guildID));
    }

    public void reset() {
        this.currentTrackIndex = -1;
        this.lastPlayedTrack = null;
    }

    public SimpleQueue getQueuedTracks() {
        return new SimpleQueue(this.queue.stream()
                .map(SimpleSong::new)
                .toList(),
                currentTrackIndex,
                this.mode);
    }

    public Optional<SimpleSong> getCurrentTrack() {
        return (currentTrackIndex >= 0 && currentTrackIndex < this.queue.size())
                ? Optional.of(new SimpleSong(this.queue.get(currentTrackIndex).getInfo()))
                : Optional.empty();
    }

    public void skipTrack() {
        switch (mode) {
            case REPEAT_SONG -> startTrack(queue.get(currentTrackIndex));
            case REPEAT_QUEUE -> {
                currentTrackIndex++;
                if (currentTrackIndex >= queue.size()) currentTrackIndex = 0;
                startTrack(queue.get(currentTrackIndex));
            }
            case NORMAL -> {
                currentTrackIndex++;
                if (currentTrackIndex < queue.size()) {
                    startTrack(queue.get(currentTrackIndex));
                } else {
                    guildMusicManager.stop();
                }
            }
        }
    }

    public void pause() {
        guildMusicManager.getLink()
                .createOrUpdatePlayer()
                .setPaused(true)
                .subscribe();
        this.isPaused = true;
    }

    public void resume() {
        guildMusicManager.getLink()
                .createOrUpdatePlayer()
                .setPaused(false)
                .subscribe();
        this.isPaused = false;
    }

    public boolean isPaused() {
        return this.isPaused;
    }

    public void removeTrack(int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= queue.size()) {
            throw new IndexOutOfBoundsException("Invalid index: " + index);
        }
        this.queue.remove(index);
        if (index < currentTrackIndex) {
            currentTrackIndex--;
        } else if (index == currentTrackIndex) {
            if (currentTrackIndex >= queue.size()) {
                currentTrackIndex = queue.size() - 1;
            }
            if (currentTrackIndex >= 0) {
                startTrack(queue.get(currentTrackIndex));
            } else {
                guildMusicManager.stop();
            }
        }
    }

    public Optional<SimpleSong> previousTrack() {
        if (currentTrackIndex > 0) {
            currentTrackIndex--;
            this.startTrack(this.queue.get(currentTrackIndex));
            return Optional.of(new SimpleSong(this.queue.get(currentTrackIndex).getInfo()));
        } else if (!queue.isEmpty()){
            currentTrackIndex = queue.size() - 1;
            this.startTrack(this.queue.get(currentTrackIndex));
            return Optional.of(new SimpleSong(this.queue.get(currentTrackIndex).getInfo()));
        }
        return Optional.empty();
    }

    public void shuffleQueue() {
        if (queue.size() > 1) {
            Collections.shuffle(queue);
            currentTrackIndex = 0;
            startTrack(queue.get(currentTrackIndex));
        }
    }

    public SimpleQueue updateQueue(SimpleQueue queue) {
        List<Track> updatedQueue = queue.songs().stream()
                .map(this::getTrack)
                .flatMap(Optional::stream)
                .toList();
        this.queue.clear();
        this.queue.addAll(updatedQueue);

        LOGGER.debug("Updating queue; \nold queue: {} \nnew queue: {}",
                this.queue.stream().map(Track::getInfo).map(TrackInfo::getTitle).toList(),
                updatedQueue.stream().map(Track::getInfo).map(TrackInfo::getTitle).toList());
        LOGGER.debug("Old index: {}", this.currentTrackIndex);
        LOGGER.debug("New index: {}", queue.currentIndex());

        setIndex(queue.currentIndex());
        setMode(queue.mode());
        return new SimpleQueue(this.queue.stream()
                .map(Track::getInfo)
                .map(SimpleSong::new)
                .toList(),
                queue.currentIndex(),
                queue.mode());
    }

    public Optional<Track> getTrack(SimpleSong song) {
        return this.queue.stream()
                .filter(track -> track.getInfo().getIdentifier().equals(song.getIdentifier()))
                .findFirst();
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Mode getMode() {
        return this.mode;
    }

    public void setIndex(int i) throws IllegalArgumentException {
        if (i < 0 || i >= this.queue.size()) {
            throw new IllegalArgumentException("Invalid index: " + i);
        }
        this.currentTrackIndex = i;
    }

    public int getIndex() {
        return this.currentTrackIndex;
    }
}