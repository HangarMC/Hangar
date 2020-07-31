package me.minidigger.hangar.db.dao;

import me.minidigger.hangar.db.model.ApiKeysTable;
import me.minidigger.hangar.model.Permission;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RegisterBeanMapper(ApiKeysTable.class)
public interface ApiKeyDao {

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO api_keys (created_at, name, owner_id, token_identifier, token, raw_key_permissions) VALUES (:now, :name, :ownerId, :tokenIdentifier, :token, :rawKeyPermissions)")
    ApiKeysTable insert(@BindBean ApiKeysTable apiKeysTable);

    @RegisterBeanMapper(value = Permission.class, prefix = "perm")
    @SqlQuery("SELECT *, raw_key_permissions::BIGINT perm_value FROM api_keys WHERE owner_id = :ownerId")
    List<ApiKeysTable> getByOwner(long ownerId);
}
