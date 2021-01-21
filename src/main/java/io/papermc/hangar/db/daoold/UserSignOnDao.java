package io.papermc.hangar.db.daoold;

import io.papermc.hangar.db.modelold.UserSignOnsTable;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
@RegisterBeanMapper(UserSignOnsTable.class)
public interface UserSignOnDao {

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO user_sign_ons (created_at, nonce) VALUES (:now, :nonce)")
    UserSignOnsTable insert(@BindBean UserSignOnsTable userSignOnsTable);

    @SqlQuery("SELECT * FROM user_sign_ons WHERE nonce = :nonce")
    UserSignOnsTable getByNonce(String nonce);

    @SqlUpdate("UPDATE user_sign_ons SET is_completed = true WHERE id = :id")
    void markCompleted(long id);
}
