package io.papermc.hangar.db.mappers;

import io.papermc.hangar.model.common.Permission;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.springframework.stereotype.Component;

/**
 * {@link Permission} should have its own mapper just since it's essentially a wrapper for a long.
 */
@Component
public class PermissionMapper implements ColumnMapper<Permission> {

    @Override
    public Permission map(final ResultSet r, final int columnNumber, final StatementContext ctx) throws SQLException {
        return Permission.fromLong(r.getLong(columnNumber));
    }
}
