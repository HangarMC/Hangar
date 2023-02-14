package io.papermc.hangar.db.dao.internal.table.roles;

import io.papermc.hangar.model.db.roles.RoleTable;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(RoleTable.class)
public interface RolesDAO {

    @Timestamped
    @SqlUpdate("INSERT INTO roles VALUES (:id, :now, :name, :category, :title, :color, :assignable, :rank, cast(:permission AS bit(64)))")
    void insert(@BindBean RoleTable roleTable);

    @SqlUpdate("UPDATE roles VALUES SET title = :title, color = :color, rank = :rank WHERE id = :id")
    void update(long id, String title, String color, @Nullable Integer rank);

    @SqlQuery("SELECT id, created_at, name, category, title, color, assignable, rank, permission::bigint FROM roles WHERE id = :id")
    RoleTable getById(long id);
}
