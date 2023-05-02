package io.papermc.hangar.db.dao.internal.table.auth;

import io.papermc.hangar.model.db.auth.ApiKeyTable;
import java.util.UUID;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(ApiKeyTable.class)
public interface ApiKeyDAO {

    @Timestamped
    @SqlUpdate("INSERT INTO api_keys (created_at, name, owner_id, token_identifier, token, raw_key_permissions, last_used) VALUES (:now, :name, :ownerId, :tokenIdentifier, :token, :permissions::bit(64), :now)")
    void insert(@BindBean ApiKeyTable apiKeyTable);

    @SqlUpdate("DELETE FROM api_keys WHERE name = :keyName AND owner_id = :userId")
    int delete(String keyName, long userId);

    @SqlQuery("SELECT *, raw_key_permissions::bigint permissions FROM api_keys WHERE owner_id = :userId AND lower(name) = lower(:name)")
    ApiKeyTable getByUserAndName(long userId, String name);

    @SqlQuery("SELECT *, raw_key_permissions::bigint permissions FROM api_keys WHERE token_identifier = :identifier AND token = :hashedToken")
    ApiKeyTable findApiKey(UUID identifier, String hashedToken);

    @SqlQuery("SELECT *, raw_key_permissions::bigint permissions FROM api_keys WHERE owner_id = :userId AND token_identifier = :identifier")
    ApiKeyTable findApiKey(long userId, UUID identifier);

    @SqlUpdate("UPDATE api_keys SET last_used = :lastUsed WHERE id = :id")
    void update(@BindBean ApiKeyTable apiKeyTable);
}
