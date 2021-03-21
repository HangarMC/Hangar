package io.papermc.hangar.service.internal.roles;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.members.MembersDAO;
import io.papermc.hangar.db.dao.internal.table.members.OrganizationMembersDAO;
import io.papermc.hangar.db.dao.internal.table.members.ProjectMembersDAO;
import io.papermc.hangar.db.dao.internal.table.roles.IRolesDAO;
import io.papermc.hangar.db.dao.internal.table.roles.OrganizationRolesDAO;
import io.papermc.hangar.db.dao.internal.table.roles.ProjectRolesDAO;
import io.papermc.hangar.model.common.roles.OrganizationRole;
import io.papermc.hangar.model.common.roles.ProjectRole;
import io.papermc.hangar.model.common.roles.Role;
import io.papermc.hangar.model.db.members.MemberTable;
import io.papermc.hangar.model.db.members.OrganizationMemberTable;
import io.papermc.hangar.model.db.members.ProjectMemberTable;
import io.papermc.hangar.model.db.roles.ExtendedRoleTable;
import io.papermc.hangar.model.db.roles.IRoleTable;
import io.papermc.hangar.model.db.roles.OrganizationRoleTable;
import io.papermc.hangar.model.db.roles.ProjectRoleTable;
import io.papermc.hangar.service.HangarService;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.BiFunction;

public abstract class MemberService<
        R extends Role<RT>,
        RT extends ExtendedRoleTable<R>,
        RD extends IRolesDAO<RT>,
        S extends RoleService<RT, R, RD>,
        MD extends MembersDAO<MT>,
        MT extends MemberTable
        > extends HangarService {

    protected final S roleService;
    protected final MD membersDao;
    protected final BiFunction<Long, Long, MT> constructor;

    protected MemberService(S roleService, MD membersDao, BiFunction<Long, Long, MT> constructor) {
        this.roleService = roleService;
        this.membersDao = membersDao;
        this.constructor = constructor;
    }

    @Nullable
    public RT addMember(long principalId, RT newRoleTable) {
        MT existingMember = membersDao.getMemberTable(principalId, newRoleTable.getUserId());
        if (existingMember != null) {
            return null;
        }
        // TODO should this fail loudly?
//        if (existingMember != null) {
//            throw new IllegalArgumentException(getClass().getName() + " One user cannot be a member twice");
//        }
        RT roleTable = roleService.addRole(newRoleTable);
        membersDao.insert(constructor.apply(roleTable.getUserId(), principalId));
        return roleTable;
    }

    public void removeMember(RT roleTable) {
        membersDao.delete(roleTable.getPrincipalId(), roleTable.getUserId());
        roleService.deleteRole(roleTable);
    }

    /**
     * Forwarding method to {@link RoleService#updateRole(IRoleTable)}
     */
    public void updateRole(RT roleTable) {
        roleService.updateRole(roleTable);
    }


    @Service
    public static class ProjectMemberService extends MemberService<
            ProjectRole,
            ProjectRoleTable,
            ProjectRolesDAO,
            RoleService.ProjectRoleService,
            ProjectMembersDAO,
            ProjectMemberTable
            > {

        @Autowired
        public ProjectMemberService(RoleService.ProjectRoleService projectRoleService, HangarDao<ProjectMembersDAO> projectMembersDAO) {
            super(projectRoleService, projectMembersDAO.get(), ProjectMemberTable::new);
        }
    }

    @Service
    public static class OrganizationMemberService extends MemberService<
            OrganizationRole,
            OrganizationRoleTable,
            OrganizationRolesDAO,
            RoleService.OrganizationRoleService,
            OrganizationMembersDAO,
            OrganizationMemberTable
            > {

        @Autowired
        public OrganizationMemberService(RoleService.OrganizationRoleService roleService, HangarDao<OrganizationMembersDAO> organizationMembersDAO) {
            super(roleService, organizationMembersDAO.get(), OrganizationMemberTable::new);
        }
    }
}
