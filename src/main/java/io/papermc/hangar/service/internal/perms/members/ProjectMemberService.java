package io.papermc.hangar.service.internal.perms.members;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.members.ProjectMembersDAO;
import io.papermc.hangar.db.dao.internal.table.roles.ProjectRolesDAO;
import io.papermc.hangar.model.common.roles.ProjectRole;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.members.ProjectMemberTable;
import io.papermc.hangar.model.db.roles.ProjectRoleTable;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.ProjectContext;
import io.papermc.hangar.service.internal.perms.roles.ProjectRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectMemberService extends MemberService<
        ProjectRole,
        ProjectRoleTable,
        ProjectRolesDAO,
        ProjectRoleService,
        ProjectMembersDAO,
        ProjectMemberTable
        > {

    @Autowired
    public ProjectMemberService(ProjectRoleService projectRoleService, HangarDao<ProjectMembersDAO> projectMembersDAO) {
        super(projectRoleService, projectMembersDAO.get(), ProjectMemberTable::new, "project.settings.error.members.");
    }

    @Override
    void logJoinedMemberByDefault(ProjectRoleTable roleTable, UserTable userTable) {
        userActionLogService.project(LogAction.PROJECT_MEMBER_ADDED.create(ProjectContext.of(roleTable.getProjectId()), userTable.getName() + " joined due to project creation", ""));
    }

    @Override
    void logMemberRemoval(long principalId, String logEntry) {
        userActionLogService.project(LogAction.PROJECT_MEMBERS_REMOVED.create(ProjectContext.of(principalId), logEntry, ""));
    }

    @Override
    void logMemberUpdate(long principalId, String oldState, String newState) {
        userActionLogService.project(LogAction.PROJECT_MEMBER_ROLES_CHANGED.create(ProjectContext.of(principalId), newState, oldState));
    }

    @Override
    List<ProjectRole> invalidRolesToChange() {
        return List.of(ProjectRole.PROJECT_OWNER);
    }
}
