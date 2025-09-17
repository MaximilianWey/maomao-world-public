package net.maomaocloud.maomaomusic.bot.commands.text;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.maomaocloud.maomaomusic.bot.commands.MusicCommand;
import net.maomaocloud.maomaomusic.music.service.MusicService;
import org.springframework.stereotype.Component;

@Component
public class StopCommand extends MusicCommand<MessageReceivedEvent> implements TextCommand {

    public StopCommand(MusicService musicService) {
        super(musicService);
    }

    @Override
    public void executeMusicCommand(MessageReceivedEvent event, String[] args) {
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
    public String[] getAliases() {
        return new String[] {};
    }

    @Override
    public ArgsLength argsLength() {
        return ArgsLength.NONE;
    }

    private boolean isStopAllowed(MessageReceivedEvent event) {
        Member member = event.getMember();
        Member self = event.getGuild().getSelfMember();
        return isUserSameVoiceChannel(member, self)
                || isUserModerator(member);
    }

}

