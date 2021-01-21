package io.papermc.hangar.db.mappers;

import io.papermc.hangar.modelold.Role;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class RoleMapper implements RowMapper<Role> {
    @Override
    public Role map(ResultSet rs, StatementContext ctx) throws SQLException {
        return Role.valueOf(rs.getString("role"));
    }
}
