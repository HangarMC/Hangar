package io.papermc.hangar.service.internal.users.invites;

import io.papermc.hangar.db.dao.internal.table.OrganizationDAO;
import io.papermc.hangar.model.common.roles.OrganizationRole;
import io.papermc.hangar.model.db.OrganizationTable;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.roles.OrganizationRoleTable;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.OrganizationContext;
import io.papermc.hangar.model.internal.user.notifications.HangarInvite.HangarOrganizationInvite;
import io.papermc.hangar.service.internal.organizations.OrganizationService;
import io.papermc.hangar.service.internal.perms.members.OrganizationMemberService;
import io.papermc.hangar.service.internal.perms.roles.OrganizationRoleService;
import io.papermc.hangar.service.internal.users.notifications.JoinableNotificationService.OrganizationNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrganizationInviteService extends InviteService<OrganizationContext, OrganizationRole, OrganizationRoleTable, OrganizationTable> {

    private final OrganizationService organizationService;
    private final OrganizationDAO organizationDAO;

    @Autowired
    public OrganizationInviteService(OrganizationRoleService roleService, OrganizationMemberService memberService, OrganizationNotificationService organizationNotificationService, final OrganizationService organizationService, final OrganizationDAO organizationDAO) {
        super(roleService, memberService, organizationNotificationService, "organization.settings.members.");
        this.organizationService = organizationService;
        this.organizationDAO = organizationDAO;
    }

    public List<HangarOrganizationInvite> getOrganizationInvites() {
        return hangarNotificationsDAO.getOrganizationInvites(getHangarPrincipal().getId());
    }

    @Override
    protected OrganizationRole getOwnerRole() {
        return OrganizationRole.ORGANIZATION_OWNER;
    }

    @Override
    protected OrganizationRole getAdminRole() {
        return OrganizationRole.ORGANIZATION_ADMIN;
    }

    @Override
    LogAction<OrganizationContext> getInviteSentAction() {
        return LogAction.ORGANIZATION_INVITES_SENT;
    }

    @Override
    public OrganizationTable getJoinable(final long id) {
        return organizationService.getOrganizationTable(id);
    }

    @Override
    protected void updateOwnerId(final OrganizationTable organization, final UserTable newOwner) {
        organization.setOwnerId(newOwner.getUserId());
        organizationDAO.update(organization);
    }

    @Override
    LogAction<OrganizationContext> getInviteAcceptAction() {
        return LogAction.ORGANIZATION_MEMBER_ADDED;
    }

    @Override
    LogAction<OrganizationContext> getInviteDeclineAction() {
        return LogAction.ORGANIZATION_INVITE_DECLINED;
    }

}
