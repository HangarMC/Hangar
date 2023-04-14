package io.papermc.hangar.db.mappers.factories;

import io.papermc.hangar.model.common.roles.CompactRole;
import io.papermc.hangar.model.common.roles.GlobalRole;
import io.papermc.hangar.model.common.roles.Role;
import java.lang.reflect.Type;
import java.util.Optional;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.mapper.ColumnMapperFactory;

public class CompactRoleColumnMapperFactory implements ColumnMapperFactory {

    @Override
    public Optional<ColumnMapper<?>> build(final Type type, final ConfigRegistry config) {
        if (!(type instanceof final Class<?> clazz) || !CompactRole.class.isAssignableFrom((Class<?>) type)) {
            return Optional.empty();
        }
        if (clazz == GlobalRole.class) {
            return Optional.of((r, columnNumber, ctx) -> {
                final Role<?> role = Role.ID_ROLES.get(r.getLong(columnNumber));
                return CompactRole.ofNullable(role);
            });
        } else {
            return Optional.of((r, columnNumber, ctx) -> {
                final Role<?> role = Role.VALUE_ROLES.get(r.getString(columnNumber));
                return CompactRole.ofNullable(role);
            });
        }
    }
}
