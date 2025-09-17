package net.maomaocloud.maomaomusic.bot.commands;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.maomaocloud.maomaomusic.utils.EmbedHelper;

import java.util.List;

public interface DiscordCommand<T> {

    default void executeWithArgsCheck(T event, String[] args) {
        switch (argsLength()) {
            case NONE -> {
                if (args.length == 0) {
                    execute(event, args);
                    return;
                }
            }
            case NOT_NONE -> {
                if (args.length != 0) {
                    execute(event, args);
                    return;
                }
            }
            case ONE -> {
                if (args.length == 1) {
                    execute(event, args);
                    return;
                }
            }
            case TWO -> {
                if (args.length == 2) {
                    execute(event, args);
                    return;
                }
            }
            case THREE -> {
                if (args.length == 3) {
                    execute(event, args);
                    return;
                }
            }
            case ANY -> {
                execute(event, args);
                return;
            }
        }

        sendInvalidArgsMessage(event, args);
    }

    void execute(T event, String[] args);
    String getName();
    String[] getAliases();
    String defaultTitle();
    EmbedHelper.EmbedColor defaultColor();
    ArgsLength argsLength();

    // helper methods
    void sendMessage(T event, String message);
    void sendMessage(T event, String title, String description, EmbedHelper.EmbedColor color);
    void sendMessage(T event, MessageEmbed embed);
    void sendMessage(T event, List<MessageEmbed> embeds);

    default MessageEmbed defaultEmbed(String description) {
        return EmbedHelper.quickEmbed(defaultTitle(), description, defaultColor());
    }

    default MessageEmbed defaultEmbedNoTitle(String description) {
        return EmbedHelper.quickEmbedNoTitle(description, defaultColor());
    }

    default MessageEmbed errorEmbed(String title, String description) {
        return EmbedHelper.quickEmbed(title, description, EmbedHelper.EmbedColor.RED);
    }

    default MessageEmbed successEmbed(String title, String description) {
        return EmbedHelper.quickEmbed(title, description, EmbedHelper.EmbedColor.GREEN);
    }

    default void sendInvalidArgsMessage(T event, String[] args) {
        String reason = "Invalid arguments for command: " + getName()
                + "\n"
                + "Expected: " + argsLength()
                + "\n"
                + "Was: " + args.length;
        sendMessage(event, errorEmbed(defaultTitle(), reason));
    }

    enum ArgsLength {
        NONE,
        NOT_NONE,
        ONE,
        TWO,
        THREE,
        ANY
    }

}
