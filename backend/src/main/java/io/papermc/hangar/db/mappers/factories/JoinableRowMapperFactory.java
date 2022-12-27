package io.papermc.hangar.db.mappers.factories;

import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.roles.ExtendedRoleTable;
import io.papermc.hangar.model.internal.user.JoinableMember;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.generic.GenericTypes;
import org.jdbi.v3.core.mapper.NoSuchMapperException;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.mapper.RowMapperFactory;
import org.jdbi.v3.core.mapper.RowMappers;

public class JoinableRowMapperFactory implements RowMapperFactory {

    @SuppressWarnings("unchecked")
    @Override
    public Optional<RowMapper<?>> build(final Type type, final ConfigRegistry config) {
        if (!JoinableMember.class.equals(GenericTypes.getErasedType(type))) {
            return Optional.empty();
        }

        final Type tableType = GenericTypes.resolveType(JoinableMember.class.getTypeParameters()[0], type);
        if (!ExtendedRoleTable.class.isAssignableFrom(GenericTypes.getErasedType(tableType))) {
            return Optional.empty();
        }
        final Class<? extends ExtendedRoleTable<?, ?>> extendedRoleTableType = (Class<? extends ExtendedRoleTable<?, ?>>) tableType;

        final RowMappers rowMappers = config.get(RowMappers.class);
        final RowMapper<? extends ExtendedRoleTable<?, ?>> tableMapper = rowMappers.findFor(extendedRoleTableType).orElseThrow(() -> new NoSuchMapperException("Could not find mapper for " + tableType.getTypeName()));
        final RowMapper<UserTable> userTableMapper = rowMappers.findFor(UserTable.class).orElseThrow(() -> new NoSuchMapperException("Could not find mapper for " + UserTable.class.getTypeName()));

        final RowMapper<JoinableMember<?>> mapper = (rs, ctx) -> new JoinableMember<>(tableMapper.map(rs, ctx), userTableMapper.map(rs, ctx), this.getOrFalse(rs, "hidden"));
        return Optional.of(mapper);
    }

    private boolean getOrFalse(final ResultSet rs, final String col) {
        try {
            return rs.getBoolean(col);
        } catch (final SQLException e) {
            return false;
        }
    }
}
