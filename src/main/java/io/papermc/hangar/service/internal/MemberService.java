package io.papermc.hangar.service.internal;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.members.MembersDAO;
import io.papermc.hangar.db.dao.internal.table.members.OrganizationMembersDAO;
import io.papermc.hangar.db.dao.internal.table.members.ProjectMembersDAO;
import io.papermc.hangar.db.dao.internal.table.roles.OrganizationRoleDAO;
import io.papermc.hangar.db.dao.internal.table.roles.ProjectRoleDAO;
import io.papermc.hangar.db.dao.internal.table.roles.RoleDAO;
import io.papermc.hangar.model.common.roles.OrganizationRole;
import io.papermc.hangar.model.common.roles.ProjectRole;
import io.papermc.hangar.model.common.roles.Role;
import io.papermc.hangar.model.db.members.MemberTable;
import io.papermc.hangar.model.db.members.OrganizationMemberTable;
import io.papermc.hangar.model.db.members.ProjectMemberTable;
import io.papermc.hangar.model.db.roles.ExtendedRoleTable;
import io.papermc.hangar.model.db.roles.OrganizationRoleTable;
import io.papermc.hangar.model.db.roles.ProjectRoleTable;
import io.papermc.hangar.model.db.roles.RoleTable;
import io.papermc.hangar.service.HangarService;
import io.papermc.hangar.service.internal.roles.OrganizationRoleService;
import io.papermc.hangar.service.internal.roles.ProjectRoleService;
import io.papermc.hangar.service.internal.roles.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.BiFunction;

public abstract class MemberService<
        R extends Role<RT>,
        RT extends ExtendedRoleTable<R>,
        RD extends RoleDAO<RT>,
        S extends RoleService<RT, R, RD>,
        MD extends MembersDAO<MT>,
        MT extends MemberTable
        > extends HangarService {

    protected final S roleService;
    protected final MD membersDao;

    protected MemberService(S roleService, MD membersDao) {
        this.roleService = roleService;
        this.membersDao = membersDao;
    }

    public RT addMember(long principalId, RT newRoleTable, BiFunction<Long, Long, MT> constructor) {
        MT existingMember = membersDao.getMemberTable(principalId, newRoleTable.getUserId());
        if (existingMember != null) {
            throw new IllegalArgumentException(getClass().getName() + " One user cannot be a member twice");
        }
        RT roleTable = roleService.addRole(newRoleTable);
        membersDao.insert(constructor.apply(roleTable.getUserId(), principalId));
        return roleTable;
    }

    public void removeMember(RT roleTable) {
        membersDao.delete(roleTable.getPrincipalId(), roleTable.getUserId());
        roleService.deleteRole(roleTable);
    }

    /**
     * Forwarding method to {@link RoleService#updateRole(RoleTable)}
     */
    public void updateRole(RT roleTable) {
        roleService.updateRole(roleTable);
    }


    @Service
    public static class ProjectMemberService extends MemberService<
            ProjectRole,
            ProjectRoleTable,
            ProjectRoleDAO,
            ProjectRoleService,
            ProjectMembersDAO,
            ProjectMemberTable
            > {

        @Autowired
        public ProjectMemberService(ProjectRoleService projectRoleService, HangarDao<ProjectMembersDAO> projectMembersDAO) {
            super(projectRoleService, projectMembersDAO.get());
        }
    }

    @Service
    public static class OrganizationMemberService extends MemberService<
            OrganizationRole,
            OrganizationRoleTable,
            OrganizationRoleDAO,
            OrganizationRoleService,
            OrganizationMembersDAO,
            OrganizationMemberTable
            > {

        @Autowired
        public OrganizationMemberService(OrganizationRoleService roleService, HangarDao<OrganizationMembersDAO> organizationMembersDAO) {
            super(roleService, organizationMembersDAO.get());
        }
    }
}
