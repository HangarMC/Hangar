package io.papermc.hangar.controller.api.v1.interfaces;

import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.project.version.Version;
import io.papermc.hangar.model.api.project.version.VersionStats;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PostMapping(value = "/projects/{author}/{slug}/versions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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
    @GetMapping(value = "/projects/{author}/{slug}/versions/{name:.*}")
    @PreAuthorize("@authenticationService.handleApiRequest(T(io.papermc.hangar.model.common.Permission).ViewPublicInfo, T(io.papermc.hangar.controller.extras.ApiScope).ofProject(#author, #slug))")
    ResponseEntity<Version> getVersion(@ApiParam("The author of the project to return the version for") @PathVariable String author,
                                       @ApiParam("The slug of the project to return") @PathVariable String slug,
                                       @ApiParam("The name of the version to return") @PathVariable String name
    );

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
    @PreAuthorize("@authenticationService.handleApiRequest(T(io.papermc.hangar.model.common.Permission).ViewPublicInfo, T(io.papermc.hangar.controller.extras.ApiScope).ofProject(#author, #slug))")
    ResponseEntity<PaginatedResult<Version>> getVersions(@ApiParam("The author of the project to return versions for") @PathVariable String author,
                                                         @ApiParam("The slug of the project to return versions for") @PathVariable String slug,
                                                         @ApiParam("A list of tags all the returned versions should have. Should be formatted either as `tagname` or `tagname:tagdata`.") @RequestParam(required = false) List<String> tags,
                                                         @ApiParam("Pagination information") @NotNull RequestPagination pagination
    );

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
    @GetMapping(value = "/projects/{author}/{slug}/versions/{version}/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("@authenticationService.handleApiRequest(T(io.papermc.hangar.model.common.Permission).IsProjectMember, T(io.papermc.hangar.controller.extras.ApiScope).ofProject(#author, #slug))")
    ResponseEntity<Map<String, VersionStats>> getVersionStats(@ApiParam("The author of the version to return the stats for") @PathVariable String author,
                                                              @ApiParam("The slug of the project to return stats for") @PathVariable String slug,
                                                              @ApiParam("The version to return the stats for") @PathVariable String version,
                                                              @ApiParam(value = "The first date to include in the result", required = true) @RequestParam @NotNull OffsetDateTime fromDate,
                                                              @ApiParam(value = "The last date to include in the result", required = true) @RequestParam @NotNull OffsetDateTime toDate
    );
}
