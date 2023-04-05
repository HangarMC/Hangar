package io.papermc.hangar.components.auth.dao;

import io.papermc.hangar.components.auth.model.credential.CredentialType;
import io.papermc.hangar.components.auth.model.db.UserCredentialTable;
import io.papermc.hangar.db.customtypes.JSONB;
import java.util.List;
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

    @Nullable
    @SqlQuery("SELECT * FROM user_credentials where type = :type and credential ->> 'user_handle' = :userHandle")
    UserCredentialTable getByUserHandle(@EnumByOrdinal CredentialType type, String userHandle);

    @SqlUpdate("DELETE FROM user_credentials WHERE type = :type and user_id = :userId")
    void remove(long userId, @EnumByOrdinal CredentialType type);

    @Timestamped
    @SqlUpdate("UPDATE user_credentials set credential = :credential, updated_at = :now WHERE type = :type and user_id = :userId")
    void update(long userId, JSONB credential, @EnumByOrdinal CredentialType type);

    @EnumByOrdinal
    @SqlQuery("SELECT type FROM user_credentials WHERE user_id = :userId AND type != :password AND (type != :webAuthn OR (credential ->> 'credentials' IS NOT NULL AND jsonb_array_length(credential -> 'credentials') > 0))")
    List<CredentialType> getAll(long userId, @EnumByOrdinal CredentialType password, @EnumByOrdinal CredentialType webAuthn);
}
