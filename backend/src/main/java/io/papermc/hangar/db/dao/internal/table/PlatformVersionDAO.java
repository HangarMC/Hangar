package io.papermc.hangar.db.dao.internal.table;

import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.db.PlatformVersionTable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.enums.EnumStrategy;
import org.jdbi.v3.spring.JdbiRepository;
import org.jdbi.v3.sqlobject.config.KeyColumn;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.config.UseEnumStrategy;
import org.jdbi.v3.sqlobject.config.ValueColumn;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindBeanList;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@JdbiRepository
@RegisterConstructorMapper(PlatformVersionTable.class)
@UseEnumStrategy(EnumStrategy.BY_ORDINAL)
public interface PlatformVersionDAO {

    @SqlUpdate("INSERT INTO platform_versions (created_at, platform, version) VALUES <platformVersionTables> ON CONFLICT DO NOTHING")
    int insertAll(@BindBeanList(propertyNames = {"createdAt", "platform", "version"}) Collection<PlatformVersionTable> platformVersionTables);

    @SqlBatch("DELETE FROM platform_versions WHERE id = :id")
    void deleteAll(@BindBean Collection<PlatformVersionTable> platformVersionTables);

    @KeyColumn("platform")
    @ValueColumn("versions")
    @SqlQuery("SELECT platform, (array_agg(version ORDER BY string_to_array(version, '.')::int[])) versions FROM platform_versions GROUP BY platform")
    TreeMap<Platform, List<String>> getVersions();

    @SqlQuery("SELECT version FROM platform_versions WHERE platform = :platform ORDER BY string_to_array(version, '.')::int[]")
    List<String> getVersionsForPlatform(@EnumByOrdinal Platform platform);

    @KeyColumn("version")
    @SqlQuery("SELECT * FROM platform_versions WHERE platform = :platform")
    Map<String, PlatformVersionTable> getForPlatform(@EnumByOrdinal Platform platform);
}
