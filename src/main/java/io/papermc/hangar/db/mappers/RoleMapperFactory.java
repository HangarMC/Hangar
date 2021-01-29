package io.papermc.hangar.db.mappers;

import io.papermc.hangar.model.roles.GlobalRole;
import io.papermc.hangar.model.roles.Role;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.mapper.ColumnMapperFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.Optional;

@Component
public class RoleMapperFactory implements ColumnMapperFactory {

    @Override
    public Optional<ColumnMapper<?>> build(Type type, ConfigRegistry config) {
        if (!(type instanceof Class) || !Role.class.isAssignableFrom((Class<?>) type) || !((Class<?>) type).isEnum()) {
            return Optional.empty();
        }
        Class<?> clazz = (Class<?>) type;
        if (clazz == GlobalRole.class) {
            return Optional.of((r, columnNumber, ctx) -> Role.ID_ROLES.get(r.getLong(columnNumber)));
        } else {
            return Optional.of((r, columnNumber, ctx) -> Role.VALUE_ROLES.get(r.getString(columnNumber)));
        }
    }
}
