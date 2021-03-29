package io.papermc.hangar.service.internal.users.invites;

import io.papermc.hangar.model.common.roles.ProjectRole;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.roles.ProjectRoleTable;
import io.papermc.hangar.model.internal.api.requests.EditMembersForm.Member;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.ProjectContext;
import io.papermc.hangar.model.internal.user.notifications.HangarInvite.HangarProjectInvite;
import io.papermc.hangar.service.internal.perms.members.ProjectMemberService;
import io.papermc.hangar.service.internal.perms.roles.ProjectRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ProjectInviteService extends InviteService<ProjectRole, ProjectRoleTable> {

    @Autowired
    public ProjectInviteService(ProjectRoleService roleService, ProjectMemberService memberService) {
        super(roleService, memberService, "project.settings.error.members.");
    }

    public List<HangarProjectInvite> getProjectInvites() {
        return hangarNotificationsDAO.get().getProjectInvites(getHangarPrincipal().getId());
    }

    @Override
    void notifyNewInvites(Member<ProjectRole> invitee, long userId, long principalId, String principalName) {
        notificationService.notifyNewProjectInvite(invitee, userId, principalId, principalName);
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
