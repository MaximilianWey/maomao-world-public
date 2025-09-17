package net.maomaocloud.maomaomusic.bot.commands.text;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.maomaocloud.maomaomusic.bot.commands.MusicCommand;
import net.maomaocloud.maomaomusic.music.service.MusicService;

public class ShuffleCommand extends MusicCommand<MessageReceivedEvent> implements TextCommand {

    public ShuffleCommand(MusicService musicService) {
        super(musicService);
    }

    @Override
    public void executeMusicCommand(MessageReceivedEvent event, String[] args) {
        getMusicService().shuffleQueue(event.getGuild().getIdLong());
        if (getMusicService().getQueuedTracks(event.getGuild().getIdLong()).songs().isEmpty()) {
            sendMessage(event, errorEmbed(defaultTitle(), "Can't shuffle an empty queue!"));
            return;
        }
        sendMessage(event, defaultEmbed("Shuffled the queue"));
    }

    @Override
    public String getName() {
        return "shuffle";
    }

    @Override
    public String[] getAliases() {
        return new String[] { "sh", "shufflequeue" };
    }

    @Override
    public ArgsLength argsLength() {
        return ArgsLength.NONE;
    }

}
