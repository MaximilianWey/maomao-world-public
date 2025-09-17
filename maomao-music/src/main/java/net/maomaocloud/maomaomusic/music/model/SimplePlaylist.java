package net.maomaocloud.maomaomusic.music.model;

import jakarta.persistence.*;
import net.maomaocloud.maomaomusic.discord.model.DiscordUser;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.util.*;

@Entity
@Table(name = "simple_playlists", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"creator_id", "name"})
})
public class SimplePlaylist {

    public enum Visibility {
        PUBLIC,
        FRIENDS,
        PRIVATE;

        public static Visibility fromString(String str) {
            return switch (str.toUpperCase()) {
                case "PUBLIC" -> PUBLIC;
                case "FRIENDS" -> FRIENDS;
                case "PRIVATE" -> PRIVATE;
                default -> throw new IllegalArgumentException("Unknown visibility: " + str);
            };
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, unique = true)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(optional = false)
    @JoinColumn(name = "creator_id")
    private DiscordUser creator;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "playlist_songs",
            joinColumns = @JoinColumn(name = "playlist_id"),
            inverseJoinColumns = @JoinColumn(name = "song_id")
    )
    private List<SimpleSong> songs = new ArrayList<>();

    @ManyToMany(mappedBy = "subscribedPlaylists")
    private Set<DiscordUser> subscribers = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Visibility visibility;

    @Column(nullable = false)
    @CreationTimestamp
    private Timestamp createdAt;

    public SimplePlaylist() {}

    public SimplePlaylist(String name, DiscordUser creator, List<SimpleSong> songs, Visibility visibility) {
        this.name = name;
        this.creator = creator;
        this.songs = songs;
        this.visibility = visibility;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DiscordUser getCreator() {
        return creator;
    }

    public void setCreator(DiscordUser creator) {
        this.creator = creator;
    }

    public List<SimpleSong> getSongs() {
        return songs;
    }

    public void addSong(SimpleSong song) {
        this.songs.add(song);
    }

    public void removeSong(SimpleSong song) {
        this.songs.remove(song);
    }

    public void removeSongAtIndex(int index) {
        if (index >= 0 && index < songs.size()) {
            this.songs.remove(index);
        } else {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + songs.size());
        }
    }

    public void clearSongs() {
        this.songs.clear();
    }

    public Set<DiscordUser> getSubscribers() {
        return subscribers;
    }

    public void removeSubscriber(DiscordUser user) {
        if (subscribers.contains(user)) {
            subscribers.remove(user);
            user.getSubscribedPlaylists().remove(this);
        }
    }

    public void addSubscriber(DiscordUser user) {
        if (!subscribers.contains(user)) {
            subscribers.add(user);
            user.getSubscribedPlaylists().add(this);
        }
    }

    public void clearSubscribers() {
        for (DiscordUser user : subscribers) {
            user.getSubscribedPlaylists().remove(this);
        }
        this.subscribers.clear();
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }
}
