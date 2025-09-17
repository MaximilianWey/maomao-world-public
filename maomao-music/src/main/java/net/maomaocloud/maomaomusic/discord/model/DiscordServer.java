package net.maomaocloud.maomaomusic.discord.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "discord_servers")
public class DiscordServer {

    @Id
    @Column(nullable = false, unique = true)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String iconUrl;

    @OneToMany(mappedBy = "server", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<DiscordVoiceChannel> voiceChannels = new ArrayList<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "servers", fetch = FetchType.EAGER)
    private List<DiscordUser> users = new ArrayList<>();

    public DiscordServer() {
        // Default constructor for JPA
    }

    public DiscordServer(Long id, String name, String iconUrl) {
        this.id = String.valueOf(id);
        this.name = name;
        this.iconUrl = iconUrl;
    }

    public Long getIdLong() {
        return Long.valueOf(id);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public List<DiscordVoiceChannel> getVoiceChannels() {
        return voiceChannels;
    }

    public DiscordVoiceChannel addVoiceChannel(Long id, String name) {
        DiscordVoiceChannel channel = new DiscordVoiceChannel(id, name, this);
        voiceChannels.add(channel);
        return channel;
    }

    public List<DiscordUser> getUsers() {
        return users;
    }

    public void addUser(DiscordUser user) {
        if (!users.contains(user)) {
            users.add(user);
            user.getServers().add(this);
        }
    }
}
