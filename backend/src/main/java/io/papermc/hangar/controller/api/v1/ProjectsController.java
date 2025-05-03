package io.papermc.hangar.controller.api.v1;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.controller.api.v1.interfaces.IProjectsController;
import io.papermc.hangar.controller.extras.pagination.PaginationType;
import io.papermc.hangar.controller.extras.pagination.SorterRegistry;
import io.papermc.hangar.controller.extras.pagination.annotations.ApplicableFilters;
import io.papermc.hangar.controller.extras.pagination.annotations.ApplicableSorters;
import io.papermc.hangar.controller.extras.pagination.annotations.ConfigurePagination;
import io.papermc.hangar.controller.extras.pagination.filters.projects.ProjectAuthorFilter;
import io.papermc.hangar.controller.extras.pagination.filters.projects.ProjectCategoryFilter;
import io.papermc.hangar.controller.extras.pagination.filters.projects.ProjectLicenseFilter;
import io.papermc.hangar.controller.extras.pagination.filters.projects.ProjectPlatformVersionFilter;
import io.papermc.hangar.controller.extras.pagination.filters.projects.ProjectMemberFilter;
import io.papermc.hangar.controller.extras.pagination.filters.projects.ProjectPlatformFilter;
import io.papermc.hangar.controller.extras.pagination.filters.projects.ProjectQueryFilter;
import io.papermc.hangar.controller.extras.pagination.filters.projects.ProjectTagFilter;
import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.User;
import io.papermc.hangar.model.api.project.DayProjectStats;
import io.papermc.hangar.model.api.project.Project;
import io.papermc.hangar.model.api.project.ProjectMember;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.PermissionType;
import io.papermc.hangar.model.db.projects.ProjectTable;
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
@RateLimit(path = "apiprojects", greedy = true)
public class ProjectsController extends HangarComponent implements IProjectsController {

    private final ProjectsApiService projectsApiService;

    @Autowired
    public ProjectsController(final ProjectsApiService projectsApiService) {
        this.projectsApiService = projectsApiService;
    }

    @Override
    @VisibilityRequired(type = VisibilityRequired.Type.PROJECT, args = "{#project}")
    public ResponseEntity<Project> getProject(final ProjectTable project) {
        return ResponseEntity.ok(this.projectsApiService.getProject(project.getId()));
    }

    @Override
    public ResponseEntity<Project> getProjectFromVersionHash(final String hash) {
        return ResponseEntity.ok(this.projectsApiService.getProjectForVersionHash(hash));
    }

    @Override
    @VisibilityRequired(type = VisibilityRequired.Type.PROJECT, args = "{#project}")
    public ResponseEntity<PaginatedResult<ProjectMember>> getProjectMembers(final ProjectTable project, final @NotNull RequestPagination pagination) {
        return ResponseEntity.ok(this.projectsApiService.getProjectMembers(project, pagination));
    }

    @Override
    @ApplicableFilters({ProjectCategoryFilter.class, ProjectPlatformFilter.class, ProjectAuthorFilter.class, ProjectQueryFilter.class, ProjectLicenseFilter.class, ProjectPlatformVersionFilter.class, ProjectTagFilter.class, ProjectMemberFilter.class})
    @ApplicableSorters({SorterRegistry.VIEWS, SorterRegistry.DOWNLOADS, SorterRegistry.NEWEST, SorterRegistry.STARS, SorterRegistry.UPDATED, SorterRegistry.RECENT_DOWNLOADS, SorterRegistry.RECENT_VIEWS, SorterRegistry.SLUG})
    public ResponseEntity<PaginatedResult<Project>> getProjects(final boolean prioritizeExactMatch, final @ConfigurePagination(paginationType = PaginationType.MEILI) @NotNull RequestPagination pagination) {
        return ResponseEntity.ok(this.projectsApiService.getProjects(pagination));
    }

    @Override
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.IS_SUBJECT_MEMBER, args = "{#project}")
    public ResponseEntity<Map<String, DayProjectStats>> getProjectStats(final ProjectTable project, final @NotNull OffsetDateTime fromDate, final @NotNull OffsetDateTime toDate) {
        return ResponseEntity.ok(this.projectsApiService.getProjectStats(project, fromDate, toDate));
    }

    @Override
    @VisibilityRequired(type = VisibilityRequired.Type.PROJECT, args = "{#project}")
    public ResponseEntity<PaginatedResult<User>> getProjectStargazers(final ProjectTable project, final @NotNull RequestPagination pagination) {
        return ResponseEntity.ok(this.projectsApiService.getProjectStargazers(project, pagination));
    }

    @Override
    @VisibilityRequired(type = VisibilityRequired.Type.PROJECT, args = "{#project}")
    public ResponseEntity<PaginatedResult<User>> getProjectWatchers(final ProjectTable project, final @NotNull RequestPagination pagination) {
        return ResponseEntity.ok(this.projectsApiService.getProjectWatchers(project, pagination));
    }
}
