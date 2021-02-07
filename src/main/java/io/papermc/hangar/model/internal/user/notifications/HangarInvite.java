package io.papermc.hangar.model.internal.user.notifications;

import io.papermc.hangar.model.common.roles.OrganizationRole;
import io.papermc.hangar.model.common.roles.ProjectRole;
import io.papermc.hangar.model.common.roles.Role;
import io.papermc.hangar.model.db.roles.IRoleTable;

public abstract class HangarInvite<R extends Role<? extends IRoleTable<R>>> {

    private final long roleTableId;
    private final R role;
    private final String type;
    private final String name;
    private final String url;

    public HangarInvite(long roleTableId, R role, String type, String name, String url) {
        this.role = role;
        this.roleTableId = roleTableId;
        this.type = type;
        this.name = name;
        this.url = url;
    }

    public long getRoleTableId() {
        return roleTableId;
    }

    public R getRole() {
        return role;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public static class HangarProjectInvite extends HangarInvite<ProjectRole> {

        public HangarProjectInvite(long roleTableId, ProjectRole role, String type, String name, String url) {
            super(roleTableId, role, type, name, url);
        }
    }

    public static class HangarOrganizationInvite extends HangarInvite<OrganizationRole> {

        public HangarOrganizationInvite(long roleTableId, OrganizationRole role, String type, String name, String url) {
            super(roleTableId, role, type, name, url);
        }
    }
}
