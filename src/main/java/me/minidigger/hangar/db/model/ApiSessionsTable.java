package me.minidigger.hangar.db.model;


import java.time.OffsetDateTime;

public class ApiSessionsTable {

    private long id;
    private OffsetDateTime createdAt;
    private String token;
    private Long keyId;
    private Long userId;
    private OffsetDateTime expires;

    public ApiSessionsTable(String token, Long keyId, Long userId, OffsetDateTime expires) {
        this.token = token;
        this.keyId = keyId;
        this.userId = userId;
        this.expires = expires;
    }

    public ApiSessionsTable() {
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


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }


    public Long getKeyId() {
        return keyId;
    }

    public void setKeyId(Long keyId) {
        this.keyId = keyId;
    }


    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }


    public OffsetDateTime getExpires() {
        return expires;
    }

    public void setExpires(OffsetDateTime expires) {
        this.expires = expires;
    }

}
