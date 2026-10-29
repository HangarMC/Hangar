package io.papermc.hangar.db.dao.internal.table.auth;

import io.papermc.hangar.model.db.auth.ApiKeyTable;
import java.util.Collection;
import java.util.UUID;
import org.jdbi.v3.spring.JdbiRepository;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@JdbiRepository
@RegisterConstructorMapper(ApiKeyTable.class)
public interface ApiKeyDAO {

    String SCOPE_COLUMNS = """
        (SELECT coalesce(array_agg(p.id), '{}'::bigint[])
             FROM api_key_projects akp JOIN projects p ON p.id = akp.project_id
             WHERE akp.key_id = k.id) scoped_project_ids,
        (SELECT coalesce(array_agg(p.slug::text), '{}'::text[])
             FROM api_key_projects akp JOIN projects p ON p.id = akp.project_id
             WHERE akp.key_id = k.id) scoped_project_slugs
        """;

    @Timestamped
    @GetGeneratedKeys("id")
    @SqlUpdate("INSERT INTO api_keys (created_at, name, owner_id, token_identifier, token, raw_key_permissions, last_used, expires_at, project_scoped) " +
        "VALUES (:now, :name, :ownerId, :tokenIdentifier, :token, :permissions::bit(64), :now, :expiresAt, :projectScoped)")
    long insert(@BindBean ApiKeyTable apiKeyTable);

    @SqlBatch("INSERT INTO api_key_projects (key_id, project_id) VALUES (:keyId, :projectId)")
    void insertScopedProjects(long keyId, @Bind("projectId") Collection<Long> projectIds);

    @SqlUpdate("DELETE FROM api_keys WHERE name = :keyName AND owner_id = :userId")
    int delete(String keyName, long userId);

    @SqlQuery("SELECT k.*, k.raw_key_permissions::bigint permissions FROM api_keys k WHERE k.owner_id = :userId AND lower(k.name) = lower(:name)")
    ApiKeyTable getByUserAndName(long userId, String name);

    @SqlQuery("SELECT k.*, k.raw_key_permissions::bigint permissions, " + SCOPE_COLUMNS + " FROM api_keys k WHERE k.token_identifier = :identifier AND k.token = :hashedToken")
    ApiKeyTable findApiKey(UUID identifier, String hashedToken);

    @SqlQuery("SELECT k.*, k.raw_key_permissions::bigint permissions, " + SCOPE_COLUMNS + " FROM api_keys k WHERE k.owner_id = :userId AND k.token_identifier = :identifier")
    ApiKeyTable findApiKey(long userId, UUID identifier);

    @SqlUpdate("UPDATE api_keys SET last_used = :lastUsed WHERE id = :id")
    void update(@BindBean ApiKeyTable apiKeyTable);
}
