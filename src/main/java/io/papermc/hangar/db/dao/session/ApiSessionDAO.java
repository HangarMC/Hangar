package io.papermc.hangar.db.dao.session;

import io.papermc.hangar.model.db.sessions.ApiSessionTable;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindFields;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(ApiSessionTable.class)
public interface ApiSessionDAO {

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO api_sessions (created_at, token, key_id, user_id, expires) VALUES (:now, :token, :keyId, :userId, :expires)")
    ApiSessionTable insert(@BindFields ApiSessionTable apiSessionTable);

    @SqlUpdate("DELETE FROM api_sessions WHERE token = :token")
    void delete(String token);
}
