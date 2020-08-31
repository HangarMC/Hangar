package io.papermc.hangar.db.mappers;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;


public class ReviewIdMapper implements RowMapper<Integer> {
    @Override
    public Integer map(ResultSet rs, StatementContext ctx) throws SQLException {
        return rs.getInt("count");
    }
}
