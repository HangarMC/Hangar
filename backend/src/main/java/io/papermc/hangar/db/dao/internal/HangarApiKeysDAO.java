package io.papermc.hangar.db.dao.internal;

import io.papermc.hangar.model.api.ApiKey;
import java.util.List;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(ApiKey.class)
public interface HangarApiKeysDAO {

    @SqlQuery("SELECT created_at, name, token_identifier, raw_key_permissions::bigint permissions, last_used FROM api_keys WHERE owner_id = :userId ORDER BY created_at DESC")
    List<ApiKey> getUserApiKeys(long userId);
}
