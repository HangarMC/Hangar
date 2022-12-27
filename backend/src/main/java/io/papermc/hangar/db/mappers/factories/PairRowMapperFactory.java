package io.papermc.hangar.db.mappers.factories;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
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

@Component
public class PairRowMapperFactory implements RowMapperFactory {

    @Override
    public Optional<RowMapper<?>> build(final Type type, final ConfigRegistry config) {
        if (!Pair.class.equals(GenericTypes.getErasedType(type))) {
            return Optional.empty();
        }

        final Type leftType = GenericTypes.resolveType(Pair.class.getTypeParameters()[0], type);
        final Type rightType = GenericTypes.resolveType(Pair.class.getTypeParameters()[1], type);

        final RowMappers rowMappers = config.get(RowMappers.class);
        final ColumnMappers columnMappers = config.get(ColumnMappers.class);

        final RowMapper<?> pairMapper = (rs, ctx) -> {
            final Object left = this.map(leftType, true, rowMappers, columnMappers, rs, ctx);
            final Object right = this.map(rightType, false, rowMappers, columnMappers, rs, ctx);
            return new ImmutablePair<>(left, right);
        };
        return Optional.of(pairMapper);
    }

    private Object map(final Type type, final boolean left, final RowMappers rowMappers, final ColumnMappers columnMappers, final ResultSet rs, final StatementContext ctx) throws SQLException {
        final Optional<RowMapper<?>> rowMapper = rowMappers.findFor(type);
        if (rowMapper.isPresent()) {
            return rowMapper.get().map(rs, ctx);
        } else {
            final ColumnMapper<?> columnMapper = columnMappers.findFor(type).orElseThrow(() -> new NoSuchMapperException("No column mapper registered for Pair " + (left ? "left" : "right") + " parameter " + type));
            return columnMapper.map(rs, 1, ctx);
        }
    }
}
