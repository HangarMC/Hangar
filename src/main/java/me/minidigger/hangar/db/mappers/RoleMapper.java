package me.minidigger.hangar.db.mappers;

import me.minidigger.hangar.model.Role;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RoleMapper implements RowMapper<Role> {
    @Override
    public Role map(ResultSet rs, StatementContext ctx) throws SQLException {
        return Role.valueOf(rs.getString("role"));
    }
}
