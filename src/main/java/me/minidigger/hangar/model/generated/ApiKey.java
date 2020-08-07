package me.minidigger.hangar.model.generated;

import me.minidigger.hangar.model.Permission;
import org.jdbi.v3.core.mapper.Nested;

public class ApiKey {

    private String name;
    private long ownerId;
    private String tokenIdentifier;
    @Nested
    private Permission rawKeyPermissions;

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

    public Permission getRawKeyPermissions() {
        return rawKeyPermissions;
    }

    public void setRawKeyPermissions(Permission rawKeyPermissions) {
        this.rawKeyPermissions = rawKeyPermissions;
    }
}
