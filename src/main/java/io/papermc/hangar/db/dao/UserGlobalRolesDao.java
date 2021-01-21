package io.papermc.hangar.db.dao;


import io.papermc.hangar.db.modelold.RolesTable;
import io.papermc.hangar.db.modelold.UserGlobalRolesTable;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.AllowUnusedBindings;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RegisterBeanMapper(UserGlobalRolesDao.class)
public interface UserGlobalRolesDao {

    @SqlUpdate("INSERT INTO user_global_roles VALUES (:userId, :roleId) ON CONFLICT DO NOTHING")
    void insert(@BindBean UserGlobalRolesTable entry);

    @AllowUnusedBindings
    @SqlQuery("SELECT r.id, r.name, r.category, r.title, r.color, r.is_assignable, r.rank, r.permission::BIGINT " +
            "FROM user_global_roles ugr " +
            "JOIN roles r on ugr.role_id = r.id " +
            "JOIN users u on ugr.user_id = u.id " +
            "WHERE (u.id = :id OR u.name = :userName)")
    @RegisterBeanMapper(RolesTable.class)
    List<RolesTable> getRolesByUserId(Long id, String userName);

    @SqlUpdate("DELETE FROM user_global_roles WHERE user_id=:userId AND role_id=:roleId")
    void delete(@BindBean UserGlobalRolesTable entry);

    @SqlUpdate("DELETE FROM user_global_roles WHERE user_id = :userId")
    void deleteAll(long userId);
}
