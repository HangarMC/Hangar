package io.papermc.hangar.controller.api.v1.interfaces;

import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.User;
import io.papermc.hangar.model.api.project.ProjectCompact;
import io.papermc.hangar.model.api.project.ProjectSortingStrategy;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Users")
@RequestMapping(path = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
public interface IUsersController {

    @Operation(
        summary = "Returns a specific user",
        operationId = "getUser",
        description = "Returns a specific user. Requires the `view_public_info` permission.",
        security = @SecurityRequirement(name = "HangarAuth", scopes = "view_public_info"),
        tags = "Users"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired"),
        @ApiResponse(responseCode = "403", description = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/users/{user}")
    ResponseEntity<User> getUser(@Parameter(description = "The name of the user to return") @PathVariable("user") String userName);

    @Operation(
        summary = "Searches for users",
        operationId = "showUsers",
        description = "Returns a list of users based on a search query. Requires the `view_public_info` permission.",
        security = @SecurityRequirement(name = "HangarAuth", scopes = "view_public_info"),
        tags = "Users"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired"),
        @ApiResponse(responseCode = "403", description = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/users")
    ResponseEntity<PaginatedResult<User>> getUsers(@Parameter(description = "The search query", required = true) @RequestParam(required = false) String query,
                                                   @Parameter(description = "Pagination information") @NotNull RequestPagination pagination);

    @Operation(
        summary = "Returns the starred projects for a specific user",
        operationId = "showStarred",
        description = "Returns the starred projects for a specific user. Requires the `view_public_info` permission.",
        security = @SecurityRequirement(name = "HangarAuth", scopes = "view_public_info"),
        tags = "Users"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired"),
        @ApiResponse(responseCode = "403", description = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/users/{user}/starred")
    ResponseEntity<PaginatedResult<ProjectCompact>> getUserStarred(@Parameter(description = "The user to return starred projects for") @PathVariable("user") String userName,
                                                                   @Parameter(description = "How to sort the projects") @RequestParam(defaultValue = "updated") ProjectSortingStrategy sort,
                                                                   @Parameter(description = "Pagination information") @NotNull RequestPagination pagination);

    @Operation(
        summary = "Returns the watched projects for a specific user",
        operationId = "getUserWatching",
        description = "Returns the watched projects for a specific user. Requires the `view_public_info` permission.",
        security = @SecurityRequirement(name = "HangarAuth", scopes = "view_public_info"),
        tags = "Users"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired"),
        @ApiResponse(responseCode = "403", description = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/users/{user}/watching")
    ResponseEntity<PaginatedResult<ProjectCompact>> getUserWatching(@Parameter(description = "The user to return watched projects for") @PathVariable("user") String userName,
                                                                    @Parameter(description = "How to sort the projects") @RequestParam(defaultValue = "updated") ProjectSortingStrategy sort,
                                                                    @Parameter(description = "Pagination information") @NotNull RequestPagination pagination);

    @Operation(
        summary = "Returns the pinned projects for a specific user",
        operationId = "getUserPinnedProjects",
        description = "Returns the pinned projects for a specific user. Requires the `view_public_info` permission.",
        security = @SecurityRequirement(name = "HangarAuth", scopes = "view_public_info"),
        tags = "Users"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired"),
        @ApiResponse(responseCode = "403", description = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/users/{user}/pinned")
    ResponseEntity<List<ProjectCompact>> getUserPinnedProjects(@Parameter(description = "The user to return pinned projects for") @PathVariable("user") String userName);

    @Operation(
        summary = "Returns all users with at least one public project",
        operationId = "getAuthors",
        description = "Returns all users that have at least one public project. Requires the `view_public_info` permission.",
        security = @SecurityRequirement(name = "HangarAuth", scopes = "view_public_info"),
        tags = "Users"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired"),
        @ApiResponse(responseCode = "403", description = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/authors")
    ResponseEntity<PaginatedResult<User>> getAuthors(@Parameter(description = "The search query", required = true) @RequestParam(required = false) String query,
                                                     @Parameter(description = "Pagination information") @NotNull RequestPagination pagination);

    @Operation(
        summary = "Returns Hangar staff",
        operationId = "getStaff",
        description = "Returns Hanagr staff. Requires the `view_public_info` permission.",
        security = @SecurityRequirement(name = "HangarAuth", scopes = "view_public_info"),
        tags = "Users"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired"),
        @ApiResponse(responseCode = "403", description = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/staff")
    ResponseEntity<PaginatedResult<User>> getStaff(@Parameter(description = "The search query", required = true) @RequestParam(required = false) String query,
                                                   @Parameter(description = "Pagination information") @NotNull RequestPagination pagination);
}
