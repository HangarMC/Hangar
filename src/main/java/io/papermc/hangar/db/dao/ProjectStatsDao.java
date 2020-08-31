package io.papermc.hangar.db.dao;

import io.papermc.hangar.db.mappers.CreatedAtMapper;
import io.papermc.hangar.db.mappers.ReviewIdMapper;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Map;

@Repository
public interface ProjectStatsDao {

    @SqlQuery("select date(created_at), count(id) as count " +
            "from project_version_reviews " +
            "where date(created_at)" +
            "between :from and :to " +
            "group by date(created_at); ")
    @RegisterRowMapper(CreatedAtMapper.class)
    @RegisterRowMapper(ReviewIdMapper.class)
    Map<LocalDate, Integer> getNumberOfReviewPerDay(LocalDate from, LocalDate to);

    @SqlQuery("select date(created_at), count(id) as count " +
            "from logged_actions_project " +
            "where (date(created_at)" +
            "between :from and :to) and " +
            "action = 'version_uploaded'::logged_action_type " +
            "group by date(created_at); ")
    @RegisterRowMapper(CreatedAtMapper.class)
    @RegisterRowMapper(ReviewIdMapper.class)
    Map<LocalDate, Integer> getNumberOfUploadsPerDay(LocalDate from, LocalDate to);

    @SqlQuery("select day as date, sum(downloads) as count " +
            "from project_versions_downloads " +
            "where day between :from and :to " +
            "group by day; ")
    @RegisterRowMapper(CreatedAtMapper.class)
    @RegisterRowMapper(ReviewIdMapper.class)
    Map<LocalDate, Integer> getNumberOfSafeDownloadsPerDay(LocalDate from, LocalDate to);

    @SqlQuery("select date(created_at), count(id) as count " +
            "from project_version_unsafe_downloads " +
            "where date(created_at) between :from and :to " +
            "group by date(created_at); ")
    @RegisterRowMapper(CreatedAtMapper.class)
    @RegisterRowMapper(ReviewIdMapper.class)
    Map<LocalDate, Integer> getNumberOfUnsafeDownloadsPerDay(LocalDate from, LocalDate to);
}
