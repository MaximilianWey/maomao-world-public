package net.maomaocloud.maomaomusic.bot.commands.text;


import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.maomaocloud.maomaomusic.bot.commands.DiscordCommand;
import net.maomaocloud.maomaomusic.utils.EmbedHelper;

import java.util.List;

public interface TextCommand extends DiscordCommand<MessageReceivedEvent> {

    default void sendMessage(MessageReceivedEvent event, String message) {
        event.getChannel().sendMessage(message).queue();
    }

    default void sendMessage(MessageReceivedEvent event, String title, String description, EmbedHelper.EmbedColor color) {
        sendMessage(event, EmbedHelper.quickEmbed(title, description, color));
    }

    default void sendMessage(MessageReceivedEvent event, MessageEmbed embed) {
        event.getChannel().sendMessageEmbeds(embed).queue();
    }

    default void sendMessage(MessageReceivedEvent event, List<MessageEmbed> embeds) {
        event.getChannel().sendMessageEmbeds(embeds).queue();
    }
}
