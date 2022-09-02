package io.papermc.hangar.controller.api.v1;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.controller.api.v1.interfaces.IProjectsController;
import io.papermc.hangar.controller.extras.pagination.SorterRegistry;
import io.papermc.hangar.controller.extras.pagination.annotations.ApplicableFilters;
import io.papermc.hangar.controller.extras.pagination.annotations.ApplicableSorters;
import io.papermc.hangar.controller.extras.pagination.filters.projects.*;
import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.User;
import io.papermc.hangar.model.api.project.DayProjectStats;
import io.papermc.hangar.model.api.project.Project;
import io.papermc.hangar.model.api.project.ProjectMember;
import io.papermc.hangar.model.api.project.ProjectSortingStrategy;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.PermissionType;
import io.papermc.hangar.security.annotations.Anyone;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.security.annotations.visibility.VisibilityRequired;
import io.papermc.hangar.security.annotations.visibility.VisibilityRequired.Type;
import io.papermc.hangar.service.api.ProjectsApiService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.time.OffsetDateTime;
import java.util.Map;

@Anyone
@Controller
@RateLimit(path = "apiprojects", overdraft = 200, refillTokens = 50, greedy = true)
public class ProjectsController extends HangarComponent implements IProjectsController {

    private final ProjectsApiService projectsApiService;

    @Autowired
    public ProjectsController(ProjectsApiService projectsApiService) {
        this.projectsApiService = projectsApiService;
    }

    @Override
    @VisibilityRequired(type = Type.PROJECT, args = "{#author, #slug}")
    public ResponseEntity<Project> getProject(String author, String slug) {
        return ResponseEntity.ok(projectsApiService.getProject(author, slug));
    }

    @Override
    @VisibilityRequired(type = Type.PROJECT, args = "{#author, #slug}")
    public ResponseEntity<PaginatedResult<ProjectMember>> getProjectMembers(String author, String slug, @NotNull RequestPagination pagination) {
        return ResponseEntity.ok(projectsApiService.getProjectMembers(author, slug, pagination));
    }

    @Override
    @ApplicableFilters({ProjectCategoryFilter.class, ProjectPlatformFilter.class, ProjectAuthorFilter.class, ProjectQueryFilter.class, ProjectLicenseFilter.class, ProjectMCVersionFilter.class})
    @ApplicableSorters({SorterRegistry.VIEWS, SorterRegistry.DOWNLOADS, SorterRegistry.NEWEST, SorterRegistry.STARS, SorterRegistry.UPDATED, SorterRegistry.RECENT_DOWNLOADS, SorterRegistry.RECENT_VIEWS})
    public ResponseEntity<PaginatedResult<Project>> getProjects(String q, boolean orderWithRelevance, @NotNull RequestPagination pagination) {
        return ResponseEntity.ok(projectsApiService.getProjects(q, orderWithRelevance, pagination));
    }

    @Override
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.IS_SUBJECT_MEMBER, args = "{#author, #slug}")
    public ResponseEntity<Map<String, DayProjectStats>> getProjectStats(String author, String slug, @NotNull OffsetDateTime fromDate, @NotNull OffsetDateTime toDate) {
        return ResponseEntity.ok(projectsApiService.getProjectStats(author, slug, fromDate, toDate));
    }

    @Override
    @VisibilityRequired(type = Type.PROJECT, args = "{#author, #slug}")
    public ResponseEntity<PaginatedResult<User>> getProjectStargazers(String author, String slug, @NotNull RequestPagination pagination) {
        return ResponseEntity.ok(projectsApiService.getProjectStargazers(author, slug, pagination));
    }

    @Override
    @VisibilityRequired(type = Type.PROJECT, args = "{#author, #slug}")
    public ResponseEntity<PaginatedResult<User>> getProjectWatchers(String author, String slug, @NotNull RequestPagination pagination) {
        return ResponseEntity.ok(projectsApiService.getProjectWatchers(author, slug, pagination));
    }
}
