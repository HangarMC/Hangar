package io.papermc.hangar.model.db;

import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;

public class NotificationTable extends Table {

    private final long userId;
    private final String action;
    private final boolean read;
    private final Long originId;
    private final String[] messageArgs;

    @JdbiConstructor
    public NotificationTable(OffsetDateTime createdAt, long id, long userId, String action, boolean read, Long originId, String[] messageArgs) {
        super(createdAt, id);
        this.userId = userId;
        this.action = action;
        this.read = read;
        this.originId = originId;
        this.messageArgs = messageArgs;
    }

    public NotificationTable(long userId, String action, Long originId, String[] messageArgs) {
        this.userId = userId;
        this.action = action;
        this.read = false;
        this.originId = originId;
        this.messageArgs = messageArgs;
    }

    public long getUserId() {
        return userId;
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
