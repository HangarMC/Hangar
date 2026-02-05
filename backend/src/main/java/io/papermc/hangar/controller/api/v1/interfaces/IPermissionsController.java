package io.papermc.hangar.controller.api.v1.interfaces;

import io.papermc.hangar.model.api.permissions.PermissionCheck;
import io.papermc.hangar.model.api.permissions.UserPermissions;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.db.OrganizationTable;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Size;
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
    @GetMapping(value = "/permissions/hasAll", params = "!slug")
    ResponseEntity<PermissionCheck> hasAllPermissions(
        @Parameter(description = "The permissions to check", required = true) @RequestParam @Size(max = 50) Set<NamedPermission> permissions,
        @Parameter(description = "The id or slug of the project to check permissions in. Must not be used together with `organization`") @RequestParam(required = false, name = "project") ProjectTable project,
        @Parameter(description = "The id or name of the organization to check permissions in. Must not be used together with `project`") @RequestParam(required = false, name = "organization") OrganizationTable organization
    );

    @Operation(
        summary = "Checks whether you have all the provided permissions",
        operationId = "hasAllLegacy",
        description = "Checks whether you have all the provided permissions in the given context",
        security = @SecurityRequirement(name = "HangarAuth"),
        tags = "Permissions"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired")
    })
    @GetMapping(value = "/permissions/hasAll", params = "slug")
    ResponseEntity<PermissionCheck> hasAllPermissionsLegacy(
        @Parameter(description = "The permissions to check", required = true) @RequestParam final @Size(max = 50) Set<NamedPermission> permissions,
        @Deprecated @Parameter(description = "Deprecated alias for `project`") @RequestParam(required = false, name = "slug") final ProjectTable slug,
        @Parameter(description = "The id or name of the organization to check permissions in. Must not be used together with `slug`") @RequestParam(required = false, name = "organization") final OrganizationTable organization
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
    @GetMapping(value = "/permissions/hasAny", params = "!slug")
    ResponseEntity<PermissionCheck> hasAny(
        @Parameter(description = "The permissions to check", required = true) @RequestParam Set<NamedPermission> permissions,
        @Parameter(description = "The id or slug of the project to check permissions in. Must not be used together with `organization`") @RequestParam(required = false, name = "project") ProjectTable project,
        @Parameter(description = "The id or name of the organization to check permissions in. Must not be used together with `project`") @RequestParam(required = false, name = "organization") OrganizationTable organization
    );

    @Operation(
        summary = "Checks whether you have at least one of the provided permissions",
        operationId = "hasAnyLegacy",
        description = "Checks whether you have at least one of the provided permissions in the given context",
        security = @SecurityRequirement(name = "HangarAuth"),
        tags = "Permissions"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired")
    })
    @GetMapping(value = "/permissions/hasAny", params = "slug")
    ResponseEntity<PermissionCheck> hasAnyLegacy(
        @Parameter(description = "The permissions to check", required = true) @RequestParam final Set<NamedPermission> permissions,
        @Deprecated @Parameter(description = "Deprecated alias for `project`") @RequestParam(required = false, name = "slug") final ProjectTable slug,
        @Parameter(description = "The id or name of the organization to check permissions in. Must not be used together with `project`") @RequestParam(required = false, name = "organization") final OrganizationTable organization
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
    @GetMapping(value = "/permissions", params = "!slug", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<UserPermissions> showPermissions(
        @Parameter(description = "The id or slug of the project to check permissions in. Must not be used together with `organization`") @RequestParam(required = false, name = "project") ProjectTable project,
        @Parameter(description = "The id or name of the organization to check permissions in. Must not be used together with `project`") @RequestParam(required = false, name = "organization") OrganizationTable organization
    );

    @Operation(
        summary = "Returns your permissions",
        operationId = "showPermissionsLegacy",
        description = "Returns a list of permissions you have in the given context",
        security = @SecurityRequirement(name = "HangarAuth"),
        tags = "Permissions"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired")
    })
    @GetMapping(value = "/permissions", params = "slug", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<UserPermissions> showPermissionsLegacy(
        @Deprecated @Parameter(description = "Deprecated alias for `project`") @RequestParam(required = false, name = "slug") final ProjectTable slug,
        @Parameter(description = "The id or name of the organization to check permissions in. Must not be used together with `project`", name = "organization") @RequestParam(required = false) final OrganizationTable organization
    );
}
