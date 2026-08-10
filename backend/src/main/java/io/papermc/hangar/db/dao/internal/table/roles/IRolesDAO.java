package io.papermc.hangar.db.dao.internal.table.roles;

public interface IRolesDAO<T> {

    long insert(T table);

    void update(T table);

    void delete(T table);

    T getTable(long id);

    T getTable(long id, long userId);

    T getTableByPrincipal(long principalId, long userId);

    T getTable(T table);
}
