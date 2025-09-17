package net.maomaocloud.maomaomusic.bot.exceptions;

import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;

public abstract class MusicExceptions extends RuntimeException {

    public MusicExceptions(String reason) {
        super(reason);
    }

    public static class BotUnavailableException extends MusicExceptions {
        public BotUnavailableException() {
            super("The Bot is currently not available!");
        }
    }


    public static class BotAlreadyConnectedToDifferentVoiceChatException extends MusicExceptions {
        public BotAlreadyConnectedToDifferentVoiceChatException(AudioChannel channel) {
            super("I'm already connected to <#" + channel.getId() + ">!");
        }
    }

    public static class UserNotConnectedToVoiceChannelException extends MusicExceptions {
        public UserNotConnectedToVoiceChannelException() {
            super("You must be connected to a voice channel to use this command!");
        }
    }
}
