package io.papermc.hangar.service.internal.users.notifications;

import io.papermc.hangar.db.dao.internal.table.NotificationsDAO;
import io.papermc.hangar.model.Named;
import io.papermc.hangar.model.common.roles.Role;
import io.papermc.hangar.model.db.NotificationTable;
import io.papermc.hangar.model.db.OrganizationTable;
import io.papermc.hangar.model.db.Table;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.roles.ExtendedRoleTable;
import io.papermc.hangar.model.db.roles.OrganizationRoleTable;
import io.papermc.hangar.model.db.roles.ProjectRoleTable;
import io.papermc.hangar.model.internal.user.notifications.NotificationType;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

public abstract class JoinableNotificationService<RT extends ExtendedRoleTable<? extends Role<RT>, ?>, J extends Table & Named> {

    @Autowired
    private NotificationsDAO notificationsDAO;

    protected final String msgPrefix;

    protected JoinableNotificationService(final String msgPrefix) {
        this.msgPrefix = msgPrefix;
    }

    public void invited(final long inviteeId, final String roleTitle, final J joinable, final long inviterId) {
        this.notificationsDAO.insert(new NotificationTable(inviteeId, "notifications", inviterId, new String[]{this.msgPrefix + "invite", roleTitle, joinable.getName()}, NotificationType.INFO));
    }

    public void invitedOrg(final OrganizationTable org, final String roleTitle, final J joinable, final long inviterId) {
        this.notificationsDAO.insert(new NotificationTable(org.getOwnerId(), "notifications", inviterId, new String[]{this.msgPrefix + "inviteOrg", org.getName(), roleTitle, joinable.getName()}, NotificationType.INFO));
    }

    public void transferRequest(final long inviteeId, final J joinable, final long inviterId, final String inviterName) {
        this.notificationsDAO.insert(new NotificationTable(inviteeId, "notifications", inviterId, new String[]{this.msgPrefix + "transfer", inviterName, joinable.getName()}, NotificationType.INFO));
    }

    public void transferRequestOrg(final OrganizationTable org, final J joinable, final long inviterId, final String inviterName) {
        this.notificationsDAO.insert(new NotificationTable(org.getOwnerId(), "notifications", inviterId, new String[]{this.msgPrefix + "transferOrg", inviterName, joinable.getName(),  org.getName()}, NotificationType.INFO));
    }

    public void removedFrom(final RT removedFromRoleTable, final J joinable, final @Nullable Long byUserId) {
        final String msgKey = this.msgPrefix + (removedFromRoleTable.isAccepted() ? "removed" : "inviteRescinded");
        this.notificationsDAO.insert(new NotificationTable(removedFromRoleTable.getUserId(), null, byUserId,
            new String[]{msgKey, removedFromRoleTable.getRole().getTitle(), joinable.getName()}, NotificationType.WARNING));
    }

    public void removedFromOrg(final RT removedFromRoleTable, OrganizationTable org, final J joinable, final @Nullable Long byUserId) {
        final String msgKey = this.msgPrefix + (removedFromRoleTable.isAccepted() ? "removedOrg" : "inviteRescindedOrg");
        this.notificationsDAO.insert(new NotificationTable(org.getOwnerId(), null, byUserId,
            new String[]{msgKey, org.getName(), removedFromRoleTable.getRole().getTitle(), joinable.getName()}, NotificationType.WARNING));
    }

    public void roleChanged(final RT changedRoleTable, final J joinable, final @Nullable Long byUserId) {
        this.notificationsDAO.insert(new NotificationTable(changedRoleTable.getUserId(), null, byUserId,
            new String[]{this.msgPrefix + "roleChanged", changedRoleTable.getRole().getTitle(), joinable.getName()}, NotificationType.INFO));
    }

    public void roleChangedOrg(final RT changedRoleTable, final OrganizationTable org, final J joinable, final @Nullable Long byUserId) {
        this.notificationsDAO.insert(new NotificationTable(org.getOwnerId(), null, byUserId,
            new String[]{this.msgPrefix + "roleChangedOrg", org.getName(), changedRoleTable.getRole().getTitle(), joinable.getName()}, NotificationType.INFO));
    }

    @Service
    public static class ProjectNotificationService extends JoinableNotificationService<ProjectRoleTable, ProjectTable> {

        public ProjectNotificationService() {
            super("notifications.project.");
        }

    }

    @Service
    public static class OrganizationNotificationService extends JoinableNotificationService<OrganizationRoleTable, OrganizationTable> {

        public OrganizationNotificationService() {
            super("notifications.organization.");
        }

    }
}
