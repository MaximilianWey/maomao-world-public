package net.maomaocloud.maomaomusic.bot.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import net.maomaocloud.maomaomusic.bot.exceptions.MusicExceptions;
import net.maomaocloud.maomaomusic.music.service.MusicService;
import net.maomaocloud.maomaomusic.utils.EmbedHelper;
import net.maomaocloud.maomaomusic.utils.VoiceChannelUtil;

import java.util.function.Consumer;

import static net.maomaocloud.maomaomusic.utils.VoiceChannelUtil.isUserInVoiceChannel;

public abstract class MusicCommand<T> implements DiscordCommand<T> {

    private final MusicService musicService;

    public MusicCommand(MusicService musicService) {
        this.musicService = musicService;
    }

    protected MusicService getMusicService() {
        return musicService;
    }

    @Override
    public void execute(T event, String[] args) {
        try {
            isAllowed(event);
            executeMusicCommand(event, args);
        } catch (MusicExceptions | IllegalStateException e) {
            sendMessage(event, errorEmbed(defaultTitle(), e.getMessage()));
        } catch (Exception e) {
            sendMessage(event, errorEmbed(defaultTitle(), "An error occurred while executing the command: " + e.getMessage()));
        }
    }

    public abstract void executeMusicCommand(T event, String[] args);

    @Override
    public EmbedHelper.EmbedColor defaultColor() {
        return EmbedHelper.EmbedColor.AQUA;
    }

    @Override
    public String defaultTitle() {
        return "[Music]";
    }

    private void isAllowed(T event) {
        if (event instanceof MessageReceivedEvent messageReceivedEvent) {
            final Member member = messageReceivedEvent.getMember();
            final Member self = messageReceivedEvent.getGuild().getSelfMember();
            isAllowed(member, self);
        } else if (event instanceof SlashCommandInteractionEvent slashCommandInteractionEvent) {
            final Member member = slashCommandInteractionEvent.getMember();
            final Member self = slashCommandInteractionEvent.getGuild().getSelfMember();
            isAllowed(member, self);
        } else {
            throw new IllegalStateException("An error occurred while processing your request: Invalid message type");
        }
    }

    private void isAllowed(Member member, Member self) throws MusicExceptions {
        if (member == null || self == null) {
            throw new MusicExceptions.BotUnavailableException();
        }

        if (member.hasPermission(Permission.ADMINISTRATOR)) {
            return;
        }

        if (isUserModerator(member)) {
            return;
        }

        if (!isUserInVoiceChannel(member)) {
            throw new MusicExceptions.UserNotConnectedToVoiceChannelException();
        }

        try {
            isBotAvailable(self);
        } catch (MusicExceptions e) {
            if (!isUserSameVoiceChannel(member, self)) {
                throw new MusicExceptions.BotAlreadyConnectedToDifferentVoiceChatException(self.getVoiceState().getChannel());
            }
        }
    }

    protected void checkAndJoinVoiceChat(MessageReceivedEvent event, Runnable onSuccess, Consumer<String> onFailure) {
        VoiceChannelUtil.checkAndJoinVoiceChannel(event, onSuccess, onFailure);
    }

    protected void checkAndJoinVoiceChat(SlashCommandInteractionEvent event, Runnable onSuccess, Consumer<String> onFailure) {
        Member member = event.getMember();
        Member bot = event.getGuild().getSelfMember();

        VoiceChannelUtil.checkAndJoinVoiceChannel(member, bot,
                (ignored) -> onSuccess.run(),
                onFailure
        );
    }

    protected boolean isUserModerator(Member member) {
        try {
            return member.hasPermission(
                    Permission.KICK_MEMBERS,
                    Permission.BAN_MEMBERS,
                    Permission.MANAGE_CHANNEL
            );
        } catch (Exception e) {
            return false;
        }
    }

    protected void isBotAvailable(Member self) {
        VoiceChannelUtil.isBotAvailable(self);
    }

    protected boolean isUserSameVoiceChannel(Member member, Member self) {
        return VoiceChannelUtil.isUserSameVoiceChannel(member, self);
    }

    protected LeaveResponse leaveVoiceChat(MessageReceivedEvent event) {
        final Member self = event.getGuild().getSelfMember();
        final AudioManager manager = event.getGuild().getAudioManager();
        return leaveVoiceChat(self, manager);
    }

    protected LeaveResponse leaveVoiceChat(SlashCommandInteractionEvent event) {
        final Member self = event.getGuild().getSelfMember();
        final AudioManager manager = event.getGuild().getAudioManager();
        return leaveVoiceChat(self, manager);
    }

    protected LeaveResponse leaveVoiceChat(Member self, AudioManager manager) {
        final var state = self.getVoiceState();
        if (state == null) {
            return new LeaveResponse("I'm not connected to a voice channel.", false);
        }

        final var selfChannel = self.getVoiceState().getChannel();

        if (self.getVoiceState() == null || selfChannel == null) {
            return new LeaveResponse("I'm not connected to a voice channel.", false);
        } else {
            manager.closeAudioConnection();
            return new LeaveResponse("Left your voice channel.", true);
        }
    }

    public record LeaveResponse(String message, Boolean left) {}
}
