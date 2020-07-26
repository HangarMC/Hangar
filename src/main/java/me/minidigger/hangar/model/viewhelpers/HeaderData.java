package me.minidigger.hangar.model.viewhelpers;

import me.minidigger.hangar.db.model.UsersTable;
import me.minidigger.hangar.model.Permission;

public class HeaderData {

    private UsersTable currentUser = null;
    private Permission globalPermission = Permission.None;
    private boolean hasNotice = false;
    private boolean hasUnreadNotifications = false;
    private boolean unresolvedFlags = false;
    private boolean hasProjectApprovals = false;
    private boolean hasReviewQueue = false;

    public boolean isAuthenticated() {
        return currentUser != null;
    }

    public boolean hasUser() {
        return currentUser != null;
    }

    public boolean isCurrentUser(UsersTable usersTable) {
        return hasUser() && currentUser.getId() == usersTable.getId();
    }

    public boolean globalPerm(Permission permission) {
        return globalPermission.has(permission);
    }

    public UsersTable getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(UsersTable currentUser) {
        this.currentUser = currentUser;
    }

    public Permission getGlobalPermission() {
        return globalPermission;
    }

    public void setGlobalPermission(Permission globalPermission) {
        this.globalPermission = globalPermission;
    }

    public boolean hasNotice() {
        return hasNotice;
    }

    public void setHasNotice(boolean hasNotice) {
        this.hasNotice = hasNotice;
    }

    public boolean hasUnreadNotifications() {
        return hasUnreadNotifications;
    }

    public void setHasUnreadNotifications(boolean hasUnreadNotifications) {
        this.hasUnreadNotifications = hasUnreadNotifications;
    }

    public boolean hasUnresolvedFlags() {
        return unresolvedFlags;
    }

    public void setUnresolvedFlags(boolean unresolvedFlags) {
        this.unresolvedFlags = unresolvedFlags;
    }

    public boolean hasProjectApprovals() {
        return hasProjectApprovals;
    }

    public void setHasProjectApprovals(boolean hasProjectApprovals) {
        this.hasProjectApprovals = hasProjectApprovals;
    }

    public boolean hasReviewQueue() {
        return hasReviewQueue;
    }

    public void setHasReviewQueue(boolean hasReviewQueue) {
        this.hasReviewQueue = hasReviewQueue;
    }
}
