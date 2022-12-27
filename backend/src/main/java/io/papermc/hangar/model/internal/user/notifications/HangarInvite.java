package io.papermc.hangar.model.internal.user.notifications;

import com.fasterxml.jackson.annotation.JsonValue;
import io.papermc.hangar.model.common.roles.OrganizationRole;
import io.papermc.hangar.model.common.roles.ProjectRole;
import io.papermc.hangar.model.common.roles.Role;
import io.papermc.hangar.model.db.roles.IRoleTable;

import java.util.Locale;

public abstract class HangarInvite<R extends Role<? extends IRoleTable<R>>> {

    private final long roleTableId;
    private final R role;
    private final String name;
    private final String url;

    protected HangarInvite(final long roleTableId, final R role, final String name, final String url) {
        this.role = role;
        this.roleTableId = roleTableId;
        this.name = name;
        this.url = url;
    }

    public long getRoleTableId() {
        return this.roleTableId;
    }

    public R getRole() {
        return this.role;
    }

    public abstract InviteType getType();

    public String getName() {
        return this.name;
    }

    public String getUrl() {
        return this.url;
    }

    public static class HangarProjectInvite extends HangarInvite<ProjectRole> {

        public HangarProjectInvite(final long roleTableId, final ProjectRole role, final String name, final String url) {
            super(roleTableId, role, name, url);
        }

        @Override
        public InviteType getType() {
            return InviteType.PROJECT;
        }
    }

    public static class HangarOrganizationInvite extends HangarInvite<OrganizationRole> {

        public HangarOrganizationInvite(final long roleTableId, final OrganizationRole role, final String name, final String url) {
            super(roleTableId, role, name, url);
        }

        @Override
        public InviteType getType() {
            return InviteType.ORGANIZATION;
        }
    }

    public enum InviteType {
        PROJECT,
        ORGANIZATION;

        @Override
        @JsonValue
        public String toString() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }
}
