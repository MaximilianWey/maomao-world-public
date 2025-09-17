package net.maomaocloud.maomaomusic.bot.commands.slash;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.maomaocloud.maomaomusic.bot.commands.MusicCommand;
import net.maomaocloud.maomaomusic.music.service.MusicService;

public class PauseSlashCommand extends MusicCommand<SlashCommandInteractionEvent> implements SlashCommand {

    public PauseSlashCommand(MusicService musicService) {
        super(musicService);
    }

    @Override
    public void executeMusicCommand(SlashCommandInteractionEvent event, String[] ignored) {
        if (getMusicService().isPaused(event.getGuild().getIdLong())) {
            sendMessage(event, errorEmbed(defaultTitle(), "Already paused!"));
            return;
        }

        getMusicService().pausePlayback(event.getGuild().getIdLong());
        sendMessage(event, defaultEmbed("Paused track"));
    }

    @Override
    public String getName() {
        return "pause";
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), "Pauses the currently playing track");
    }

}
