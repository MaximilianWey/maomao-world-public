package net.maomaocloud.maomaomusic.music.events;

import org.springframework.context.ApplicationEvent;

public class QueueClearedEvent extends ApplicationEvent {

    private final String guildId;

    public QueueClearedEvent(Object source, String guildId) {
        super(source);
        this.guildId = guildId;
    }

    public String getGuildId() {
        return guildId;
    }
}
