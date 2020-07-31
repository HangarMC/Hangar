package me.minidigger.hangar.db.model;


import me.minidigger.hangar.model.Permission;
import org.jdbi.v3.core.mapper.Nested;

import java.time.OffsetDateTime;

public class ApiKeysTable {

    private long id;
    private OffsetDateTime createdAt;
    private String name;
    private long ownerId;
    private String tokenIdentifier;
    private String token;
    @Nested("perm")
    private Permission rawKeyPermissions;


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


    public Permission getRawKeyPermissions() {
        return rawKeyPermissions;
    }

    public void setRawKeyPermissions(Permission rawKeyPermissions) {
        this.rawKeyPermissions = rawKeyPermissions;
    }

}
