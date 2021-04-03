package io.papermc.hangar.model.internal.user.notifications;

import org.jdbi.v3.core.enums.EnumByOrdinal;

import java.util.List;

public class HangarNotification {

    private final long id;
    private final String action;
    private final List<String> message;
    private final boolean read;
    private final String originUserName;
    private final NotificationType type;

    public HangarNotification(long id, String action, List<String> message, boolean read, String originUserName, @EnumByOrdinal NotificationType type) {
        this.id = id;
        this.action = action;
        this.message = message;
        this.read = read;
        this.originUserName = originUserName;
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public String getAction() {
        return action;
    }

    public List<String> getMessage() {
        return message;
    }

    public boolean isRead() {
        return read;
    }

    public String getOriginUserName() {
        return originUserName;
    }

    public NotificationType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "HangarNotification{" +
                "id=" + id +
                ", action='" + action + '\'' +
                ", message=" + message +
                ", read=" + read +
                ", originUserName='" + originUserName + '\'' +
                ", type=" + type +
                '}';
    }
}
