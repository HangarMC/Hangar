package io.papermc.hangar.model.db.members;

import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class OrganizationMemberTable extends MemberTable {

    private final long organizationId;
    private final boolean hidden;

    public OrganizationMemberTable(final long userId, final long organizationId) {
        this(userId, organizationId, false);
    }

    @JdbiConstructor
    public OrganizationMemberTable(final long userId, final long organizationId, final boolean hidden) {
        super(userId);
        this.organizationId = organizationId;
        this.hidden = hidden;
    }

    public long getOrganizationId() {
        return this.organizationId;
    }

    public boolean isHidden() {
        return this.hidden;
    }
}
