package io.papermc.hangar.controller.api;

import io.papermc.hangar.model.api.PlatformInfo;
import io.papermc.hangar.model.generated.DeployVersionInfo;
import io.papermc.hangar.model.generated.PaginatedVersionResult;
import io.papermc.hangar.model.generated.Version;
import io.papermc.hangar.model.generated.VersionStatsDay;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Api(value = "versions", tags = "Versions")
@RequestMapping({"/api", "/api/v1"})
public interface VersionsApi {

    @ApiOperation(
            value = "Creates a new version",
            nickname = "deployVersion",
            notes = "Creates a new version for a project. Requires the `create_version` permission in the project or owning organization.",
            response = Version.class,
            authorizations = {
                    @Authorization(value = "Session")
            },
            tags = "Versions"
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "Ok", response = Version.class),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")
    })
    @PostMapping(value = "/projects/{author}/{slug}/versions", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<Version> deployVersion(@ApiParam(value = "", required = true) @RequestParam(value = "plugin-info", required = true) DeployVersionInfo pluginInfo,
                                          @ApiParam(value = "file detail") @Valid @RequestPart("file") MultipartFile pluginFile,
                                          @ApiParam(value = "The author of the project to create the version for", required = true) @PathVariable("author") String author,
                                          @ApiParam(value = "The slug of the project to create the version for", required = true) @PathVariable("slug") String slug);

    @ApiOperation(
            value = "Returns the versions of a project",
            nickname = "listVersions",
            notes = "Returns the versions of a project. Requires the `view_public_info` permission in the project or owning organization.",
            response = PaginatedVersionResult.class,
            authorizations = {
                    @Authorization(value = "Session")
            },
            tags = "Versions"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok", response = PaginatedVersionResult.class),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")
    })
    @GetMapping(value = "/projects/{author}/{slug}/versions", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PaginatedVersionResult> listVersions(@ApiParam(value = "The author of the project to return versions for", required = true) @PathVariable("author") String author,
                                                        @ApiParam(value = "The slug of the project to return versions for", required = true) @PathVariable("slug") String slug,
                                                        @ApiParam(value = "A list of tags all the returned versions should have. Should be formated either as `tagname` or `tagname:tagdata`.") @Valid @RequestParam(value = "tags", required = false) List<String> tags,
                                                        @ApiParam(value = "The maximum amount of versions to return") @Valid @RequestParam(value = "limit", required = false) Long limit,
                                                        @ApiParam(value = "Where to start returning", defaultValue = "0") @Valid @RequestParam(value = "offset", required = false, defaultValue = "0") Long offset);

    @ApiOperation(
            value = "Returns a specific version of a project",
            nickname = "showVersion",
            notes = "Returns a specific version of a project. Requires the `view_public_info` permission in the project or owning organization.",
            response = Version.class,
            authorizations = {
                    @Authorization(value = "Session")
            },
            tags = "Versions"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok", response = Version.class),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")
    })
    @GetMapping(value = "/projects/{author}/{slug}/versions/{name:.*}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Version> showVersion(@ApiParam(value = "The author of the project to return the version for", required = true) @PathVariable("author") String author,
                                        @ApiParam(value = "The slug of the project to return", required = true) @PathVariable("slug") String slug,
                                        @ApiParam(value = "The name of the version to return", required = true) @PathVariable("name") String name);

    @ApiOperation(
            value = "Returns the stats for a version",
            nickname = "showVersionStats",
            notes = "Returns the stats(downloads) for a version per day for a certain date range. Requires the `is_subject_member` permission.",
            response = VersionStatsDay.class,
            responseContainer = "Map",
            authorizations = {
                    @Authorization(value = "Session")
            },
            tags = "Versions"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok", response = VersionStatsDay.class, responseContainer = "Map"),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")
    })
    @GetMapping(value = "/projects/{author}/{slug}/versions/{version}/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Map<String, VersionStatsDay>> showVersionStats(@ApiParam(value = "The author of the version to return the stats for", required = true) @PathVariable("author") String author,
                                                                  @ApiParam(value = "The slug of the project to return stats for", required = true) @PathVariable("slug") String slug,
                                                                  @ApiParam(value = "The version to return the stats for", required = true) @PathVariable("version") String version,
                                                                  @ApiParam(value = "The first date to include in the result", required = true) @RequestParam(value = "fromDate") @NotNull @Valid String fromDate,
                                                                  @ApiParam(value = "The last date to include in the result", required = true) @RequestParam(value = "toDate") @NotNull @Valid String toDate);

    @ApiOperation(
            value = "Downloads the recommended version",
            nickname = "downloadRecommended",
            notes = "Downloads the file of the recommended version of this project",
            response = Object.class,
            authorizations = {
                    @Authorization(value = "Session")
            },
            tags = "Versions"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok", response = Object.class),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")
    })
    @GetMapping(value = "/projects/{author}/{slug}/versions/recommended/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    Object downloadRecommended(@ApiParam(value = "The author of the version to return the stats for", required = true) @PathVariable("author") String author,
                               @ApiParam(value = "The slug of the project to return stats for", required = true) @PathVariable("slug") String slug,
                               @ApiParam(value = "The download token")  @RequestParam(required = false) String token);

    @ApiOperation(
            value = "Downloads the version",
            nickname = "download",
            notes = "Downloads the file of the given version of this project",
            response = Object.class,
            authorizations = {
                    @Authorization(value = "Session")
            },
            tags = "Versions"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok", response = Object.class),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")
    })
    @GetMapping(value = "/projects/{author}/{slug}/versions/{name}/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    Object download(@ApiParam(value = "The author of the version to return the stats for", required = true) @PathVariable("author") String author,
                    @ApiParam(value = "The slug of the project to return stats for", required = true) @PathVariable("slug") String slug,
                    @ApiParam(value = "The name of the version", required = true) @PathVariable("name") String name,
                    @ApiParam(value = "The download token")  @RequestParam(required = false) String token);

    @ApiOperation(
            value = "Returns a list of platforms and their information",
            nickname = "showPlatforms",
            notes = "Returns a list of platforms and their information. Used internally for dependency selection.",
            response = PlatformInfo.class,
            responseContainer = "List",
            hidden = true,
            authorizations = {
                    @Authorization("Session")
            },
            tags = "Versions"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok", response = PlatformInfo.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")
    })
    @GetMapping(value = "/platforms", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<PlatformInfo>> showPlatforms();
}
