package io.papermc.hangar.controller.api.v1.interfaces;

import io.papermc.hangar.model.api.permissions.PermissionCheck;
import io.papermc.hangar.model.api.permissions.UserPermissions;
import io.papermc.hangar.model.common.NamedPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Set;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Permissions")
@RequestMapping(path = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
public interface IPermissionsController {

    @Operation(
        summary = "Checks whether you have all the provided permissions",
        operationId = "hasAll",
        description = "Checks whether you have all the provided permissions in the given context",
        security = @SecurityRequirement(name = "HangarAuth"),
        tags = "Permissions"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired")
    })
    @GetMapping("/permissions/hasAll")
    ResponseEntity<PermissionCheck> hasAllPermissions(@Parameter(description = "The permissions to check", required = true) @RequestParam Set<NamedPermission> permissions,
                                                      @Parameter(description = "The project slug of the project to check permissions in. Must not be used together with `organizationName`") @RequestParam(required = false) String slug,
                                                      @Parameter(description = "The organization to check permissions in. Must not be used together with `projectOwner` and `projectSlug`") @RequestParam(required = false) String organization
    );

    @Operation(
        summary = "Checks whether you have at least one of the provided permissions",
        operationId = "hasAny",
        description = "Checks whether you have at least one of the provided permissions in the given context",
        security = @SecurityRequirement(name = "HangarAuth"),
        tags = "Permissions"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired")
    })
    @GetMapping("/permissions/hasAny")
    ResponseEntity<PermissionCheck> hasAny(@Parameter(description = "The permissions to check", required = true) @RequestParam Set<NamedPermission> permissions,
                                           @Parameter(description = "The slug of the project to check permissions in. Must not be used together with `organizationName`") @RequestParam(required = false) String slug,
                                           @Parameter(description = "The organization to check permissions in. Must not be used together with `projectOwner` and `projectSlug`") @RequestParam(required = false) String organization
    );

    @Operation(
        summary = "Returns your permissions",
        operationId = "showPermissions",
        description = "Returns a list of permissions you have in the given context",
        security = @SecurityRequirement(name = "HangarAuth"),
        tags = "Permissions"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired")
    })
    @GetMapping(value = "/permissions",
        produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<UserPermissions> showPermissions(
        @Parameter(description = "The slug of the project get the permissions for. Must not be used together with `organizationName`") @RequestParam(required = false) String slug,
        @Parameter(description = "The organization to check permissions in. Must not be used together with `projectOwner` and `projectSlug`") @RequestParam(required = false) String organization
    );
}
