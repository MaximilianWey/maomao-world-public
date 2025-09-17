package net.maomaocloud.maomaomusic.music.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "bot_sessions")
public class BotSession {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String botId;

    @Column(nullable = false)
    private long startedAt;

    private Long stoppedAt;

    @Column(name = "voice_channel_id", nullable = false)
    private String voiceChannelId;

    public BotSession() {}

    public BotSession(String botId, long startedAt, String voiceChannelId) {
        this.botId = botId;
        this.startedAt = startedAt;
        this.voiceChannelId = voiceChannelId;
    }

    public UUID getId() {
        return id;
    }

    public String getBotId() {
        return botId;
    }

    public void setBotId(String botId) {
        this.botId = botId;
    }

    public long getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(long startedAt) {
        this.startedAt = startedAt;
    }

    public Long getStoppedAt() {
        return stoppedAt;
    }

    public void setStoppedAt(Long stoppedAt) {
        this.stoppedAt = stoppedAt;
    }

    public String getVoiceChannel() {
        return voiceChannelId;
    }

    public void setVoiceChannel(String voiceChannelId) {
        this.voiceChannelId = voiceChannelId;
    }
}
