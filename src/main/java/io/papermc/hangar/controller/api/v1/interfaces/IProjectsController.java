package io.papermc.hangar.controller.api.v1.interfaces;

import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.User;
import io.papermc.hangar.model.api.project.DayProjectStats;
import io.papermc.hangar.model.api.project.Project;
import io.papermc.hangar.model.api.project.ProjectMember;
import io.papermc.hangar.model.api.project.ProjectSortingStrategy;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.OffsetDateTime;
import java.util.Map;

@Api(tags = "Projects", produces = MediaType.APPLICATION_JSON_VALUE)
@RequestMapping(path ="/api/v1", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
public interface IProjectsController {

    @ApiOperation(
            value = "Returns info on a specific project",
            nickname = "getProject",
            notes = "Returns info on a specific project. Requires the `view_public_info` permission.",
            authorizations = @Authorization("Session"),
            tags = "Projects"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")
    })
    @GetMapping( "/projects/{author}/{slug}")
    ResponseEntity<Project> getProject(@ApiParam("The author of the project to return") @PathVariable String author,
                                       @ApiParam("The slug of the project to return") @PathVariable String slug);

    @ApiOperation(
            value = "Returns the members of a project",
            nickname = "getProjectMembers",
            notes = "Returns the members of a project. Requires the `view_public_info` permission.",
            authorizations = @Authorization("Session"),
            tags = "Projects"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/projects/{author}/{slug}/members")
    ResponseEntity<PaginatedResult<ProjectMember>> getProjectMembers(
            @ApiParam("The author of the project to return members for") @PathVariable("author") String author,
            @ApiParam("The slug of the project to return") @PathVariable("slug") String slug,
            @ApiParam("Pagination information") @NotNull RequestPagination pagination
    );

    @ApiOperation(
            value = "Searches the projects on Hangar",
            nickname = "getProjects",
            notes = "Searches all the projects on Hangar, or for a single user. Requires the `view_public_info` permission.",
            authorizations = @Authorization("Session"),
            tags = "Projects"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/projects")
    ResponseEntity<PaginatedResult<Project>> getProjects(
            @ApiParam("The query to use when searching") @RequestParam(required = false) String q,
            @ApiParam("How to sort the projects") @RequestParam(defaultValue = "updated") ProjectSortingStrategy sort,
            @ApiParam("If how relevant the project is to the given query should be used when sorting the projects") @RequestParam(defaultValue = "true") boolean relevance,
            @ApiParam("Pagination information") @NotNull RequestPagination pagination
    );

    @ApiOperation(
            value = "Returns the stats for a project",
            nickname = "showProjectStats",
            notes = "Returns the stats(downloads, views) for a project per day for a certain date range. Requires the `is_subject_member` permission.",
            authorizations = @Authorization("Session"),
            tags = "Projects"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/projects/{author}/{slug}/stats")
    ResponseEntity<Map<String, DayProjectStats>> getProjectStats(@ApiParam("The author of the project to return the stats for") @PathVariable String author,
                                                                 @ApiParam("The slug of the project to return") @PathVariable String slug,
                                                                 @NotNull @ApiParam(value = "The first date to include in the result", required = true) @RequestParam OffsetDateTime fromDate,
                                                                 @NotNull @ApiParam(value = "The last date to include in the result", required = true) @RequestParam OffsetDateTime toDate
    );

    @ApiOperation(
            value = "Returns the stargazers of a project",
            nickname = "getProjectStargazers",
            notes = "Returns the stargazers of a project. Requires the `view_public_info` permission.",
            authorizations = @Authorization("Session"),
            tags = "Projects"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/projects/{author}/{slug}/stargazers")
    ResponseEntity<PaginatedResult<User>> getProjectStargazers(
            @ApiParam("The author of the project to return stargazers for") @PathVariable("author") String author,
            @ApiParam("The slug of the project to return") @PathVariable("slug") String slug,
            @ApiParam("Pagination information") @NotNull RequestPagination pagination
    );

    @ApiOperation(
            value = "Returns the watchers of a project",
            nickname = "getProjectWatchers",
            notes = "Returns the watchers of a project. Requires the `view_public_info` permission.",
            authorizations = @Authorization("Session"),
            tags = "Projects"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/projects/{author}/{slug}/watchers")
    ResponseEntity<PaginatedResult<User>> getProjectWatchers(
            @ApiParam("The author of the project to return watchers for") @PathVariable("author") String author,
            @ApiParam("The slug of the project to return") @PathVariable("slug") String slug,
            @ApiParam("Pagination information") @NotNull RequestPagination pagination
    );
}
