package me.minidigger.hangar.db.dao;

import me.minidigger.hangar.db.model.ApiKeysTable;
import me.minidigger.hangar.model.ApiAuthInfo;
import me.minidigger.hangar.model.viewhelpers.ApiKey;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RegisterBeanMapper(ApiKeysTable.class)
public interface ApiKeyDao {

    @Timestamped
    @SqlUpdate("INSERT INTO api_keys (created_at, name, owner_id, token_identifier, token, raw_key_permissions) VALUES (:now, :name, :ownerId, :tokenIdentifier, crypt(:token, gen_salt('bf')), :permValue::BIT(64))")
    void insert(@BindBean ApiKeysTable apiKeysTable, long permValue);

    @SqlUpdate("DELETE FROM api_keys k WHERE k.name = :keyName AND k.owner_id = :ownerId")
    int delete(String keyName, long ownerId);

    @RegisterBeanMapper(ApiKey.class)
    @SqlQuery("SELECT *, raw_key_permissions::BIGINT perm_value FROM api_keys WHERE owner_id = :ownerId")
    List<ApiKey> getByOwner(long ownerId);

    @SqlQuery("SELECT *, raw_key_permissions::BIGINT perm_value FROM api_keys WHERE name = :keyName AND owner_id = :ownerId")
    ApiKeysTable getKey(String keyName, long ownerId);

    @SqlQuery("SELECT *, raw_key_permissions::BIGINT perm_value FROM api_keys k WHERE k.token_identifier = :identifier AND k.token = crypt(:token, k.token)")
    ApiKeysTable findApiKey(String identifier, String token);

    @RegisterBeanMapper(ApiAuthInfo.class)
    @SqlQuery("SELECT u.id u_id," +
            "       u.created_at u_created_at," +
            "       u.full_name u_full_name," +
            "       u.name u_name," +
            "       u.email u_email," +
            "       u.tagline u_tagline," +
            "       u.join_date u_join_date," +
            "       u.read_prompts u_read_prompts," +
            "       u.is_locked u_is_locked," +
            "       u.language u_language," +
            "       ak.name ak_name," +
            "       ak.owner_id ak_owner_id," +
            "       ak.token ak_token," +
            "       ak.raw_key_permissions::BIGINT ak_value," +
            "       aks.token \"session\"," +
            "       aks.expires," +
            "       CASE" +
            "           WHEN u.id IS NULL THEN 1::BIT(64)::BIGINT" +
            "           ELSE ((coalesce(gt.permission, B'0'::BIT(64)) | 1::BIT(64) | (1::BIT(64) << 1) | (1::BIT(64) << 2)) &" +
            "                coalesce(ak.raw_key_permissions, (-1)::BIT(64)))::BIGINT" +
            "           END AS gp_value" +
            "    FROM api_sessions aks" +
            "             LEFT JOIN api_keys ak ON aks.key_id = ak.id" +
            "             LEFT JOIN users u ON aks.user_id = u.id" +
            "             LEFT JOIN global_trust gt ON gt.user_id = u.id" +
            "  WHERE aks.token = :token")
    ApiAuthInfo getApiAuthInfo(String token);
}
