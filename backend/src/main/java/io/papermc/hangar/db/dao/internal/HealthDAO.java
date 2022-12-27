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
    @SqlQuery("SELECT pv.version_string,\n" +
        "       pvd.file_name,\n" +
        "       p.owner_name \"owner\",\n" +
        "       p.slug,\n" +
        "       p.name,\n" +
        "       pq.platform\n" +
        "FROM project_versions pv\n" +
        "         JOIN projects p ON pv.project_id = p.id\n" +
        "         JOIN (SELECT DISTINCT plv.platform, pvpd.version_id\n" +
        "               FROM project_version_platform_dependencies pvpd\n" +
        "                        JOIN platform_versions plv ON pvpd.platform_version_id = plv.id) pq ON pv.id = pq.version_id\n" +
        "         JOIN project_version_downloads pvd ON pvd.version_id = pq.version_id\n" +
        "WHERE pvd.file_name IS NOT NULL\n" +
        "ORDER BY pv.created_at DESC")
    List<MissingFileCheck> getVersionsForMissingFiles();
}
