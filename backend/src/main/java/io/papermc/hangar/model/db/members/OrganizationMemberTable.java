package io.papermc.hangar.model.db.members;

import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class OrganizationMemberTable extends MemberTable {

    private final long organizationId;
    private final boolean hidden;

    public OrganizationMemberTable(long userId, long organizationId) {
        this(userId, organizationId, false);
    }

    @JdbiConstructor
    public OrganizationMemberTable(long userId, long organizationId, boolean hidden) {
        super(userId);
        this.organizationId = organizationId;
        this.hidden = hidden;
    }

    public long getOrganizationId() {
        return organizationId;
    }

    public boolean isHidden() {
        return hidden;
    }
}
