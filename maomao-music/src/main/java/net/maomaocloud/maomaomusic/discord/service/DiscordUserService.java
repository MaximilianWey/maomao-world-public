package net.maomaocloud.maomaomusic.discord.service;

import net.dv8tion.jda.api.entities.Member;
import net.maomaocloud.maomaomusic.discord.model.DiscordServer;
import net.maomaocloud.maomaomusic.discord.model.DiscordUser;
import net.maomaocloud.maomaomusic.discord.repositories.DiscordUserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DiscordUserService {

    private final DiscordServerService discordServerService;
    private final DiscordUserRepository discordUserRepository;

    private DiscordUser botUser;

    public DiscordUserService(DiscordServerService discordServerService,
                              DiscordUserRepository discordUserRepository) {
        this.discordServerService = discordServerService;
        this.discordUserRepository = discordUserRepository;
    }

    public Optional<DiscordUser> getDiscordUser(Long userId) {
        return discordUserRepository.findById(String.valueOf(userId));
    }

    public DiscordUser getOrCreateDiscordUser(Member member) {
        Optional<DiscordUser> userOpt = getDiscordUser(member.getIdLong());
        return userOpt.orElseGet(() -> createOrUpdateDiscordUser(member));
    }

    public DiscordUser createOrUpdateDiscordUser(Member member) {
        return discordUserRepository.findById(member.getId())
                .map(existingUser -> updateDiscordUser(existingUser, member))
                .orElseGet(() -> createDiscordUser(member));
    }

    private DiscordUser createDiscordUser(Member member) {
        DiscordServer guild = discordServerService.getOrCreateDiscordServer(member.getGuild());
        String username = member.getUser().getGlobalName() != null ?
                member.getUser().getGlobalName() : member.getUser().getName();

        DiscordUser newUser = new DiscordUser(
                member.getIdLong(),
                username,
                member.getUser().getName(),
                member.getUser().getAvatarUrl()
        );

        newUser.addServer(guild);
        return discordUserRepository.save(newUser);
    }

    private DiscordUser updateDiscordUser(DiscordUser user, Member member) {
        DiscordServer guild = discordServerService.getOrCreateDiscordServer(member.getGuild());

        String username = member.getUser().getGlobalName() != null ?
                member.getUser().getGlobalName() : member.getUser().getName();

        user.setUsername(username);
        user.setUniqueName(member.getUser().getName());
        user.setAvatarUrl(member.getUser().getAvatarUrl());
        user.addServer(guild);

        return discordUserRepository.save(user);
    }

    public void registerBotUser(Member bot) {
        this.botUser = getOrCreateDiscordUser(bot);
    }

    public DiscordUser getBotUser() {
        return botUser;
    }

    public boolean isBotUser(String userId) {
        return botUser != null && botUser.getId().equals(userId);
    }
}
