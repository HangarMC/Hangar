package me.minidigger.hangar.db.dao;


import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import me.minidigger.hangar.db.model.UserGlobalRolesTable;

@Repository
@RegisterBeanMapper(UserGlobalRolesDao.class)
public interface UserGlobalRolesDao {

    @SqlUpdate("INSERT INTO user_global_roles VALUES (:userId, :roleId)")
    void insert(@BindBean UserGlobalRolesTable entry);
}
