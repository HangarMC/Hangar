package io.papermc.hangar.db.dao.internal.table.auth;

import io.papermc.hangar.model.db.auth.UserOauthTokenTable;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(UserOauthTokenTable.class)
public interface UserOauthTokenDAO {

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO user_oauth_token (created_at, username, id_token) VALUES (:now, :username, :idToken)")
    UserOauthTokenTable insert(@BindBean UserOauthTokenTable userOauthTokenTable);

    @SqlQuery("SELECT * FROM user_oauth_token WHERE username = :username ORDER BY created_at DESC LIMIT 1")
    UserOauthTokenTable getByUsername(String username);

    @SqlUpdate("DELETE FROM user_oauth_token WHERE username = :username")
    void remove(String username);
}
