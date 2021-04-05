package io.papermc.hangar.security.authentication;

import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.db.projects.ProjectOwner;

public class HangarPrincipal implements ProjectOwner {

    private final long id;
    private final String name;
    private final boolean locked;
    private final Permission globalPermissions;

    public HangarPrincipal(long id, String name, boolean locked, Permission globalPermissions) {
        this.id = id;
        this.name = name;
        this.locked = locked;
        this.globalPermissions = globalPermissions;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getUserId() {
        return id;
    }

    public boolean isLocked() {
        return locked;
    }

    public Permission getGlobalPermissions() {
        return globalPermissions;
    }

    @Override
    public String toString() {
        return "HangarPrincipal{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", locked=" + locked +
                ", globalPermissions=" + globalPermissions +
                '}';
    }
}
