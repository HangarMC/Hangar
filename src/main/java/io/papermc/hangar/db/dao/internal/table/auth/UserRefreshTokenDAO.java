package io.papermc.hangar.db.dao.internal.table.auth;

import io.papermc.hangar.model.db.auth.UserRefreshToken;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(UserRefreshToken.class)
public interface UserRefreshTokenDAO {

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO user_refresh_tokens (created_at, user_id, token) VALUES (:now, :userId, :token) ON CONFLICT (user_id) DO UPDATE SET created_at = :now, token = :token")
    UserRefreshToken insert(@BindBean UserRefreshToken userRefreshToken);

    @SqlQuery("SELECT * FROM user_refresh_tokens WHERE token = :token")
    UserRefreshToken getByToken(String token);
}
