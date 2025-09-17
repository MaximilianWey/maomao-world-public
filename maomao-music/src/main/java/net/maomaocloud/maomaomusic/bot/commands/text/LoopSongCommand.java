package net.maomaocloud.maomaomusic.bot.commands.text;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.maomaocloud.maomaomusic.bot.commands.MusicCommand;
import net.maomaocloud.maomaomusic.music.manager.TrackScheduler.Mode;
import net.maomaocloud.maomaomusic.music.service.MusicService;

public class LoopSongCommand extends MusicCommand<MessageReceivedEvent> implements TextCommand {

    private static final Mode REPEAT_SONG_MODE = Mode.REPEAT_SONG;

    public LoopSongCommand(MusicService musicService) {
        super(musicService);
    }

    @Override
    public void executeMusicCommand(MessageReceivedEvent event, String[] args) {
        Mode oldMode = getMusicService().getMode(event.getGuild().getIdLong());
        getMusicService().setMode(event.getGuild().getIdLong(), REPEAT_SONG_MODE);
        Mode updatedMode = getMusicService().getMode(event.getGuild().getIdLong());
        sendMessage(event, defaultEmbed("Toggled loop song mode: `"
                + oldMode + "` -> `"
                + updatedMode+ "`"));
    }

    @Override
    public String getName() {
        return "loopsong";
    }

    @Override
    public String[] getAliases() {
        return new String[] {"ls", "repeatsong", "rs"};
    }

    @Override
    public ArgsLength argsLength() {
        return ArgsLength.NONE;
    }
}
