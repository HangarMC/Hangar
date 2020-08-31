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

}
