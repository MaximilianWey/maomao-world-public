package net.maomaocloud.maomaomusic.config;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.event.WebSocketClosedEvent;
import dev.arbjerg.lavalink.libraries.jda.JDAVoiceUpdateListener;
import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class JdaConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdaConfig.class);

    @Value("${discord.bot.token}")
    private String discordToken;

    private final LavalinkClient lavalinkClient;
    private final List<ListenerAdapter> listeners;

    @Autowired
    public JdaConfig(LavalinkClient lavalinkClient, List<ListenerAdapter> listeners) {
        this.lavalinkClient = lavalinkClient;
        this.listeners = listeners;
    }

    @Bean
    public JDA jdaClient() throws InterruptedException {
        return JDABuilder.createDefault(discordToken)
                .setVoiceDispatchInterceptor(new JDAVoiceUpdateListener(lavalinkClient))
                .enableIntents(GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.MESSAGE_CONTENT)
                .enableCache(CacheFlag.VOICE_STATE)
                .addEventListeners(listeners.toArray())
                .build()
                .awaitReady();
    }

    @PostConstruct
    public void setupWebSocketCloseHandling() {
        final int SESSION_INVALID = 4006;

        lavalinkClient.on(WebSocketClosedEvent.class)
                .subscribe(event -> {
                    if (event.getCode() == SESSION_INVALID) {
                        try {
                            final long guildId = event.getGuildId();
                            final var guild = jdaClient().getGuildById(guildId);

                            if (guild == null) {
                                return;
                            }

                            final var connectedChannel = guild.getSelfMember().getVoiceState().getChannel();

                            if (connectedChannel == null) {
                                return;
                            }

                            jdaClient().getDirectAudioController().reconnect(connectedChannel);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                });
    }

}
