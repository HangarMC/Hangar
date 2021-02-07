package io.papermc.hangar.db.dao.internal.table;

import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.db.PlatformVersionTable;
import org.jdbi.v3.core.enums.EnumStrategy;
import org.jdbi.v3.sqlobject.config.KeyColumn;
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
public interface PlatformVersionDAO {

    @Timestamped
    @SqlBatch("INSERT INTO platform_versions (created_at, platform, version) VALUES (:now, :platform, :version)")
    void insert(@BindBean Collection<PlatformVersionTable> platformVersionTables);

    @KeyColumn("platform")
    @ValueColumn("versions")
    @UseEnumStrategy(EnumStrategy.BY_ORDINAL)
    @SqlQuery("SELECT platform, (array_agg(version ORDER BY string_to_array(version, '.')::INT[])) versions FROM platform_versions GROUP BY platform")
    TreeMap<Platform, List<String>> getVersions();
}
