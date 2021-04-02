package io.papermc.hangar.service.internal.users.invites;

import io.papermc.hangar.model.common.roles.ProjectRole;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.roles.ProjectRoleTable;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.ProjectContext;
import io.papermc.hangar.model.internal.user.notifications.HangarInvite.HangarProjectInvite;
import io.papermc.hangar.service.internal.perms.members.ProjectMemberService;
import io.papermc.hangar.service.internal.perms.roles.ProjectRoleService;
import io.papermc.hangar.service.internal.users.notifications.JoinableNotificationService.ProjectNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ProjectInviteService extends InviteService<ProjectRole, ProjectRoleTable, ProjectTable> {

    @Autowired
    public ProjectInviteService(ProjectRoleService roleService, ProjectMemberService memberService, ProjectNotificationService projectNotificationService) {
        super(roleService, memberService, projectNotificationService, "project.settings.error.members.");
    }

    public List<HangarProjectInvite> getProjectInvites() {
        return hangarNotificationsDAO.get().getProjectInvites(getHangarPrincipal().getId());
    }

    @Override
    void logInvitesSent(long principalId, String log) {
        userActionLogService.project(LogAction.PROJECT_INVITES_SENT.create(ProjectContext.of(principalId), log, ""));
    }

    @Override
    void logInviteAccepted(ProjectRoleTable roleTable, UserTable userTable) {
        userActionLogService.project(LogAction.PROJECT_MEMBER_ADDED.create(ProjectContext.of(roleTable.getProjectId()), userTable.getName() + " accepted an invite for " + roleTable.getRole().getTitle(), roleTable.getCreatedAt().format(DateTimeFormatter.RFC_1123_DATE_TIME)));
    }

    @Override
    void logInviteUnaccepted(ProjectRoleTable roleTable, UserTable userTable) {
        userActionLogService.project(LogAction.PROJECT_INVITE_UNACCEPTED.create(ProjectContext.of(roleTable.getProjectId()), userTable.getName() + " unaccepted an invite for " + roleTable.getRole().getTitle(), roleTable.getCreatedAt().format(DateTimeFormatter.RFC_1123_DATE_TIME)));
    }

    @Override
    void logInviteDeclined(ProjectRoleTable roleTable, UserTable userTable) {
        userActionLogService.project(LogAction.PROJECT_INVITE_DECLINED.create(ProjectContext.of(roleTable.getProjectId()), userTable.getName() + " declined an invite for " + roleTable.getRole().getTitle(), roleTable.getCreatedAt().format(DateTimeFormatter.RFC_1123_DATE_TIME)));
    }
}
