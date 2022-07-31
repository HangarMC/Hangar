package io.papermc.hangar.service.internal.users.invites;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.HangarNotificationsDAO;
import io.papermc.hangar.db.dao.internal.table.UserDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.Named;
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
import java.util.ArrayList;
import java.util.List;

public abstract class InviteService<LC extends LogContext<?, LC>, R extends Role<RT>, RT extends ExtendedRoleTable<R, LC>, J extends Table & Named & Loggable<LC>> extends HangarComponent {

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
    public void sendInvites(List<HangarApiException> errors, List<Member<R>> invitees, J joinable) {
        StringBuilder sb = new StringBuilder("Invited: ");
        List<RT> toBeInvited = new ArrayList<>();
        for (int i = 0; i < invitees.size(); i++) {
            Member<R> invitee = invitees.get(i);
            UserTable userTable = userDAO.getUserTable(invitee.getName());
            if (userTable == null) {
                errors.add(new HangarApiException(this.errorPrefix + "invalidUser", invitee.getName()));
                continue;
            }
            RT rt = roleService.addRole(invitee.getRole().create(joinable.getId(), userTable.getId(), false), true);
            if (rt == null) {
                errors.add(new HangarApiException(this.errorPrefix + "alreadyInvited", invitee.getName()));
                continue;
            }
            toBeInvited.add(rt);
            sb.append(userTable.getName()).append(" (").append(invitee.getRole().getTitle()).append(")");
            if (i + 1 != invitees.size()) {
                sb.append(", ");
            }
        }
        if (!invitees.isEmpty()) {
            joinableNotificationService.invited(toBeInvited, joinable);
            logInvitesSent(joinable, sb.toString());
        }
    }

    public void sendInvite(Member<R> invitee, J joinable) {
        UserTable userTable = userDAO.getUserTable(invitee.getName());
        if (userTable == null) {
            throw new HangarApiException(this.errorPrefix + "invalidUser", invitee.getName());
        }

        RT roleTable = roleService.addRole(invitee.getRole().create(joinable.getId(), userTable.getId(), false), true);
        if (roleTable == null) {
            throw new HangarApiException(this.errorPrefix + "alreadyInvited", invitee.getName());
        }

        joinableNotificationService.invited(List.of(roleTable), joinable);
        logInvitesSent(joinable, "Invited: " + userTable.getName() + " (" + invitee.getRole().getTitle() + ")");
    }

    abstract LogAction<LC> getInviteSentAction();

    protected void logInvitesSent(Loggable<LC> loggable, String log) {
        loggable.logAction(actionLogger, getInviteSentAction(), log, "");
    }

    public void acceptInvite(RT roleTable) {
        if (roleTable.isAccepted()) {
            throw new IllegalArgumentException("Cannot accept an invite that has already been accepted");
        }
        roleTable = roleService.changeAcceptance(roleTable, true);
        memberService.addMember(roleTable);
        UserTable userTable = userDAO.getUserTable(roleTable.getUserId());
        logInviteAccepted(roleTable, userTable);
    }

    abstract LogAction<LC> getInviteAcceptAction();

    protected void logInviteAccepted(RT roleTable, UserTable userTable) {
        roleTable.logAction(actionLogger, getInviteAcceptAction(), userTable.getName() + " accepted an invite for " + roleTable.getRole().getTitle(), roleTable.getCreatedAt().format(DateTimeFormatter.RFC_1123_DATE_TIME));
    }

    public void declineInvite(RT roleTable) {
        roleService.deleteRole(roleTable);
        logInviteDeclined(roleTable, userDAO.getUserTable(roleTable.getUserId()));
    }

    abstract LogAction<LC> getInviteDeclineAction();

    protected void logInviteDeclined(RT roleTable, UserTable userTable) {
        roleTable.logAction(actionLogger, getInviteDeclineAction(), userTable.getName() + " declined an invite for " + roleTable.getRole().getTitle(), roleTable.getCreatedAt().format(DateTimeFormatter.RFC_1123_DATE_TIME));
    }

}
