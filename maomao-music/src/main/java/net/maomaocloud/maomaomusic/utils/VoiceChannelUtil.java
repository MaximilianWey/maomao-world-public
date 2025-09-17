package net.maomaocloud.maomaomusic.utils;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.maomaocloud.maomaomusic.bot.exceptions.MusicExceptions;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public final class VoiceChannelUtil {

    private VoiceChannelUtil() {
        // Prevent instantiation
    }

    public static Optional<String> isVoiceChatJoinable(Member user, Member bot) {
        if (user == null || user.getVoiceState() == null || bot.getVoiceState() == null) {
            return Optional.of("Cannot retrieve voice state.");
        }

        final var userChannel = user.getVoiceState().getChannel();
        final var botChannel = bot.getVoiceState().getChannel();

        if (userChannel == null) {
            return Optional.of("You must be in a voice channel to use this feature.");
        }

        if (botChannel != null && !botChannel.equals(userChannel)) {
            return Optional.of("I'm already in another voice channel. Please join me there or wait.");
        }

        return Optional.empty();
    }

    public static boolean isVoiceChatJoinable(JDA jda, Long guildId, Long userid) {
        Guild guild = jda.getGuildById(guildId);
        if (guild == null) {
            return false;
        }
        Member member = guild.getMemberById(userid);
        if (member == null || member.getVoiceState() == null || member.getVoiceState().getChannel() == null) {
            return false;
        }
        Member self = guild.getSelfMember();
        return isVoiceChatJoinable(member, self).isEmpty();
    }

    public static void connectToVoiceChannel(AudioChannel audioChannel) {
        if (audioChannel != null) {
            audioChannel.getGuild().getAudioManager().openAudioConnection(audioChannel);
        }
    }

    public static void checkAndJoinVoiceChannel(Member user, Member bot, Consumer<Void> onSuccess, Consumer<String> onFailure) {
        isVoiceChatJoinable(user, bot).ifPresentOrElse(
                onFailure,
                () -> {
                    AudioChannel channel = Objects.requireNonNull(user.getVoiceState()).getChannel();
                    connectToVoiceChannel(channel);
                    onSuccess.accept(null);
                }
        );
    }

    // Convenience wrapper for use in event listeners
    public static void checkAndJoinVoiceChannel(MessageReceivedEvent event, Runnable onSuccess, Consumer<String> onFailure) {
        Member user = event.getMember();
        Member bot = event.getGuild().getSelfMember();

        checkAndJoinVoiceChannel(user, bot, (ignored) -> onSuccess.run(), onFailure);
    }

    public static void checkAndJoinVoiceChannel(JDA jda, Long guildId, Long userId, Runnable onSuccess, Consumer<String> onFailure) {
        Guild guild = jda.getGuildById(guildId);
        if (guild == null) {
            onFailure.accept("Guild not found.");
            return;
        }
        Member user = guild.getMemberById(userId);
        if (user == null) {
            onFailure.accept("User not found.");
            return;
        }
        checkAndJoinVoiceChannel(user, guild.getSelfMember(), (ignored) -> onSuccess.run(), onFailure);
    }

    public static boolean isUserSameVoiceChannel(Member member, Member self) {
        if (member == null || self == null) {
            return false;
        }
        if (member.getVoiceState() == null || self.getVoiceState() == null) {
            return false;
        }
        if (member.getVoiceState().getChannel() == null || self.getVoiceState().getChannel() == null) {
            return false;
        }
        try {
            return member.getVoiceState().getChannel().equals(self.getVoiceState().getChannel());
        } catch (Exception e) {
            return false;
        }
    }

    public static void isBotAvailable(Member self) throws MusicExceptions {
        if (self == null) {
            throw new MusicExceptions.BotUnavailableException();
        }

        if (self.getVoiceState() == null || self.getVoiceState().getChannel() == null) {
            return;
        }

        if (self.getVoiceState().getChannel().getMembers().size() > 1) {
            throw new MusicExceptions.BotAlreadyConnectedToDifferentVoiceChatException(self.getVoiceState().getChannel());
        }
    }

    public static boolean isUserInVoiceChannel(Member member) {
        return member != null && member.getVoiceState() != null && member.getVoiceState().getChannel() != null;
    }
}
