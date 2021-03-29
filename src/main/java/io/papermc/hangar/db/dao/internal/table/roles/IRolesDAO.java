package io.papermc.hangar.db.dao.internal.table.roles;

import io.papermc.hangar.model.common.roles.Role;
import io.papermc.hangar.model.db.roles.IRoleTable;

public interface IRolesDAO<T extends IRoleTable<? extends Role<T>>> {

    T insert(T table);

    T update(T table);

    void delete(T table);

    T getTable(long id, long userId);

    T getTableByPrincipal(long principalId, long userId);

    T getTable(T table);
}
