package io.papermc.hangar.controller.api.v1.interfaces;

import io.papermc.hangar.model.api.permissions.PermissionCheck;
import io.papermc.hangar.model.api.permissions.UserPermissions;
import io.papermc.hangar.modelold.NamedPermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@Api(tags = "Permissions", produces = MediaType.APPLICATION_JSON_VALUE)
@RequestMapping(path = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
@PreAuthorize("@authenticationService.handleApiRequest(T(io.papermc.hangar.model.Permission).None, T(io.papermc.hangar.controller.extras.ApiScope).ofGlobal())")
public interface IPermissionsController {

    @ApiOperation(
            value = "Do an AND permission check",
            nickname = "hasAll",
            notes = "Checks that you have all the permissions passed in with a given session in a given context",
            response = PermissionCheck.class,
            authorizations = @Authorization("Session"),
            tags = "Permissions"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok", response = PermissionCheck.class),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired")
    })
    @GetMapping("/permissions/hasAll")
    ResponseEntity<PermissionCheck> hasAllPermissions(@ApiParam(value = "The permissions to check", required = true) @RequestParam List<NamedPermission> permissions,
                                                      @ApiParam("The owner of the project to check permissions in. Must not be used together with `organizationName`") @RequestParam(required = false) String author,
                                                      @ApiParam("The project slug of the project to check permissions in. Must not be used together with `organizationName`") @RequestParam(required = false) String slug,
                                                      @ApiParam("The organization to check permissions in. Must not be used together with `projectOwner` and `projectSlug`") @RequestParam(required = false) String organization
    );

    @ApiOperation(
            value = "Do an OR permission check",
            nickname = "hasAny",
            notes = "Checks that you have any of the permissions passed in with a given session in a given context",
            authorizations = @Authorization("Session"),
            tags = "Permissions"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired")
    })
    @GetMapping("/permissions/hasAny")
    ResponseEntity<PermissionCheck> hasAny(@ApiParam(value = "The permissions to check", required = true) @RequestParam List<NamedPermission> permissions,
                                           @ApiParam("The owner of the project to check permissions in. Must not be used together with `organizationName") @RequestParam(required = false) String author,
                                           @ApiParam("The project slug of the project to check permissions in. Must not be used together with `organizationName`") @RequestParam(required = false) String slug,
                                           @ApiParam("The organization to check permissions in. Must not be used together with `projectOwner` and `projectSlug`") @RequestParam(required = false) String organization
    );

    @ApiOperation(
            value = "Checks your permissions",
            nickname = "showPermissions",
            notes = "Checks your permissions with a given session in a given context",
            authorizations = @Authorization("Session"),
            tags = "Permissions"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired")
    })
    @GetMapping(value = "/permissions",
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<UserPermissions> showPermissions(
            @ApiParam("The owner of the project to get the permissions for. Must not be used together with `organizationName`") @RequestParam(required = false) String author,
            @ApiParam("The project slug of the project get the permissions for. Must not be used together with `organizationName`") @RequestParam(required = false) String slug,
            @ApiParam("The organization to check permissions in. Must not be used together with `projectOwner` and `projectSlug`") @RequestParam(required = false) String organization
    );
}
