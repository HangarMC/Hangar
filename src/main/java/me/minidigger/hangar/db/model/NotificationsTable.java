package me.minidigger.hangar.db.model;


import java.time.OffsetDateTime;

public class NotificationsTable {

    private long id;
    private OffsetDateTime createdAt;
    private long userId;
    private long notificationType;
    private String action;
    private boolean read;
    private long originId;
    private String messageArgs;


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


    public long getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(long notificationType) {
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


    public long getOriginId() {
        return originId;
    }

    public void setOriginId(long originId) {
        this.originId = originId;
    }


    public String getMessageArgs() {
        return messageArgs;
    }

    public void setMessageArgs(String messageArgs) {
        this.messageArgs = messageArgs;
    }

}
