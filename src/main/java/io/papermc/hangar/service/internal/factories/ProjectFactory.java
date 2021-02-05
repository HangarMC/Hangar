package io.papermc.hangar.service.internal.factories;

import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.db.projects.ProjectOwner;
import io.papermc.hangar.model.internal.api.requests.projects.NewProject;
import io.papermc.hangar.service.HangarService;
import io.papermc.hangar.service.internal.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

public class ProjectFactory extends HangarService {

    private final ProjectService projectService;

    @Autowired
    public ProjectFactory(ProjectService projectService) {
        this.projectService = projectService;
    }

    public void createProject(NewProject newProject) {
        ProjectOwner projectOwner = projectService.getProjectOwner(newProject.getOwnerId());
        if (projectOwner == null) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "error.project.ownerNotFound");
        }

    }
}
