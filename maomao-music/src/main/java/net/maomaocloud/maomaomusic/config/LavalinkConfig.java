package net.maomaocloud.maomaomusic.config;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.NodeOptions;
import dev.arbjerg.lavalink.client.loadbalancing.builtin.VoiceRegionPenaltyProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LavalinkConfig {

    @Value("${discord.bot.id}")
    private long userId;

    @Value("${lavalink.uri}")
    private String lavalinkUri;

    @Value("${lavalink.password}")
    private String lavalinkPassword;

    @Value("${lavalink.name}")
    private String lavalinkName;

    @Bean
    public LavalinkClient lavalinkClient() {

        LavalinkClient client = new LavalinkClient(userId);

        client.getLoadBalancer().addPenaltyProvider(new VoiceRegionPenaltyProvider());

        client.addNode(
                new NodeOptions.Builder()
                        .setName(lavalinkName)
                        .setServerUri(lavalinkUri)
                        .setPassword(lavalinkPassword)
                        .build()
        );

        return client;
    }
}
