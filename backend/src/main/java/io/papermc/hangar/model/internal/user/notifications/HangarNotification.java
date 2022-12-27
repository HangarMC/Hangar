package io.papermc.hangar.model.internal.user.notifications;

import org.jdbi.v3.core.enums.EnumByOrdinal;

import java.time.OffsetDateTime;
import java.util.List;

public class HangarNotification {

    private final OffsetDateTime createdAt;
    private final long id;
    private final String action;
    private final List<String> message;
    private final boolean read;
    private final String originUserName;
    private final NotificationType type;

    public HangarNotification(final OffsetDateTime createdAt, final long id, final String action, final List<String> message, final boolean read, final String originUserName, @EnumByOrdinal final NotificationType type) {
        this.createdAt = createdAt;
        this.id = id;
        this.action = action;
        this.message = message;
        this.read = read;
        this.originUserName = originUserName;
        this.type = type;
    }

    public OffsetDateTime getCreatedAt() {
        return this.createdAt;
    }

    public long getId() {
        return this.id;
    }

    public String getAction() {
        return this.action;
    }

    public List<String> getMessage() {
        return this.message;
    }

    public boolean isRead() {
        return this.read;
    }

    public String getOriginUserName() {
        return this.originUserName;
    }

    public NotificationType getType() {
        return this.type;
    }

    @Override
    public String toString() {
        return "HangarNotification{" +
                "id=" + this.id +
                ", action='" + this.action + '\'' +
                ", message=" + this.message +
                ", read=" + this.read +
                ", originUserName='" + this.originUserName + '\'' +
                ", type=" + this.type +
                '}';
    }
}
