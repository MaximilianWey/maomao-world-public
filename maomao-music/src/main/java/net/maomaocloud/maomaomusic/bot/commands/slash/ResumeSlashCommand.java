package net.maomaocloud.maomaomusic.bot.commands.slash;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.maomaocloud.maomaomusic.bot.commands.MusicCommand;
import net.maomaocloud.maomaomusic.music.service.MusicService;

public class ResumeSlashCommand extends MusicCommand<SlashCommandInteractionEvent> implements SlashCommand {

    public ResumeSlashCommand(MusicService musicService) {
        super(musicService);
    }

    @Override
    public void executeMusicCommand(SlashCommandInteractionEvent event, String[] ignored) {
        if (!getMusicService().isPaused(event.getGuild().getIdLong())) {
            sendMessage(event, errorEmbed(defaultTitle(), "Already playing!"));
            return;
        }

        getMusicService().resumePlayback(event.getGuild().getIdLong());
        sendMessage(event, defaultEmbed("Resumed track"));
    }

    @Override
    public String getName() {
        return "resume";
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), "Resumes the currently playing track");
    }

}
