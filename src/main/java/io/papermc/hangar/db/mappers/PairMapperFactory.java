package io.papermc.hangar.db.mappers;

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

import java.lang.reflect.Type;
import java.util.Optional;

public class PairMapperFactory implements RowMapperFactory {

    @Override
    public Optional<RowMapper<?>> build(Type type, ConfigRegistry config) {
        if (!Pair.class.equals(GenericTypes.getErasedType(type))) {
            return Optional.empty();
        }

        Type leftType = GenericTypes.resolveType(Pair.class.getTypeParameters()[0], type);
        Type rightType = GenericTypes.resolveType(Pair.class.getTypeParameters()[1], type);

        RowMappers rowMappers = config.get(RowMappers.class);

        Optional<RowMapper<?>> leftMapper = rowMappers.findFor(leftType);
        ColumnMapper<?> columnMapper = null;
        if (leftMapper.isEmpty()) {
            columnMapper = config.get(ColumnMappers.class).findFor(leftType).orElseThrow(() -> new NoSuchMapperException("No column mapper registered for Pair left parameter " + leftType));
        }
        RowMapper<?> rightMapper = rowMappers.findFor(rightType).orElseThrow(() -> new NoSuchMapperException("No row mapper registered for Pair right parameter " + rightType));

        ColumnMapper<?> finalColumnMapper = columnMapper;
        RowMapper<?> pairMapper = (rs, ctx) -> {
            Object left;
            if (leftMapper.isPresent()) {
                left = leftMapper.get().map(rs, ctx);
            } else {
                left = finalColumnMapper.map(rs, 1, ctx);
            }
            return new ImmutablePair(left, rightMapper.map(rs, ctx));
        };
        return Optional.of(pairMapper);
    }
}
