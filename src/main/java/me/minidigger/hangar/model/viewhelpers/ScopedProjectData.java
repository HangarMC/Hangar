package me.minidigger.hangar.model.viewhelpers;

import me.minidigger.hangar.model.Permission;
import org.jdbi.v3.core.annotation.Unmappable;
import org.jdbi.v3.core.mapper.Nested;

public class ScopedProjectData {

    private boolean canPostAsOwnerOrga = false;
    private boolean uprojectFlags = false;
    private boolean starred = false;
    private boolean watching = false;
    private Permission permissions = Permission.None;

    public boolean perms(Permission perm) {
        return permissions.has(perm);
    }

    @Unmappable
    public boolean isCanPostAsOwnerOrga() {
        return canPostAsOwnerOrga;
    }

    @Unmappable
    public void setCanPostAsOwnerOrga(boolean canPostAsOwnerOrga) {
        this.canPostAsOwnerOrga = canPostAsOwnerOrga;
    }

    public boolean isUprojectFlags() {
        return uprojectFlags;
    }

    public void setUprojectFlags(boolean uprojectFlags) {
        this.uprojectFlags = uprojectFlags;
    }

    public boolean isStarred() {
        return starred;
    }

    public void setStarred(boolean starred) {
        this.starred = starred;
    }

    public boolean isWatching() {
        return watching;
    }

    public void setWatching(boolean watching) {
        this.watching = watching;
    }

    public Permission getPermissions() {
        return permissions;
    }

    @Nested("perm")
    public void setPermissions(Permission permissions) {
        this.permissions = permissions;
        this.canPostAsOwnerOrga = permissions.has(Permission.PostAsOrganization);
    }
}
