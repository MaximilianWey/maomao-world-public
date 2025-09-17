package net.maomaocloud.maomaomusic.bot.commands.slash;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.maomaocloud.maomaomusic.bot.commands.MusicCommand;
import net.maomaocloud.maomaomusic.discord.model.DiscordUser;
import net.maomaocloud.maomaomusic.music.model.SimplePlaylist;
import net.maomaocloud.maomaomusic.music.service.MusicService;
import net.maomaocloud.maomaomusic.utils.EmbedHelper;

import java.util.Optional;

public class EnqueuePlaylistSlashCommand extends MusicCommand<SlashCommandInteractionEvent> implements SlashCommand {

    public EnqueuePlaylistSlashCommand(MusicService musicService) {
        super(musicService);
    }

    @Override
    public void executeMusicCommand(SlashCommandInteractionEvent event, String[] ignored) {

        String playlistName = event.getOption("name") != null
                ? event.getOption("name").getAsString()
                : null;

        if (playlistName == null || playlistName.isBlank()) {
            sendMessage(event, "Error", "Please provide a playlist name.", EmbedHelper.EmbedColor.RED);
            return;
        }

        Member member = event.getMember();
        DiscordUser _ = getMusicService().createOrUpdateUser(member);

        Long guildId = event.getGuild().getIdLong();
        Long channelId = member.getVoiceState().getChannel().getIdLong();

        Optional<SimplePlaylist> playlist = getMusicService().enqueuePlaylist(guildId, channelId, member.getIdLong(), playlistName);

        checkAndJoinVoiceChat(event,
                () -> {
                    if (playlist.isPresent()) {
                        sendMessage(event, defaultEmbed("Added " + playlist.get().getSongs().size() + " to queue from: " + playlist.get().getName()));
                    } else {
                        sendMessage(event, errorEmbed(defaultTitle(), "Couldn't find playlist: " + playlistName));
                    }
                },
                reason -> sendMessage(event, errorEmbed(defaultTitle(), reason))
        );

    }

    @Override
    public String getName() {
        return "enqueueplaylist";
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), "Enqueues a playlist")
                .addOption(OptionType.STRING, "name", "The playlists name to enqueue. (can be substring of name)", true);
    }

}
