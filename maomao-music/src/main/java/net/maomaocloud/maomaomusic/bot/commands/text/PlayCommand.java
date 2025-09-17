package net.maomaocloud.maomaomusic.bot.commands.text;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.maomaocloud.maomaomusic.bot.commands.MusicCommand;
import net.maomaocloud.maomaomusic.discord.model.DiscordUser;
import net.maomaocloud.maomaomusic.music.service.MusicService;
import net.maomaocloud.maomaomusic.utils.EmbedHelper;
import net.maomaocloud.maomaomusic.utils.QueryUtils;
import org.springframework.stereotype.Component;

@Component
public class PlayCommand extends MusicCommand<MessageReceivedEvent> implements TextCommand {

    public PlayCommand(MusicService musicService) {
        super(musicService);
    }

    @Override
    public void executeMusicCommand(MessageReceivedEvent event, String[] args) {

        Member member = event.getMember();
        DiscordUser user = getMusicService().createOrUpdateUser(member);

        Long guildId = event.getGuild().getIdLong();
        Long channelId = member.getVoiceState().getChannel().getIdLong();
        String query = QueryUtils.getYoutubeSearchQuery(String.join(" ", args));

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
    public String[] getAliases() {
        return new String[] {"p"};
    }

    @Override
    public ArgsLength argsLength() {
        return ArgsLength.NOT_NONE;
    }
}
