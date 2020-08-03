package me.minidigger.hangar.controller.api;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import me.minidigger.hangar.model.generated.Permissions;
import me.minidigger.hangar.model.generated.PermissionCheck;
import me.minidigger.hangar.model.NamedPermission;

@Api(value = "permissions", description = "the permissions API", tags = "Permissions")
@RequestMapping("/api/v2/")
public interface PermissionsApi {

    @ApiOperation(value = "Do an AND permission check", nickname = "hasAll", notes = "Checks that you have all the permissions passed in with a given session in a given context", response = PermissionCheck.class, authorizations = {
            @Authorization(value = "Session")}, tags = "Permissions")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok", response = PermissionCheck.class),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired")})
    @GetMapping(value = "/permissions/hasAll",
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PermissionCheck> hasAll(@NotNull @ApiParam(value = "The permissions to check", required = true) @Valid @RequestParam(value = "permissions", required = true) List<NamedPermission> permissions
            , @ApiParam(value = "The plugin to check permissions in. Must not be used together with `organizationName`") @Valid @RequestParam(value = "pluginId", required = false) String pluginId
            , @ApiParam(value = "The organization to check permissions in. Must not be used together with `pluginId`") @Valid @RequestParam(value = "organizationName", required = false) String organizationName
    );


    @ApiOperation(value = "Do an OR permission check", nickname = "hasAny", notes = "Checks that you have any of the permissions passed in with a given session in a given context", response = PermissionCheck.class, authorizations = {
            @Authorization(value = "Session")}, tags = "Permissions")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok", response = PermissionCheck.class),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired")})
    @GetMapping(value = "/permissions/hasAny",
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PermissionCheck> hasAny(@NotNull @ApiParam(value = "The permissions to check", required = true) @Valid @RequestParam(value = "permissions", required = true) List<NamedPermission> permissions
            , @ApiParam(value = "The plugin to check permissions in. Must not be used together with `organizationName`") @Valid @RequestParam(value = "pluginId", required = false) String pluginId
            , @ApiParam(value = "The organization to check permissions in. Must not be used together with `pluginId`") @Valid @RequestParam(value = "organizationName", required = false) String organizationName
    );


    @ApiOperation(value = "Checks your permissions", nickname = "showPermissions", notes = "Checks your permissions with a given session in a given context", response = Permissions.class, authorizations = {
            @Authorization(value = "Session")}, tags = "Permissions")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok", response = Permissions.class),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired")})
    @GetMapping(value = "/permissions",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Secured("ROLE_USER")
    ResponseEntity<Permissions> showPermissions(@ApiParam(value = "The plugin to check permissions in. Must not be used together with `organizationName`") @Valid @RequestParam(value = "pluginId", required = false) String pluginId
            , @ApiParam(value = "The organization to check permissions in. Must not be used together with `pluginId`") @Valid @RequestParam(value = "organizationName", required = false) String organizationName
    );

}
