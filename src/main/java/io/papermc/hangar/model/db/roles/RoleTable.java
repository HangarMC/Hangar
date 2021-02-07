package io.papermc.hangar.model.db.roles;

import io.papermc.hangar.db.customtypes.RoleCategory;
import io.papermc.hangar.model.Named;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.common.roles.Role;
import io.papermc.hangar.model.db.Table;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;

public class RoleTable extends Table implements Named {

    private final String name;
    private final RoleCategory category;
    private final String title;
    private final String color;
    private final boolean assignable;
    private final Integer rank;
    private final Permission permission;

    @JdbiConstructor
    public RoleTable(OffsetDateTime createdAt, long id, String name, RoleCategory category, String title, String color, boolean assignable, Integer rank, Permission permission) {
        super(createdAt, id);
        this.name = name;
        this.category = category;
        this.title = title;
        this.color = color;
        this.assignable = assignable;
        this.rank = rank;
        this.permission = permission;
    }

    private RoleTable(long id, String name, RoleCategory category, String title, String color, boolean assignable, Integer rank, Permission permission) {
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
        return name;
    }

    public RoleCategory getCategory() {
        return category;
    }

    public String getTitle() {
        return title;
    }

    public String getColor() {
        return color;
    }

    public boolean isAssignable() {
        return assignable;
    }

    public Integer getRank() {
        return rank;
    }

    public Permission getPermission() {
        return permission;
    }

    public static RoleTable fromRole(Role<?> role) {
        return new RoleTable(
            role.getRoleId(),
                role.getValue(),
                role.getRoleCategory(),
                role.getTitle(),
                role.getColor().getHex(),
                role.isAssignable(),
                role.getRank(),
                role.getPermissions()
        );
    }

    @Override
    public String toString() {
        return "RoleTable{" +
                "name='" + name + '\'' +
                ", category=" + category +
                ", title='" + title + '\'' +
                ", color='" + color + '\'' +
                ", assignable=" + assignable +
                ", rank=" + rank +
                ", permission=" + permission +
                "} " + super.toString();
    }
}
