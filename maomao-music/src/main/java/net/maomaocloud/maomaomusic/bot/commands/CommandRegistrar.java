package net.maomaocloud.maomaomusic.bot.commands;

import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.JDA;
import net.maomaocloud.maomaomusic.bot.commands.slash.SlashCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CommandRegistrar {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandRegistrar.class);

    private final JDA jda;
    private final CommandHandler commandHandler;

    public CommandRegistrar(JDA jda, CommandHandler commandHandler) {
        this.jda = jda;
        this.commandHandler = commandHandler;
    }

    @PostConstruct
    public void registerSlashCommands() {
        jda.updateCommands()
                .addCommands(commandHandler.getSlashCommands().stream()
                        .peek(cmd -> LOGGER.info("Registering slash command: {}", cmd.getName()))
                        .map(SlashCommand::getCommandData)
                        .toList())
                .queue(
                        success -> LOGGER.info("All slash commands registered successfully."),
                        error -> LOGGER.error("Error registering slash commands: {}", error.getMessage())
                );
    }
}
