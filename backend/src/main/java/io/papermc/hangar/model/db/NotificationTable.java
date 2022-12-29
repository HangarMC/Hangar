package io.papermc.hangar.model.db;

import io.papermc.hangar.model.internal.user.notifications.NotificationType;
import java.time.OffsetDateTime;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class NotificationTable extends Table {

    private final long userId;
    private final String action;
    private final boolean read;
    private final Long originId;
    private final String[] messageArgs;
    private final NotificationType type;

    @JdbiConstructor
    public NotificationTable(final OffsetDateTime createdAt, final long id, final long userId, final String action, final boolean read, final Long originId, final String[] messageArgs, @EnumByOrdinal final NotificationType type) {
        super(createdAt, id);
        this.userId = userId;
        this.action = action;
        this.read = read;
        this.originId = originId;
        this.messageArgs = messageArgs;
        this.type = type;
    }

    public NotificationTable(final long userId, final String action, final Long originId, final String[] messageArgs, final NotificationType type) {
        this.userId = userId;
        this.action = action;
        this.read = false;
        this.originId = originId;
        this.messageArgs = messageArgs;
        this.type = type;
    }

    public long getUserId() {
        return this.userId;
    }

    public String getAction() {
        return this.action;
    }

    public boolean isRead() {
        return this.read;
    }

    /**
     * Returns the user id of the user that initiated the notification, if present.
     *
     * @return user id of the user that initiated the notification
     */
    public Long getOriginId() {
        return this.originId;
    }

    public String[] getMessageArgs() {
        return this.messageArgs;
    }

    @EnumByOrdinal
    public NotificationType getType() {
        return this.type;
    }
}
