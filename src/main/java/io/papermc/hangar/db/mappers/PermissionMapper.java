package io.papermc.hangar.db.mappers;

import io.papermc.hangar.model.Permission;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * {@link Permission} should have it's own mapper just since its essentially a wrapper for a long.
 */
public class PermissionMapper implements ColumnMapper<Permission> {

    @Override
    public Permission map(ResultSet r, int columnNumber, StatementContext ctx) throws SQLException {
        return Permission.fromLong(r.getLong(columnNumber));
    }
}
