package me.minidigger.hangar.db.model;


import java.time.OffsetDateTime;

public class ApiKeysTable {

    private long id;
    private OffsetDateTime createdAt;
    private String name;
    private long ownerId;
    private String tokenIdentifier;
    private String token;
    private long rawKeyPermissions;


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


    public String getTokenIdentifier() {
        return tokenIdentifier;
    }

    public void setTokenIdentifier(String tokenIdentifier) {
        this.tokenIdentifier = tokenIdentifier;
    }


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }


    public long getRawKeyPermissions() {
        return rawKeyPermissions;
    }

    public void setRawKeyPermissions(long rawKeyPermissions) {
        this.rawKeyPermissions = rawKeyPermissions;
    }

}
