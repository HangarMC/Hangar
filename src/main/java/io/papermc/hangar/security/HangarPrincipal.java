package io.papermc.hangar.security;

import io.papermc.hangar.model.Permission;
import io.papermc.hangar.model.db.projects.ProjectOwner;

public class HangarPrincipal implements ProjectOwner {

    private final long id;
    private final String userName;
    private final Permission globalPermissions;

    public HangarPrincipal(long id, String userName, Permission globalPermissions) {
        this.id = id;
        this.userName = userName;
        this.globalPermissions = globalPermissions;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return userName;
    }

    @Override
    public long getUserId() {
        return id;
    }

    public Permission getGlobalPermissions() {
        return globalPermissions;
    }

    @Override
    public String toString() {
        return "HangarPrincipal{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", globalPermissions=" + globalPermissions +
                '}';
    }
}
