package io.papermc.hangar.controller.api.v1;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.time.OffsetDateTime;
import java.util.Map;

import io.papermc.hangar.controller.HangarController;
import io.papermc.hangar.controller.api.v1.interfaces.IProjectsController;
import io.papermc.hangar.controller.extras.pagination.annotations.ApplicableFilters;
import io.papermc.hangar.controller.extras.pagination.filters.projects.ProjectAuthorFilter;
import io.papermc.hangar.controller.extras.pagination.filters.projects.ProjectCategoryFilter;
import io.papermc.hangar.controller.extras.pagination.filters.projects.ProjectQueryFilter;
import io.papermc.hangar.controller.extras.pagination.filters.projects.ProjectTagFilter;
import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.User;
import io.papermc.hangar.model.api.project.DayProjectStats;
import io.papermc.hangar.model.api.project.Project;
import io.papermc.hangar.model.api.project.ProjectMember;
import io.papermc.hangar.model.api.project.ProjectSortingStrategy;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.papermc.hangar.service.api.ProjectsApiService;

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
    @ApplicableFilters({ProjectCategoryFilter.class, ProjectAuthorFilter.class, ProjectQueryFilter.class, ProjectTagFilter.class})
    public ResponseEntity<PaginatedResult<Project>> getProjects(String q, ProjectSortingStrategy sort, boolean orderWithRelevance, @NotNull RequestPagination pagination) {
        return ResponseEntity.ok(projectsApiService.getProjects(q, sort, orderWithRelevance, pagination));
    }

    @Override
    public ResponseEntity<Map<String, DayProjectStats>> getProjectStats(String author, String slug, @NotNull OffsetDateTime fromDate, @NotNull OffsetDateTime toDate) {
        return ResponseEntity.ok(projectsApiService.getProjectStats(author, slug, fromDate, toDate));
    }

    @Override
    public ResponseEntity<PaginatedResult<User>> getProjectStargazers(String author, String slug, @NotNull RequestPagination pagination) {
        return ResponseEntity.ok(projectsApiService.getProjectStargazers(author, slug, pagination));
    }

    @Override
    public ResponseEntity<PaginatedResult<User>> getProjectWatchers(String author, String slug, @NotNull RequestPagination pagination) {
        return ResponseEntity.ok(projectsApiService.getProjectWatchers(author, slug, pagination));
    }
}
