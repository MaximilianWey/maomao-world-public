package net.maomaocloud.maomaomusic.bootstrap;

import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.JDA;
import net.maomaocloud.maomaomusic.discord.service.DiscordUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RegisterBotUser {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterBotUser.class);

    private final JDA jda;
    private final DiscordUserService discordUserService;

    @Autowired
    public RegisterBotUser(JDA jda, DiscordUserService discordUserService) {
        this.jda = jda;
        this.discordUserService = discordUserService;
    }

    @PostConstruct
    public void registerBotUser() {
        var bot = jda.getSelfUser();
        if (discordUserService.getDiscordUser(bot.getIdLong()).isPresent()) {
            LOGGER.info("Bot user {} with id {} already registered", bot.getName(), bot.getIdLong());
        } else {
            LOGGER.info("Registered bot user {} with id {}", bot.getName(), bot.getIdLong());
        }
        discordUserService.registerBotUser(jda.getGuilds().getFirst().getSelfMember());
    }
}
