package io.papermc.hangar.db.dao.internal.table.auth;

import io.papermc.hangar.model.db.auth.ApiKeyTable;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(ApiKeyTable.class)
public interface ApiKeyDAO {

    @SqlQuery("SELECT *, raw_key_permissions::BIGINT permissions FROM api_keys k WHERE k.token_identifier = :identifier AND k.token = crypt(:token, k.token)")
    ApiKeyTable findApiKey(String identifier, String token);
}
