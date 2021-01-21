package io.papermc.hangar.db.modelold;


import org.jdbi.v3.core.annotation.Unmappable;

import java.time.OffsetDateTime;

public class UserSessionsTable {

    private long id;
    private OffsetDateTime createdAt;
    private OffsetDateTime expiration;
    private String token;
    private long userId;

    public UserSessionsTable(OffsetDateTime expiration, String token, long userId) {
        this.expiration = expiration;
        this.token = token;
        this.userId = userId;
    }

    public UserSessionsTable() { }

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


    public OffsetDateTime getExpiration() {
        return expiration;
    }

    public void setExpiration(OffsetDateTime expiration) {
        this.expiration = expiration;
    }


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }


    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    @Unmappable
    public boolean hasExpired() {
        return this.expiration.isBefore(OffsetDateTime.now());
    }

}
