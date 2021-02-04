package io.papermc.hangar.model.internal.user.notifications;

import org.jdbi.v3.core.enums.EnumByOrdinal;

import java.util.List;

public class HangarNotification {

    private final long id;
    private final NotificationType type;
    private final String action;
    private final List<String> message;
    private final boolean read;
    private final String originUserName;

    public HangarNotification(long id, @EnumByOrdinal NotificationType type, String action, List<String> message, boolean read, String originUserName) {
        this.id = id;
        this.type = type;
        this.action = action;
        this.message = message;
        this.read = read;
        this.originUserName = originUserName;
    }

    public long getId() {
        return id;
    }

    public NotificationType getType() {
        return type;
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

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", type=" + type +
                ", action='" + action + '\'' +
                ", message=" + message +
                ", read=" + read +
                ", originUserName='" + originUserName + '\'' +
                '}';
    }
}
