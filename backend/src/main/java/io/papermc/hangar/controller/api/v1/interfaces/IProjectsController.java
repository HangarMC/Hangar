package io.papermc.hangar.controller.api.v1.interfaces;

import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.User;
import io.papermc.hangar.model.api.project.DayProjectStats;
import io.papermc.hangar.model.api.project.Project;
import io.papermc.hangar.model.api.project.ProjectMember;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.OffsetDateTime;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Projects")
@RequestMapping(path = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
public interface IProjectsController {

    @Operation(
        summary = "Returns info on a specific project",
        operationId = "getProject",
        description = "Returns info on a specific project. Requires the `view_public_info` permission.",
        security = @SecurityRequirement(name = "HangarAuth", scopes = "view_public_info"),
        tags = "Projects"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired"),
        @ApiResponse(responseCode = "403", description = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/projects/{slug}")
    ResponseEntity<Project> getProject(@Parameter(description = "The slug of the project to return") @PathVariable String slug);

    @GetMapping("/projects/{author}/{slug}")
    @Deprecated(forRemoval = true)
    default ResponseEntity<Project> getProject(@Parameter(description = "The author of the project to return") @PathVariable String author,
                                       @Parameter(description = "The slug of the project to return") @PathVariable String slug) {
        return this.getProject(slug);
    }

    @Operation(
        summary = "Returns the members of a project",
        operationId = "getProjectMembers",
        description = "Returns the members of a project. Requires the `view_public_info` permission.",
        security = @SecurityRequirement(name = "HangarAuth", scopes = "view_public_info"),
        tags = "Projects"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired"),
        @ApiResponse(responseCode = "403", description = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/projects/{slug}/members")
    ResponseEntity<PaginatedResult<ProjectMember>> getProjectMembers(
        @Parameter(description = "The slug of the project to return members for") @PathVariable("slug") String slug,
        @Parameter(description = "Pagination information") @NotNull RequestPagination pagination
    );

    @GetMapping("/projects/{author}/{slug}/members")
    @Deprecated(forRemoval = true)
    default ResponseEntity<PaginatedResult<ProjectMember>> getProjectMembers(
        @Parameter(description = "The author of the project to return members for") @PathVariable("author") String author,
        @Parameter(description = "The slug of the project to return members for") @PathVariable("slug") String slug,
        @Parameter(description = "Pagination information") @NotNull RequestPagination pagination
    ) {
        return this.getProjectMembers(slug, pagination);
    }

    @Operation(
        summary = "Searches the projects on Hangar",
        operationId = "getProjects",
        description = "Searches all the projects on Hangar, or for a single user. Requires the `view_public_info` permission.",
        security = @SecurityRequirement(name = "HangarAuth", scopes = "view_public_info"),
        tags = "Projects"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired"),
        @ApiResponse(responseCode = "403", description = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/projects")
    ResponseEntity<PaginatedResult<Project>> getProjects(
            @Parameter(description = "Whether to prioritize the project with an exact name match if present") @RequestParam(defaultValue = "true", required = false) boolean prioritizeExactMatch,
            @Parameter(description = "Pagination information") @NotNull RequestPagination pagination
    );

    @Operation(
        summary = "Returns the stats for a project",
        operationId = "showProjectStats",
        description = "Returns the stats (downloads and views) for a project per day for a certain date range. Requires the `is_subject_member` permission.",
        security = @SecurityRequirement(name = "HangarAuth", scopes = "is_subject_member"),
        tags = "Projects"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired"),
        @ApiResponse(responseCode = "403", description = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/projects/{slug}/stats")
    ResponseEntity<Map<String, DayProjectStats>> getProjectStats(@Parameter(description = "The slug of the project to return stats for") @PathVariable String slug,
                                                                 @NotNull @Parameter(description = "The first date to include in the result", required = true) @RequestParam OffsetDateTime fromDate,
                                                                 @NotNull @Parameter(description = "The last date to include in the result", required = true) @RequestParam OffsetDateTime toDate
    );

    @GetMapping("/projects/{author}/{slug}/stats")
    @Deprecated(forRemoval = true)
    default ResponseEntity<Map<String, DayProjectStats>> getProjectStats(@Parameter(description = "The author of the project to return stats for") @PathVariable String author,
                                                                 @Parameter(description = "The slug of the project to return stats for") @PathVariable String slug,
                                                                 @NotNull @Parameter(description = "The first date to include in the result", required = true) @RequestParam OffsetDateTime fromDate,
                                                                 @NotNull @Parameter(description = "The last date to include in the result", required = true) @RequestParam OffsetDateTime toDate
    ) {
        return this.getProjectStats(slug, fromDate, toDate);
    }

    @Operation(
        summary = "Returns the stargazers of a project",
        operationId = "getProjectStargazers",
        description = "Returns the stargazers of a project. Requires the `view_public_info` permission.",
        security = @SecurityRequirement(name = "HangarAuth", scopes = "view_public_info"),
        tags = "Projects"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired"),
        @ApiResponse(responseCode = "403", description = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/projects/{slug}/stargazers")
    ResponseEntity<PaginatedResult<User>> getProjectStargazers(
        @Parameter(description = "The slug of the project to return stargazers for") @PathVariable("slug") String slug,
        @Parameter(description = "Pagination information") @NotNull RequestPagination pagination
    );

    @GetMapping("/projects/{author}/{slug}/stargazers")
    @Deprecated(forRemoval = true)
    default ResponseEntity<PaginatedResult<User>> getProjectStargazers(
        @Parameter(description = "The author of the project to return stargazers for") @PathVariable("author") String author,
        @Parameter(description = "The slug of the project to return stargazers for") @PathVariable("slug") String slug,
        @Parameter(description = "Pagination information") @NotNull RequestPagination pagination
    ) {
        return this.getProjectStargazers(slug, pagination);
    }

    @Operation(
        summary = "Returns the watchers of a project",
        operationId = "getProjectWatchers",
        description = "Returns the watchers of a project. Requires the `view_public_info` permission.",
        security = @SecurityRequirement(name = "HangarAuth", scopes = "view_public_info"),
        tags = "Projects"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired"),
        @ApiResponse(responseCode = "403", description = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/projects/{slug}/watchers")
    ResponseEntity<PaginatedResult<User>> getProjectWatchers(
        @Parameter(description = "The slug of the project to return watchers for") @PathVariable("slug") String slug,
        @Parameter(description = "Pagination information") @NotNull RequestPagination pagination
    );

    @GetMapping("/projects/{author}/{slug}/watchers")
    @Deprecated(forRemoval = true)
    default ResponseEntity<PaginatedResult<User>> getProjectWatchers(
        @Parameter(description = "The author of the project to return watchers for") @PathVariable("author") String author,
        @Parameter(description = "The slug of the project to return watchers for") @PathVariable("slug") String slug,
        @Parameter(description = "Pagination information") @NotNull RequestPagination pagination
    ) {
        return this.getProjectWatchers(slug, pagination);
    }
}
