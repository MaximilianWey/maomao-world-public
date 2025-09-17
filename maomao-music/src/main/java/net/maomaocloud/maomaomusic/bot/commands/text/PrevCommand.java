package net.maomaocloud.maomaomusic.bot.commands.text;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.maomaocloud.maomaomusic.bot.commands.MusicCommand;
import net.maomaocloud.maomaomusic.music.service.MusicService;
import net.maomaocloud.maomaomusic.utils.EmbedHelper;


public class PrevCommand extends MusicCommand<MessageReceivedEvent> implements TextCommand {

    public PrevCommand(MusicService musicService) {
        super(musicService);
    }

    @Override
    public void executeMusicCommand(MessageReceivedEvent event, String[] args) {
        getMusicService().playPreviousTrack(event.getGuild().getIdLong())
                .ifPresentOrElse(prev -> {
                    String description = "Playing previous song: \n" +
                            String.format(EmbedHelper.TEXT_WITH_URL_FORMAT,
                                    prev.getTitle(),
                                    prev.getUrl());
                    sendMessage(event,
                            EmbedHelper.songEmbedWithThumbnail(
                                    defaultTitle(),
                                    description,
                                    prev.getThumbnail(),
                                    prev.getArtist(),
                                    defaultColor()));
                }, () -> sendMessage(event, errorEmbed(defaultTitle(), "No previous track found")));
    }

    @Override
    public String getName() {
        return "previous";
    }

    @Override
    public String[] getAliases() {
        return new String[] {"prev"};
    }

    @Override
    public ArgsLength argsLength() {
        return ArgsLength.NONE;
    }
}
