package io.papermc.hangar.controller.api.v1;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.controller.api.v1.interfaces.IProjectsController;
import io.papermc.hangar.controller.extras.pagination.SorterRegistry;
import io.papermc.hangar.controller.extras.pagination.annotations.ApplicableFilters;
import io.papermc.hangar.controller.extras.pagination.annotations.ApplicableSorters;
import io.papermc.hangar.controller.extras.pagination.filters.projects.ProjectAuthorFilter;
import io.papermc.hangar.controller.extras.pagination.filters.projects.ProjectCategoryFilter;
import io.papermc.hangar.controller.extras.pagination.filters.projects.ProjectLicenseFilter;
import io.papermc.hangar.controller.extras.pagination.filters.projects.ProjectMCVersionFilter;
import io.papermc.hangar.controller.extras.pagination.filters.projects.ProjectPlatformFilter;
import io.papermc.hangar.controller.extras.pagination.filters.projects.ProjectQueryFilter;
import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.User;
import io.papermc.hangar.model.api.project.DayProjectStats;
import io.papermc.hangar.model.api.project.Project;
import io.papermc.hangar.model.api.project.ProjectMember;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.PermissionType;
import io.papermc.hangar.security.annotations.Anyone;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.security.annotations.visibility.VisibilityRequired;
import io.papermc.hangar.service.api.ProjectsApiService;
import java.time.OffsetDateTime;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Anyone
@Controller
@RateLimit(path = "apiprojects", overdraft = 200, refillTokens = 50, greedy = true)
public class ProjectsController extends HangarComponent implements IProjectsController {

    private final ProjectsApiService projectsApiService;

    @Autowired
    public ProjectsController(final ProjectsApiService projectsApiService) {
        this.projectsApiService = projectsApiService;
    }

    @Override
    @VisibilityRequired(type = VisibilityRequired.Type.PROJECT, args = "{#author, #slug}")
    public ResponseEntity<Project> getProject(final String author, final String slug) {
        return ResponseEntity.ok(this.projectsApiService.getProject(author, slug));
    }

    @Override
    @VisibilityRequired(type = VisibilityRequired.Type.PROJECT, args = "{#author, #slug}")
    public ResponseEntity<PaginatedResult<ProjectMember>> getProjectMembers(final String author, final String slug, final @NotNull RequestPagination pagination) {
        return ResponseEntity.ok(this.projectsApiService.getProjectMembers(author, slug, pagination));
    }

    @Override
    @ApplicableFilters({ProjectCategoryFilter.class, ProjectPlatformFilter.class, ProjectAuthorFilter.class, ProjectQueryFilter.class, ProjectLicenseFilter.class, ProjectMCVersionFilter.class})
    @ApplicableSorters({SorterRegistry.VIEWS, SorterRegistry.DOWNLOADS, SorterRegistry.NEWEST, SorterRegistry.STARS, SorterRegistry.UPDATED, SorterRegistry.RECENT_DOWNLOADS, SorterRegistry.RECENT_VIEWS})
    public ResponseEntity<PaginatedResult<Project>> getProjects(final String q, final boolean orderWithRelevance, final @NotNull RequestPagination pagination) {
        return ResponseEntity.ok(this.projectsApiService.getProjects(q, orderWithRelevance, pagination));
    }

    @Override
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.IS_SUBJECT_MEMBER, args = "{#author, #slug}")
    public ResponseEntity<Map<String, DayProjectStats>> getProjectStats(final String author, final String slug, final @NotNull OffsetDateTime fromDate, final @NotNull OffsetDateTime toDate) {
        return ResponseEntity.ok(this.projectsApiService.getProjectStats(author, slug, fromDate, toDate));
    }

    @Override
    @VisibilityRequired(type = VisibilityRequired.Type.PROJECT, args = "{#author, #slug}")
    public ResponseEntity<PaginatedResult<User>> getProjectStargazers(final String author, final String slug, final @NotNull RequestPagination pagination) {
        return ResponseEntity.ok(this.projectsApiService.getProjectStargazers(author, slug, pagination));
    }

    @Override
    @VisibilityRequired(type = VisibilityRequired.Type.PROJECT, args = "{#author, #slug}")
    public ResponseEntity<PaginatedResult<User>> getProjectWatchers(final String author, final String slug, final @NotNull RequestPagination pagination) {
        return ResponseEntity.ok(this.projectsApiService.getProjectWatchers(author, slug, pagination));
    }
}
