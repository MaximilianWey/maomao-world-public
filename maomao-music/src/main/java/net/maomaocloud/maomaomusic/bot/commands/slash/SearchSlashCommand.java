package net.maomaocloud.maomaomusic.bot.commands.slash;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.maomaocloud.maomaomusic.bot.commands.MusicCommand;
import net.maomaocloud.maomaomusic.bot.commands.text.SearchCommand;
import net.maomaocloud.maomaomusic.music.model.SimpleSong;
import net.maomaocloud.maomaomusic.music.service.MusicService;
import net.maomaocloud.maomaomusic.utils.EmbedHelper;

import java.util.ArrayList;
import java.util.List;

import static net.maomaocloud.maomaomusic.utils.EmbedHelper.TEXT_WITH_URL_FORMAT;

public class SearchSlashCommand extends MusicCommand<SlashCommandInteractionEvent> implements SlashCommand {

    protected enum SlashSourceWrapper {
        YOUTUBE(SearchCommand.Source.YOUTUBE),
        SOUNDCLOUD(SearchCommand.Source.SOUNDCLOUD),
        UNSPECIFIED(SearchCommand.Source.UNSPECIFIED);

        private final SearchCommand.Source source;

        SlashSourceWrapper(SearchCommand.Source source) {
            this.source = source;
        }
    }

    public SearchSlashCommand(MusicService musicService) {
        super(musicService);
    }

    @Override
    public void executeMusicCommand(SlashCommandInteractionEvent event, String[] ignored) {

        String query = event.getOption("source") != null
                ? event.getOption("source").getAsString()
                : "";

        SearchCommand.Source source = query.isEmpty()
                ? SearchCommand.Source.UNSPECIFIED
                : SearchCommand.Source.fromString(query);

        EmbedHelper.EmbedColor color = source.color;

        List<SimpleSong> songs = new ArrayList<>();
        switch (source) {
            case INVALID -> {
                sendInvalidArgsMessage(event, new String[] {getName() + " <source [yt|sc] || [empty]>"});
                return;
            }
            case YOUTUBE -> {
                songs.addAll(getMusicService().searchYoutubeTracks(query));
            }
            case SOUNDCLOUD -> {
                songs.addAll(getMusicService().searchSoundcloudTracks(query));
            }
            case UNSPECIFIED -> {
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
    public CommandData getCommandData() {
        return Commands.slash(getName(), "Searches for a song")
                .addOption(OptionType.STRING,
                        "source",
                        "The source to search from. Currently supporting: youtube (yt), soundcloud (sc). Defaults to youtube",
                        false,
                        true
                );
    }
}
