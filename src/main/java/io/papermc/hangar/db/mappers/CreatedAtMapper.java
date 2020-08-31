package io.papermc.hangar.db.mappers;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class CreatedAtMapper implements RowMapper<LocalDate> {
    @Override
    public LocalDate map(ResultSet rs, StatementContext ctx) throws SQLException {
        return LocalDate.parse(rs.getString("date"));
    }
}
