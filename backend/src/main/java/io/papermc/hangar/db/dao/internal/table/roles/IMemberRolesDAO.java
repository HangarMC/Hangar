package io.papermc.hangar.db.dao.internal.table.roles;

import io.papermc.hangar.model.db.roles.ExtendedRoleTable;
import java.util.List;

public interface IMemberRolesDAO<T extends ExtendedRoleTable<?>> extends IRolesDAO<T> {

    List<T> getOwnerTables(long principalId);
}
