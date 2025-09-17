package net.maomaocloud.maomaomusic.bot.commands.slash;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.maomaocloud.maomaomusic.bot.commands.MusicCommand;
import net.maomaocloud.maomaomusic.discord.model.DiscordUser;
import net.maomaocloud.maomaomusic.music.service.MusicService;
import net.maomaocloud.maomaomusic.utils.EmbedHelper;
import net.maomaocloud.maomaomusic.utils.QueryUtils;

public class PlaySlashCommand extends MusicCommand<SlashCommandInteractionEvent> implements SlashCommand {

    public PlaySlashCommand(MusicService musicService) {
        super(musicService);
    }

    @Override
    public void executeMusicCommand(SlashCommandInteractionEvent event, String[] args) {
        String queryInput = event.getOption("query") != null
                ? event.getOption("query").getAsString()
                : null;

        if (queryInput == null || queryInput.isBlank()) {
            sendMessage(event, "Error", "Please provide a song name.", EmbedHelper.EmbedColor.RED);
            return;
        }

        Member member = event.getMember();
        DiscordUser user = getMusicService().createOrUpdateUser(member);

        Long guildId = event.getGuild().getIdLong();
        Long channelId = member.getVoiceState().getChannel().getIdLong();
        String query = QueryUtils.getYoutubeSearchQuery(queryInput);

        checkAndJoinVoiceChat(
                event,
                () -> getMusicService().enqueueAndGetSong(guildId, channelId, user.getId(), query)
                        .ifPresentOrElse(
                                song -> {
                                    String description = "Added to queue: \n" +
                                            String.format(EmbedHelper.TEXT_WITH_URL_FORMAT,
                                                    song.getTitle(),
                                                    song.getUrl());
                                    sendMessage(event,
                                            EmbedHelper.songEmbedWithThumbnail(
                                                    defaultTitle(),
                                                    description,
                                                    song.getThumbnail(),
                                                    song.getArtist(),
                                                    defaultColor()));
                                },
                                () -> sendMessage(event,
                                        errorEmbed(defaultTitle(), "An error occurred while playing the track."))
                        ),
                reason -> sendMessage(event, errorEmbed(defaultTitle(), reason))
        );
    }

    @Override
    public String getName() {
        return "play";
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), "Play a song by name or URL.")
                .addOption(OptionType.STRING, "query", "The song name or URL to play.", true);
    }
}
