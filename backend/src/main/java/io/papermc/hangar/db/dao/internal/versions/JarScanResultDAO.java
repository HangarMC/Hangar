package io.papermc.hangar.db.dao.internal.versions;

import io.papermc.hangar.model.api.project.version.VersionToScan;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.db.versions.JarScanResultEntryTable;
import io.papermc.hangar.model.db.versions.JarScanResultTable;
import java.util.List;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.spring.JdbiRepository;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@JdbiRepository
@RegisterConstructorMapper(JarScanResultTable.class)
@RegisterConstructorMapper(JarScanResultEntryTable.class)
public interface JarScanResultDAO {

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO jar_scan_result(version_id, scanner_version, platform, highest_severity, created_at) VALUES (:versionId, :scannerVersion, :platform, :highestSeverity, :now)")
    JarScanResultTable save(@BindBean JarScanResultTable table);

    @SqlUpdate("INSERT INTO jar_scan_result_entry(result_id, location, message, severity, check_name, hash, checked, checked_at, checked_by) VALUES (:resultId, :location, :message, :severity, :checkName, :hash, :checked, :checkedAt, :checkedBy)")
    void save(@BindBean JarScanResultEntryTable table);

    @SqlUpdate("UPDATE jar_scan_result_entry SET location = :location, message = :message, severity = :severity, check_name = :checkName, hash = :hash, checked = :checked, checked_at = :checkedAt, checked_by = :checkedBy WHERE id = :id")
    void update(@BindBean JarScanResultEntryTable entry);

    @SqlQuery("SELECT * FROM jar_scan_result WHERE version_id = :versionId AND platform = :platform ORDER BY created_at DESC LIMIT 1")
    JarScanResultTable getLastResult(long versionId, @EnumByOrdinal final Platform platform);

    @SqlQuery("""
        SELECT DISTINCT ON (platform) *
        FROM jar_scan_result
        WHERE version_id = :versionId
        ORDER BY platform, created_at DESC;
        """)
    List<JarScanResultTable> getLastResults(long versionId);

    @SqlQuery("SELECT * FROM jar_scan_result_entry WHERE result_id = :resultId")
    List<JarScanResultEntryTable> getEntries(long resultId);

    @SqlQuery("SELECT * FROM jar_scan_result_entry WHERE hash = :hash and checked = TRUE")
    JarScanResultEntryTable getCheckedByHash(String hash);

    @SqlQuery("SELECT * FROM jar_scan_result_entry WHERE id = :entryId")
    JarScanResultEntryTable getEntry(long entryId);

    @SqlUpdate("UPDATE jar_scan_result_entry SET checked = TRUE, checked_at = now(), checked_by = :userId WHERE result_id = :resultId")
    void markAllSafe(long resultId, long userId);

    // TODO Don't skip approved versions if HIGHEST severity should unlist it
    // WHERE jsr.version_id = pv.id AND jsr.scanner_version >= :scannerVersion
    @RegisterConstructorMapper(VersionToScan.class)
    @SqlQuery("""
        SELECT pv.id AS version_id, pv.project_id, pv.review_state, pv.version_string, array_agg(DISTINCT pvd.platforms) AS platforms
        FROM project_versions pv
            JOIN project_version_downloads pvd ON pv.id = pvd.version_id
        WHERE pv.review_state != 1 AND pv.visibility = 0 AND pvd.file_name IS NOT NULL
          AND NOT exists (
            SELECT 1
            FROM jar_scan_result jsr
            WHERE jsr.version_id = pv.id
        )
        GROUP BY pv.id;
        """)
    List<VersionToScan> versionsRequiringScans();
}
