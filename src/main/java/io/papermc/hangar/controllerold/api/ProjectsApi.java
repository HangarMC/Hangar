package io.papermc.hangar.controllerold.api;

import io.papermc.hangar.modelold.generated.Project;
import io.papermc.hangar.modelold.generated.ProjectStatsDay;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Api(value = "projects", tags = "Projects")
@RequestMapping({"/api", "/api/v1"})
public interface ProjectsApi {

    @ApiOperation(
            value = "Returns info on a specific project",
            nickname = "showProjectById",
            notes = "Returns info on a specific project. Requires the `view_public_info` permission.",
            response = Project.class,
            authorizations = {
                    @Authorization(value = "Session")
            },
            tags = "Projects",
            hidden = true
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok", response = Project.class),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")
    })
    @GetMapping(value = "/projects/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Project> showProject(
            @ApiParam(value = "The id of the project to return", required = true) @PathVariable("id") long id
    );

    @ApiOperation(
            value = "Returns the stats for a project",
            nickname = "showProjectStats",
            notes = "Returns the stats(downloads, views) for a project per day for a certain date range. Requires the `is_subject_member` permission.",
            response = ProjectStatsDay.class,
            responseContainer = "Map",
            authorizations = {
                    @Authorization(value = "Session")
            },
            tags = "Projects"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "Ok", response = ProjectStatsDay.class, responseContainer = "Map"),
                    @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
                    @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")
            })
    @GetMapping(value = "/projects/{author}/{slug}/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Map<String, ProjectStatsDay>> showProjectStats(
            @ApiParam(value = "The author of the project to return the stats for", required = true) @PathVariable("author") String author,
            @ApiParam(value = "The slug of the project to return", required = true) @PathVariable("slug") String slug,
            @NotNull @ApiParam(value = "The first date to include in the result", required = true) @Valid @RequestParam(value = "fromDate", required = true) String fromDate,
            @NotNull @ApiParam(value = "The last date to include in the result", required = true) @Valid @RequestParam(value = "toDate", required = true) String toDate
    );
}
