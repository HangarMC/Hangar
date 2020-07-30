package me.minidigger.hangar.db.dao;

import me.minidigger.hangar.model.Permission;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionsDao {

    @RegisterBeanMapper(value = Permission.class, prefix = "perm")
    @SqlQuery("SELECT gt.permission::bigint perm_value FROM global_trust gt JOIN users u on gt.user_id = u.id WHERE (u.id = :userId OR u.name = :userName)")
    Permission getGlobalPermission(Long userId, String userName);
}
