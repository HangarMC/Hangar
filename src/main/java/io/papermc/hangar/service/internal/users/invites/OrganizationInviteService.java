package io.papermc.hangar.service.internal.users.invites;

import io.papermc.hangar.model.common.roles.OrganizationRole;
import io.papermc.hangar.model.db.OrganizationTable;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.roles.OrganizationRoleTable;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.OrganizationContext;
import io.papermc.hangar.model.internal.user.notifications.HangarInvite.HangarOrganizationInvite;
import io.papermc.hangar.service.internal.perms.members.OrganizationMemberService;
import io.papermc.hangar.service.internal.perms.roles.OrganizationRoleService;
import io.papermc.hangar.service.internal.users.notifications.JoinableNotificationService.OrganizationNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class OrganizationInviteService extends InviteService<OrganizationRole, OrganizationRoleTable, OrganizationTable> {

    @Autowired
    public OrganizationInviteService(OrganizationRoleService roleService, OrganizationMemberService memberService, OrganizationNotificationService organizationNotificationService) {
        super(roleService, memberService, organizationNotificationService, "organization.settings.members.");
    }

    public List<HangarOrganizationInvite> getOrganizationInvites() {
        return hangarNotificationsDAO.get().getOrganizationInvites(getHangarPrincipal().getId());
    }

    @Override
    void logInvitesSent(long principalId, String log) {
        userActionLogService.organization(LogAction.ORGANIZATION_INVITES_SENT.create(OrganizationContext.of(principalId), log, ""));
    }

    @Override
    void logInviteAccepted(OrganizationRoleTable roleTable, UserTable userTable) {
        userActionLogService.organization(LogAction.ORGANIZATION_MEMBER_ADDED.create(OrganizationContext.of(roleTable.getOrganizationId()), userTable.getName() + " accepted an invite for " + roleTable.getRole().getTitle(), roleTable.getCreatedAt().format(/*TODO check this is right formatter*/DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
    }

    @Override
    void logInviteUnaccepted(OrganizationRoleTable roleTable, UserTable userTable) {
        userActionLogService.organization(LogAction.ORGANIZATION_INVITE_UNACCEPTED.create(OrganizationContext.of(roleTable.getOrganizationId()), userTable.getName() + " unaccepted an invite for " + roleTable.getRole().getTitle(), roleTable.getCreatedAt().format(/*TODO check this is right formatter*/DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
    }

    @Override
    void logInviteDeclined(OrganizationRoleTable roleTable, UserTable userTable) {
        userActionLogService.organization(LogAction.ORGANIZATION_INVITE_DECLINED.create(OrganizationContext.of(roleTable.getOrganizationId()), userTable.getName() + " declined an invite for " + roleTable.getRole().getTitle(), roleTable.getCreatedAt().format(/*TODO check this is right formatter*/DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
    }

}
