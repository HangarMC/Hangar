package io.papermc.hangar.db.dao.internal.versions;

import io.papermc.hangar.model.api.project.version.VersionToScan;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.db.versions.JarScanResultEntryTable;
import io.papermc.hangar.model.db.versions.JarScanResultTable;
import java.util.List;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(JarScanResultTable.class)
@RegisterConstructorMapper(JarScanResultEntryTable.class)
public interface JarScanResultDAO {

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO jar_scan_result(version_id, scanner_version, platform, highest_severity, created_at) VALUES (:versionId, :scannerVersion, :platform, :highestSeverity, :now)")
    JarScanResultTable save(@BindBean JarScanResultTable table);

    @SqlUpdate("INSERT INTO jar_scan_result_entry(result_id, location, message, severity) VALUES (:resultId, :location, :message, :severity)")
    void save(@BindBean JarScanResultEntryTable table);

    @SqlQuery("SELECT * FROM jar_scan_result WHERE version_id = :versionId AND platform = :platform ORDER BY created_at DESC LIMIT 1")
    JarScanResultTable getLastResult(long versionId, @EnumByOrdinal final Platform platform);

    @SqlQuery("SELECT * FROM jar_scan_result_entry WHERE result_id = :resultId")
    List<JarScanResultEntryTable> getEntries(long resultId);

    //TODO Don't skip approved versions if HIGHEST severity should unlist it
    // WHERE jsr.version_id = pv.id AND jsr.scanner_version >= :scannerVersion
    @RegisterConstructorMapper(VersionToScan.class)
    @SqlQuery("""
        SELECT pv.id AS version_id, pv.project_id, pv.review_state, pv.version_string, array_agg(DISTINCT pvpd.platform) AS platforms
        FROM project_versions pv
        LEFT JOIN (
            SELECT DISTINCT ON (pvpd.download_id) pvpd.id, pvpd.version_id, pvpd.platform
            FROM project_version_platform_downloads pvpd
            LEFT JOIN project_version_downloads pvd ON pvd.id = pvpd.download_id AND pvd.file_name IS NOT NULL
            WHERE pvd.id IS NOT NULL
            ORDER BY pvpd.download_id
        ) pvpd ON pvpd.version_id = pv.id
        WHERE pv.review_state != 1 AND pv.visibility = 0 AND pvpd.id IS NOT NULL
        AND NOT EXISTS (
            SELECT 1
            FROM jar_scan_result jsr
            WHERE jsr.version_id = pv.id
        )
        GROUP BY pv.id;
                        """)
    List<VersionToScan> versionsRequiringScans();
}
