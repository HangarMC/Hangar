package io.papermc.hangar.controller.api.v1.interfaces;

import io.papermc.hangar.model.api.permissions.PermissionCheck;
import io.papermc.hangar.model.api.permissions.UserPermissions;
import io.papermc.hangar.model.common.NamedPermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@Api(tags = "Permissions", produces = MediaType.APPLICATION_JSON_VALUE)
@RequestMapping(path = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
public interface IPermissionsController {

    @ApiOperation(
            value = "Checks whether you have all the provided permissions",
            nickname = "hasAll",
            notes = "Checks whether you have all the provided permissions in the given context",
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
            value = "Checks whether you have at least one of the provided permissions",
            nickname = "hasAny",
            notes = "Checks whether you have at least one of the provided permissions in the given context",
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
                                           @ApiParam("The slug of the project to check permissions in. Must not be used together with `organizationName`") @RequestParam(required = false) String slug,
                                           @ApiParam("The organization to check permissions in. Must not be used together with `projectOwner` and `projectSlug`") @RequestParam(required = false) String organization
    );

    @ApiOperation(
            value = "Returns your permissions",
            nickname = "showPermissions",
            notes = "Returns a list of permissions you have in the given context",
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
            @ApiParam("The slug of the project get the permissions for. Must not be used together with `organizationName`") @RequestParam(required = false) String slug,
            @ApiParam("The organization to check permissions in. Must not be used together with `projectOwner` and `projectSlug`") @RequestParam(required = false) String organization
    );
}
