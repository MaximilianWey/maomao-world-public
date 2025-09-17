package net.maomaocloud.maomaomusic.discord.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import net.maomaocloud.maomaomusic.music.model.SimplePlaylist;

import java.util.*;

@Entity
@Table(name = "discord_users")
public class DiscordUser {

    @Id
    @Column(nullable = false, unique = true)
    @JsonProperty("discordId")
    private String discordId;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "unique_name", nullable = false)
    private String uniqueName;

    @Column(name = "avatar_url", nullable = false)
    private String avatarUrl;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "discord_user_servers",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "server_id")
    )
    private Set<DiscordServer> servers = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<SimplePlaylist> createdPlaylists = new HashSet<>();

    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "playlist_subscribers",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "playlist_id")
    )
    private Set<SimplePlaylist> subscribedPlaylists = new HashSet<>();

    public DiscordUser() {}

    public DiscordUser(String id, String username, String uniqueName, String avatarUrl) {
        this.discordId = id;
        this.username = username;
        this.uniqueName = uniqueName;
        this.avatarUrl = avatarUrl;
    }

    public DiscordUser(Long id, String username, String uniqueName, String avatarUrl) {
        this(id.toString(), username, uniqueName, avatarUrl);
    }

    public String getId() {
        return discordId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Set<SimplePlaylist> getCreatedPlaylists() {
        return createdPlaylists;
    }

    public void setCreatedPlaylists(Set<SimplePlaylist> createdPlaylists) {
        this.createdPlaylists = createdPlaylists;
    }

    public Set<SimplePlaylist> getSubscribedPlaylists() {
        return subscribedPlaylists;
    }

    public void setSubscribedPlaylists(Set<SimplePlaylist> subscribedPlaylists) {
        this.subscribedPlaylists = subscribedPlaylists;
    }

    public Set<DiscordServer> getServers() {
        return servers;
    }

    public List<DiscordServer> getServersAsSortedList() {
        return new ArrayList<>(servers).stream()
                .sorted(Comparator.comparing(DiscordServer::getName)
                        .thenComparing(DiscordServer::getIdLong)
                )
                .toList();
    }

    public void addServer(DiscordServer server) {
        this.servers.add(server);
        server.getUsers().add(this);
    }

    public void removeServer(DiscordServer server) {
        this.servers.remove(server);
        server.getUsers().remove(this);
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }

}
