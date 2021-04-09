package io.papermc.hangar.db.mappers.factories;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.generic.GenericTypes;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.mapper.ColumnMappers;
import org.jdbi.v3.core.mapper.NoSuchMapperException;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.mapper.RowMapperFactory;
import org.jdbi.v3.core.mapper.RowMappers;
import org.jdbi.v3.core.statement.StatementContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Component
public class PairMapperFactory implements RowMapperFactory {

    @Override
    public Optional<RowMapper<?>> build(Type type, ConfigRegistry config) {
        if (!Pair.class.equals(GenericTypes.getErasedType(type))) {
            return Optional.empty();
        }

        Type leftType = GenericTypes.resolveType(Pair.class.getTypeParameters()[0], type);
        Type rightType = GenericTypes.resolveType(Pair.class.getTypeParameters()[1], type);

        RowMappers rowMappers = config.get(RowMappers.class);
        ColumnMappers columnMappers = config.get(ColumnMappers.class);

        RowMapper<?> pairMapper = (rs, ctx) -> {
            Object left = map(leftType, true, rowMappers, columnMappers, rs, ctx);
            Object right = map(rightType, false, rowMappers, columnMappers, rs, ctx);
            return new ImmutablePair<>(left, right);
        };
        return Optional.of(pairMapper);
    }

    private Object map(Type type, boolean left, RowMappers rowMappers, ColumnMappers columnMappers, ResultSet rs, StatementContext ctx) throws SQLException {
        Optional<RowMapper<?>> rowMapper = rowMappers.findFor(type);
        if (rowMapper.isPresent()) {
            return rowMapper.get().map(rs, ctx);
        } else {
            ColumnMapper<?> columnMapper = columnMappers.findFor(type).orElseThrow(() -> new NoSuchMapperException("No column mapper registered for Pair " + (left ? "left" : "right") +  " parameter " + type));
            return columnMapper.map(rs, 1, ctx);
        }
    }
}
