package io.papermc.hangar.service.internal.perms.members;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.members.OrganizationMembersDAO;
import io.papermc.hangar.db.dao.internal.table.roles.OrganizationRolesDAO;
import io.papermc.hangar.model.common.roles.OrganizationRole;
import io.papermc.hangar.model.db.OrganizationTable;
import io.papermc.hangar.model.db.members.OrganizationMemberTable;
import io.papermc.hangar.model.db.roles.OrganizationRoleTable;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.OrganizationContext;
import io.papermc.hangar.service.internal.perms.roles.OrganizationRoleService;
import io.papermc.hangar.service.internal.users.notifications.JoinableNotificationService.OrganizationNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrganizationMemberService extends MemberService<
        OrganizationContext,
        OrganizationRole,
        OrganizationRoleTable,
        OrganizationRolesDAO,
        OrganizationRoleService,
        OrganizationTable,
        OrganizationNotificationService,
        OrganizationMembersDAO,
        OrganizationMemberTable
        > {

    @Autowired
    public OrganizationMemberService(OrganizationRoleService roleService, HangarDao<OrganizationMembersDAO> organizationMembersDAO, OrganizationNotificationService organizationNotificationService) {
        super(roleService, organizationMembersDAO.get(), organizationNotificationService, OrganizationMemberTable::new, "organization.settings.members.", LogAction.ORGANIZATION_MEMBER_ADDED, LogAction.ORGANIZATION_MEMBERS_REMOVED, LogAction.ORGANIZATION_MEMBER_ROLES_CHANGED);
    }

    @Override
    List<OrganizationRole> invalidRolesToChange() {
        return List.of(OrganizationRole.ORGANIZATION_OWNER);
    }
}
