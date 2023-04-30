package io.papermc.hangar.service.internal.users.invites;

import io.papermc.hangar.db.dao.internal.table.OrganizationDAO;
import io.papermc.hangar.model.common.roles.OrganizationRole;
import io.papermc.hangar.model.db.OrganizationTable;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.roles.OrganizationRoleTable;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.OrganizationContext;
import io.papermc.hangar.model.internal.user.notifications.HangarInvite;
import io.papermc.hangar.service.internal.organizations.OrganizationService;
import io.papermc.hangar.service.internal.perms.members.OrganizationMemberService;
import io.papermc.hangar.service.internal.perms.roles.OrganizationRoleService;
import io.papermc.hangar.service.internal.users.notifications.JoinableNotificationService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrganizationInviteService extends InviteService<OrganizationContext, OrganizationRole, OrganizationRoleTable, OrganizationTable> {

    private final OrganizationService organizationService;
    private final OrganizationDAO organizationDAO;

    @Autowired
    public OrganizationInviteService(final OrganizationRoleService roleService, final OrganizationMemberService memberService, final JoinableNotificationService.OrganizationNotificationService organizationNotificationService, final OrganizationService organizationService, final OrganizationDAO organizationDAO) {
        super(roleService, memberService, organizationNotificationService, "organization.settings.members.");
        this.organizationService = organizationService;
        this.organizationDAO = organizationDAO;
    }

    public List<HangarInvite.HangarOrganizationInvite> getOrganizationInvites() {
        return this.hangarNotificationsDAO.getOrganizationInvites(this.getHangarPrincipal().getId());
    }

    @Override
    protected boolean canInviteOrganizationUser() {
        return false;
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
        return this.organizationService.getOrganizationTable(id);
    }

    @Override
    protected void updateOwnerId(final OrganizationTable organization, final UserTable newOwner) {
        organization.setOwnerId(newOwner.getUserId());
        this.organizationDAO.update(organization);
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
