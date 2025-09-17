package net.maomaocloud.maomaomusic.bot.commands.slash;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.maomaocloud.maomaomusic.bot.commands.DiscordCommand;
import net.maomaocloud.maomaomusic.utils.EmbedHelper;

import java.util.List;

public interface SlashCommand extends DiscordCommand<SlashCommandInteractionEvent> {

    CommandData getCommandData();

    default void sendMessage(SlashCommandInteractionEvent event, String message) {
        event.reply(message).queue();
    }

    default void sendMessage(SlashCommandInteractionEvent event, String title, String description, EmbedHelper.EmbedColor color) {
        sendMessage(event, EmbedHelper.quickEmbed(title, description, color));
    }

    default void sendMessage(SlashCommandInteractionEvent event, MessageEmbed embed) {
        event.replyEmbeds(embed).queue();
    }

    default void sendMessage(SlashCommandInteractionEvent event, List<MessageEmbed> embeds) {
        event.replyEmbeds(embeds).queue();
    }

    /**
     * Aliases are not encouraged for SlashCommand.
     */
    @Override
    default String[] getAliases() {
        return new String[0];
    }

    @Override
    default ArgsLength argsLength() {
        return ArgsLength.NONE;
    }

}
