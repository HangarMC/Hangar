package io.papermc.hangar.db.dao.internal;

import io.papermc.hangar.model.api.ApiKey;
import io.papermc.hangar.model.internal.api.responses.ScopableProject;
import java.util.List;
import org.jdbi.v3.spring.JdbiRepository;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

@JdbiRepository
@RegisterConstructorMapper(ApiKey.class)
public interface HangarApiKeysDAO {

    @SqlQuery("""
        SELECT k.created_at,
               k.name,
               k.token_identifier,
               k.raw_key_permissions::bigint permissions,
               k.last_used,
               k.expires_at,
               k.project_scoped,
               (SELECT coalesce(array_agg(p.owner_name || '/' || p.slug ORDER BY p.slug), '{}'::text[])
                    FROM api_key_projects akp JOIN projects p ON p.id = akp.project_id
                    WHERE akp.key_id = k.id) projects
        FROM api_keys k
        WHERE k.owner_id = :userId
        ORDER BY k.created_at DESC
        """)
    List<ApiKey> getUserApiKeys(long userId);

    @RegisterConstructorMapper(ScopableProject.class)
    @SqlQuery("""
        SELECT p.name,
               p.owner_name AS owner,
               p.slug,
               (SELECT '/project/' || p.id || '.webp?v=' || a.version
                    FROM avatars a
                    WHERE a.type = 'project' AND a.subject = p.id::text) AS avatar,
               (SELECT '/user/' || o.uuid::text || '.webp?v=' || a.version
                    FROM avatars a JOIN users o ON a.subject = o.uuid::text
                    WHERE o.id = p.owner_id AND a.type = 'user') AS avatar_fallback
        FROM projects p
        WHERE p.visibility != 4
          AND EXISTS (SELECT 1 FROM project_members_all pma WHERE pma.id = p.id AND pma.user_id = :userId)
        ORDER BY lower(p.name)
        """)
    List<ScopableProject> getScopableProjects(long userId);
}
