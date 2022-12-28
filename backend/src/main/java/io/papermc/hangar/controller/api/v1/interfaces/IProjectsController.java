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
        security = @SecurityRequirement(name = "Session"),
        tags = "Projects"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired"),
        @ApiResponse(responseCode = "403", description = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/projects/{author}/{slug}")
    ResponseEntity<Project> getProject(@Parameter(description = "The author of the project to return") @PathVariable String author,
                                       @Parameter(description = "The slug of the project to return") @PathVariable String slug);

    @Operation(
        summary = "Returns the members of a project",
        operationId = "getProjectMembers",
        description = "Returns the members of a project. Requires the `view_public_info` permission.",
        security = @SecurityRequirement(name = "Session"),
        tags = "Projects"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired"),
        @ApiResponse(responseCode = "403", description = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/projects/{author}/{slug}/members")
    ResponseEntity<PaginatedResult<ProjectMember>> getProjectMembers(
        @Parameter(description = "The author of the project to return members for") @PathVariable("author") String author,
        @Parameter(description = "The slug of the project to return members for") @PathVariable("slug") String slug,
        @Parameter(description = "Pagination information") @NotNull RequestPagination pagination
    );

    @Operation(
        summary = "Searches the projects on Hangar",
        operationId = "getProjects",
        description = "Searches all the projects on Hangar, or for a single user. Requires the `view_public_info` permission.",
        security = @SecurityRequirement(name = "Session"),
        tags = "Projects"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired"),
        @ApiResponse(responseCode = "403", description = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/projects")
    ResponseEntity<PaginatedResult<Project>> getProjects(
        @Parameter(description = "The query to use when searching") @RequestParam(required = false) String q,
        @Parameter(description = "Whether projects should be sorted by the relevance to the given query") @RequestParam(defaultValue = "true") boolean relevance,
        @Parameter(description = "Pagination information") @NotNull RequestPagination pagination
    );

    @Operation(
        summary = "Returns the stats for a project",
        operationId = "showProjectStats",
        description = "Returns the stats (downloads and views) for a project per day for a certain date range. Requires the `is_subject_member` permission.",
        security = @SecurityRequirement(name = "Session"),
        tags = "Projects"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired"),
        @ApiResponse(responseCode = "403", description = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/projects/{author}/{slug}/stats")
    ResponseEntity<Map<String, DayProjectStats>> getProjectStats(@Parameter(description = "The author of the project to return stats for") @PathVariable String author,
                                                                 @Parameter(description = "The slug of the project to return stats for") @PathVariable String slug,
                                                                 @NotNull @Parameter(description = "The first date to include in the result", required = true) @RequestParam OffsetDateTime fromDate,
                                                                 @NotNull @Parameter(description = "The last date to include in the result", required = true) @RequestParam OffsetDateTime toDate
    );

    @Operation(
        summary = "Returns the stargazers of a project",
        operationId = "getProjectStargazers",
        description = "Returns the stargazers of a project. Requires the `view_public_info` permission.",
        security = @SecurityRequirement(name = "Session"),
        tags = "Projects"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired"),
        @ApiResponse(responseCode = "403", description = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/projects/{author}/{slug}/stargazers")
    ResponseEntity<PaginatedResult<User>> getProjectStargazers(
        @Parameter(description = "The author of the project to return stargazers for") @PathVariable("author") String author,
        @Parameter(description = "The slug of the project to return stargazers for") @PathVariable("slug") String slug,
        @Parameter(description = "Pagination information") @NotNull RequestPagination pagination
    );

    @Operation(
        summary = "Returns the watchers of a project",
        operationId = "getProjectWatchers",
        description = "Returns the watchers of a project. Requires the `view_public_info` permission.",
        security = @SecurityRequirement(name = "Session"),
        tags = "Projects"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired"),
        @ApiResponse(responseCode = "403", description = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/projects/{author}/{slug}/watchers")
    ResponseEntity<PaginatedResult<User>> getProjectWatchers(
        @Parameter(description = "The author of the project to return watchers for") @PathVariable("author") String author,
        @Parameter(description = "The slug of the project to return watchers for") @PathVariable("slug") String slug,
        @Parameter(description = "Pagination information") @NotNull RequestPagination pagination
    );
}
