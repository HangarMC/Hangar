package io.papermc.hangar.controller.api.v1.interfaces;

import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.project.version.Version;
import io.papermc.hangar.model.api.project.version.VersionStats;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.papermc.hangar.model.common.Platform;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.OffsetDateTime;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Versions")
@RequestMapping(path = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public interface IVersionsController {

    // TODO implement version creation via API
    /*@Operation(
            summary ="Creates a new version",
            operationId = "deployVersion",
            notes = "Creates a new version for a project. Requires the `create_version` permission in the project or owning organization.",
            security = @SecurityRequirement(name = "Session"),
            tags = "Versions"
    )
    @ApiResponses({
            @ApiResponse(responseCode = 201, description = "Ok"),
            @ApiResponse(responseCode = 401, description = "Api session missing, invalid or expired"),
            @ApiResponse(responseCode = 403, description = "Not enough permissions to use this endpoint")
    })
    @PostMapping(path = "/projects/{author}/{slug}/versions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<Version> deployVersion()*/

    @Operation(
        summary = "Returns a specific version of a project",
        operationId = "showVersion",
        description = "Returns a specific version of a project. Requires the `view_public_info` permission in the project or owning organization.",
        security = @SecurityRequirement(name = "Session"),
        tags = "Version"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired"),
        @ApiResponse(responseCode = "403", description = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/projects/{author}/{slug}/versions/{name}")
    Version getVersion(@Parameter(description = "The author of the project to return the version for") @PathVariable String author,
                       @Parameter(description = "The slug of the project to return the version for") @PathVariable String slug,
                       @Parameter(description = "The name of the version to return") @PathVariable("name") String versionString);

    @Operation(
        summary = "Returns all versions of a project",
        operationId = "listVersions",
        description = "Returns all versions of a project. Requires the `view_public_info` permission in the project or owning organization.",
        security = @SecurityRequirement(name = "Session"),
        tags = "Versions"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired"),
        @ApiResponse(responseCode = "403", description = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/projects/{author}/{slug}/versions")
    PaginatedResult<Version> getVersions(@Parameter(description = "The author of the project to return versions for") @PathVariable String author,
                                         @Parameter(description = "The slug of the project to return versions for") @PathVariable String slug,
                                         @Parameter(description = "Pagination information") @NotNull RequestPagination pagination);

    @Operation(
        summary = "Returns the stats for a version",
        operationId = "showVersionStats",
        description = "Returns the stats (downloads) for a version per day for a certain date range. Requires the `is_subject_member` permission.",
        security = @SecurityRequirement(name = "Session"),
        tags = "Versions"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired"),
        @ApiResponse(responseCode = "403", description = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/projects/{author}/{slug}/versions/{name}/{platform}/stats")
    Map<String, VersionStats> getVersionStats(@Parameter(description = "The author of the version to return the stats for") @PathVariable String author,
                                              @Parameter(description = "The slug of the project to return stats for") @PathVariable String slug,
                                              @Parameter(description = "The version to return the stats for") @PathVariable("name") String versionString,
                                              @Parameter(description = "The platform of the version to return stats for") @PathVariable Platform platform,
                                              @Parameter(description = "The first date to include in the result", required = true) @RequestParam @NotNull OffsetDateTime fromDate,
                                              @Parameter(description = "The last date to include in the result", required = true) @RequestParam @NotNull OffsetDateTime toDate);

    @Operation(
        summary = "Downloads a version",
        operationId = "downloadVersion",
        description = "Downloads the file for a specific platform of a version. Requires visibility of the project and version.",
        security = @SecurityRequirement(name = "Session"),
        tags = "Versions"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "303", description = "Version has an external download url"),
        @ApiResponse(responseCode = "400", description = "Version doesn't have a file attached to it"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired"),
        @ApiResponse(responseCode = "403", description = "Not enough permissions to use this endpoint")
    })
    @GetMapping(value = "/projects/{author}/{slug}/versions/{name}/{platform}/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    Resource downloadVersion(@Parameter(description = "The author of the project to download the version from") @PathVariable String author,
                             @Parameter(description = "The slug of the project to download the version from") @PathVariable String slug,
                             @Parameter(description = "The name of the version to download") @PathVariable("name") String versionString,
                             @Parameter(description = "The platform of the version to download") @PathVariable Platform platform);
}
