package net.maomaocloud.maomaomusic.music.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.protocol.v4.TrackInfo;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "simple_songs")
public class SimpleSong {

    @Id
    @Column(nullable = false, unique = true)
    private String identifier;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String artist;

    @Column(nullable = false)
    private String url;

    @Column
    private String thumbnail;

    @Column(nullable = false)
    private String source;

    @Column(nullable = false)
    @CreationTimestamp
    private Timestamp createdAt;

    @JsonIgnore
    @ManyToMany(mappedBy = "songs")
    private List<SimplePlaylist> playlists = new ArrayList<>();

    public SimpleSong() {
        // JPA requires a no-arg constructor
    }

    public SimpleSong(String identifier, String title, String artist, String url, String thumbnail, String source) {
        this.identifier = identifier;
        this.title = title;
        this.artist = artist;
        this.url = url;
        this.thumbnail = thumbnail;
        this.source = source;
    }

    public SimpleSong(Track track) {
        this(track.getInfo());
    }

    public SimpleSong(TrackInfo info) {
        this(
                info.getIdentifier(),
                info.getTitle(),
                info.getAuthor(),
                info.getUri(),
                info.getArtworkUrl(),
                info.getSourceName()
        );
    }

    // Getters

    public String getIdentifier() {
        return identifier;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getUrl() {
        return url;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getSource() {
        return source;
    }

    public List<SimplePlaylist> getPlaylists() {
        return playlists;
    }

    public void addPlaylist(SimplePlaylist playlist) {
        if (!this.playlists.contains(playlist)) {
            this.playlists.add(playlist);
        }
    }

    public void removePlaylist(SimplePlaylist playlist) {
        this.playlists.remove(playlist);
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    // Setters

    public void setTitle(String title) {
        this.title = title;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
