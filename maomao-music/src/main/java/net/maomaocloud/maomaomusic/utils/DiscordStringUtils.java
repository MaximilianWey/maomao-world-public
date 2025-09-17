package net.maomaocloud.maomaomusic.utils;

public class DiscordStringUtils {

    private static final String[] SPECIAL_CHARACTERS = {
            "\\", "*", "_", "~", "|", ">", "`", "(", ")", "[", "]", "{", "}", "#", "+", "-", "=", ".", "!"
    };

    private static final String[] SPECIAL_CHARACTERS_ALLOWING_URLS = {
            "\\", "*", "_", "~", "|", ">", "`", "{", "}", "#", "+", "-", "=", ".", "!", "# ", "## ", "### "
    };

    /**
     * Escapes all special Discord Markdown characters with a backslash.
     *
     * @param input the raw string
     * @return the escaped string safe for Discord
     */
    public static String escapeDiscordMarkdown(String input) {
        String escaped = input;
        for (String ch : SPECIAL_CHARACTERS) {
            escaped = escaped.replace(ch, "\\" + ch);
        }

        // Escape '# ' at the beginning of lines to prevent large headers
        escaped = escaped.replaceAll("(?m)^# ", "\\\\# ");
        escaped = escaped.replaceAll("(?m)^## ", "\\\\## ");
        escaped = escaped.replaceAll("(?m)^### ", "\\\\### ");

        return escaped;
    }

    public static String escapeDiscordMarkdownIgnoreUrls(String input) {
        String escaped = input;
        for (String ch : SPECIAL_CHARACTERS_ALLOWING_URLS) {
            escaped = escaped.replace(ch, "\\" + ch);
        }

        // Escape '# ' at the beginning of lines to prevent large headers
        escaped = escaped.replaceAll("(?m)^# ", "\\\\# ");
        escaped = escaped.replaceAll("(?m)^## ", "\\\\## ");
        escaped = escaped.replaceAll("(?m)^### ", "\\\\### ");

        return escaped;
    }

}
