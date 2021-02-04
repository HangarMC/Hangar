package io.papermc.hangar.db.dao.internal.table.auth;

import io.papermc.hangar.model.db.auth.UserRefreshToken;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RegisterConstructorMapper(UserRefreshToken.class)
public interface UserRefreshTokenDAO {

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO user_refresh_tokens (created_at, user_id, last_updated, token, device_id) VALUES (:now, :userId, :now, :token, :deviceId)")
    UserRefreshToken insert(@BindBean UserRefreshToken userRefreshToken);

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("UPDATE user_refresh_tokens SET last_updated = :now, token = :token WHERE id = :id")
    UserRefreshToken update(@BindBean UserRefreshToken userRefreshToken);

    @SqlUpdate("DELETE FROM user_refresh_tokens WHERE token = :token")
    void delete(UUID token);

    @SqlQuery("SELECT * FROM user_refresh_tokens WHERE token = :token")
    UserRefreshToken getByToken(UUID token);
}
