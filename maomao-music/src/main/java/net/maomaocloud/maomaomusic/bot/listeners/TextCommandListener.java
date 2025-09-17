package net.maomaocloud.maomaomusic.bot.listeners;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.maomaocloud.maomaomusic.bot.commands.CommandHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class TextCommandListener extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(TextCommandListener.class);

    @Value("${discord.bot.prefix}")
    private String prefix;

    private final CommandHandler handler;

    public TextCommandListener(CommandHandler handler) {
        this.handler = handler;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        try {
            String message = event.getMessage().getContentRaw();

            if (!message.startsWith(prefix)) {
                return;
            }

            String withoutPrefix = message.substring(prefix.length()).trim();
            String[] parts = withoutPrefix.split("\\s+");

            String command = parts[0].toLowerCase();
            String[] args = Arrays.copyOfRange(parts, 1, parts.length);

            handler.handle(event, command, args);

        } catch (Exception e) {
            String message = "An error occurred while executing the command: " + e.getMessage();
            event.getChannel().sendMessage(message).queue();
            LOGGER.error(message, e);
        }
    }

}
