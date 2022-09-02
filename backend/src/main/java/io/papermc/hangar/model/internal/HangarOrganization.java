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

    public HangarOrganization(long id, UserTable owner, List<JoinableMember<OrganizationRoleTable>> members) {
        this.id = id;
        this.owner = owner;
        this.members = members;
    }

    public long getId() {
        return id;
    }

    @Override
    public ProjectOwner getOwner() {
        return owner;
    }

    @Override
    public RoleCategory getRoleCategory() {
        return RoleCategory.ORGANIZATION;
    }

    @Override
    public List<JoinableMember<OrganizationRoleTable>> getMembers() {
        return members;
    }

    @Override
    public String toString() {
        return "HangarOrganization{" +
                "id=" + id +
                ", owner=" + owner +
                ", members=" + members +
                '}';
    }
}
