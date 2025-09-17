package net.maomaocloud.maomaomusic.music.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "track_playback_sessions")
public class TrackPlaybackSession {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, updatable = false)
    private String songId;

    @Column(nullable = false, updatable = false)
    private String requesterId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "track_playback_listeners",
            joinColumns = @JoinColumn(name = "track_playback_session_id"))
    @Column(name = "listener_id")
    private List<String> listenerIds = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private String guildId;

    @Column(nullable = false, updatable = false)
    private Long voiceChannelId;

    private Long startedAt;

    private Long endedAt;

    public TrackPlaybackSession() {
        // Default constructor for JPA

    }
    public TrackPlaybackSession(List<String> listenerIds) {
        this.listenerIds = listenerIds;
    }

    public UUID getId() {
        return id;
    }

    public String getSongId() {
        return songId;
    }

    public void setSongId(String songId) {
        this.songId = songId;
    }

    public List<String> getListenerIds() {
        return listenerIds;
    }

    public void addListenerId(String listenerId) {
        this.listenerIds.add(listenerId);
    }

    public void removeListenerId(String listenerId) {
        this.listenerIds.remove(listenerId);
    }

    public String getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    public String getGuildId() {
        return guildId;
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }

    public Long getVoiceChannelId() {
        return voiceChannelId;
    }

    public void setVoiceChannelId(Long voiceChannelId) {
        this.voiceChannelId = voiceChannelId;
    }

    public Long getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(long startedAt) {
        this.startedAt = startedAt;
    }

    public Long getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(long endedAt) {
        this.endedAt = endedAt;
    }
}
