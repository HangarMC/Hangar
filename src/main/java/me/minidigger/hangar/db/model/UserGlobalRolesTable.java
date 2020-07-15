package me.minidigger.hangar.db.model;


public class UserGlobalRolesTable {

    private long userId;
    private long roleId;


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
