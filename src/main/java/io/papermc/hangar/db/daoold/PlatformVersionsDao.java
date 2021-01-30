package io.papermc.hangar.db.daoold;

import io.papermc.hangar.db.modelold.PlatformVersionsTable;
import io.papermc.hangar.model.Platform;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.enums.EnumStrategy;
import org.jdbi.v3.sqlobject.config.KeyColumn;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.config.UseEnumStrategy;
import org.jdbi.v3.sqlobject.config.ValueColumn;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

@Repository
@RegisterBeanMapper(PlatformVersionsTable.class)
public interface PlatformVersionsDao {

    @Timestamped
    @SqlBatch("INSERT INTO platform_versions (created_at, platform, version) VALUES (:now, :platform, :version)")
    void insert(@BindBean Collection<PlatformVersionsTable> platformVersionsTableCollection);

    @SqlBatch("DELETE FROM platform_versions WHERE version = :version AND platform = :platform")
    void delete(List<String> version, int platform);

    @KeyColumn("platform")
    @ValueColumn("versions")
    @UseEnumStrategy(EnumStrategy.BY_ORDINAL)
    @SqlQuery("SELECT platform, (array_agg(version ORDER BY string_to_array(version, '.')::INT[])) versions FROM platform_versions GROUP BY platform")
    TreeMap<Platform, List<String>> getVersions();

    @SqlQuery("SELECT version FROM platform_versions WHERE platform = :platform ORDER BY string_to_array(version, '.')::INT[]")
    List<String> getVersionsForPlatform(int platform);

    @SqlQuery("SELECT * FROM platform_versions WHERE platform = :platform")
    List<PlatformVersionsTable> getVersionsForPlatform(@EnumByOrdinal io.papermc.hangar.modelold.Platform platform);

    @SqlQuery("SELECT pv.* FROM project_version_platform_dependencies pvpd JOIN platform_versions pv ON pv.id = pvpd.platform_version_id WHERE pvpd.version_id = :versionId")
    List<PlatformVersionsTable> getPlatformVersionsForVersion(long versionId);

}
