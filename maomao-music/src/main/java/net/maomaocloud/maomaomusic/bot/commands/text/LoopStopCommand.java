package net.maomaocloud.maomaomusic.bot.commands.text;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.maomaocloud.maomaomusic.bot.commands.MusicCommand;
import net.maomaocloud.maomaomusic.music.manager.TrackScheduler.Mode;
import net.maomaocloud.maomaomusic.music.service.MusicService;

public class LoopStopCommand extends MusicCommand<MessageReceivedEvent> implements TextCommand {

    private static final Mode NORMAL_MODE = Mode.NORMAL;

    public LoopStopCommand(MusicService musicService) {
        super(musicService);
    }

    @Override
    public void executeMusicCommand(MessageReceivedEvent event, String[] args) {
        Mode oldMode = getMusicService().getMode(event.getGuild().getIdLong());
        getMusicService().setMode(event.getGuild().getIdLong(), NORMAL_MODE);
        Mode updatedMode = getMusicService().getMode(event.getGuild().getIdLong());
        sendMessage(event, defaultEmbed("Toggled loop song mode: `"
                + oldMode + "` -> `"
                + updatedMode+ "`"));
    }

    @Override
    public String getName() {
        return "loopstop";
    }

    @Override
    public String[] getAliases() {
        return new String[] {"nl", "noloop", "ls"};
    }

    @Override
    public ArgsLength argsLength() {
        return ArgsLength.NONE;
    }
}
