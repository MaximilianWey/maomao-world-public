package net.maomaocloud.maomaomusic.bot.commands.slash;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.maomaocloud.maomaomusic.bot.commands.MusicCommand;
import net.maomaocloud.maomaomusic.music.model.SimpleSong;
import net.maomaocloud.maomaomusic.music.service.MusicService;
import net.maomaocloud.maomaomusic.utils.EmbedHelper;

import java.util.List;
import java.util.Optional;

public class SkipSlashCommand extends MusicCommand<SlashCommandInteractionEvent> implements SlashCommand {

    public SkipSlashCommand(MusicService musicService) {
        super(musicService);
    }

    @Override
    public void executeMusicCommand(SlashCommandInteractionEvent event, String[] ignored) {
        getMusicService().getCurrentTrack(event.getGuild().getIdLong()).ifPresentOrElse(song -> {
            getMusicService().skipTrack(event.getGuild().getIdLong());
            String skippedSongDescription = "Skipped song: \n" +
                    String.format(EmbedHelper.TEXT_WITH_URL_FORMAT,
                            song.getTitle(),
                            song.getUrl());

            Optional<SimpleSong> nextSongOpt = getMusicService().getCurrentTrack(event.getGuild().getIdLong());

            if (nextSongOpt.isPresent()) {
                SimpleSong nextSong = nextSongOpt.get();
                String nowPlayingSongDescription = "Now playing: \n" +
                        String.format(EmbedHelper.TEXT_WITH_URL_FORMAT,
                                nextSong.getTitle(),
                                nextSong.getUrl());
                sendMessage(event,
                        List.of(EmbedHelper.songEmbedWithThumbnail(
                                        defaultTitle(),
                                        skippedSongDescription,
                                        song.getThumbnail(),
                                        song.getArtist(),
                                        defaultColor()),
                                EmbedHelper.songEmbedWithThumbnail(
                                        defaultTitle(),
                                        nowPlayingSongDescription,
                                        nextSong.getThumbnail(),
                                        nextSong.getArtist(),
                                        defaultColor())
                        )
                );
            } else {
                sendMessage(event,
                        EmbedHelper.songEmbedWithThumbnail(
                                defaultTitle(),
                                skippedSongDescription,
                                song.getThumbnail(),
                                song.getArtist(),
                                defaultColor())
                );
            }
        }, () -> sendMessage(event, errorEmbed(defaultTitle(), "Not playing any tracks")));
    }

    @Override
    public String getName() {
        return "skip";
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), "Skip the currently playing track");
    }

}
