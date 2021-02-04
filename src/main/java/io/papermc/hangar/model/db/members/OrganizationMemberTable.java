package io.papermc.hangar.model.db.members;

public class OrganizationMemberTable extends MemberTable {

    private final long organizationId;

    public OrganizationMemberTable(long userId, long organizationId) {
        super(userId);
        this.organizationId = organizationId;
    }

    public long getOrganizationId() {
        return organizationId;
    }
}
