package io.papermc.hangar.db.modelold;


import io.papermc.hangar.modelold.NotificationType;
import org.jdbi.v3.core.enums.EnumByOrdinal;

import java.time.OffsetDateTime;

public class NotificationsTable {

    private long id;
    private OffsetDateTime createdAt;
    private long userId;
    private NotificationType notificationType;
    private String action;
    private boolean read;
    private Long originId;
    private String[] messageArgs;

    public NotificationsTable(long userId, NotificationType notificationType, String action, Long originId, String[] messageArgs) {
        this.userId = userId;
        this.notificationType = notificationType;
        this.action = action;
        this.originId = originId;
        this.messageArgs = messageArgs;
    }

    public NotificationsTable() { }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }


    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }


    @EnumByOrdinal
    public NotificationType getNotificationType() {
        return notificationType;
    }

    @EnumByOrdinal
    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }


    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }


    public boolean getRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }


    public Long getOriginId() {
        return originId;
    }

    public void setOriginId(Long originId) {
        this.originId = originId;
    }


    public String[] getMessageArgs() {
        return messageArgs;
    }

    public void setMessageArgs(String[] messageArgs) {
        this.messageArgs = messageArgs;
    }

}
