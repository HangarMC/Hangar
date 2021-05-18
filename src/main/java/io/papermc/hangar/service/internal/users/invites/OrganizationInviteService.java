package io.papermc.hangar.service.internal.users.invites;

import io.papermc.hangar.model.common.roles.OrganizationRole;
import io.papermc.hangar.model.db.OrganizationTable;
import io.papermc.hangar.model.db.roles.OrganizationRoleTable;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.OrganizationContext;
import io.papermc.hangar.model.internal.user.notifications.HangarInvite.HangarOrganizationInvite;
import io.papermc.hangar.service.internal.perms.members.OrganizationMemberService;
import io.papermc.hangar.service.internal.perms.roles.OrganizationRoleService;
import io.papermc.hangar.service.internal.users.notifications.JoinableNotificationService.OrganizationNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrganizationInviteService extends InviteService<OrganizationContext, OrganizationRole, OrganizationRoleTable, OrganizationTable> {

    @Autowired
    public OrganizationInviteService(OrganizationRoleService roleService, OrganizationMemberService memberService, OrganizationNotificationService organizationNotificationService) {
        super(roleService, memberService, organizationNotificationService, "organization.settings.members.");
    }

    public List<HangarOrganizationInvite> getOrganizationInvites() {
        return hangarNotificationsDAO.get().getOrganizationInvites(getHangarPrincipal().getId());
    }

    @Override
    LogAction<OrganizationContext> getInviteSentAction() {
        return LogAction.ORGANIZATION_INVITES_SENT;
    }

    @Override
    LogAction<OrganizationContext> getInviteAcceptAction() {
        return LogAction.ORGANIZATION_MEMBER_ADDED;
    }

    @Override
    LogAction<OrganizationContext> getInviteUnacceptAction() {
        return LogAction.ORGANIZATION_INVITE_UNACCEPTED;
    }

    @Override
    LogAction<OrganizationContext> getInviteDeclineAction() {
        return LogAction.ORGANIZATION_INVITE_DECLINED;
    }

}
