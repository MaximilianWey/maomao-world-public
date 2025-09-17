package net.maomaocloud.maomaomusic.utils;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;

public final class JwtUtils {

    private static final List<String> DISCORD_ACCOUNT_KEYS = List.of(
            "discord", "Discord", "DiscordAccount", "discord (dev)", "Discord (Dev)"
    );

    public static Long getDiscordId(Jwt jwt) {
        Map<String, Object> discordAccount = getDiscordAccount(jwt);
        if (discordAccount.isEmpty()) {
            throw new RuntimeException("Discord account not linked");
        }
        Object discordIdObj = discordAccount.get("externalId");
        try {
            return Long.parseLong(discordIdObj.toString());
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid Discord ID format in JWT", e);
        }
    }

    private static Map<String, Object> getDiscordAccount(Jwt jwt) {
        List<Map<String, Object>> linkedAccounts = getLinkedAccounts(jwt);
        for (Map<String, Object> account : linkedAccounts) {
            Object provider = account.get("providerName");
            if (provider == null) {
                continue;
            }
            for (String key : DISCORD_ACCOUNT_KEYS) {
                if (provider.toString().toLowerCase().contains(key)) {
                    return account;
                }
            }
        }
        return Map.of();
    }

    private static Map<String, Object> getProfile(Jwt jwt) {
        Map<String, Object> profile = jwt.getClaim("profile");
        if (profile == null) {
            throw new RuntimeException("Profile section not found in JWT");
        }
        return profile;
    }

    /**
     * Returns the linked accounts from the JWT in their current list format.
     */
    public static List<Map<String, Object>> getLinkedAccounts(Jwt jwt) {
        Map<String, Object> profile = getProfile(jwt);
        Object linkedAccountsObj = profile.get("linkedAccounts");
        if (!(linkedAccountsObj instanceof List)) {
            throw new RuntimeException("Linked accounts section is missing or invalid");
        }

        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> linkedAccounts = (List<Map<String, Object>>) linkedAccountsObj;
            return linkedAccounts;
        } catch (ClassCastException e) {
            throw new RuntimeException("Linked accounts data is malformed", e);
        }
    }
}
