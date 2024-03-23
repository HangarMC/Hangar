package io.papermc.hangar.components.auth.dao;

import io.papermc.hangar.components.auth.model.credential.CredentialType;
import io.papermc.hangar.components.auth.model.db.UserCredentialTable;
import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.model.db.UserTable;
import java.util.List;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.JoinRow;
import org.jdbi.v3.spring5.JdbiRepository;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.config.RegisterJoinRowMapper;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;
import org.checkerframework.checker.nullness.qual.Nullable;

@JdbiRepository
@RegisterConstructorMapper(UserCredentialTable.class)
public interface UserCredentialDAO {

    @Timestamped
    @SqlUpdate("INSERT INTO user_credentials(created_at, user_id, credential, type, updated_at) VALUES (:now, :userId, :credential, :type, :now)")
    void insert(long userId, JSONB credential, @EnumByOrdinal CredentialType type);

    @Nullable
    @SqlQuery("SELECT * FROM user_credentials where type = :type and user_id = :userId LIMIT 1")
    UserCredentialTable getByType(@EnumByOrdinal CredentialType type, long userId);

    @Nullable
    @SqlQuery("SELECT * FROM user_credentials where type = :type and credential ->> 'user_handle' = :userHandle")
    UserCredentialTable getByUserHandle(@EnumByOrdinal CredentialType type, String userHandle);

    @SqlUpdate("DELETE FROM user_credentials WHERE type = :type and user_id = :userId")
    void remove(long userId, @EnumByOrdinal CredentialType type);

    @Timestamped
    @SqlUpdate("UPDATE user_credentials set credential = :credential, updated_at = :now WHERE type = :type and user_id = :userId")
    boolean update(long userId, JSONB credential, @EnumByOrdinal CredentialType type);

    @EnumByOrdinal
    @SqlQuery("SELECT type FROM user_credentials WHERE user_id = :userId AND type != :password AND type != :oauth AND (type != :webAuthn OR (credential ->> 'credentials' IS NOT NULL AND jsonb_array_length(credential -> 'credentials') > 0))")
    List<CredentialType> getAll(long userId, @EnumByOrdinal CredentialType password, @EnumByOrdinal CredentialType webAuthn, @EnumByOrdinal CredentialType oauth);

    @UseStringTemplateEngine
    @RegisterConstructorMapper(value = UserCredentialTable.class, prefix = "uc")
    @RegisterConstructorMapper(UserTable.class)
    @RegisterJoinRowMapper({UserTable.class, UserCredentialTable.class})
    @SqlQuery("""
            SELECT u.*,
                   uc.id                           uc_id,
                   uc.credential                   uc_credential,
                   uc.created_at                   uc_created_at,
                   uc.updated_at                   uc_updated_at,
                   uc.user_id                      uc_user_id,
                   uc.type                         uc_type
            FROM user_credentials uc
                JOIN users u ON uc.user_id = u.id
            WHERE uc.type = :oauth AND jsonb_path_exists(uc.credential, '$.connections[*] ? (@.provider == "<provider>") ? (@.id == "<id>")');
            """)
    JoinRow getOAuthUser(@Define String provider, @Define String id, @EnumByOrdinal CredentialType oauth);
}
