package io.papermc.hangar.db.dao.internal.table.roles;

import io.papermc.hangar.model.db.roles.RoleTable;
import io.papermc.hangar.model.roles.Role;

public interface RoleDAO<T extends RoleTable<? extends Role<T>>> {

    T insert(T table);

    void update(T table);

    void delete(T table);

    T getTable(long id);

    T getTable(T table);

}
