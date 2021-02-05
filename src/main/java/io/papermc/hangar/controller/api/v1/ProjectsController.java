package io.papermc.hangar.controller.api.v1;

import io.papermc.hangar.controller.HangarController;
import io.papermc.hangar.controller.api.v1.interfaces.IProjectsController;
import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.project.Project;
import io.papermc.hangar.model.api.project.ProjectMember;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.papermc.hangar.model.common.projects.Category;
import io.papermc.hangar.modelold.generated.ProjectSortingStrategy;
import io.papermc.hangar.service.api.ProjectsApiService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class ProjectsController extends HangarController implements IProjectsController {

    private final ProjectsApiService projectsApiService;

    @Autowired
    public ProjectsController(ProjectsApiService projectsApiService) {
        this.projectsApiService = projectsApiService;
    }

    @Override
    public ResponseEntity<Project> getProject(String author, String slug) {
        return ResponseEntity.ok(projectsApiService.getProject(author, slug));
    }

    @Override
    public ResponseEntity<PaginatedResult<ProjectMember>> getProjectMembers(String author, String slug, @NotNull RequestPagination pagination) {
        return ResponseEntity.ok(projectsApiService.getProjectMembers(author, slug, pagination));
    }

    @Override
    public ResponseEntity<PaginatedResult<Project>> getProjects(String q, List<Category> categories, List<String> tags, String owner, ProjectSortingStrategy sort, boolean orderWithRelevance, @NotNull RequestPagination pagination) {
        return ResponseEntity.ok(projectsApiService.getProjects(q, categories, tags, owner, sort, orderWithRelevance, pagination));
    }
}
