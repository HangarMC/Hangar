package io.papermc.hangar.service.internal.perms.members;

import io.papermc.hangar.db.dao.internal.table.members.ProjectMembersDAO;
import io.papermc.hangar.db.dao.internal.table.roles.ProjectRolesDAO;
import io.papermc.hangar.model.common.roles.ProjectRole;
import io.papermc.hangar.model.db.members.ProjectMemberTable;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.roles.ProjectRoleTable;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.ProjectContext;
import io.papermc.hangar.service.internal.perms.roles.ProjectRoleService;
import io.papermc.hangar.service.internal.users.notifications.JoinableNotificationService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectMemberService extends MemberService<
    ProjectContext,
    ProjectRole,
    ProjectRoleTable,
    ProjectRolesDAO,
    ProjectRoleService,
    ProjectTable,
    JoinableNotificationService.ProjectNotificationService,
    ProjectMembersDAO,
    ProjectMemberTable
    > {

    @Autowired
    public ProjectMemberService(final ProjectRoleService projectRoleService, final ProjectMembersDAO projectMembersDAO, final JoinableNotificationService.ProjectNotificationService projectNotificationService) {
        super(projectRoleService, projectMembersDAO, projectNotificationService, ProjectMemberTable::new, "project.settings.error.members.", LogAction.PROJECT_MEMBER_ADDED, LogAction.PROJECT_MEMBERS_REMOVED, LogAction.PROJECT_MEMBER_ROLES_CHANGED);
    }

    @Override
    List<ProjectRole> invalidRolesToChange() {
        return List.of(ProjectRole.PROJECT_OWNER);
    }
}
