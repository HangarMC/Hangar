package me.minidigger.hangar.security.voters;

import me.minidigger.hangar.service.PermissionService;
import me.minidigger.hangar.service.project.ProjectService;

public class ProjectPermissionVoter extends HangarPermissionVoter {

    public ProjectPermissionVoter(ProjectService projectService, PermissionService permissionService) {
        super(hangarAuth -> permissionService.getProjectPermissions(hangarAuth.getUserId(), hangarAuth.getProjectId()), hangarAuth -> hangarAuth.getProjectId() == null);
    }
}
