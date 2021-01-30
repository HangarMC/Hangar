package io.papermc.hangar.model.roles;

import io.papermc.hangar.db.customtypes.RoleCategory;
import io.papermc.hangar.db.dao.internal.table.roles.RoleDAO;
import io.papermc.hangar.model.Color;
import io.papermc.hangar.model.Permission;
import io.papermc.hangar.model.db.roles.RoleTable;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.statement.StatementContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public interface Role<T extends RoleTable<? extends Role<T>>> extends Argument {

    Map<String, Role<?>> VALUE_ROLES = new HashMap<>();
    Map<Long, Role<?>> ID_ROLES = new HashMap<>();
    static <C extends Enum<C> & Role<?>> void registerRole(C roleEnum) {
        if (ID_ROLES.containsKey(roleEnum.getRoleId()) || VALUE_ROLES.containsKey(roleEnum.getValue())) {
            throw new IllegalArgumentException(roleEnum + " has a duplicate role ID or value");
        }
        ID_ROLES.put(roleEnum.getRoleId(), roleEnum);
        VALUE_ROLES.put(roleEnum.getValue(), roleEnum);
    }

    @NotNull
    String getValue();

    long getRoleId();

    @NotNull
    RoleCategory getRoleCategory();

    @NotNull
    Permission getPermissions();

    @NotNull
    String getTitle();

    @NotNull
    Color getColor();

    boolean isAssignable();

    @Nullable
    Long getRank();

    @NotNull
    T create(@Nullable Long principalId, long userId, boolean isAccepted);

    RoleDAO<T> getRoleDAO();

    @Override
    default void apply(int position, PreparedStatement statement, StatementContext ctx) throws SQLException {
        statement.setString(position, getValue());
    }
}
