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
        "          coalesce(hp.last_updated, p.created_at) AS last_updated," +
        "          p.visibility" +
        "   FROM projects p " +
        "       JOIN home_projects hp ON p.id = hp.id" +
        "   WHERE hp.last_updated < (now() - interval <age>)" +
        "   ORDER BY p.created_at DESC")
    List<UnhealthyProject> getStaleProjects(@Define("age") String staleAgeSeconds);

    @SqlQuery(" SELECT p.owner_name \"owner\"," +
        "          p.slug," +
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
               p.owner_name "owner",
               p.slug,
               array_agg(pvpd.file_name) as fileNames,
               array_agg(DISTINCT pvpd.platform) AS platforms
        FROM project_versions pv
        JOIN projects p ON p.id = pv.project_id
        LEFT JOIN (
            SELECT DISTINCT ON (pvpd.download_id) pvpd.id, pvpd.version_id, pvpd.platform, pvd.file_name
            FROM project_version_platform_downloads pvpd
            LEFT JOIN project_version_downloads pvd ON pvd.id = pvpd.download_id AND pvd.file_name IS NOT NULL
            WHERE pvd.id IS NOT NULL
            ORDER BY pvpd.download_id
        ) pvpd ON pvpd.version_id = pv.id
        WHERE pvpd.id IS NOT NULL
        GROUP BY p.id, pv.id""")
    List<MissingFileCheck> getVersionsForMissingFiles();
}
