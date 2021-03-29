package io.papermc.hangar.service.internal.perms.members;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.members.OrganizationMembersDAO;
import io.papermc.hangar.db.dao.internal.table.roles.OrganizationRolesDAO;
import io.papermc.hangar.model.common.roles.OrganizationRole;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.members.OrganizationMemberTable;
import io.papermc.hangar.model.db.roles.OrganizationRoleTable;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.OrganizationContext;
import io.papermc.hangar.service.internal.perms.roles.OrganizationRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrganizationMemberService extends MemberService<
        OrganizationRole,
        OrganizationRoleTable,
        OrganizationRolesDAO,
        OrganizationRoleService,
        OrganizationMembersDAO,
        OrganizationMemberTable
        > {

    @Autowired
    public OrganizationMemberService(OrganizationRoleService roleService, HangarDao<OrganizationMembersDAO> organizationMembersDAO) {
        super(roleService, organizationMembersDAO.get(), OrganizationMemberTable::new, "organization.settings.members.");
    }

    @Override
    void logJoinedMemberByDefault(OrganizationRoleTable roleTable, UserTable userTable) {
        userActionLogService.organization(LogAction.ORGANIZATION_MEMBER_ADDED.create(OrganizationContext.of(roleTable.getOrganizationId()), userTable.getName() + " joined due to organization creation", ""));
    }

    @Override
    void logMemberRemoval(long principalId, String logEntry) {
        userActionLogService.organization(LogAction.ORGANIZATION_MEMBERS_REMOVED.create(OrganizationContext.of(principalId), logEntry, ""));
    }

    @Override
    void logMemberUpdate(long principalId, String oldState, String newState) {
        userActionLogService.organization(LogAction.ORGANIZATION_MEMBER_ROLES_CHANGED.create(OrganizationContext.of(principalId), newState, oldState));
    }

    @Override
    List<OrganizationRole> invalidRolesToChange() {
        return List.of(OrganizationRole.ORGANIZATION_OWNER);
    }
}
