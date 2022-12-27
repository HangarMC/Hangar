package io.papermc.hangar.security.authentication;

import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.db.projects.ProjectOwner;

public class HangarPrincipal implements ProjectOwner {

    private final long id;
    private final String name;
    private final boolean locked;
    private final Permission globalPermissions;

    public HangarPrincipal(final long id, final String name, final boolean locked, final Permission globalPermissions) {
        this.id = id;
        this.name = name;
        this.locked = locked;
        this.globalPermissions = globalPermissions;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public long getUserId() {
        return this.id;
    }

    public final boolean isLocked() {
        return this.locked;
    }

    public Permission getPossiblePermissions(){ return Permission.All; }

    public final Permission getGlobalPermissions() {
        return this.globalPermissions.intersect(this.getPossiblePermissions());
    }

    public final boolean isAllowedGlobal(final Permission requiredPermission) {
        return this.isAllowed(requiredPermission, this.globalPermissions);
    }

    public final boolean isAllowed(final Permission requiredPermission, final Permission currentPermission) {
        final Permission intersect = requiredPermission.intersect(currentPermission);
        if (intersect.isNone()) {
            return false;
        }
        return this.getPossiblePermissions().has(requiredPermission.intersect(currentPermission));
    }

    @Override
    public String toString() {
        return "HangarPrincipal{" +
                "id=" + this.id +
                ", name='" + this.name + '\'' +
                ", locked=" + this.locked +
                ", globalPermissions=" + this.globalPermissions +
                '}';
    }
}
