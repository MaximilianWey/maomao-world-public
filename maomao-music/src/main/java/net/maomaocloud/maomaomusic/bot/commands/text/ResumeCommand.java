package net.maomaocloud.maomaomusic.bot.commands.text;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.maomaocloud.maomaomusic.bot.commands.MusicCommand;
import net.maomaocloud.maomaomusic.music.service.MusicService;

public class ResumeCommand extends MusicCommand<MessageReceivedEvent> implements TextCommand {

    public ResumeCommand(MusicService musicService) {
        super(musicService);
    }

    @Override
    public void executeMusicCommand(MessageReceivedEvent event, String[] args) {
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
    public String[] getAliases() {
        return new String[] {"res", "unpause"};
    }

    @Override
    public ArgsLength argsLength() {
        return ArgsLength.NONE;
    }
}
