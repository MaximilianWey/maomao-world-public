package net.maomaocloud.maomaomusic.utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class EmbedHelper {

    public static final String TEXT_WITH_URL_FORMAT = "[%s](%s)";

    public static MessageEmbed quickEmbed(String title, String description, EmbedColor color) {
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(color.getCode())
                .build();
    }

    public static MessageEmbed quickEmbedNoTitle(String description, EmbedColor color) {
        return new EmbedBuilder()
                .setDescription(description)
                .setColor(color.getCode())
                .build();
    }

    public static MessageEmbed songEmbedWithThumbnail(String title,
                                                      String description,
                                                      String thumbnailUrl,
                                                      String artistName,
                                                      EmbedColor color) {
        return new EmbedBuilder()
                .setTitle("**" + title + "**")
                .setDescription(DiscordStringUtils.escapeDiscordMarkdownIgnoreUrls(description))
                .setFooter("by " + artistName)
                .setThumbnail(thumbnailUrl)
                .setColor(color.getCode())
                .build();
    }

    public enum EmbedColor {
        DEFAULT(0),
        AQUA(1752220),
        DARK_AQUA(1146986),
        GREEN(5763719),
        DARK_GREEN(2067276),
        BLUE(3447003),
        DARK_BLUE(2123412),
        PURPLE(10181046),
        DARK_PURPLE(7419530),
        LUMINOUS_VIVID_PINK(15277667),
        DARK_VIVID_PINK(11342935),
        GOLD(15844367),
        DARK_GOLD(12745742),
        ORANGE(15105570),
        DARK_ORANGE(11027200),
        RED(15548997),
        DARK_RED(10038562),
        GREY(9807270),
        DARK_GREY(9936031),
        DARKER_GREY(8359053),
        LIGHT_GREY(12370112),
        NAVY(3426654),
        DARK_NAVY(2899536),
        YELLOW(16776960);

        private final int code;

        EmbedColor(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

}
