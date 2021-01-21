package io.papermc.hangar.db;

import io.papermc.hangar.db.modelold.UserSessionsTable;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
@RegisterBeanMapper(UserSessionsTable.class)
public interface UserSessionDao {

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO user_sessions (created_at, expiration, token, user_id) VALUES (:now, :expiration, :token, :userId)")
    UserSessionsTable insert(@BindBean UserSessionsTable userSessionsTable);

    @SqlUpdate("DELETE FROM user_sessions WHERE id = :id")
    void delete(@BindBean UserSessionsTable userSessionsTable);

    @SqlQuery("SELECT * FROM user_sessions WHERE token = :token")
    UserSessionsTable getByToken(String token);
}
