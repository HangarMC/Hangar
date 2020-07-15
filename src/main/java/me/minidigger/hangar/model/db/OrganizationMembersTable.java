package me.minidigger.hangar.model.db;


public class OrganizationMembersTable {

    private long userId;
    private long organizationId;


    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }


    public long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(long organizationId) {
        this.organizationId = organizationId;
    }

}
