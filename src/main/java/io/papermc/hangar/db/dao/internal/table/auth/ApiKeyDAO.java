package io.papermc.hangar.db.dao.internal.table.auth;

import io.papermc.hangar.db.mappers.PermissionMapper;
import io.papermc.hangar.model.db.auth.ApiKeyTable;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapper;
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
    @SqlUpdate("INSERT INTO api_keys (created_at, name, owner_id, token_identifier, token, raw_key_permissions) VALUES (:now, :name, :ownerId, :tokenIdentifier, crypt(:token, gen_salt('bf')), :permissions::bit(64))")
    void insert(@BindBean ApiKeyTable apiKeyTable);

    @SqlUpdate("DELETE FROM api_keys WHERE name = :keyName AND owner_id = :userId")
    int delete(String keyName, long userId);

    @RegisterColumnMapper(PermissionMapper.class)
    @SqlQuery("SELECT *, raw_key_permissions::bigint permissions FROM api_keys WHERE owner_id = :userId AND lower(name) = lower(:name)")
    ApiKeyTable getByUserAndName(long userId, String name);

//    @SqlQuery("SELECT *, raw_key_permissions::BIGINT permissions FROM api_keys k WHERE k.token_identifier = :identifier AND k.token = crypt(:token, k.token)")
//    ApiKeyTable findApiKey(String identifier, String token);
    // 1318e930-ef32-4034-88bd-967285a9d28b.f22f2f94-e3a5-496c-8ff7-5230ed16c8a6
}
