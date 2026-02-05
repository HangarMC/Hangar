package io.papermc.hangar.controller.api.v1.interfaces;

import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.project.version.UploadedVersion;
import io.papermc.hangar.model.api.project.version.Version;
import io.papermc.hangar.model.api.project.version.VersionStats;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import io.papermc.hangar.model.internal.versions.VersionUpload;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Versions")
@RequestMapping(path = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public interface IVersionsController {

    @Operation(
        summary = "Creates a new version and returns parts of its metadata",
        operationId = "uploadVersion",
        description = """
        Creates a new version for a project. Requires the `create_version` permission in the project or owning organization.
        Make sure you provide the contents of this request as multipart/form-data.
        You can find a simple example implementation written in Java here: https://gist.github.com/kennytv/a227d82249f54e0ad35005330256fee2""",
        security = @SecurityRequirement(name = "HangarAuth", scopes = "create_version"),
        tags = "Versions"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired"),
        @ApiResponse(responseCode = "403", description = "Not enough permissions to use this endpoint")
    })
    @RequestBody(content = @Content(encoding = @Encoding(name = "versionUpload", contentType = "application/json")))
    @PostMapping(path = "/projects/{slugOrId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    UploadedVersion uploadVersion(@Parameter(description = "The slug or id of the project to return versions for") @PathVariable("slugOrId") ProjectTable project,
                       @Parameter(description = "The version files in order of selected platforms, if any") @RequestPart(required = false, name = "files") @Size(max = 3, message = "version.new.error.invalidNumOfPlatforms") List<@Valid MultipartFile> files,
                       @Parameter(description = "Version data. See the VersionUpload schema for more info") @RequestPart("versionUpload") @Valid VersionUpload versionUpload);

    @PostMapping(path = "/projects/{author}/{slugOrId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Deprecated(forRemoval = true)
    UploadedVersion uploadVersion(@Parameter(description = "The author of the project to return versions for") @PathVariable String author,
                        @Parameter(description = "The slug or id of the project to return versions for") @PathVariable("slugOrId") ProjectTable project,
                        @Parameter(description = "The version files in order of selected platforms, if any") @RequestPart(required = false) @Size(max = 3, message = "version.new.error.invalidNumOfPlatforms") List<@Valid MultipartFile> files,
                        @RequestPart @Valid VersionUpload versionUpload);

    @Operation(
        summary = "Returns a specific version of a project",
        operationId = "showVersion",
        description = "Returns a specific version of a project. Requires the `view_public_info` permission in the project or owning organization.",
        security = @SecurityRequirement(name = "HangarAuth", scopes = "view_public_info"),
        tags = "Versions"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired"),
        @ApiResponse(responseCode = "403", description = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/projects/{slugOrId}/versions/{nameOrId}")
    Version getVersion(@Parameter(description = "The slug or id of the project to return the version for") @PathVariable("slugOrId") ProjectTable project,
                       @Parameter(description = "The name or id of the version to return") @PathVariable("nameOrId") ProjectVersionTable version);

    @GetMapping("/projects/{author}/{slugOrId}/versions/{nameOrId}")
    @Deprecated(forRemoval = true)
    Version getVersion(@Parameter(description = "The author of the project to return the version for") @PathVariable String author,
                       @Parameter(description = "The slug or id of the project to return the version for") @PathVariable("slugOrId") ProjectTable project,
                       @Parameter(description = "The name of the version to return") @PathVariable("nameOrId") ProjectVersionTable version);

    @Operation(
        summary = "Returns a specific version by its ID",
        operationId = "showVersionById",
        description = "Returns a specific version by its ID. Requires the `view_public_info` permission in the project or owning organization.",
        security = @SecurityRequirement(name = "HangarAuth", scopes = "view_public_info"),
        tags = "Versions"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired"),
        @ApiResponse(responseCode = "403", description = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/versions/{id}")
    Version getVersionById(@Parameter(description = "The id of the version to return") @PathVariable("id") ProjectVersionTable version);

    @Operation(
        summary = "Returns all versions of a project",
        operationId = "listVersions",
        description = "Returns all versions of a project. Requires the `view_public_info` permission in the project or owning organization.",
        security = @SecurityRequirement(name = "HangarAuth", scopes = "view_public_info"),
        tags = "Versions"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired"),
        @ApiResponse(responseCode = "403", description = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/projects/{slugOrId}/versions")
    PaginatedResult<Version> getVersions(@Parameter(description = "The slug or id of the project to return versions for") @PathVariable("slugOrId") ProjectTable project,
                                         @Parameter(description = "Pagination information") @NotNull RequestPagination pagination,
                                         @Parameter(description = "Whether to include hidden-by-default channels in the result, defaults to true") boolean includeHiddenChannels);

    @GetMapping("/projects/{author}/{slugOrId}/versions")
    @Deprecated(forRemoval = true)
    PaginatedResult<Version> getVersions(@Parameter(description = "The author of the project to return versions for") @PathVariable String author,
                                         @Parameter(description = "The slug or id of the project to return versions for") @PathVariable("slugOrId") ProjectTable project,
                                         @Parameter(description = "Pagination information") @NotNull RequestPagination pagination);

    @Operation(
        summary = "Returns the latest release version of a project",
        operationId = "latestReleaseVersion",
        description = "Returns the latest version of a project. Requires the `view_public_info` permission in the project or owning organizations.",
        security = @SecurityRequirement(name = "HangarAuth", scopes = "view_public_info"),
        tags = "Versions"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok", content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE, schema = @Schema(type = "string"))),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired"),
        @ApiResponse(responseCode = "403", description = "Not enough permissions to use this endpoint")
    })
    @GetMapping(value = "/projects/{slugOrId}/latestrelease", produces = MediaType.TEXT_PLAIN_VALUE)
    String getLatestReleaseVersion(@Parameter(description = "The slug or id of the project to return the latest version for") @PathVariable("slugOrId") ProjectTable project);

    @GetMapping(value = "/projects/{author}/{slugOrId}/latestrelease", produces = MediaType.TEXT_PLAIN_VALUE)
    @Deprecated(forRemoval = true)
    String getLatestReleaseVersion(@Parameter(description = "The author of the project to return the latest version for") @PathVariable String author,
                                         @Parameter(description = "The slug or id of the project to return the latest version for") @PathVariable("slugOrId") ProjectTable project);

    @Operation(
        summary = "Returns the latest version of a project for a specific channel",
        operationId = "latestVersion",
        description = "Returns the latest version of a project. Requires the `view_public_info` permission in the project or owning organization.",
        security = @SecurityRequirement(name = "HangarAuth", scopes = "view_public_info"),
        tags = "Versions"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok", content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE, schema = @Schema(type = "string"))),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired"),
        @ApiResponse(responseCode = "403", description = "Not enough permissions to use this endpoint")
    })
    @GetMapping(value = "/projects/{slugOrId}/latest", produces = MediaType.TEXT_PLAIN_VALUE)
    String getLatestVersion(@Parameter(description = "The slug or id of the project to return the latest version for") @PathVariable("slugOrId") ProjectTable project,
                            @Parameter(description = "The channel to return the latest version for", required = true) @NotNull String channel);

    @GetMapping(value = "/projects/{author}/{slugOrId}/latest", produces = MediaType.TEXT_PLAIN_VALUE)
    @Deprecated(forRemoval = true)
    String getLatestVersion(@Parameter(description = "The author of the project to return the latest version for") @PathVariable String author,
                            @Parameter(description = "The slug or id of the project to return the latest version for") @PathVariable("slugOrId") ProjectTable project,
                            @Parameter(description = "The channel to return the latest version for", required = true) @NotNull String channel);

    @Operation(
        summary = "Returns the stats for a version",
        operationId = "showVersionStats",
        description = "Returns the stats (downloads) for a version per day for a certain date range. Requires the `is_subject_member` permission.",
        security = @SecurityRequirement(name = "HangarAuth", scopes = "is_subject_member"),
        tags = "Versions"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired"),
        @ApiResponse(responseCode = "403", description = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/projects/{slugOrId}/versions/{nameOrId}/stats")
    Map<String, VersionStats> getVersionStats(@Parameter(description = "The slug or id of the project to return stats for") @PathVariable("slugOrId") ProjectTable project,
                                              @Parameter(description = "The version to return the stats for") @PathVariable("nameOrId") ProjectVersionTable version,
                                              @Parameter(description = "The first date to include in the result", required = true) @RequestParam @NotNull OffsetDateTime fromDate,
                                              @Parameter(description = "The last date to include in the result", required = true) @RequestParam @NotNull OffsetDateTime toDate);

    @GetMapping("/projects/{author}/{slugOrId}/versions/{nameOrId}/stats")
    @Deprecated(forRemoval = true)
    Map<String, VersionStats> getVersionStats(@Parameter(description = "The author of the version to return the stats for") @PathVariable String author,
                                              @Parameter(description = "The slug or id of the project to return stats for") @PathVariable("slugOrId") ProjectTable project,
                                              @Parameter(description = "The name or id of the version to return the stats for") @PathVariable("nameOrId") ProjectVersionTable version,
                                              @Parameter(description = "The first date to include in the result", required = true) @RequestParam @NotNull OffsetDateTime fromDate,
                                              @Parameter(description = "The last date to include in the result", required = true) @RequestParam @NotNull OffsetDateTime toDate);

    @Operation(
        summary = "Returns the stats for a version by its ID",
        operationId = "showVersionStatsById",
        description = "Returns the stats (downloads) for a version per day for a certain date range. Requires the `is_subject_member` permission.",
        security = @SecurityRequirement(name = "HangarAuth", scopes = "is_subject_member"),
        tags = "Versions"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired"),
        @ApiResponse(responseCode = "403", description = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/versions/{id}/stats")
    Map<String, VersionStats> getVersionStatsById(@Parameter(description = "The id of version to return the stats for") @PathVariable("id") ProjectVersionTable version,
                                              @Parameter(description = "The first date to include in the result", required = true) @RequestParam @NotNull OffsetDateTime fromDate,
                                              @Parameter(description = "The last date to include in the result", required = true) @RequestParam @NotNull OffsetDateTime toDate);

    @Operation(
        summary = "Downloads a version",
        operationId = "downloadVersion",
        description = "Downloads the file for a specific platform of a version. Requires visibility of the project and version.",
        security = @SecurityRequirement(name = "HangarAuth"),
        tags = "Versions"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "303", description = "Version has an external download url"),
        @ApiResponse(responseCode = "400", description = "Version doesn't have a file attached to it"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired"),
        @ApiResponse(responseCode = "403", description = "Not enough permissions to use this endpoint")
    })
    @GetMapping(value = "/projects/{slugOrId}/versions/{nameOrId}/{platform}/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    ResponseEntity<?> downloadVersion(@Parameter(description = "The slug or id of the project to download the version from") @PathVariable("slugOrId") ProjectTable project,
                             @Parameter(description = "The name of the version to download") @PathVariable("nameOrId") ProjectVersionTable version,
                             @Parameter(description = "The platform of the version to download") @PathVariable Platform platform,
                             HttpServletResponse response
    );

    @GetMapping(value = "/projects/{author}/{slugOrId}/versions/{nameOrId}/{platform}/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Deprecated(forRemoval = true)
    ResponseEntity<?> downloadVersion(@Parameter(description = "The author of the project to download the version from") @PathVariable String author,
                             @Parameter(description = "The slug or id of the project to download the version from") @PathVariable("slugOrId") ProjectTable project,
                             @Parameter(description = "The name of the version to download") @PathVariable("nameOrId") ProjectVersionTable version,
                             @Parameter(description = "The platform of the version to download") @PathVariable Platform platform,
                             HttpServletResponse response
    );

    @Operation(
        summary = "Downloads a version by its ID",
        operationId = "downloadVersionById",
        description = "Downloads the file for a specific platform of a version. Requires visibility of the project and version.",
        security = @SecurityRequirement(name = "HangarAuth"),
        tags = "Versions"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "303", description = "Version has an external download url"),
        @ApiResponse(responseCode = "400", description = "Version doesn't have a file attached to it"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired"),
        @ApiResponse(responseCode = "403", description = "Not enough permissions to use this endpoint")
    })
    @GetMapping(value = "/versions/{id}/{platform}/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    ResponseEntity<?> downloadVersionById(@Parameter(description = "The id of the version to download") @PathVariable("id") ProjectVersionTable version,
                                      @Parameter(description = "The platform of the version to download") @PathVariable Platform platform,
                                      HttpServletResponse response
    );
}
