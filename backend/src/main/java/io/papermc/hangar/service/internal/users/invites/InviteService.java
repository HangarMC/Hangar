package io.papermc.hangar.service.internal.users.invites;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.HangarNotificationsDAO;
import io.papermc.hangar.db.dao.internal.table.UserDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.Named;
import io.papermc.hangar.model.Owned;
import io.papermc.hangar.model.common.MemberPermissions;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.db.OrganizationTable;
import io.papermc.hangar.model.db.Table;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.roles.ExtendedRoleTable;
import io.papermc.hangar.model.internal.api.requests.EditMembersForm;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.LogContext;
import io.papermc.hangar.model.loggable.Loggable;
import io.papermc.hangar.service.internal.organizations.OrganizationService;
import io.papermc.hangar.service.internal.perms.members.MemberService;
import io.papermc.hangar.service.internal.perms.roles.RoleService;
import io.papermc.hangar.service.internal.users.NotificationService;
import io.papermc.hangar.service.internal.users.notifications.JoinableNotificationService;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public abstract class InviteService<LC extends LogContext<?, LC>, RT extends ExtendedRoleTable<LC>, J extends Table & Named & Owned & Loggable<LC>> extends HangarComponent {

    @Autowired
    protected HangarNotificationsDAO hangarNotificationsDAO;

    @Autowired
    protected NotificationService notificationService;

    @Autowired
    protected OrganizationService organizationService;

    @Autowired
    private UserDAO userDAO;

    private final RoleService<RT, ?> roleService;
    private final MemberService<LC, RT, ?, ?, ?, ?, ?, ?> memberService;
    private final JoinableNotificationService<RT, J> joinableNotificationService;
    private final String errorPrefix;

    protected InviteService(final RoleService<RT, ?> roleService, final MemberService<LC, RT, ?, ?, ?, ?, ?, ?> memberService, final JoinableNotificationService<RT, J> joinableNotificationService, final String errorPrefix) {
        this.roleService = roleService;
        this.memberService = memberService;
        this.joinableNotificationService = joinableNotificationService;
        this.errorPrefix = errorPrefix;
    }

    @Transactional
    public void sendInvite(final EditMembersForm.Member invitee, final J joinable) {
        final UserTable userTable = this.userDAO.getUserTable(invitee.getName());
        if (userTable == null) {
            throw new HangarApiException(this.errorPrefix + "invalidUser", invitee.getName());
        }

        // Only allow inviting users
        if (this.organizationService.getOrganizationTableByUser(userTable.getId()) != null) {
            throw new HangarApiException(this.errorPrefix + "cannotInviteOrganization", invitee.getName());
        }

        final String title = this.memberService.requireTitle(invitee);
        final Permission permissions = this.memberService.sanitize(invitee.asPermission());
        final RT roleTable = this.roleService.addRole(this.createRole(joinable.getId(), userTable, permissions, title, false, false), true);
        if (roleTable == null) {
            throw new HangarApiException(this.errorPrefix + "alreadyInvited", invitee.getName());
        }

        this.joinableNotificationService.invited(roleTable.getUserId(), title, joinable, this.getHangarPrincipal().getUserId());

        this.logInvitesSent(joinable, "Invited: " + userTable.getName() + " (" + title + ")");
    }

    @Transactional
    public void sendTransferRequest(final String user, final J joinable) {
        final UserTable userTable = this.userDAO.getUserTable(user);
        if (userTable == null) {
            throw new HangarApiException(this.errorPrefix + "invalidUser", user);
        }

        final OrganizationTable organizationTable = this.organizationService.getOrganizationTableByUser(userTable.getId());
        if (!this.canTransferToOrganization() && organizationTable != null) {
            throw new HangarApiException("Cannot transfer to an organization");
        }

        final List<RT> ownerRoles = this.roleService.getOwnerRoles(joinable.getId());
        if (ownerRoles.stream().anyMatch(rt -> rt.getUserId() != joinable.getOwnerId())) {
            throw new HangarApiException(this.errorPrefix + "pendingTransfer");
        }

        final RT roleTable = this.roleService.addRole(this.createRole(joinable.getId(), userTable, this.memberService.ownerPermissions(), MemberPermissions.DEFAULT_OWNER_TITLE, false, true), true);
        if (roleTable == null) {
            final RT existingRole = this.roleService.getRole(joinable.getId(), userTable.getId());
            if (existingRole == null || !existingRole.isAccepted()) {
                throw new HangarApiException(this.errorPrefix + "alreadyInvited", user);
            }

            existingRole.setPermissions(this.memberService.ownerPermissions());
            existingRole.setOwner(true);
            existingRole.setAccepted(false);
            this.roleService.updateRole(existingRole);
        }

        // If transferred to an organization, notify the organization owner
        if (organizationTable != null) {
            this.joinableNotificationService.transferRequestOrg(organizationTable, joinable, this.getHangarPrincipal().getUserId(), this.getHangarPrincipal().getName());
        } else {
            this.joinableNotificationService.transferRequest(userTable.getId(), joinable, this.getHangarPrincipal().getUserId(), this.getHangarPrincipal().getName());
        }
        this.logInvitesSent(joinable, "Sent transfer request: " + userTable.getName());
    }

    protected boolean canTransferToOrganization() {
        return true;
    }

    public void cancelTransferRequest(final J joinable) {
        for (final RT ownerRole : this.roleService.getOwnerRoles(joinable.getId())) {
            if (!ownerRole.isAccepted()) {
                this.roleService.deleteRole(ownerRole);
            }
        }
    }

    protected abstract RT createRole(long principalId, UserTable user, Permission permissions, String title, boolean accepted, boolean owner);

    abstract LogAction<LC> getInviteSentAction();

    protected void logInvitesSent(final Loggable<LC> loggable, final String log) {
        loggable.logAction(this.actionLogger, this.getInviteSentAction(), log, "");
    }

    @Transactional
    public void acceptInvite(RT roleTable) {
        if (roleTable.isAccepted()) {
            throw new HangarApiException("Cannot accept an invite that has already been accepted");
        }

        roleTable = this.roleService.changeAcceptance(roleTable, true);
        this.memberService.addMemberIfNeeded(roleTable);

        final UserTable userTable = this.userDAO.getUserTable(roleTable.getUserId());
        this.logInviteAccepted(roleTable, userTable);

        if (roleTable.isOwner()) {
            this.setOwner(this.getJoinable(roleTable.getPrincipalId()), userTable, false);
        }
    }

    @Transactional
    public void setOwner(final J joinable, final UserTable userTable, final boolean addRole) {
        if (addRole) {
            final RT oldRole = this.roleService.getRole(joinable.getId(), userTable.getId());
            if (oldRole != null) {
                oldRole.setPermissions(this.memberService.ownerPermissions());
                oldRole.setOwner(true);
                oldRole.setAccepted(true);
                this.roleService.updateRole(oldRole);
            } else {
                final RT roleTable = this.roleService.addRole(this.createRole(joinable.getId(), userTable, this.memberService.ownerPermissions(), MemberPermissions.DEFAULT_OWNER_TITLE, true, true), false);
                this.memberService.addMemberIfNeeded(roleTable);
            }
        }

        // The previous owner keeps everything they can still be given, minus ownership itself
        final long oldOwnerId = joinable.getOwnerId();
        final RT oldOwnerRoleTable = this.roleService.getRole(joinable.getId(), oldOwnerId);
        if (oldOwnerRoleTable != null) {
            oldOwnerRoleTable.setOwner(false);
            oldOwnerRoleTable.setPermissions(this.memberService.sanitize(this.memberService.ownerPermissions()));
            if (MemberPermissions.DEFAULT_OWNER_TITLE.equals(oldOwnerRoleTable.getTitle())) {
                oldOwnerRoleTable.setTitle("Admin");
            }
            this.roleService.updateRole(oldOwnerRoleTable);
        }
        // Transfer of ownership and move files if needed - should always be done last
        this.updateOwnerId(joinable, userTable);
    }

    public abstract J getJoinable(final long id);

    protected abstract void updateOwnerId(J joinable, UserTable newOwner);

    abstract LogAction<LC> getInviteAcceptAction();

    protected void logInviteAccepted(final RT roleTable, final UserTable userTable) {
        roleTable.logAction(this.actionLogger, this.getInviteAcceptAction(), userTable.getName() + " accepted an invite for " + roleTable.getTitle(), roleTable.getCreatedAt().format(DateTimeFormatter.RFC_1123_DATE_TIME));
    }

    @Transactional
    public void declineInvite(final RT roleTable) {
        this.roleService.deleteRole(roleTable);
        this.logInviteDeclined(roleTable, this.userDAO.getUserTable(roleTable.getUserId()));
    }

    abstract LogAction<LC> getInviteDeclineAction();

    protected void logInviteDeclined(final RT roleTable, final UserTable userTable) {
        roleTable.logAction(this.actionLogger, this.getInviteDeclineAction(), userTable.getName() + " declined an invite for " + roleTable.getTitle(), roleTable.getCreatedAt().format(DateTimeFormatter.RFC_1123_DATE_TIME));
    }

}
