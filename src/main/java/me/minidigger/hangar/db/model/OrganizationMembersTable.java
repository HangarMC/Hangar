package me.minidigger.hangar.db.model;


public class OrganizationMembersTable {

    private long userId;
    private long organizationId;

    public OrganizationMembersTable(long userId, long organizationId) {
        this.userId = userId;
        this.organizationId = organizationId;
    }

    public OrganizationMembersTable() { }

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
