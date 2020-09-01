package io.papermc.hangar.db.dao;

import io.papermc.hangar.db.mappers.StatsMapper;
import io.papermc.hangar.db.model.Stats;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProjectStatsDao {

    @SqlQuery("SELECT (SELECT COUNT(*) FROM project_version_reviews WHERE CAST(ended_at AS DATE) = day) AS review_count, " +
            "(SELECT COUNT(*) FROM project_versions WHERE CAST(created_at AS DATE) = day) AS created_projects, " +
            "(SELECT COUNT(*) FROM project_versions_downloads_individual WHERE CAST(created_at AS DATE) = day) AS download_count, " +
            "(SELECT COUNT(*) FROM project_version_unsafe_downloads " +
            "   WHERE CAST(created_at AS DATE) = day) AS unsafe_download_count, " +
            "(SELECT COUNT(*) FROM project_flags " +
            "   WHERE CAST(created_at AS DATE) <= day " +
            "   AND (CAST(resolved_at AS DATE) >= day OR resolved_at IS NULL)) AS flags_created, " +
            "(SELECT COUNT(*) FROM project_flags WHERE CAST(resolved_at AS DATE) = day) AS flags_resolved, " +
            "CAST(day AS DATE) " +
            "FROM (SELECT generate_series(:startDate, :endDate, INTERVAL '1 DAY') AS day) dates " +
            "ORDER BY day ASC; ")
    @RegisterRowMapper(StatsMapper.class)
    List<Stats> getStats(LocalDate startDate, LocalDate endDate);


}
