package io.papermc.hangar.service.internal.users.invites;

import io.papermc.hangar.model.common.roles.ProjectRole;
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
import java.util.List;

@Service
public class ProjectInviteService extends InviteService<ProjectContext, ProjectRole, ProjectRoleTable, ProjectTable> {

    @Autowired
    public ProjectInviteService(ProjectRoleService roleService, ProjectMemberService memberService, ProjectNotificationService projectNotificationService) {
        super(roleService, memberService, projectNotificationService, "project.settings.error.members.");
    }

    public List<HangarProjectInvite> getProjectInvites() {
        return hangarNotificationsDAO.getProjectInvites(getHangarPrincipal().getId());
    }

    @Override
    LogAction<ProjectContext> getInviteSentAction() {
        return LogAction.PROJECT_INVITES_SENT;
    }

    @Override
    LogAction<ProjectContext> getInviteAcceptAction() {
        return LogAction.PROJECT_MEMBER_ADDED;
    }

    @Override
    LogAction<ProjectContext> getInviteDeclineAction() {
        return LogAction.PROJECT_INVITE_DECLINED;
    }
}
