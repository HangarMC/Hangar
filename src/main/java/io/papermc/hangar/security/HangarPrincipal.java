package io.papermc.hangar.security;

import io.papermc.hangar.model.Permission;

public class HangarPrincipal {

    private final long id;
    private final String userName;
    private final Permission globalPermissions;

    public HangarPrincipal(long id, String userName, Permission globalPermissions) {
        this.id = id;
        this.userName = userName;
        this.globalPermissions = globalPermissions;
    }

    public long getId() {
        return id;
    }

    public String getUserName() {
        return userName;
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
