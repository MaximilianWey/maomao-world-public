package net.maomaocloud.maomaomusic.bot.commands.text;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.maomaocloud.maomaomusic.bot.commands.MusicCommand;
import net.maomaocloud.maomaomusic.music.model.SimpleSong;
import net.maomaocloud.maomaomusic.music.service.MusicService;
import net.maomaocloud.maomaomusic.utils.EmbedHelper;

import java.util.Optional;

public class SkipCommand extends MusicCommand<MessageReceivedEvent> implements TextCommand {

    public SkipCommand(MusicService musicService) {
        super(musicService);
    }

    @Override
    public void executeMusicCommand(MessageReceivedEvent event, String[] args) {
        getMusicService().getCurrentTrack(event.getGuild().getIdLong()).ifPresentOrElse(song -> {
            getMusicService().skipTrack(event.getGuild().getIdLong());
            String skippedSongDescription = "Skipped song: \n" +
                    String.format(EmbedHelper.TEXT_WITH_URL_FORMAT,
                            song.getTitle(),
                            song.getUrl());
            sendMessage(event,
                    EmbedHelper.songEmbedWithThumbnail(
                            defaultTitle(),
                            skippedSongDescription,
                            song.getThumbnail(),
                            song.getArtist(),
                            defaultColor()));

            Optional<SimpleSong> nextSongOpt = getMusicService().getCurrentTrack(event.getGuild().getIdLong());

            if (nextSongOpt.isPresent()) {
                SimpleSong nextSong = nextSongOpt.get();
                String nowPlayingSongDescription = "Now playing: \n" +
                        String.format(EmbedHelper.TEXT_WITH_URL_FORMAT,
                                nextSong.getTitle(),
                                nextSong.getUrl());
                sendMessage(event,
                        EmbedHelper.songEmbedWithThumbnail(
                                defaultTitle(),
                                nowPlayingSongDescription,
                                nextSong.getThumbnail(),
                                nextSong.getArtist(),
                                defaultColor()));
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
    public String[] getAliases() {
        return new String[] {"s"};
    }

    @Override
    public ArgsLength argsLength() {
        return ArgsLength.NONE;
    }

}
