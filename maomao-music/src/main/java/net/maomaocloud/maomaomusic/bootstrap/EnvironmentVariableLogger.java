package net.maomaocloud.maomaomusic.bootstrap;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentVariableLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnvironmentVariableLogger.class);

    @Value("${discord.bot.token}")
    private String discordToken;

    @Value("${lavalink.uri}")
    private String lavalinkUri;

    @Value("${lavalink.name}")
    private String lavalinkName;

    @Value("${lavalink.password}")
    private String lavalinkPassword;

    public EnvironmentVariableLogger() {}

    @PostConstruct
    public void logEnvironmentVariables() {
        LOGGER.info("Discord Bot Token: {}", discordToken);
        LOGGER.info("Lavalink Server Uri: {}", lavalinkUri);
        LOGGER.info("Lavalink Server Name: {}", lavalinkName);
        String maskedPassword = lavalinkPassword.replaceAll(".", "*");
        LOGGER.info("Lavalink Server Password: {}", maskedPassword);
    }

}
