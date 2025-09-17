package net.maomaocloud.maomaomusic.bot.commands.text;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.maomaocloud.maomaomusic.bot.commands.MusicCommand;
import net.maomaocloud.maomaomusic.music.model.SimpleSong;
import net.maomaocloud.maomaomusic.music.service.MusicService;
import net.maomaocloud.maomaomusic.utils.EmbedHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static net.maomaocloud.maomaomusic.utils.EmbedHelper.TEXT_WITH_URL_FORMAT;

public class SearchCommand extends MusicCommand<MessageReceivedEvent> implements TextCommand {

    public enum Source {
        YOUTUBE(EmbedHelper.EmbedColor.DARK_RED,"youtube", "yt"),
        SOUNDCLOUD(EmbedHelper.EmbedColor.ORANGE,"soundcloud", "sc"),
        UNSPECIFIED(EmbedHelper.EmbedColor.DEFAULT,"unspecified", ""),
        INVALID(EmbedHelper.EmbedColor.RED,"invalid");

        public final EmbedHelper.EmbedColor color;
        private final String name;
        private final String[] aliases;

        Source(EmbedHelper.EmbedColor color, String name, String... aliases) {
            this.color = color;
            this.name = name;
            this.aliases = aliases;
        }

        private static Source fromArgs(String[] args) {
            if (args.length == 0) {
                return Source.INVALID;
            }
            String firstArg = args[0].toLowerCase();
            for (Source source : Source.values()) {
                if (source.name.equals(firstArg) || List.of(source.aliases).contains(firstArg)) {
                    return source;
                }
            }
            return Source.UNSPECIFIED;
        }

        public static Source fromString(String str) {
            for (Source source : Source.values()) {
                if (source.name.equalsIgnoreCase(str) || List.of(source.aliases).contains(str.toLowerCase())) {
                    return source;
                }
            }
            return Source.INVALID;
        }
    }

    public SearchCommand(MusicService musicService) {
        super(musicService);
    }

    @Override
    public void executeMusicCommand(MessageReceivedEvent event, String[] args) {

        Source source = Source.fromArgs(args);
        EmbedHelper.EmbedColor color = source.color;
        List<SimpleSong> songs = new ArrayList<>();
        switch (source) {
            case INVALID -> {
                sendInvalidArgsMessage(event, args);
                return;
            }
            case YOUTUBE -> {
                String query = Arrays.stream(args).skip(1).collect(Collectors.joining(" "));
                songs.addAll(getMusicService().searchYoutubeTracks(query));
            }
            case SOUNDCLOUD -> {
                String query = Arrays.stream(args).skip(1).collect(Collectors.joining(" "));
                songs.addAll(getMusicService().searchSoundcloudTracks(query));
            }
            case UNSPECIFIED -> {
                String query = String.join(" ", args);
                songs.addAll(getMusicService().searchTracks(query));
                color = defaultColor();
            }
        }

        if (songs.isEmpty()) {
            sendMessage(event, errorEmbed(defaultTitle(), "No results found for your search."));
            return;
        }

        List<SimpleSong> topSongs = songs.subList(0, Math.min(songs.size(), 10));
        EmbedHelper.EmbedColor finalColor = color;
        List<MessageEmbed> embeds = topSongs.stream()
                .map(song -> EmbedHelper.songEmbedWithThumbnail(defaultTitle(),
                        String.format(TEXT_WITH_URL_FORMAT, song.getTitle(), song.getUrl()),
                        song.getThumbnail(),
                        song.getArtist(),
                        finalColor)
                ).toList();
        sendMessage(event, embeds);
    }

    @Override
    public String getName() {
        return "search";
    }

    @Override
    public String[] getAliases() {
        return new String[] {"find", "f"};
    }

    @Override
    public ArgsLength argsLength() {
        return ArgsLength.ANY;
    }

}
