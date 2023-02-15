package io.papermc.hangar.db.dao.internal;

import io.papermc.hangar.model.internal.admin.health.MissingFileCheck;
import io.papermc.hangar.model.internal.admin.health.UnhealthyProject;
import java.util.List;
import org.jdbi.v3.core.enums.EnumStrategy;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.config.UseEnumStrategy;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(UnhealthyProject.class)
public interface HealthDAO {

    @SqlQuery(" SELECT p.owner_name \"owner\"," +
        "          p.slug," +
        "          p.topic_id," +
        "          p.post_id," +
        "          coalesce(hp.last_updated, p.created_at) AS last_updated," +
        "          p.visibility" +
        "   FROM projects p" +
        "       JOIN home_projects hp ON p.id = hp.id" +
        "   WHERE p.topic_id IS NULL OR " +
        "         p.post_id IS NULL" +
        "   ORDER BY p.created_at DESC")
    List<UnhealthyProject> getProjectsWithoutTopic();

    @SqlQuery(" SELECT p.owner_name \"owner\"," +
        "          p.slug," +
        "          p.topic_id," +
        "          p.post_id," +
        "          coalesce(hp.last_updated, p.created_at) AS last_updated," +
        "          p.visibility" +
        "   FROM projects p " +
        "       JOIN home_projects hp ON p.id = hp.id" +
        "   WHERE hp.last_updated < (now() - interval <age>)" +
        "   ORDER BY p.created_at DESC")
    List<UnhealthyProject> getStaleProjects(@Define("age") String staleAgeSeconds);

    @SqlQuery(" SELECT p.owner_name \"owner\"," +
        "          p.slug," +
        "          p.topic_id," +
        "          p.post_id," +
        "          coalesce(hp.last_updated, p.created_at) AS last_updated," +
        "          p.visibility" +
        "   FROM projects p " +
        "       JOIN home_projects hp ON p.id = hp.id" +
        "   WHERE p.visibility != 0" +
        "   ORDER BY p.created_at DESC")
    List<UnhealthyProject> getNonPublicProjects();

    @UseEnumStrategy(EnumStrategy.BY_ORDINAL)
    @RegisterConstructorMapper(MissingFileCheck.class)
    @SqlQuery("""
        SELECT pv.version_string,
               pvd.file_name,
               p.owner_name "owner",
               p.slug,
               p.name,
               pq.platform
        FROM project_versions pv
                 JOIN projects p ON pv.project_id = p.id
                 JOIN (SELECT DISTINCT plv.platform, pvpd.version_id
                       FROM project_version_platform_dependencies pvpd
                                JOIN platform_versions plv ON pvpd.platform_version_id = plv.id) pq ON pv.id = pq.version_id
                 JOIN project_version_downloads pvd ON pvd.version_id = pq.version_id
        WHERE pvd.file_name IS NOT NULL
        ORDER BY pv.created_at DESC""")
    List<MissingFileCheck> getVersionsForMissingFiles();
}
