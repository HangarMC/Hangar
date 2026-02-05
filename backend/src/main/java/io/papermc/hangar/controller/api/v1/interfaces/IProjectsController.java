package io.papermc.hangar.controller.api.v1.interfaces;

import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.User;
import io.papermc.hangar.model.api.project.DayProjectStats;
import io.papermc.hangar.model.api.project.Project;
import io.papermc.hangar.model.api.project.ProjectMember;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.papermc.hangar.model.db.projects.ProjectTable;
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
    @GetMapping("/projects/{slugOrId}")
    ResponseEntity<Project> getProject(@Parameter(description = "The slug or id or id of the project to return") @PathVariable("slugOrId") ProjectTable project);

    @GetMapping("/projects/{author}/{slugOrId}")
    @Deprecated(forRemoval = true)
    ResponseEntity<Project> getProject(@Parameter(description = "The author of the project to return") @PathVariable String author,
                                       @Parameter(description = "The slug or id of the project to return") @PathVariable("slugOrId") ProjectTable project);

    @Operation(
        summary = "Returns project of the first version that matches the given file hash (SHA-256)",
        operationId = "projectByVersionHash",
        security = @SecurityRequirement(name = "HangarAuth", scopes = "view_public_info"),
        tags = "Projects"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired"),
        @ApiResponse(responseCode = "403", description = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/versions/hash/{hash}")
    ResponseEntity<Project> getProjectFromVersionHash(@Parameter(description = "The SHA-256 hash of the version") @PathVariable String hash);

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
    @GetMapping("/projects/{slugOrId}/members")
    ResponseEntity<PaginatedResult<ProjectMember>> getProjectMembers(
        @Parameter(description = "The slug or id of the project to return members for") @PathVariable("slugOrId") ProjectTable project,
        @Parameter(description = "Pagination information") @NotNull RequestPagination pagination
    );

    @GetMapping("/projects/{author}/{slugOrId}/members")
    @Deprecated(forRemoval = true)
    ResponseEntity<PaginatedResult<ProjectMember>> getProjectMembers(
        @Parameter(description = "The author of the project to return members for") @PathVariable("author") String author,
        @Parameter(description = "The slug or id of the project to return members for") @PathVariable("slugOrId") ProjectTable project,
        @Parameter(description = "Pagination information") @NotNull RequestPagination pagination
    );

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
        @Deprecated(forRemoval = true) @Parameter(description = "Whether to prioritize the project with an exact name match if present") @RequestParam(defaultValue = "true", required = false) boolean prioritizeExactMatch,
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
    @GetMapping("/projects/{slugOrId}/stats")
    ResponseEntity<Map<String, DayProjectStats>> getProjectStats(@Parameter(description = "The slug or id of the project to return stats for") @PathVariable("slugOrId") ProjectTable project,
                                                                 @NotNull @Parameter(description = "The first date to include in the result", required = true) @RequestParam OffsetDateTime fromDate,
                                                                 @NotNull @Parameter(description = "The last date to include in the result", required = true) @RequestParam OffsetDateTime toDate
    );

    @GetMapping("/projects/{author}/{slugOrId}/stats")
    @Deprecated(forRemoval = true)
    ResponseEntity<Map<String, DayProjectStats>> getProjectStats(@Parameter(description = "The author of the project to return stats for") @PathVariable String author,
                                                                 @Parameter(description = "The slug or id of the project to return stats for") @PathVariable("slugOrId") ProjectTable project,
                                                                 @NotNull @Parameter(description = "The first date to include in the result", required = true) @RequestParam OffsetDateTime fromDate,
                                                                 @NotNull @Parameter(description = "The last date to include in the result", required = true) @RequestParam OffsetDateTime toDate
    );

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
    @GetMapping("/projects/{slugOrId}/stargazers")
    ResponseEntity<PaginatedResult<User>> getProjectStargazers(
        @Parameter(description = "The slug or id of the project to return stargazers for") @PathVariable("slugOrId") ProjectTable project,
        @Parameter(description = "Pagination information") @NotNull RequestPagination pagination
    );

    @GetMapping("/projects/{author}/{slugOrId}/stargazers")
    @Deprecated(forRemoval = true)
    ResponseEntity<PaginatedResult<User>> getProjectStargazers(
        @Parameter(description = "The author of the project to return stargazers for") @PathVariable("author") String author,
        @Parameter(description = "The slug or id of the project to return stargazers for") @PathVariable("slugOrId") ProjectTable project,
        @Parameter(description = "Pagination information") @NotNull RequestPagination pagination
    );

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
    @GetMapping("/projects/{slugOrId}/watchers")
    ResponseEntity<PaginatedResult<User>> getProjectWatchers(
        @Parameter(description = "The slug or id of the project to return watchers for") @PathVariable("slugOrId") ProjectTable project,
        @Parameter(description = "Pagination information") @NotNull RequestPagination pagination
    );

    @GetMapping("/projects/{author}/{slugOrId}/watchers")
    @Deprecated(forRemoval = true)
    ResponseEntity<PaginatedResult<User>> getProjectWatchers(
        @Parameter(description = "The author of the project to return watchers for") @PathVariable("author") String author,
        @Parameter(description = "The slug or id of the project to return watchers for") @PathVariable("slugOrId") ProjectTable project,
        @Parameter(description = "Pagination information") @NotNull RequestPagination pagination
    );
}
