package net.maomaocloud.maomaomusic.bot.listeners;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.maomaocloud.maomaomusic.bot.commands.CommandHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SlashCommandListener extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SlashCommandListener.class);

    private final CommandHandler handler;

    @Autowired
    public SlashCommandListener(CommandHandler handler) {
        this.handler = handler;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        try {
            String command = event.getName();
            handler.getSlashCommand(command).execute(event, new String[0]);
        } catch (Exception e) {
            LOGGER.error("An error occurred while executing the command: {}", e.getMessage(), e);
        }
    }
}
