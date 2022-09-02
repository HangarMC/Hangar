package io.papermc.hangar.controller.api.v1.interfaces;

import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.User;
import io.papermc.hangar.model.api.project.ProjectCompact;
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
import java.util.List;

@Api(tags = "Users", produces = MediaType.APPLICATION_JSON_VALUE)
@RequestMapping(path = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
public interface IUsersController {

    @ApiOperation(
            value = "Returns a specific user",
            nickname = "getUser",
            notes = "Returns a specific user. Requires the `view_public_info` permission.",
            authorizations = @Authorization("Session"),
            tags = "Users"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/users/{user}")
    ResponseEntity<User> getUser(@ApiParam("The name of the user to return") @PathVariable("user") String userName);

    @ApiOperation(
            value = "Searches for users",
            nickname = "showUsers",
            notes = "Returns a list of users based on a search query",
            authorizations = @Authorization("Session"),
            tags = "Users"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/users")
    ResponseEntity<PaginatedResult<User>> getUsers(@ApiParam(value = "The search query", required = true) @RequestParam("query") String query,
                                                   @ApiParam("Pagination information") @NotNull RequestPagination pagination);

    @ApiOperation(
            value = "Returns the starred projects for a specific user",
            nickname = "showStarred",
            notes = "Returns the starred projects for a specific user. Requires the `view_public_info` permission.",
            authorizations = @Authorization("Session"),
            tags = "Users"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/users/{user}/starred")
    ResponseEntity<PaginatedResult<ProjectCompact>> getUserStarred(@ApiParam("The user to return starred projects for") @PathVariable("user") String userName,
                                                                   @ApiParam("How to sort the projects") @RequestParam(defaultValue = "updated") ProjectSortingStrategy sort,
                                                                   @ApiParam("Pagination information") @NotNull RequestPagination pagination);

    @ApiOperation(
            value = "Returns the watched projects for a specific user",
            nickname = "getUserWatching",
            notes = "Returns the watched projects for a specific user. Requires the `view_public_info` permission.",
            authorizations = @Authorization("Session"),
            tags = "Users"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/users/{user}/watching")
    ResponseEntity<PaginatedResult<ProjectCompact>> getUserWatching(@ApiParam("The user to return watched projects for") @PathVariable("user") String userName,
                                                                    @ApiParam("How to sort the projects") @RequestParam(defaultValue = "updated") ProjectSortingStrategy sort,
                                                                    @ApiParam("Pagination information") @NotNull RequestPagination pagination);

    @ApiOperation(
        value = "Returns the pinned projects for a specific user",
        nickname = "getUserPinnedProjects",
        notes = "Returns the pinned projects for a specific user. Requires the `view_public_info` permission.",
        authorizations = @Authorization("Session"),
        tags = "Users"
    )
    @ApiResponses({
        @ApiResponse(code = 200, message = "Ok"),
        @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
        @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/users/{user}/pinned")
    ResponseEntity<List<ProjectCompact>> getUserPinnedProjects(@ApiParam("The user to return pinned projects for") @PathVariable("user") String userName);

    @ApiOperation(
            value = "Returns all users with at least one public project",
            nickname = "getAuthors",
            notes = "Returns all users that have at least one public project. Requires the `view_public_info` permission.",
            authorizations = @Authorization("Session"),
            tags = "Users"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/authors")
    ResponseEntity<PaginatedResult<User>> getAuthors(@ApiParam("Pagination information") @NotNull RequestPagination pagination);

    @ApiOperation(
            value = "Returns Hangar staff",
            nickname = "getStaff",
            notes = "Returns Hanagr staff. Requires the `view_public_info` permission.",
            authorizations = @Authorization("Session"),
            tags = "Users"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/staff")
    ResponseEntity<PaginatedResult<User>> getStaff(@ApiParam("Pagination information") @NotNull RequestPagination pagination);
}
