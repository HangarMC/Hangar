package io.papermc.hangar.db.modelold;


import io.papermc.hangar.model.common.Permission;
import org.jdbi.v3.core.mapper.Nested;

import java.time.OffsetDateTime;

public class ApiKeysTable {

    private long id;
    private OffsetDateTime createdAt;
    private String name;
    private long ownerId;
    private String tokenIdentifier;
    private String token;
    private Permission rawKeyPermissions;

    public ApiKeysTable(String name, long ownerId, String tokenIdentifier, String token, Permission rawKeyPermissions) {
        this.name = name;
        this.ownerId = ownerId;
        this.tokenIdentifier = tokenIdentifier;
        this.token = token;
        this.rawKeyPermissions = rawKeyPermissions;
    }

    public ApiKeysTable() { }

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

    @Nested("perm")
    public void setRawKeyPermissions(Permission rawKeyPermissions) {
        this.rawKeyPermissions = rawKeyPermissions;
    }

}
