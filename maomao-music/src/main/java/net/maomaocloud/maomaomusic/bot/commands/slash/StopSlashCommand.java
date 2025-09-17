package net.maomaocloud.maomaomusic.bot.commands.slash;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.maomaocloud.maomaomusic.bot.commands.MusicCommand;
import net.maomaocloud.maomaomusic.music.service.MusicService;

public class StopSlashCommand extends MusicCommand<SlashCommandInteractionEvent> implements SlashCommand {

    public StopSlashCommand(MusicService musicService) {
        super(musicService);
    }

    @Override
    public void executeMusicCommand(SlashCommandInteractionEvent event, String[] ignored) {

        if (!isStopAllowed(event)) {
            String reason = "You must be in the same voice channel as me to use this command.";
            sendMessage(event, errorEmbed(defaultTitle(), reason));
            return;
        }

        getMusicService().stop(event.getGuild().getIdLong());
        sendMessage(event, defaultEmbed("Stopped playback"));

        var response = leaveVoiceChat(event);
        if (response.left()) {
            // sendMessage(event, defaultEmbed(defaultTitle(), response.message()));
        } else {
            sendMessage(event, errorEmbed(defaultTitle(), response.message()));
        }
    }

    @Override
    public String getName() {
        return "stop";
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), "Stops playback and clears the queue");
    }

    private boolean isStopAllowed(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        Member self = event.getGuild().getSelfMember();
        return isUserSameVoiceChannel(member, self)
                || isUserModerator(member);
    }
}
