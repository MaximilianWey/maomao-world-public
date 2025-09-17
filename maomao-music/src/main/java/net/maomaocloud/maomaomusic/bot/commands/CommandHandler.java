package net.maomaocloud.maomaomusic.bot.commands;

import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.maomaocloud.maomaomusic.bot.commands.slash.*;
import net.maomaocloud.maomaomusic.bot.commands.text.*;
import net.maomaocloud.maomaomusic.bot.commands.text.LoopStopCommand;
import net.maomaocloud.maomaomusic.music.service.MusicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandHandler.class);

    private final MusicService musicService;

    private final Map<String, TextCommand> textCommands;
    private final Map<String, SlashCommand> slashCommands;

    @Autowired
    public CommandHandler(MusicService musicService) {
        this.musicService = musicService;
        this.textCommands = new HashMap<>();
        this.slashCommands = new HashMap<>();

    }

    @PostConstruct
    public void registerCommands() {
        registerCommand(new PlayCommand(musicService));
        registerCommand(new StopCommand(musicService));
        registerCommand(new SkipCommand(musicService));
        registerCommand(new PrevCommand(musicService));
        registerCommand(new InfoCommand(musicService));
        registerCommand(new PauseCommand(musicService));
        registerCommand(new ResumeCommand(musicService));
        registerCommand(new SearchCommand(musicService));
        registerCommand(new ShuffleCommand(musicService));
        registerCommand(new LoopSongCommand(musicService));
        registerCommand(new LoopStopCommand(musicService));
        registerCommand(new LoopQueueCommand(musicService));
        registerCommand(new EnqueuePlaylistCommand(musicService));

        registerCommand(new PlaySlashCommand(musicService));
        registerCommand(new StopSlashCommand(musicService));
        registerCommand(new SkipSlashCommand(musicService));
        registerCommand(new PrevSlashCommand(musicService));
        registerCommand(new InfoSlashCommand(musicService));
        registerCommand(new PauseSlashCommand(musicService));
        registerCommand(new ResumeSlashCommand(musicService));
        registerCommand(new SearchSlashCommand(musicService));
        registerCommand(new ShuffleSlashCommand(musicService));
        registerCommand(new LoopSongSlashCommand(musicService));
        registerCommand(new LoopStopSlashCommand(musicService));
        registerCommand(new LoopQueueSlashCommand(musicService));
        registerCommand(new EnqueuePlaylistSlashCommand(musicService));
    }

    private void registerCommand(TextCommand command) {
        textCommands.put(command.getName(), command);
        for (String alias : command.getAliases()) {
            if (alias.isEmpty() || textCommands.containsKey(alias) || alias.equalsIgnoreCase(command.getName())) {
                continue;
            }
            textCommands.put(alias, command);
        }
    }

    private void registerCommand(SlashCommand command) {
        slashCommands.put(command.getName(), command);
    }


    public void handle(MessageReceivedEvent event, String command, String[] args) {

        var handlerOpt = textCommands.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(command))
                .findFirst();

        if (handlerOpt.isEmpty()) {
            // todo : move embed messaging to helper class and send this as embed
            event.getChannel().sendMessage("Unknown command: " + command).queue();
            return;
        }
        try {
            handlerOpt.get().getValue().executeWithArgsCheck(event, args);
        } catch (Exception e) {
            event.getChannel().sendMessage("An error occurred while executing the command: " + e.getMessage()).queue();
            LOGGER.error("An error occurred while executing the command: {}", e.getMessage(), e);
        }
    }

    public List<SlashCommand> getSlashCommands() {
        return slashCommands.values().stream().toList();
    }

    public SlashCommand getSlashCommand(String name) {
        return slashCommands.get(name);
    }
}
