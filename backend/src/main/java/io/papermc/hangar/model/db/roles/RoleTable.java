package io.papermc.hangar.model.db.roles;

import io.papermc.hangar.db.customtypes.RoleCategory;
import io.papermc.hangar.model.Named;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.common.roles.Role;
import io.papermc.hangar.model.db.Table;
import java.time.OffsetDateTime;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class RoleTable extends Table implements Named {

    private final String name;
    private final RoleCategory category;
    private final String title;
    private final String color;
    private final boolean assignable;
    private final Integer rank;
    private final Permission permission;

    @JdbiConstructor
    public RoleTable(final OffsetDateTime createdAt, final long id, final String name, final RoleCategory category, final String title, final String color, final boolean assignable, final Integer rank, final Permission permission) {
        super(createdAt, id);
        this.name = name;
        this.category = category;
        this.title = title;
        this.color = color;
        this.assignable = assignable;
        this.rank = rank;
        this.permission = permission;
    }

    public RoleTable(final long id, final String name, final RoleCategory category, final String title, final String color, final boolean assignable, final Integer rank, final Permission permission) {
        super(id);
        this.name = name;
        this.category = category;
        this.title = title;
        this.color = color;
        this.assignable = assignable;
        this.rank = rank;
        this.permission = permission;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public RoleCategory getCategory() {
        return this.category;
    }

    public String getTitle() {
        return this.title;
    }

    public String getColor() {
        return this.color;
    }

    public boolean isAssignable() {
        return this.assignable;
    }

    public Integer getRank() {
        return this.rank;
    }

    public Permission getPermission() {
        return this.permission;
    }

    public static RoleTable fromRole(final Role<?> role) {
        return new RoleTable(
            role.getRoleId(),
            role.getValue(),
            role.getRoleCategory(),
            role.getTitle(),
            role.getColor().getHex(),
            role.isAssignable(),
            role.rank(),
            role.getPermissions()
        );
    }

    @Override
    public String toString() {
        return "RoleTable{" +
            "name='" + this.name + '\'' +
            ", category=" + this.category +
            ", title='" + this.title + '\'' +
            ", color='" + this.color + '\'' +
            ", assignable=" + this.assignable +
            ", rank=" + this.rank +
            ", permission=" + this.permission +
            "} " + super.toString();
    }
}
