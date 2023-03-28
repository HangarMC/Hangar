package io.papermc.hangar.components.auth.dao;

import io.papermc.hangar.components.auth.model.credential.CredentialType;
import io.papermc.hangar.components.auth.model.db.UserCredentialTable;
import io.papermc.hangar.db.customtypes.JSONB;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(UserCredentialTable.class)
public interface UserCredentialDAO {

    @Timestamped
    @SqlUpdate("INSERT INTO user_credentials(created_at, user_id, credential, type, updated_at) VALUES (:now, :userId, :credential, :type, :now)")
    void insert(long userId, JSONB credential, @EnumByOrdinal CredentialType type);

    @Nullable
    @SqlQuery("SELECT * FROM user_credentials where type = :type and user_id = :userId")
    UserCredentialTable getByType(@EnumByOrdinal CredentialType type, long userId);
}
