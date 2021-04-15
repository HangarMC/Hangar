package io.papermc.hangar.controller.api.v1.interfaces;

import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.project.version.Version;
import io.papermc.hangar.model.api.project.version.VersionStats;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.papermc.hangar.model.common.Platform;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Api(tags = "Versions", produces = MediaType.APPLICATION_JSON_VALUE)
@RequestMapping(path = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public interface IVersionsController {

    // TODO implement version creation via API
/*    @ApiOperation(
            value = "Creates a new version",
            nickname = "deployVersion",
            notes = "Creates a new version for a project. Requires the `create_version` permission in the project or owning organization.",
            authorizations = @Authorization("Session"),
            tags = "Versions"
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "Ok"),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")
    })
    @PostMapping(path = "/projects/{author}/{slug}/versions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<Version> deployVersion()*/

    @ApiOperation(
            value = "Returns a specific version of a project",
            nickname = "showVersion",
            notes = "Returns a specific version of a project. Requires the `view_public_info` permission in the project or owning organization.",
            authorizations = @Authorization("Session"),
            tags = "Versions"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/projects/{author}/{slug}/versions/{name}/{platform}/")
    Version getVersion(@ApiParam("The author of the project to return the version for") @PathVariable String author,
                       @ApiParam("The slug of the project to return") @PathVariable String slug,
                       @ApiParam("The name of the version to return") @PathVariable("name") String versionString,
                       @ApiParam("The platform of the version to return") @PathVariable Platform platform);

    @ApiOperation(
            value = "Returns versions of a project with the specified version string",
            nickname = "showVersion",
            notes = "Returns versions of a project with the specified version string. Requires the `view_public_info` permission in the project or owning organization.",
            authorizations = @Authorization("Session"),
            tags = "Versions"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/projects/{author}/{slug}/versions/{name}")
    List<Version> getVersions(@ApiParam("The author of the project to return the versions for") @PathVariable String author,
                              @ApiParam("The slug of the project to return") @PathVariable String slug,
                              @ApiParam("The name of the versions to return") @PathVariable("name") String versionString);

    @ApiOperation(
            value = "Returns the versions of a project",
            nickname = "listVersions",
            notes = "Returns the versions of a project. Requires the `view_public_info` permission in the project or owning organization.",
            authorizations = @Authorization("Session"),
            tags = "Versions"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/projects/{author}/{slug}/versions")
    PaginatedResult<Version> getVersions(@ApiParam("The author of the project to return versions for") @PathVariable String author,
                                         @ApiParam("The slug of the project to return versions for") @PathVariable String slug,
                                         @ApiParam("Pagination information") @NotNull RequestPagination pagination);

    @ApiOperation(
            value = "Returns the stats for a version",
            nickname = "showVersionStats",
            notes = "Returns the stats(downloads) for a version per day for a certain date range. Requires the `is_subject_member` permission.",
            responseContainer = "Map",
            authorizations = @Authorization("Session"),
            tags = "Versions"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/projects/{author}/{slug}/versions/{name}/{platform}/stats")
    Map<String, VersionStats> getVersionStats(@ApiParam("The author of the version to return the stats for") @PathVariable String author,
                                              @ApiParam("The slug of the project to return stats for") @PathVariable String slug,
                                              @ApiParam("The version to return the stats for") @PathVariable("name") String versionString,
                                              @ApiParam("The platform of the version to return") @PathVariable Platform platform,
                                              @ApiParam(value = "The first date to include in the result", required = true) @RequestParam @NotNull OffsetDateTime fromDate,
                                              @ApiParam(value = "The last date to include in the result", required = true) @RequestParam @NotNull OffsetDateTime toDate);

    @ApiOperation(
            value = "Downloads a version",
            nickname = "downloadVersion",
            notes = "Downloads the file for a specific platform of a version. Requires visibility of the project and version.",
            authorizations = @Authorization("Session"),
            tags = "Versions"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 303, message = "Version has an external download url"),
            @ApiResponse(code = 400, message = "Version doesn't have a file attached to it"),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")
    })
    @GetMapping("/projects/{author}/{slug}/versions/{name}/{platform}/download")
    FileSystemResource downloadVersion(@ApiParam("The author of the project to return the version for") @PathVariable String author,
                                       @ApiParam("The slug of the project to return") @PathVariable String slug,
                                       @ApiParam("The name of the version to return") @PathVariable("name") String versionString,
                                       @ApiParam("The platform of the version to return") @PathVariable Platform platform);
}
