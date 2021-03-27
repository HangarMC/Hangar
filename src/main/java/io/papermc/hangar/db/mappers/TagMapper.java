package io.papermc.hangar.db.mappers;

import io.papermc.hangar.model.api.color.TagColor;
import io.papermc.hangar.model.api.project.version.Tag;
import io.papermc.hangar.util.StringUtils;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class TagMapper implements RowMapper<Tag> {

    @Override
    public Tag map(ResultSet rs, StatementContext ctx) throws SQLException {
        String name = rs.getString("name");
        String[] data = null;
        if (rs.getArray("data") != null) {
            data = (String[]) rs.getArray("data").getArray();
        }
        TagColor tagColor = io.papermc.hangar.model.common.TagColor.getValues()[rs.getInt("color")].toTagColor();
        return new Tag(name, data != null ? StringUtils.formatVersionNumbers(Arrays.asList(data)) : null, tagColor);
    }
}
