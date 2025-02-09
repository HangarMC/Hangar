package io.papermc.hangar.db.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.papermc.hangar.model.common.Platform;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.postgresql.util.PGobject;
import org.springframework.stereotype.Component;

@Component
public class VersionStatsMapper implements ColumnMapper<Map<Platform, Long>> {

    private final ObjectMapper objectMapper;

    public VersionStatsMapper(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Map<Platform, Long> map(final ResultSet r, final int columnNumber, final StatementContext ctx) throws SQLException {
        final Map<Platform, Long> result = new EnumMap<>(Platform.class);

        int type = r.getMetaData().getColumnType(columnNumber);
        if (type == Types.ARRAY) {
            final Array array = r.getArray(columnNumber);
            if (array == null) {
                return result;
            }

            for (final Long[] el : ((Long[][]) array.getArray())) {
                if (el.length < 2 || el[0] == null) {
                    continue;
                }

                result.put(Platform.getValues()[el[0].intValue()], el[1]);
            }

            return result;
        } else if (type == Types.OTHER) {
            final Object object = r.getObject(columnNumber);
            if (object instanceof final PGobject pgObject) {
                if (pgObject.getType().equals("json")) {
                    try {
                        @SuppressWarnings("unchecked") List<Map<String, Integer>> list = this.objectMapper.readValue(pgObject.getValue(), List.class);
                        for (var o : list) {
                            result.put(Platform.getValues()[o.get("platform")], Long.valueOf(o.get("downloads")));
                        }
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    return result;
                }
            }
        }

        return result;
    }
}
