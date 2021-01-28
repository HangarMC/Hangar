package io.papermc.hangar.model.internal;

import io.papermc.hangar.db.customtypes.RoleCategory;
import io.papermc.hangar.model.db.projects.ProjectOwner;
import io.papermc.hangar.model.db.roles.RoleTable;

import java.util.List;

public interface Joinable<R extends RoleTable> {

    ProjectOwner getOwner();

    RoleCategory getRoleCategory();

    List<JoinableMember<R>> getMembers();
}
