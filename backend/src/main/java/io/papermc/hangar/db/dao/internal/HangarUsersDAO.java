package io.papermc.hangar.db.dao.internal;

import io.papermc.hangar.model.db.OrganizationTable;
import io.papermc.hangar.model.db.UserTable;
import org.apache.commons.lang3.tuple.Pair;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
public interface HangarUsersDAO {

    @RegisterConstructorMapper(UserTable.class)
    @RegisterConstructorMapper(value = OrganizationTable.class, prefix = "o_")
    @SqlQuery("""
        SELECT u.*,
           o.id o_id,
           o.created_at o_created_at,
           o.name o_name,
           o.owner_id o_owner_id,
           o.user_id o_user_id,
           u.uuid o_user_uuid
           FROM users u
               LEFT JOIN organizations o ON u.id = o.user_id
           WHERE lower(u.name) = lower(:userName)
        """)
    Pair<UserTable, OrganizationTable> getUserAndOrg(String userName);

    @SqlUpdate("INSERT INTO project_stars VALUES (:userId, :projectId) ON CONFLICT DO NOTHING")
    void setStarred(long projectId, long userId);

    @SqlUpdate("DELETE FROM project_stars WHERE user_id = :userId AND project_id = :projectId")
    void setNotStarred(long projectId, long userId);

    @SqlUpdate("INSERT INTO project_watchers VALUES (:projectId, :userId) ON CONFLICT DO NOTHING")
    void setWatching(long projectId, long userId);

    @SqlUpdate("DELETE FROM project_watchers WHERE project_id = :projectId AND user_id = :userId")
    void setNotWatching(long projectId, long userId);
}
