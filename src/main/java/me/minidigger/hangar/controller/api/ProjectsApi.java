package me.minidigger.hangar.controller.api;

import me.minidigger.hangar.model.ApiAuthInfo;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import me.minidigger.hangar.model.Category;
import me.minidigger.hangar.model.generated.PaginatedProjectResult;
import me.minidigger.hangar.model.generated.Project;
import me.minidigger.hangar.model.generated.ProjectMember;
import me.minidigger.hangar.model.generated.ProjectStatsDay;
import me.minidigger.hangar.model.generated.ProjectSortingStrategy;

@Api(value = "projects", description = "the projects API", tags = "Projects")
@RequestMapping("/api/v2/")
public interface ProjectsApi {

    @ApiOperation(value = "Searches the projects on Ore", nickname = "listProjects", notes = "Searches all the projects on ore, or for a single user. Requires the `view_public_info` permission.", response = PaginatedProjectResult.class, authorizations = {
            @Authorization(value = "Session")}, tags = "Projects")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok", response = PaginatedProjectResult.class),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")})
    @GetMapping(value = "/projects",
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PaginatedProjectResult> listProjects(@ApiParam(value = "The query to use when searching") @Valid @RequestParam(value = "q", required = false) String q
            , @ApiParam(value = "Restrict your search to a list of categories") @Valid @RequestParam(value = "categories", required = false) List<Category> categories
            , @ApiParam(value = "A list of tags all the returned projects should have. Should be formated either as `tagname` or `tagname:tagdata`.") @Valid @RequestParam(value = "tags", required = false) List<String> tags
            , @ApiParam(value = "Limit the search to a specific user") @Valid @RequestParam(value = "owner", required = false) String owner
            , @ApiParam(value = "How to sort the projects") @Valid @RequestParam(value = "sort", required = false, defaultValue = "updated") ProjectSortingStrategy sort
            , @ApiParam(value = "If how relevant the project is to the given query should be used when sorting the projects") @RequestParam(value = "relevance", required = false, defaultValue = "true") boolean relevance
            , @ApiParam(value = "The maximum amount of projects to return") @Valid @RequestParam(value = "limit", required = false) Long limit
            , @ApiParam(value = "Where to start searching", defaultValue = "0") @Valid @RequestParam(value = "offset", required = false, defaultValue = "0") Long offset,
                                                        ApiAuthInfo apiAuthInfo);

    @ApiOperation(value = "Returns the members of a project", nickname = "showMembers", notes = "Returns the members of a project. Requires the `view_public_info` permission.", response = ProjectMember.class, authorizations = {
            @Authorization(value = "Session")}, tags = "Projects")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok", response = ProjectMember.class),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")})
    @GetMapping(value = "/projects/{pluginId}/members",
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ProjectMember> showMembers(@ApiParam(value = "The plugin id of the project to return members for", required = true) @PathVariable("pluginId") String pluginId
            , @ApiParam(value = "The maximum amount of members to return") @Valid @RequestParam(value = "limit", required = false) Long limit
            , @ApiParam(value = "Where to start returning", defaultValue = "0") @Valid @RequestParam(value = "offset", required = false, defaultValue = "0") Long offset
    );

    @ApiOperation(value = "Returns info on a specific project", nickname = "showProject", notes = "Returns info on a specific project. Requires the `view_public_info` permission.", response = Project.class, authorizations = {
            @Authorization(value = "Session")}, tags = "Projects")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok", response = Project.class),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")})
    @GetMapping(value = "/projects/{pluginId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Project> showProject(@ApiParam(value = "The plugin id of the project to return", required = true) @PathVariable("pluginId") String pluginId
    );

    @ApiOperation(value = "Returns the stats for a project", nickname = "showProjectStats", notes = "Returns the stats(downloads, views) for a project per day for a certain date range. Requires the `is_subject_member` permission.", response = ProjectStatsDay.class, responseContainer = "Map", authorizations = {
            @Authorization(value = "Session")}, tags = "Projects")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok", response = ProjectStatsDay.class, responseContainer = "Map"),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")})
    @GetMapping(value = "/projects/{pluginId}/stats",
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Map<String, ProjectStatsDay>> showProjectStats(@ApiParam(value = "The plugin id of the project to return the stats for", required = true) @PathVariable("pluginId") String pluginId
            , @NotNull @ApiParam(value = "The first date to include in the result", required = true) @Valid @RequestParam(value = "fromDate", required = true) LocalDate fromDate
            , @NotNull @ApiParam(value = "The last date to include in the result", required = true) @Valid @RequestParam(value = "toDate", required = true) LocalDate toDate
    );
}
