package io.papermc.hangar.db.mappers;

import io.papermc.hangar.model.internal.logs.LogAction;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLDataException;
import java.sql.SQLException;

public class LogActionColumnMapper implements ColumnMapper<LogAction<?>> {

    @Override
    public LogAction<?> map(ResultSet r, int columnNumber, StatementContext ctx) throws SQLException {
        String action = r.getString(columnNumber);
        if (!LogAction.LOG_REGISTRY.containsKey(action)) {
            throw new SQLDataException(action + " is not a valid LogAction");
        }
        return LogAction.LOG_REGISTRY.get(action);
    }
}
