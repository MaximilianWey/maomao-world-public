package net.maomaocloud.maomaomusic.music.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "user_listening_sessions")
public class UserListeningSession {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "guild_id", nullable = false)
    private String guildId;

    @Column(name = "voice_channel_id", nullable = false)
    private String voiceChannelId;

    @Column(name = "track_playback_session_id", nullable = false)
    private UUID trackPlaybackSessionId;

    @Column(name = "left_at")
    private Long leftAt;

    public UserListeningSession() {}

    public UserListeningSession(String userId, String guildId, String voiceChannelId, UUID trackPlaybackSessionId, Long leftAt) {
        this.userId = userId;
        this.guildId = guildId;
        this.voiceChannelId = voiceChannelId;
        this.trackPlaybackSessionId = trackPlaybackSessionId;
        this.leftAt = leftAt;
    }

    public UUID getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGuildId() {
        return guildId;
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }

    public UUID getTrackPlaybackSessionId() {
        return trackPlaybackSessionId;
    }

    public void setTrackPlaybackSessionId(UUID trackPlaybackSessionId) {
        this.trackPlaybackSessionId = trackPlaybackSessionId;
    }

    public String getVoiceChannelId() {
        return voiceChannelId;
    }

    public void setVoiceChannelId(String voiceChannelId) {
        this.voiceChannelId = voiceChannelId;
    }

    public Long getLeftAt() {
        return leftAt;
    }

    public void setLeftAt(long leftAt) {
        this.leftAt = leftAt;
    }
}
