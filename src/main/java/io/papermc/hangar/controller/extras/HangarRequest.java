package io.papermc.hangar.controller.extras;

import io.papermc.hangar.model.Permission;
import io.papermc.hangar.model.db.UserTable;

public class HangarRequest {

    private final UserTable userTable;
    private final Permission globalPermissions;

    public HangarRequest(UserTable userTable, Permission globalPermissions) {
        this.userTable = userTable;
        this.globalPermissions = globalPermissions;
    }

    public boolean hasUser() {
        return userTable != null;
    }

    public Long getUserId() {
        return userTable == null ? null : userTable.getId();
    }

    public UserTable getUserTable() {
        return userTable;
    }

    public Permission getGlobalPermissions() {
        return globalPermissions;
    }

    @Override
    public String toString() {
        return "HangarRequest{" +
                "userTable=" + userTable +
                ", globalPermissions=" + globalPermissions +
                '}';
    }
}
