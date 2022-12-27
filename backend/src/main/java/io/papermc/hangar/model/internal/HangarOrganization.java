package io.papermc.hangar.model.internal;

import io.papermc.hangar.db.customtypes.RoleCategory;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.projects.ProjectOwner;
import io.papermc.hangar.model.db.roles.OrganizationRoleTable;
import io.papermc.hangar.model.internal.user.JoinableMember;
import java.util.List;

public class HangarOrganization implements Joinable<OrganizationRoleTable> {

    private final long id;
    private final UserTable owner;
    private final List<JoinableMember<OrganizationRoleTable>> members;

    public HangarOrganization(final long id, final UserTable owner, final List<JoinableMember<OrganizationRoleTable>> members) {
        this.id = id;
        this.owner = owner;
        this.members = members;
    }

    public long getId() {
        return this.id;
    }

    @Override
    public ProjectOwner getOwner() {
        return this.owner;
    }

    @Override
    public RoleCategory getRoleCategory() {
        return RoleCategory.ORGANIZATION;
    }

    @Override
    public List<JoinableMember<OrganizationRoleTable>> getMembers() {
        return this.members;
    }

    @Override
    public String toString() {
        return "HangarOrganization{" +
            "id=" + this.id +
            ", owner=" + this.owner +
            ", members=" + this.members +
            '}';
    }
}
