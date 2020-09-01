package io.papermc.hangar.db.mappers;

import io.papermc.hangar.db.model.Stats;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class StatsMapper implements RowMapper<Stats> {
    @Override
    public Stats map(ResultSet rs, StatementContext ctx) throws SQLException {
        Stats stats = new Stats();
        stats.setReview(rs.getLong("review_count"));
        stats.setUploads(rs.getLong("created_projects"));
        stats.setTotalDownloads(rs.getLong("download_count"));
        stats.setUnsafeDownloads(rs.getLong("unsafe_download_count"));
        stats.setFlagsOpened(rs.getLong("flags_created"));
        stats.setFlagsClosed(rs.getLong("flags_resolved"));
        stats.setDay(LocalDate.parse(rs.getString("day")));
        return stats;
    }
}
