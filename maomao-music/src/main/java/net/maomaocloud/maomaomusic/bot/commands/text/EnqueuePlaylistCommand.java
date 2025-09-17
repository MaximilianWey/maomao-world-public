package net.maomaocloud.maomaomusic.bot.commands.text;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.maomaocloud.maomaomusic.bot.commands.MusicCommand;
import net.maomaocloud.maomaomusic.discord.model.DiscordUser;
import net.maomaocloud.maomaomusic.music.model.SimplePlaylist;
import net.maomaocloud.maomaomusic.music.service.MusicService;

import java.util.Optional;

public class EnqueuePlaylistCommand extends MusicCommand<MessageReceivedEvent> implements TextCommand {

    public EnqueuePlaylistCommand(MusicService musicService) {
        super(musicService);
    }

    @Override
    public void executeMusicCommand(MessageReceivedEvent event, String[] args) {

        Member member = event.getMember();


        if (member == null) {
            sendMessage(event, errorEmbed(defaultTitle(), "You must be a member of the server to use this command."));
            return;
        }

        DiscordUser _ = getMusicService().createOrUpdateUser(member);

        var memberVoiceState = member.getVoiceState();
        if (memberVoiceState == null || !memberVoiceState.inAudioChannel() || memberVoiceState.getChannel() == null) {
            sendMessage(event, errorEmbed(defaultTitle(), "You must be in a voice channel to use this command."));
            return;
        }

        Long guildId = event.getGuild().getIdLong();
        Long channelId = member.getVoiceState().getChannel().getIdLong();
        String playlistName = String.join(" ", args);

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
    public String[] getAliases() {
        return new String[] {"ep", "addplaylist", "ap"};
    }

    @Override
    public ArgsLength argsLength() {
        return ArgsLength.NOT_NONE;
    }

}
