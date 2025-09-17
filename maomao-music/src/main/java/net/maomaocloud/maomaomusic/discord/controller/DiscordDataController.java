package net.maomaocloud.maomaomusic.discord.controller;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.maomaocloud.maomaomusic.discord.model.DiscordServer;
import net.maomaocloud.maomaomusic.discord.model.DiscordUser;
import net.maomaocloud.maomaomusic.discord.model.DiscordVoiceChannel;
import net.maomaocloud.maomaomusic.discord.model.DiscordVoiceChannelStateDTO;
import net.maomaocloud.maomaomusic.discord.service.DiscordServerService;
import net.maomaocloud.maomaomusic.discord.service.DiscordUserService;
import net.maomaocloud.maomaomusic.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/discord")
public class DiscordDataController {

    private final DiscordUserService discordUserService;
    private final DiscordServerService discordServerService;
    private final JDA jda;

    @Autowired
    public DiscordDataController(DiscordUserService discordUserService,
                                 DiscordServerService discordServerService,
                                 JDA jda) {
        this.discordUserService = discordUserService;
        this.discordServerService = discordServerService;
        this.jda = jda;
    }

    @GetMapping("/guilds")
    public ResponseEntity<?> getGuilds(@AuthenticationPrincipal Jwt jwt) {
        try {
            Long discordId = JwtUtils.getDiscordId(jwt);

            Optional<DiscordUser> userOpt = discordUserService.getDiscordUser(discordId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not found");
            }

            DiscordUser user = userOpt.get();
            return ResponseEntity.ok(user.getServersAsSortedList());

        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @GetMapping("/{guildId}/voice-channels")
    public ResponseEntity<?> getVoiceChannels(@PathVariable String guildId,
                                              @AuthenticationPrincipal Jwt jwt) {
        try {
            Long discordId = JwtUtils.getDiscordId(jwt);

            Optional<DiscordUser> userOpt = discordUserService.getDiscordUser(discordId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not found");
            }

            DiscordServer server = discordServerService.getDiscordServerById(guildId)
                    .orElseThrow(() -> new RuntimeException("Guild not found"));
            return ResponseEntity.ok(server.getVoiceChannels());

        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @GetMapping("/{guildId}/current-voice-channel")
    public ResponseEntity<?> getCurrentVoiceChannel(@PathVariable String guildId,
                                                     @AuthenticationPrincipal Jwt jwt) {
        try {
            Long discordId = JwtUtils.getDiscordId(jwt);

            Optional<DiscordUser> userOpt = discordUserService.getDiscordUser(discordId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not found");
            }

            DiscordServer server = discordServerService.getDiscordServerById(guildId)
                    .orElseThrow(() -> new RuntimeException("Guild not found"));

            Optional<DiscordVoiceChannel> channel = discordServerService.getCurrentVoiceChannel(jda, server, userOpt.get());

            DiscordVoiceChannelStateDTO channelStateDTO;
            if (channel.isEmpty()) {
                channelStateDTO = new DiscordVoiceChannelStateDTO(
                        null,
                        guildId,
                        List.of(),
                        discordUserService.getBotUser()
                );
            } else {
                List<Member> members = discordServerService.getMembersInVoiceChannel(jda, channel.get());
                List<DiscordUser> currentlyConnectedUsers = members.stream()
                        .map(discordUserService::getOrCreateDiscordUser)
                        .toList();
                DiscordUser botUser = discordUserService.getBotUser();
                channelStateDTO = new DiscordVoiceChannelStateDTO(
                        channel.get(),
                        guildId,
                        currentlyConnectedUsers,
                        botUser
                );
            }
            return ResponseEntity.ok(channelStateDTO);

        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }


}
