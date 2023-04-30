package io.papermc.hangar.db.dao.internal.table.roles;

import io.papermc.hangar.model.common.roles.Role;
import io.papermc.hangar.model.db.roles.IRoleTable;
import java.util.List;

public interface IRolesDAO<T extends IRoleTable<? extends Role<T>>> {

    T insert(T table);

    T update(T table);

    void delete(T table);

    T getTable(long id);

    T getTable(long id, long userId);

    List<T> getRoleTablesByPrincipal(long principalId, String role);

    T getTableByPrincipal(long principalId, long userId);

    T getTable(T table);
}
