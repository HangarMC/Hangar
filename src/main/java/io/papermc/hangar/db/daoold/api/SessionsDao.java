package io.papermc.hangar.db.daoold.api;

import io.papermc.hangar.db.modelold.ApiSessionsTable;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
@RegisterBeanMapper(ApiSessionsTable.class)
public interface SessionsDao {

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO api_sessions (created_at, token, key_id, user_id, expires) VALUES (:now, :token, :keyId, :userId, :expires)")
    ApiSessionsTable insert(@BindBean ApiSessionsTable apiSessionsTable);

    @SqlUpdate("DELETE FROM api_sessions WHERE token = :token")
    void delete(String token);
}
