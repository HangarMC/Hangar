package io.papermc.hangar.db.mappers;

import io.papermc.hangar.model.internal.logs.LogAction;
import java.sql.ResultSet;
import java.sql.SQLDataException;
import java.sql.SQLException;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class LogActionColumnMapper implements ColumnMapper<LogAction<?>> {

    @Override
    public LogAction<?> map(final ResultSet r, final int columnNumber, final StatementContext ctx) throws SQLException {
        final String action = r.getString(columnNumber);
        if (!LogAction.LOG_REGISTRY.containsKey(action)) {
            throw new SQLDataException(action + " is not a valid LogAction");
        }
        return LogAction.LOG_REGISTRY.get(action);
    }
}
