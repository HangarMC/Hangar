package me.minidigger.hangar.controller.generated;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import me.minidigger.hangar.model.generated.PaginatedCompactProjectResult;
import me.minidigger.hangar.model.generated.User;
import me.minidigger.hangar.model.generated.ProjectSortingStrategy;

@Api(value = "users", description = "the users API", tags = {"Users"})
public interface UsersApi {

    @ApiOperation(value = "Gets the starred projects for a specific user", nickname = "showStarred", notes = "Gets the starred projects for a specific user. Requires the `view_public_info` permission.", response = PaginatedCompactProjectResult.class, authorizations = {
            @Authorization(value = "Session")}, tags = {"Users",})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok", response = PaginatedCompactProjectResult.class),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")})
    @RequestMapping(value = "/users/{user}/starred",
            produces = {"application/json"},
            method = RequestMethod.GET)
    ResponseEntity<PaginatedCompactProjectResult> showStarred(@ApiParam(value = "The user to return for", required = true) @PathVariable("user") String user
            , @ApiParam(value = "How to sort the projects") @Valid @RequestParam(value = "sort", required = false) ProjectSortingStrategy sort
            , @ApiParam(value = "The maximum amount of projects to return") @Valid @RequestParam(value = "limit", required = false) Long limit
            , @ApiParam(value = "Where to start searching", defaultValue = "0") @Valid @RequestParam(value = "offset", required = false, defaultValue = "0") Long offset
    );


    @ApiOperation(value = "Gets a specific user", nickname = "showUser", notes = "Gets a specific user. Requires the `view_public_info` permission.", response = User.class, authorizations = {
            @Authorization(value = "Session")}, tags = {"Users",})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok", response = User.class),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")})
    @RequestMapping(value = "/users/{user}",
            produces = {"application/json"},
            method = RequestMethod.GET)
    ResponseEntity<User> showUser(@ApiParam(value = "The user to return", required = true) @PathVariable("user") String user
    );


    @ApiOperation(value = "Gets the watched projects for a specific user", nickname = "showWatching", notes = "Gets the watched projects for a specific user. Requires the `view_public_info` permission.", response = PaginatedCompactProjectResult.class, authorizations = {
            @Authorization(value = "Session")}, tags = {"Users",})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok", response = PaginatedCompactProjectResult.class),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")})
    @RequestMapping(value = "/users/{user}/watching",
            produces = {"application/json"},
            method = RequestMethod.GET)
    ResponseEntity<PaginatedCompactProjectResult> showWatching(@ApiParam(value = "The user to return for", required = true) @PathVariable("user") String user
            , @ApiParam(value = "How to sort the projects") @Valid @RequestParam(value = "sort", required = false) ProjectSortingStrategy sort
            , @ApiParam(value = "The maximum amount of projects to return") @Valid @RequestParam(value = "limit", required = false) Long limit
            , @ApiParam(value = "Where to start searching", defaultValue = "0") @Valid @RequestParam(value = "offset", required = false, defaultValue = "0") Long offset
    );

}
