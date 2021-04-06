package io.papermc.hangar.db.daoold.api;

import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionsDao {

    @SqlUpdate("DELETE FROM api_sessions WHERE token = :token")
    void delete(String token);
}
