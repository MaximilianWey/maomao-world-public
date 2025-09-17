package net.maomaocloud.maomaomusic.bot.commands.text;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.maomaocloud.maomaomusic.bot.commands.MusicCommand;
import net.maomaocloud.maomaomusic.music.service.MusicService;

public class PauseCommand extends MusicCommand<MessageReceivedEvent> implements TextCommand {

    public PauseCommand(MusicService musicService) {
        super(musicService);
    }

    @Override
    public void executeMusicCommand(MessageReceivedEvent event, String[] args) {
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
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public ArgsLength argsLength() {
        return ArgsLength.NONE;
    }
}
