package io.papermc.hangar.db.dao.internal.versions;

import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.db.versions.JarScanResultTable;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
public interface JarScanResultDAO {

    @Timestamped
    @SqlUpdate("INSERT INTO jar_scan_result(version_id, platform, data, highest_severity, created_at) VALUES (:versionId, :platform, :data, :highestSeverity, :now)")
    void save(@BindBean JarScanResultTable table);

    @RegisterConstructorMapper(JarScanResultTable.class)
    @SqlQuery("SELECT * FROM jar_scan_result WHERE version_id = :versionId AND platform = :platform ORDER BY created_at DESC LIMIT 1")
    JarScanResultTable getLastResult(long versionId, @EnumByOrdinal final Platform platform);
}
