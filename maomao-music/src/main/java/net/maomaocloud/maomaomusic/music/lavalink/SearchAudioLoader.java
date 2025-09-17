package net.maomaocloud.maomaomusic.music.lavalink;

import dev.arbjerg.lavalink.client.AbstractAudioLoadResultHandler;
import dev.arbjerg.lavalink.client.player.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SearchAudioLoader extends AbstractAudioLoadResultHandler {

    public record MusicSearchResult(List<Track> tracks, Reason reason) {
        public enum Reason {
            LOAD_FAILED,
            NO_MATCHES,
            SEARCH_RESULT_LOADED,
            TIMEOUT
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchAudioLoader.class);
    private final CompletableFuture<MusicSearchResult> future;
    private final boolean returnFirst;

    public SearchAudioLoader(CompletableFuture<MusicSearchResult> future, boolean returnFirst) {
        this.future = future;
        this.returnFirst = returnFirst;
    }

    @Override
    public void loadFailed(@NotNull LoadFailed loadFailed) {
        LOGGER.warn("Load failed: {}", loadFailed.getException().getMessage());
        future.complete(new MusicSearchResult(List.of(), MusicSearchResult.Reason.LOAD_FAILED));
    }

    @Override
    public void ontrackLoaded(@NotNull TrackLoaded trackLoaded) {
        if (returnFirst) {
            LOGGER.info("Track loaded: {}, returning first track", trackLoaded.getTrack().getInfo().getTitle());
            future.complete(
                    new MusicSearchResult(List.of(trackLoaded.getTrack()), MusicSearchResult.Reason.SEARCH_RESULT_LOADED)
            );
        } else {
            LOGGER.info("Track loaded: {}, ignoring...", trackLoaded.getTrack().getInfo().getTitle());
        }
    }

    @Override
    public void onPlaylistLoaded(@NotNull PlaylistLoaded playlistLoaded) {
        if (returnFirst) {
            LOGGER.info("Playlist loaded: {}, returning first track", playlistLoaded.getTracks().getFirst().getInfo().getTitle());
            future.complete(
                    new MusicSearchResult(List.of(playlistLoaded.getTracks().getFirst()),
                            MusicSearchResult.Reason.SEARCH_RESULT_LOADED)
            );
            return;
        }
        LOGGER.info("Playlist loaded: {}, ignoring... ", playlistLoaded.getInfo().getName());
    }

    @Override
    public void onSearchResultLoaded(@NotNull SearchResult searchResult) {
        LOGGER.info("Search result loaded with {} tracks", searchResult.getTracks().size());
        future.complete(
                new MusicSearchResult(searchResult.getTracks(),
                MusicSearchResult.Reason.SEARCH_RESULT_LOADED)
        );
    }

    @Override
    public void noMatches() {
        LOGGER.warn("No matches found");
        future.complete(new MusicSearchResult(List.of(), MusicSearchResult.Reason.NO_MATCHES));
    }
}
