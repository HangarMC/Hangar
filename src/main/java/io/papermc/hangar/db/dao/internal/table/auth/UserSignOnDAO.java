package io.papermc.hangar.db.dao.internal.table.auth;

import io.papermc.hangar.model.db.auth.UserSignOnTable;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(UserSignOnTable.class)
public interface UserSignOnDAO {

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO user_sign_ons (created_at, nonce) VALUES (:now, :nonce)")
    UserSignOnTable insert(@BindBean UserSignOnTable userSignOnsTable);

    @SqlQuery("SELECT * FROM user_sign_ons WHERE nonce = :nonce")
    UserSignOnTable getByNonce(String nonce);

    @SqlUpdate("UPDATE user_sign_ons SET is_completed = true WHERE id = :id")
    void markCompleted(long id);
}
