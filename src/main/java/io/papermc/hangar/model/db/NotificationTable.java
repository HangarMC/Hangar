package io.papermc.hangar.model.db;

import io.papermc.hangar.model.internal.user.notifications.NotificationType;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;

public class NotificationTable extends Table {

    private final long userId;
    @EnumByOrdinal
    private final NotificationType notificationType;
    private final String action;
    private final boolean read;
    private final Long originId;
    private final String[] messageArgs;

    @JdbiConstructor
    public NotificationTable(OffsetDateTime createdAt, long id, long userId, @EnumByOrdinal NotificationType notificationType, String action, boolean read, Long originId, String[] messageArgs) {
        super(createdAt, id);
        this.userId = userId;
        this.notificationType = notificationType;
        this.action = action;
        this.read = read;
        this.originId = originId;
        this.messageArgs = messageArgs;
    }

    public NotificationTable(long userId, NotificationType notificationType, String action, Long originId, String[] messageArgs) {
        this.userId = userId;
        this.notificationType = notificationType;
        this.action = action;
        this.read = false;
        this.originId = originId;
        this.messageArgs = messageArgs;
    }

    public long getUserId() {
        return userId;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public String getAction() {
        return action;
    }

    public boolean isRead() {
        return read;
    }

    public Long getOriginId() {
        return originId;
    }

    public String[] getMessageArgs() {
        return messageArgs;
    }
}
