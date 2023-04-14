package io.papermc.hangar.db.mappers;

import io.papermc.hangar.model.common.Platform;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.postgresql.util.PGobject;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Map;

public class VersionStatsMapper implements ColumnMapper<Map<Platform, Long>> {
    @Override
    public Map<Platform, Long> map(final ResultSet r, final int columnNumber, final StatementContext ctx) throws SQLException {
        final Map<Platform, Long> result = new EnumMap<>(Platform.class);
        final Array arr = r.getArray(columnNumber);
        if (arr == null) {
            return result;
        }

        final Object[] array = (Object[]) arr.getArray();
        for (final Object entry : array) {
            if (entry instanceof final PGobject pgObject) {
                if (pgObject.getValue() == null) {
                    continue;
                }

                final String val = pgObject.getValue().substring(1, pgObject.getValue().length() - 1);
                final String[] split = val.split(",");
                final int platformIndex = Integer.parseInt(split[0]);
                final long downloads = Long.parseLong(split[1]);
                result.put(Platform.getValues()[platformIndex], downloads);
            } else if (entry instanceof final Long[] longArr){
                if (longArr.length < 2 || longArr[0] == null) {
                    continue;
                }

                result.put(Platform.getValues()[longArr[0].intValue()], longArr[1]);
            }
        }
        return result;
    }
}
