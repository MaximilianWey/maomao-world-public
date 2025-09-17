package net.maomaocloud.maomaomusic.discord.service;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.maomaocloud.maomaomusic.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DiscordInfoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscordInfoService.class);

    private final JDA jda;
    private final DiscordUserService discordUserService;
    private final DiscordServerService discordServerService;

    @Autowired
    public DiscordInfoService(JDA jda,
                              DiscordUserService discordUserService,
                              DiscordServerService discordServerService) {
        this.jda = jda;
        this.discordUserService = discordUserService;
        this.discordServerService = discordServerService;
    }

    public void verifyAccess(String guildId, Jwt jwt) throws RuntimeException {
        Long userId = JwtUtils.getDiscordId(jwt);
        verifyAccess(Long.parseLong(guildId), userId, jwt);
    }

    public void verifyAccess(Long guildId, Long userId, Jwt jwt) throws RuntimeException {
        List<Map<String, String>> roles = jwt.getClaim("roles");

        boolean isAdmin = roles.stream()
                .anyMatch(role -> "ROLE_ADMIN".equals(role.get("authority")));

        if (isAdmin) {
            return;
        }
        if (isUserAdminOrModInGuild(guildId, userId)) {
            return;
        }
        if (isUserInVoiceChannelWithBot(guildId, userId)) {
            return;
        }
        throw new RuntimeException("Access denied");
    }


    public boolean isUserAdminOrModInGuild(Long guildId, Long userId) {
        var guild = jda.getGuildById(guildId);
        if (guild == null) {
            return false;
        }

        var member = guild.getMemberById(userId);
        if (member == null) {
            return false;
        }

        return member.hasPermission(Permission.ADMINISTRATOR)
                || member.hasPermission(Permission.MANAGE_ROLES)
                || member.hasPermission(Permission.MANAGE_CHANNEL)
                || member.hasPermission(Permission.KICK_MEMBERS)
                || member.hasPermission(Permission.BAN_MEMBERS);
    }

    public boolean isUserInVoiceChannelWithBot(Long guildId, Long userId) {
        var guild = jda.getGuildById(guildId);
        if (guild == null) {
            return false;
        }
        var member = guild.getMemberById(userId);
        var self = guild.getSelfMember();
        if (member == null) {
            return false;
        }
        try {
            var channelMember = member.getVoiceState().getChannel();
            var channelBot = self.getVoiceState().getChannel();
            return channelMember.equals(channelBot);
        } catch (NullPointerException e) {
            return false;
        }
    }
}
