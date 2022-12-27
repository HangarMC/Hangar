package io.papermc.hangar.service.internal.perms.members;

import io.papermc.hangar.db.dao.internal.table.members.OrganizationMembersDAO;
import io.papermc.hangar.db.dao.internal.table.roles.OrganizationRolesDAO;
import io.papermc.hangar.model.common.roles.OrganizationRole;
import io.papermc.hangar.model.db.OrganizationTable;
import io.papermc.hangar.model.db.members.OrganizationMemberTable;
import io.papermc.hangar.model.db.roles.OrganizationRoleTable;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.OrganizationContext;
import io.papermc.hangar.service.internal.perms.roles.OrganizationRoleService;
import io.papermc.hangar.service.internal.users.notifications.JoinableNotificationService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrganizationMemberService extends MemberService<
    OrganizationContext,
    OrganizationRole,
    OrganizationRoleTable,
    OrganizationRolesDAO,
    OrganizationRoleService,
    OrganizationTable,
    JoinableNotificationService.OrganizationNotificationService,
    OrganizationMembersDAO,
    OrganizationMemberTable
    > {

    @Autowired
    private OrganizationMembersDAO dao;

    @Autowired
    public OrganizationMemberService(final OrganizationRoleService roleService, final OrganizationMembersDAO organizationMembersDAO, final JoinableNotificationService.OrganizationNotificationService organizationNotificationService) {
        super(roleService, organizationMembersDAO, organizationNotificationService, OrganizationMemberTable::new, "organization.settings.members.", LogAction.ORGANIZATION_MEMBER_ADDED, LogAction.ORGANIZATION_MEMBERS_REMOVED, LogAction.ORGANIZATION_MEMBER_ROLES_CHANGED);
    }

    @Override
    List<OrganizationRole> invalidRolesToChange() {
        return List.of(OrganizationRole.ORGANIZATION_OWNER);
    }

    public void setMembershipVisibility(final long organizationId, final long userId, final boolean hidden) {
        this.dao.setMembershipVisibility(organizationId, userId, hidden);
    }

    public Map<String, Boolean> getUserOrganizationMembershipVisibility(final String user) {
        return this.dao.getUserOrganizationMembershipVisibility(user);
    }
}
