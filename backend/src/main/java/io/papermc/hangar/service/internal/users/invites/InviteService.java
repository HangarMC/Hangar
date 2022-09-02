package io.papermc.hangar.service.internal.users.invites;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.HangarNotificationsDAO;
import io.papermc.hangar.db.dao.internal.table.UserDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.Named;
import io.papermc.hangar.model.Owned;
import io.papermc.hangar.model.common.roles.Role;
import io.papermc.hangar.model.db.Table;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.roles.ExtendedRoleTable;
import io.papermc.hangar.model.internal.api.requests.EditMembersForm.Member;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.LogContext;
import io.papermc.hangar.model.loggable.Loggable;
import io.papermc.hangar.service.internal.perms.members.MemberService;
import io.papermc.hangar.service.internal.perms.roles.RoleService;
import io.papermc.hangar.service.internal.users.NotificationService;
import io.papermc.hangar.service.internal.users.notifications.JoinableNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

public abstract class InviteService<LC extends LogContext<?, LC>, R extends Role<RT>, RT extends ExtendedRoleTable<R, LC>, J extends Table & Named & Owned & Loggable<LC>> extends HangarComponent {

    @Autowired
    protected HangarNotificationsDAO hangarNotificationsDAO;

    @Autowired
    protected NotificationService notificationService;

    @Autowired
    private UserDAO userDAO;

    private final RoleService<RT, R, ?> roleService;
    private final MemberService<LC, R, RT, ?, ?, ?, ?, ?, ?> memberService;
    private final JoinableNotificationService<RT, J> joinableNotificationService;
    private final String errorPrefix;

    protected InviteService(RoleService<RT, R, ?> roleService, MemberService<LC, R, RT, ?, ?, ?, ?, ?, ?> memberService, JoinableNotificationService<RT, J> joinableNotificationService, String errorPrefix) {
        this.roleService = roleService;
        this.memberService = memberService;
        this.joinableNotificationService = joinableNotificationService;
        this.errorPrefix = errorPrefix;
    }

    @Transactional
    public void sendInvite(Member<R> invitee, J joinable) {
        UserTable userTable = userDAO.getUserTable(invitee.getName());
        if (userTable == null) {
            throw new HangarApiException(this.errorPrefix + "invalidUser", invitee.getName());
        }

        RT roleTable = roleService.addRole(invitee.getRole().create(joinable.getId(), userTable.getId(), false), true);
        if (roleTable == null) {
            throw new HangarApiException(this.errorPrefix + "alreadyInvited", invitee.getName());
        }

        joinableNotificationService.invited(List.of(roleTable), joinable, getHangarUserId());
        logInvitesSent(joinable, "Invited: " + userTable.getName() + " (" + invitee.getRole().getTitle() + ")");
    }

    @Transactional
    public void sendTransferRequest(final String user, final J joinable) {
        final UserTable userTable = userDAO.getUserTable(user);
        //TODO transfer project to organization (and divert invite to owner)
        if (userTable == null || userTable.isOrganization()) {
            throw new HangarApiException(this.errorPrefix + "invalidUser", user);
        }

        final List<RT> ownerRoles = roleService.getRoles(joinable.getId(), getOwnerRole());
        if (ownerRoles.stream().anyMatch(rt -> rt.getUserId() != joinable.getOwnerId())) {
            throw new HangarApiException(this.errorPrefix + "pendingTransfer");
        }

        final RT roleTable = roleService.addRole(getOwnerRole().create(joinable.getId(), userTable.getId(), false), true);
        if (roleTable == null) {
            throw new HangarApiException(this.errorPrefix + "alreadyInvited", user);
        }

        joinableNotificationService.transferRequest(roleTable, joinable, getHangarPrincipal().getUserId(), getHangarPrincipal().getName());
        logInvitesSent(joinable, "Sent transfer request: " + userTable.getName());
    }

    @Transactional
    public void cancelTransferRequest(final J joinable) {
        final List<RT> ownerRoles = roleService.getRoles(joinable.getId(), getOwnerRole());
        for (final RT ownerRole : ownerRoles) {
            if (!ownerRole.isAccepted()) {
                roleService.deleteRole(ownerRole);
            }
        }
    }

    protected abstract R getOwnerRole();

    protected abstract R getAdminRole();

    abstract LogAction<LC> getInviteSentAction();

    protected void logInvitesSent(Loggable<LC> loggable, String log) {
        loggable.logAction(actionLogger, getInviteSentAction(), log, "");
    }

    @Transactional
    public void acceptInvite(RT roleTable) {
        if (roleTable.isAccepted()) {
            throw new IllegalArgumentException("Cannot accept an invite that has already been accepted");
        }

        final UserTable userTable = userDAO.getUserTable(roleTable.getUserId());
        roleTable = roleService.changeAcceptance(roleTable, true);
        memberService.addMember(roleTable);
        logInviteAccepted(roleTable, userTable);

        if (roleTable.getRole().getRoleId() == getOwnerRole().getRoleId()) {
            setOwner(getJoinable(roleTable.getPrincipalId()), userTable, false);
        }
    }

    @Transactional
    public void setOwner(final J joinable, final UserTable userTable, final boolean addRole) { //TODO make sure new owner doesn't have project of the same name
        if (addRole) {
            final RT oldRole = roleService.getRole(joinable.getId(), userTable.getId());
            if (oldRole != null) {
                oldRole.setRole(getOwnerRole());
                oldRole.setAccepted(true);
                roleService.updateRole(oldRole);
            } else {
                final RT roleTable = roleService.addRole(getOwnerRole().create(joinable.getId(), userTable.getId(), true), false);
                memberService.addMember(roleTable);
            }
        }

        // Set role of old owner to next highest
        final long oldOwnerId = joinable.getOwnerId();
        final RT oldOwnerRoleTable = roleService.getRole(joinable.getId(), oldOwnerId);
        oldOwnerRoleTable.setRole(getAdminRole());
        roleService.updateRole(oldOwnerRoleTable);
        // Transfer of ownership and move files if needed - should always be done last
        updateOwnerId(joinable, userTable);
    }

    public abstract J getJoinable(final long id);

    protected abstract void updateOwnerId(J joinable, UserTable newOwner);

    abstract LogAction<LC> getInviteAcceptAction();

    protected void logInviteAccepted(RT roleTable, UserTable userTable) {
        roleTable.logAction(actionLogger, getInviteAcceptAction(), userTable.getName() + " accepted an invite for " + roleTable.getRole().getTitle(), roleTable.getCreatedAt().format(DateTimeFormatter.RFC_1123_DATE_TIME));
    }

    @Transactional
    public void declineInvite(RT roleTable) {
        roleService.deleteRole(roleTable);
        logInviteDeclined(roleTable, userDAO.getUserTable(roleTable.getUserId()));
    }

    abstract LogAction<LC> getInviteDeclineAction();

    protected void logInviteDeclined(RT roleTable, UserTable userTable) {
        roleTable.logAction(actionLogger, getInviteDeclineAction(), userTable.getName() + " declined an invite for " + roleTable.getRole().getTitle(), roleTable.getCreatedAt().format(DateTimeFormatter.RFC_1123_DATE_TIME));
    }

}
