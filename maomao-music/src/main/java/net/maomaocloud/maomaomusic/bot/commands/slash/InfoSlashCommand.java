package net.maomaocloud.maomaomusic.bot.commands.slash;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.maomaocloud.maomaomusic.bot.commands.MusicCommand;
import net.maomaocloud.maomaomusic.music.service.MusicService;

public class InfoSlashCommand extends MusicCommand<SlashCommandInteractionEvent> implements SlashCommand {

    public InfoSlashCommand(MusicService musicService) {
        super(musicService);
    }

    @Override
    public void executeMusicCommand(SlashCommandInteractionEvent event, String[] ignored) {
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
    public CommandData getCommandData() {
        return Commands.slash(getName(), "Sets the queue to play normally");
    }


}
