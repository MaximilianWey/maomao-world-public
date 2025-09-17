package net.maomaocloud.maomaomusic.utils;

import java.net.URI;
import java.net.URISyntaxException;

public class QueryUtils {

    /**
     * Checks if the input is a valid URL. If not, prepends "ytsearch:".
     *
     * @param input The user's query (could be a URL or search text)
     * @return The processed query, ready for Lavalink
     */
    public static String getYoutubeSearchQuery(String input) {
        if (input == null || input.isBlank()) {
            return "ytsearch:";
        }

        if (isValidUrl(input)) {
            return input;
        } else {
            return "ytsearch:" + input;
        }
    }

    /**
     * Checks if the input is a valid URL. If not, prepends "scsearch:".
     *
     * @param input The user's query (could be a URL or search text)
     * @return The processed query, ready for Lavalink
     */
    public static String getSoundCloudSearchQuery(String input) {
        if (input == null || input.isBlank()) {
            return "scsearch:";
        }

        if (isValidUrl(input)) {
            return input;
        } else {
            return "scsearch:" + input;
        }
    }

    /**
     * Checks if the given input is a valid HTTP or HTTPS URL.
     *
     * @param input The string to check.
     * @return true if it's a valid URL, false otherwise.
     */
    public static boolean isValidUrl(String input) {
        if (input.startsWith("scsearch:") || input.startsWith("ytsearch") || input.startsWith("O:http")) {
            return true;
        }
        try {
            URI uri = new URI(input);
            String scheme = uri.getScheme();
            return scheme != null && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"));
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
