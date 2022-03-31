package io.papermc.hangar.db.dao.api.mappers;

import io.papermc.hangar.model.Color;
import io.papermc.hangar.model.Visibility;
import io.papermc.hangar.model.generated.FileInfo;
import io.papermc.hangar.model.generated.ReviewState;
import io.papermc.hangar.model.generated.Tag;
import io.papermc.hangar.model.generated.TagColor;
import io.papermc.hangar.model.generated.Version;
import io.papermc.hangar.model.generated.VersionStatsAll;
import io.papermc.hangar.model.viewhelpers.VersionDependencies;
import io.papermc.hangar.util.StringUtils;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class VersionMapper implements RowMapper<Version> {
    @Override
    public Version map(ResultSet rs, StatementContext ctx) throws SQLException {
        Optional<ColumnMapper<String[]>> mapper = ctx.findColumnMapperFor(String[].class);
        Optional<ColumnMapper<VersionDependencies>> versionDependenciesColumnMapper = ctx.findColumnMapperFor(VersionDependencies.class);
        if (mapper.isEmpty() || versionDependenciesColumnMapper.isEmpty()) throw new UnsupportedOperationException("couldn't find required mappers");

        String[] tagNames = (String[]) rs.getArray("tag_name").getArray();
        String[] tagData = (String[]) rs.getArray("tag_data").getArray();
        Integer[] tagColors = (Integer[]) rs.getArray("tag_color").getArray();
        if (tagNames.length != tagColors.length || tagData.length != tagColors.length) {
            throw new IllegalArgumentException("All 3 tag arrays must be the same length");
        }

        List<Tag> tags = new ArrayList<>();
        for (int i = 0; i < tagNames.length; i++) {
            io.papermc.hangar.model.TagColor tagColor = io.papermc.hangar.model.TagColor.getByName(tagNames[i]);
            Tag newTag = new Tag().name(tagNames[i]);
            if (tagData[i] != null) {
                newTag.data(StringUtils.formatVersionNumbers(Arrays.asList(tagData[i].split(", "))));
            }
            if (tagColor != null) {
                tags.add(newTag.color(new TagColor().foreground(tagColor.getForeground()).background(tagColor.getBackground())));
            } else {
                Color color = Color.getValues()[tagColors[i]];
                tags.add(newTag.color(new TagColor().background(color.getHex())));
            }
        }

        return new Version()
                .createdAt(rs.getObject("created_at", OffsetDateTime.class))
                .name(rs.getString("version_string"))
                .urlName(rs.getString("version_string") + "." + rs.getLong("id"))
                .dependencies(versionDependenciesColumnMapper.get().map(rs, rs.findColumn("dependencies"), ctx))
                .visibility(Visibility.fromId(rs.getLong("visibility")))
                .description(rs.getString("description"))
                .stats(new VersionStatsAll().downloads(rs.getLong("downloads")))
                .fileInfo(new FileInfo().name(rs.getString("fi_name")).md5Hash(rs.getString("fi_md5_hash")).sizeBytes(rs.getLong("fi_size_bytes")))
                .author(rs.getString("author"))
                .reviewState(ReviewState.values()[rs.getInt("review_state")])
                .tags(tags);
    }
}
