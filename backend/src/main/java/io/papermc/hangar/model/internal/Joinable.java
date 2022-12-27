package io.papermc.hangar.model.internal;

import io.papermc.hangar.db.customtypes.RoleCategory;
import io.papermc.hangar.model.db.projects.ProjectOwner;
import io.papermc.hangar.model.db.roles.ExtendedRoleTable;
import io.papermc.hangar.model.internal.user.JoinableMember;
import java.util.List;

public interface Joinable<R extends ExtendedRoleTable<?, ?>> {

    ProjectOwner getOwner();

    RoleCategory getRoleCategory();

    List<JoinableMember<R>> getMembers();
}
