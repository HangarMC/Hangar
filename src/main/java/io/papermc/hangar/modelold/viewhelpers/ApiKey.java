package io.papermc.hangar.modelold.viewhelpers;

import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.modelold.NamedPermission;
import org.jdbi.v3.core.mapper.Nested;

import java.util.Collection;

public class ApiKey {

    private long id;
    private String name;
    private String tokenIdentifier;
    private Permission rawKeyPermissions;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    @Nested("perm")
    public void setRawKeyPermissions(Permission rawKeyPermissions) {
        this.rawKeyPermissions = rawKeyPermissions;
    }

    public boolean getIsSubKey(Permission perm) {
        return rawKeyPermissions.has(perm);
    }

    public Collection<NamedPermission> getNamedRawPermissions() {
        return rawKeyPermissions.toNamed();
    }
}
