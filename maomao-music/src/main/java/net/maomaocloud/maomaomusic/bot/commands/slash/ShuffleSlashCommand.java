package net.maomaocloud.maomaomusic.bot.commands.slash;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.maomaocloud.maomaomusic.bot.commands.MusicCommand;
import net.maomaocloud.maomaomusic.music.service.MusicService;

public class ShuffleSlashCommand extends MusicCommand<SlashCommandInteractionEvent> implements SlashCommand {

    public ShuffleSlashCommand(MusicService musicService) {
        super(musicService);
    }

    @Override
    public void executeMusicCommand(SlashCommandInteractionEvent event, String[] args) {
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
    public CommandData getCommandData() {
        return Commands.slash(getName(), "Shuffles the current queue");
    }
}
