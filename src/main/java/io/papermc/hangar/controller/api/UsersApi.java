package io.papermc.hangar.controller.api;

import io.papermc.hangar.db.model.UsersTable;
import io.papermc.hangar.model.api.PaginatedUserResult;
import io.papermc.hangar.model.generated.PaginatedCompactProjectResult;
import io.papermc.hangar.model.generated.ProjectSortingStrategy;
import io.papermc.hangar.model.generated.User;
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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

@Api(value = "users", description = "the users API", tags = {"Users"})
@RequestMapping({"/api", "/api/v1"})
public interface UsersApi {

    @ApiOperation(
            value = "Gets the starred projects for a specific user",
            nickname = "showStarred",
            notes = "Gets the starred projects for a specific user. Requires the `view_public_info` permission.",
            response = PaginatedCompactProjectResult.class,
            authorizations = {
                    @Authorization(value = "Session")
            },
            tags = "Users"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok", response = PaginatedCompactProjectResult.class),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")
    })
    @GetMapping(value = "/users/{user}/starred", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PaginatedCompactProjectResult> showStarred(
            @ApiParam(value = "The user to return for", required = true) @PathVariable("user") String user,
            @ApiParam(value = "How to sort the projects") @Valid @RequestParam(value = "sort", required = false) ProjectSortingStrategy sort,
            @ApiParam(value = "The maximum amount of projects to return") @Valid @RequestParam(value = "limit", required = false) Long limit,
            @ApiParam(value = "Where to start searching", defaultValue = "0") @Valid @RequestParam(value = "offset", required = false, defaultValue = "0") Long offset
    );

    @ApiOperation(
            value = "Searches for users",
            nickname = "showUsers",
            notes = "Gets a list of users based on a search query",
            response = PaginatedUserResult.class,
            authorizations = {
                    @Authorization("Session")
            },
            tags = "Users"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok", response = PaginatedUserResult.class),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")
    })
    @GetMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PaginatedUserResult> showUsers(
            @ApiParam(value = "The search query", required = true) @RequestParam("q") String q,
            @ApiParam("The maximum amount of users to return") @RequestParam(value = "limit", required = false) Long limit,
            @ApiParam("Where to start searching") @RequestParam(value = "offset", defaultValue = "0") long offset);


    @ApiOperation(value = "Gets a specific user", nickname = "showUser", notes = "Gets a specific user. Requires the `view_public_info` permission.", response = UsersTable.class, authorizations = {
            @Authorization(value = "Session")}, tags = {"Users",})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok", response = UsersTable.class),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")})
    @GetMapping(value = "/users/{user}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<User> showUser(@ApiParam(value = "The user to return", required = true) @PathVariable("user") String user
    );


    @ApiOperation(value = "Gets the watched projects for a specific user", nickname = "showWatching", notes = "Gets the watched projects for a specific user. Requires the `view_public_info` permission.", response = PaginatedCompactProjectResult.class, authorizations = {
            @Authorization(value = "Session")}, tags = {"Users",})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok", response = PaginatedCompactProjectResult.class),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")})
    @GetMapping(value = "/users/{user}/watching", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PaginatedCompactProjectResult> showWatching(@ApiParam(value = "The user to return for", required = true) @PathVariable("user") String user
            , @ApiParam(value = "How to sort the projects") @Valid @RequestParam(value = "sort", required = false) ProjectSortingStrategy sort
            , @ApiParam(value = "The maximum amount of projects to return") @Valid @RequestParam(value = "limit", required = false) Long limit
            , @ApiParam(value = "Where to start searching", defaultValue = "0") @Valid @RequestParam(value = "offset", required = false, defaultValue = "0") Long offset
    );

}
