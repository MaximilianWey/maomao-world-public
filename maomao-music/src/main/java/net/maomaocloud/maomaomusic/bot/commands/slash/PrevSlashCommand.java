package net.maomaocloud.maomaomusic.bot.commands.slash;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.maomaocloud.maomaomusic.bot.commands.MusicCommand;
import net.maomaocloud.maomaomusic.music.service.MusicService;
import net.maomaocloud.maomaomusic.utils.EmbedHelper;

public class PrevSlashCommand extends MusicCommand<SlashCommandInteractionEvent> implements SlashCommand {


    public PrevSlashCommand(MusicService musicService) {
        super(musicService);
    }

    @Override
    public void executeMusicCommand(SlashCommandInteractionEvent event, String[] ignored) {
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
    public CommandData getCommandData() {
        return Commands.slash(getName(), "Play the previous track");
    }

}
