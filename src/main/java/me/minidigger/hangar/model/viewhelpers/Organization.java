package me.minidigger.hangar.model.viewhelpers;

import java.time.OffsetDateTime;

public class Organization {
    private long id;
    private OffsetDateTime createdAt;
    private String name;
    private long ownerId;
    private long userId;

    public Organization(long id, OffsetDateTime createdAt, String name, long ownerId, long userId) {
        this.id = id;
        this.createdAt = createdAt;
        this.name = name;
        this.ownerId = ownerId;
        this.userId = userId;
    }

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}
