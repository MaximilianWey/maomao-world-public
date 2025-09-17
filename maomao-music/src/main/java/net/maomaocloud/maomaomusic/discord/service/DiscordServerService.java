package net.maomaocloud.maomaomusic.discord.service;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.maomaocloud.maomaomusic.discord.model.DiscordServer;
import net.maomaocloud.maomaomusic.discord.model.DiscordUser;
import net.maomaocloud.maomaomusic.discord.model.DiscordVoiceChannel;
import net.maomaocloud.maomaomusic.discord.repositories.DiscordServerRepository;
import net.maomaocloud.maomaomusic.discord.repositories.DiscordVoiceChannelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DiscordServerService {

    private final DiscordServerRepository discordServerRepository;
    private final DiscordVoiceChannelRepository discordVoiceChannelRepository;

    @Autowired
    public DiscordServerService(DiscordServerRepository discordServerRepository,
                                DiscordVoiceChannelRepository discordVoiceChannelRepository) {
        this.discordServerRepository = discordServerRepository;
        this.discordVoiceChannelRepository = discordVoiceChannelRepository;
    }

    public Optional<DiscordServer> getDiscordServerById(String guildId) {
        return discordServerRepository.findById(guildId);
    }

    public Optional<DiscordServer> getDiscordServerById(Long guildId) {
        return discordServerRepository.findById(String.valueOf(guildId));
    }

    public Optional<DiscordVoiceChannel> getDiscordVoiceChannelById(String channelId) {
        return discordVoiceChannelRepository.findById(channelId);
    }

    public DiscordServer getOrCreateDiscordServer(Guild guild) {
        String guildId = guild.getId();
        Optional<DiscordServer> serverOpt = discordServerRepository.findById(guildId);
        if (serverOpt.isPresent()) {
            return serverOpt.get();
        } else {
            DiscordServer newServer = new DiscordServer(
                    guild.getIdLong(),
                    guild.getName(),
                    guild.getIconUrl()
            );
            return discordServerRepository.save(newServer);
        }
    }


    public DiscordVoiceChannel getOrCreateVoiceChannel(AudioChannel channel) {

        Guild guild = channel.getGuild();
        String guildId = guild.getId();
        long channelIdLong = channel.getIdLong();

        // Try to find server in DB
        Optional<DiscordServer> serverOpt = discordServerRepository.findById(guildId);
        DiscordServer server;

        if (serverOpt.isPresent()) {
            server = serverOpt.get();

            // Check if the voice channel already exists
            Optional<DiscordVoiceChannel> existingChannel = server.getVoiceChannels().stream()
                    .filter(vc -> vc.getId() == channelIdLong)
                    .findFirst();

            if (existingChannel.isPresent()) {
                return existingChannel.get();
            }

            // Channel not found, create and add it to the server
            DiscordVoiceChannel newChannel = new DiscordVoiceChannel(
                    channelIdLong,
                    channel.getName(),
                    server
            );
            server.getVoiceChannels().add(newChannel);
            discordServerRepository.save(server); // update with new channel
            return newChannel;

        } else {
            // Server not found, create server and voice channel
            server = new DiscordServer(
                    guild.getIdLong(),
                    guild.getName(),
                    guild.getIconUrl()
            );

            DiscordVoiceChannel newChannel = new DiscordVoiceChannel(
                    channelIdLong,
                    channel.getName(),
                    server
            );
            server.getVoiceChannels().add(newChannel);
            discordServerRepository.save(server);
            return newChannel;
        }
    }

    public Optional<DiscordVoiceChannel> getCurrentVoiceChannel(JDA jda, DiscordServer server, DiscordUser requester) {
        Guild guild = jda.getGuildById(server.getId());
        if (guild == null) {
            throw new RuntimeException("Guild not found for ID: " + server.getId());
        }

        Member selfMember = guild.getSelfMember();
        GuildVoiceState voiceStateBot = selfMember.getVoiceState();
        if (voiceStateBot != null) {
            AudioChannel channelBot = voiceStateBot.getChannel();
            if (channelBot != null) {
                return Optional.ofNullable(getOrCreateVoiceChannel(channelBot));
            }
        }

        Member user = guild.getMemberById(requester.getId());

        if (user != null) {
            GuildVoiceState voiceStateUser = user.getVoiceState();
            if (voiceStateUser == null) {
                return Optional.empty();
            }
            AudioChannel channelUser = voiceStateUser.getChannel();
            if (channelUser != null) {
                return Optional.ofNullable(getOrCreateVoiceChannel(channelUser));
            }
        }
        return Optional.empty();
    }

    public List<Member> getMembersInVoiceChannel(JDA jda, DiscordVoiceChannel channel) {
        Guild guild = jda.getGuildById(channel.getServer().getId());
        if (guild == null) {
            throw new RuntimeException("Guild not found for ID: " + channel.getServer().getId());
        }

        AudioChannel audioChannel = guild.getVoiceChannelById(channel.getId());
        if (audioChannel == null) {
            throw new RuntimeException("Audio channel not found for ID: " + channel.getId());
        }

        return audioChannel.getMembers();
    }


    public Optional<DiscordVoiceChannel> getDiscordVoiceChannelById(Long channelId) {
        return discordVoiceChannelRepository.findById(String.valueOf(channelId));
    }
}
