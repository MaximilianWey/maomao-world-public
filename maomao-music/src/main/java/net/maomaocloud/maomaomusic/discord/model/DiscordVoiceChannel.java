package net.maomaocloud.maomaomusic.discord.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "discord_voice_channels")
public class DiscordVoiceChannel {

    @Id
    @Column(nullable = false, unique = true)
    private String id;

    @Column(nullable = false)
    private String name;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(nullable = false)
    private DiscordServer server;

    public DiscordVoiceChannel() {
        // Default constructor for JPA
    }

    public DiscordVoiceChannel(Long id, String name, DiscordServer server) {
        this.id = id.toString();
        this.name = name;
        this.server = server;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return Long.valueOf(id);
    }

    public void setId(Long id) {
        this.id = id.toString();
    }

    public DiscordServer getServer() {
        return server;
    }

    public void setServer(DiscordServer server) {
        this.server = server;
    }
}
