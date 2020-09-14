package io.papermc.hangar.db.dao;

import io.papermc.hangar.db.model.PlatformVersionsTable;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
@RegisterBeanMapper(PlatformVersionsTable.class)
public interface PlatformVersionsDao {

    @Timestamped
    @SqlBatch("INSERT INTO platform_versions (created_at, platform, version) VALUES (:now, :platform, :version)")
    void insert(@BindBean Collection<PlatformVersionsTable> platformVersionsTableCollection);

    @SqlBatch("DELETE FROM platform_versions WHERE version = :version AND platform = :platform")
    void delete(List<String> version, int platform);

    @SqlQuery("SELECT * FROM platform_versions")
    List<PlatformVersionsTable> getVersions();


}
