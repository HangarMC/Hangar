package me.minidigger.hangar.db.dao.api.mappers;

import me.minidigger.hangar.model.Visibility;
import me.minidigger.hangar.model.generated.Dependency;
import me.minidigger.hangar.model.generated.FileInfo;
import me.minidigger.hangar.model.generated.ReviewState;
import me.minidigger.hangar.model.generated.Version;
import me.minidigger.hangar.model.generated.VersionStatsAll;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Optional;

public class VersionMapper implements RowMapper<Version> {
    @Override
    public Version map(ResultSet rs, StatementContext ctx) throws SQLException {
        Optional<ColumnMapper<String[]>> mapper = ctx.findColumnMapperFor(String[].class);
        if (mapper.isEmpty()) throw new UnsupportedOperationException("couldn't find a mapper for String[]");
        return new Version()
                .createdAt(rs.getObject("created_at", OffsetDateTime.class))
                .name(rs.getString("version_string"))
                .dependencies(Dependency.from(Arrays.asList(mapper.get().map(rs, "dependencies", ctx))))
                .visibility(Visibility.fromId(rs.getLong("visibility")))
                .description(rs.getString("description"))
                .stats(new VersionStatsAll().downloads(rs.getLong("downloads")))
                .fileInfo(new FileInfo().name(rs.getString("fi_name")).md5Hash(rs.getString("fi_md5_hash")).sizeBytes(rs.getLong("fi_size_bytes")))
                .author(rs.getString("author"))
                .reviewState(ReviewState.values()[rs.getInt("review_state")]);

    }
}
