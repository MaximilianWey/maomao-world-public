package net.maomaocloud.maomaomusic.bot.commands.text;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.maomaocloud.maomaomusic.bot.commands.MusicCommand;
import net.maomaocloud.maomaomusic.music.service.MusicService;

public class InfoCommand extends MusicCommand<MessageReceivedEvent> implements TextCommand {


    public InfoCommand(MusicService musicService) {
        super(musicService);
    }

    @Override
    public void executeMusicCommand(MessageReceivedEvent event, String[] args) {
        try {
            sendMessage(event, getMusicService().getQueueInfo(event.getGuild().getIdLong()).stream()
                    .map(this::defaultEmbedNoTitle)
                    .toList()
            );
        } catch (Exception e) {
            String reason = "An error occurred while retrieving the queue info: " + e.getMessage();
            sendMessage(event, errorEmbed(defaultTitle(), reason));
        }
    }

    @Override
    public String getName() {
        return "queueinfo";
    }

    @Override
    public String[] getAliases() {
        return new String[] {"qi", "queue", "q"};
    }

    @Override
    public ArgsLength argsLength() {
        return ArgsLength.NONE;
    }
}
