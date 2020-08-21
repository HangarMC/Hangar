package io.papermc.hangar.db.model;


public class UserGlobalRolesTable {

    private long userId;
    private long roleId;

    public UserGlobalRolesTable(long userId, long roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }

    public UserGlobalRolesTable() {
        //
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }


    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

}
