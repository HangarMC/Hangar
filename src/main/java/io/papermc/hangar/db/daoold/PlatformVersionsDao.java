package io.papermc.hangar.db.daoold;

import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Deprecated(forRemoval = true)
public interface PlatformVersionsDao {

    @SqlQuery("SELECT version FROM platform_versions WHERE platform = :platform ORDER BY string_to_array(version, '.')::INT[]")
    List<String> getVersionsForPlatform(int platform);
}
