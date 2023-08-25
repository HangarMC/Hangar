package io.papermc.hangar.controller.api.v1.interfaces;

import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.project.version.UploadedVersion;
import io.papermc.hangar.model.api.project.version.Version;
import io.papermc.hangar.model.api.project.version.VersionStats;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.internal.versions.VersionUpload;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
        @ApiResponse(responseCode = "201", description = "Ok"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired"),
        @ApiResponse(responseCode = "403", description = "Not enough permissions to use this endpoint")
    })
    @PostMapping(path = "/projects/{slug}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    UploadedVersion uploadVersion(@Parameter(description = "The slug of the project to return versions for") @PathVariable String slug,
                       @Parameter(description = "The version files in order of selected platforms, if any") @RequestPart(required = false) @Size(max = 3, message = "version.new.error.invalidNumOfPlatforms") List<@Valid MultipartFile> files,
                       @Parameter(description = "Version data. See the VersionUpload schema for more info") @RequestPart @Valid VersionUpload versionUpload);

    @PostMapping(path = "/projects/{author}/{slug}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Deprecated(forRemoval = true)
    default UploadedVersion uploadVersion(@Parameter(description = "The author of the project to return versions for") @PathVariable String author,
                        @Parameter(description = "The slug of the project to return versions for") @PathVariable String slug,
                        @Parameter(description = "The version files in order of selected platforms, if any") @RequestPart(required = false) @Size(max = 3, message = "version.new.error.invalidNumOfPlatforms") List<@Valid MultipartFile> files,
                        @RequestPart @Valid VersionUpload versionUpload) {
       return this.uploadVersion(slug, files, versionUpload);
    }

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
    @GetMapping("/projects/{slug}/versions/{name}")
    Version getVersion(@Parameter(description = "The slug of the project to return the version for") @PathVariable String slug,
                       @Parameter(description = "The name of the version to return") @PathVariable("name") String versionString);

    @GetMapping("/projects/{author}/{slug}/versions/{name}")
    @Deprecated(forRemoval = true)
    default Version getVersion(@Parameter(description = "The author of the project to return the version for") @PathVariable String author,
                       @Parameter(description = "The slug of the project to return the version for") @PathVariable String slug,
                       @Parameter(description = "The name of the version to return") @PathVariable("name") String versionString) {
        return this.getVersion(slug, versionString);
    }

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
    @GetMapping("/projects/{slug}/versions")
    PaginatedResult<Version> getVersions(@Parameter(description = "The slug of the project to return versions for") @PathVariable String slug,
                                         @Parameter(description = "Pagination information") @NotNull RequestPagination pagination);

    @GetMapping("/projects/{author}/{slug}/versions")
    @Deprecated(forRemoval = true)
    default PaginatedResult<Version> getVersions(@Parameter(description = "The author of the project to return versions for") @PathVariable String author,
                                         @Parameter(description = "The slug of the project to return versions for") @PathVariable String slug,
                                         @Parameter(description = "Pagination information") @NotNull RequestPagination pagination) {
        return this.getVersions(slug, pagination);
    }

    @Operation(
        summary = "Returns the latest release version of a project",
        operationId = "latestReleaseVersion",
        description = "Returns the latest version of a project. Requires the `view_public_info` permission in the project or owning organizations.",
        security = @SecurityRequirement(name = "HangarAuth", scopes = "view_public_info"),
        tags = "Versions"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired"),
        @ApiResponse(responseCode = "403", description = "Not enough permissions to use this endpoint")
    })
    @GetMapping(value = "/projects/{slug}/latestrelease", produces = MediaType.TEXT_PLAIN_VALUE)
    String getLatestReleaseVersion(@Parameter(description = "The slug of the project to return the latest version for") @PathVariable String slug);

    @GetMapping(value = "/projects/{author}/{slug}/latestrelease", produces = MediaType.TEXT_PLAIN_VALUE)
    @Deprecated(forRemoval = true)
    default String getLatestReleaseVersion(@Parameter(description = "The author of the project to return the latest version for") @PathVariable String author,
                                         @Parameter(description = "The slug of the project to return the latest version for") @PathVariable String slug) {
        return this.getLatestReleaseVersion(slug);
    }

    @Operation(
        summary = "Returns the latest version of a project for a specific channel",
        operationId = "latestVersion",
        description = "Returns the latest version of a project. Requires the `view_public_info` permission in the project or owning organization.",
        security = @SecurityRequirement(name = "HangarAuth", scopes = "view_public_info"),
        tags = "Versions"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired"),
        @ApiResponse(responseCode = "403", description = "Not enough permissions to use this endpoint")
    })
    @GetMapping(value = "/projects/{slug}/latest", produces = MediaType.TEXT_PLAIN_VALUE)
    String getLatestVersion(@Parameter(description = "The slug of the project to return the latest version for") @PathVariable String slug,
                            @Parameter(description = "The channel to return the latest version for", required = true) @NotNull String channel);

    @GetMapping(value = "/projects/{author}/{slug}/latest", produces = MediaType.TEXT_PLAIN_VALUE)
    @Deprecated(forRemoval = true)
    default String getLatestVersion(@Parameter(description = "The author of the project to return the latest version for") @PathVariable String author,
                            @Parameter(description = "The slug of the project to return the latest version for") @PathVariable String slug,
                            @Parameter(description = "The channel to return the latest version for", required = true) @NotNull String channel) {
        return this.getLatestVersion(slug, channel);
    }

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
    @GetMapping("/projects/{slug}/versions/{name}/stats")
    Map<String, VersionStats> getVersionStats(@Parameter(description = "The slug of the project to return stats for") @PathVariable String slug,
                                              @Parameter(description = "The version to return the stats for") @PathVariable("name") String versionString,
                                              @Parameter(description = "The first date to include in the result", required = true) @RequestParam @NotNull OffsetDateTime fromDate,
                                              @Parameter(description = "The last date to include in the result", required = true) @RequestParam @NotNull OffsetDateTime toDate);

    @GetMapping("/projects/{author}/{slug}/versions/{name}/stats")
    @Deprecated(forRemoval = true)
    default Map<String, VersionStats> getVersionStats(@Parameter(description = "The author of the version to return the stats for") @PathVariable String author,
                                              @Parameter(description = "The slug of the project to return stats for") @PathVariable String slug,
                                              @Parameter(description = "The version to return the stats for") @PathVariable("name") String versionString,
                                              @Parameter(description = "The first date to include in the result", required = true) @RequestParam @NotNull OffsetDateTime fromDate,
                                              @Parameter(description = "The last date to include in the result", required = true) @RequestParam @NotNull OffsetDateTime toDate) {
        return this.getVersionStats(slug, versionString, fromDate, toDate);
    }

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
    @GetMapping(value = "/projects/{slug}/versions/{name}/{platform}/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    ResponseEntity<?> downloadVersion(@Parameter(description = "The slug of the project to download the version from") @PathVariable String slug,
                             @Parameter(description = "The name of the version to download") @PathVariable("name") String versionString,
                             @Parameter(description = "The platform of the version to download") @PathVariable Platform platform,
                             HttpServletResponse response
    );

    @GetMapping(value = "/projects/{author}/{slug}/versions/{name}/{platform}/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Deprecated(forRemoval = true)
    default ResponseEntity<?> downloadVersion(@Parameter(description = "The author of the project to download the version from") @PathVariable String author,
                             @Parameter(description = "The slug of the project to download the version from") @PathVariable String slug,
                             @Parameter(description = "The name of the version to download") @PathVariable("name") String versionString,
                             @Parameter(description = "The platform of the version to download") @PathVariable Platform platform,
                             HttpServletResponse response
    ) {
        return this.downloadVersion(slug, versionString, platform, response);
    }
}
