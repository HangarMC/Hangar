package io.papermc.hangar.db.dao;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import io.papermc.hangar.db.model.RolesTable;

@Repository
@RegisterBeanMapper(RolesTable.class)
public interface RoleDao {

    @SqlUpdate("INSERT INTO roles VALUES (:id, :name, :category, :title, :color, :isAssignable, :rank, CAST(:permission AS bit(64)))")
    void insert(@BindBean RolesTable role);

    @SqlQuery("SELECT id, name, category, title, color, is_assignable, rank, permission AS long FROM roles WHERE id = :id")
    RolesTable getById(long id);
}
