package net.maomaocloud.maomaomusic.bot.commands.slash;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.maomaocloud.maomaomusic.bot.commands.MusicCommand;
import net.maomaocloud.maomaomusic.music.manager.TrackScheduler.Mode;
import net.maomaocloud.maomaomusic.music.service.MusicService;

public class LoopSongSlashCommand extends MusicCommand<SlashCommandInteractionEvent> implements SlashCommand {

    private static final Mode REPEAT_SONG_MODE = Mode.REPEAT_SONG;

    public LoopSongSlashCommand(MusicService musicService) {
        super(musicService);
    }

    @Override
    public void executeMusicCommand(SlashCommandInteractionEvent event, String[] ignored) {
        Mode oldMode = getMusicService().getMode(event.getGuild().getIdLong());
        getMusicService().setMode(event.getGuild().getIdLong(), REPEAT_SONG_MODE);
        Mode updatedMode = getMusicService().getMode(event.getGuild().getIdLong());
        sendMessage(event, defaultEmbed("Toggled loop song mode: `"
                + oldMode + "` -> `"
                + updatedMode+ "`"));
    }

    @Override
    public String getName() {
        return "loopsong";
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), "Makes the current song loop");
    }

}
