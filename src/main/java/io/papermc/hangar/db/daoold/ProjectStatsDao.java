package io.papermc.hangar.db.daoold;

import io.papermc.hangar.db.modelold.Stats;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProjectStatsDao {

    @SqlQuery("SELECT (SELECT COUNT(*) FROM project_version_reviews WHERE CAST(ended_at AS DATE) = day) AS reviews, " +
            "(SELECT COUNT(*) FROM project_versions WHERE CAST(created_at AS DATE) = day) AS uploads, " +
            "(SELECT COUNT(*) FROM project_versions_downloads_individual WHERE CAST(created_at AS DATE) = day) AS totalDownloads, " +
            "(SELECT COUNT(*) FROM project_version_unsafe_downloads " +
            "   WHERE CAST(created_at AS DATE) = day) AS unsafeDownloads, " +
            "(SELECT COUNT(*) FROM project_flags " +
            "   WHERE CAST(created_at AS DATE) <= day " +
            "   AND (CAST(resolved_at AS DATE) >= day OR resolved_at IS NULL)) AS flagsOpened, " +
            "(SELECT COUNT(*) FROM project_flags WHERE CAST(resolved_at AS DATE) = day) AS flagsClosed, " +
            "CAST(day AS DATE) " +
            "FROM (SELECT generate_series(:startDate, :endDate, INTERVAL '1 DAY') AS day) dates " +
            "ORDER BY day ASC; ")
    @RegisterBeanMapper(Stats.class)
    List<Stats> getStats(LocalDate startDate, LocalDate endDate);

}
