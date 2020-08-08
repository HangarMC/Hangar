package me.minidigger.hangar.controller.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import me.minidigger.hangar.model.ApiAuthInfo;
import me.minidigger.hangar.model.generated.DeployVersionInfo;
import me.minidigger.hangar.model.generated.PaginatedVersionResult;
import me.minidigger.hangar.model.generated.Version;
import me.minidigger.hangar.model.generated.VersionStatsDay;
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

@Api(value = "versions", description = "the versions API", tags = "Versions")
@RequestMapping("/api/v2/")
public interface VersionsApi {

    @ApiOperation(value = "Creates a new version", nickname = "deployVersion", notes = "Creates a new version for a project. Requires the `create_version` permission in the project or owning organization.", response = Version.class, authorizations = {
            @Authorization(value = "Session")}, tags = "Versions")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Ok", response = Version.class),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")})
    @PostMapping(value = "/projects/{pluginId}/versions",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<Version> deployVersion(@ApiParam(value = "", required = true) @RequestParam(value = "plugin-info", required = true) DeployVersionInfo pluginInfo
            , @ApiParam(value = "file detail") @Valid @RequestPart("file") MultipartFile pluginFile
            , @ApiParam(value = "The plugin id of the project to create the version for", required = true) @PathVariable("pluginId") String pluginId
    );

    @ApiOperation(value = "Returns the versions of a project", nickname = "listVersions", notes = "Returns the versions of a project. Requires the `view_public_info` permission in the project or owning organization.", response = PaginatedVersionResult.class, authorizations = {
            @Authorization(value = "Session")}, tags = "Versions")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok", response = PaginatedVersionResult.class),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")})
    @GetMapping(value = "/projects/{pluginId}/versions",
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PaginatedVersionResult> listVersions(@ApiParam(value = "The plugin id of the project to return versions for", required = true) @PathVariable("pluginId") String pluginId
            , @ApiParam(value = "A list of tags all the returned versions should have. Should be formated either as `tagname` or `tagname:tagdata`.") @Valid @RequestParam(value = "tags", required = false) List<String> tags
            , @ApiParam(value = "The maximum amount of versions to return") @Valid @RequestParam(value = "limit", required = false) Long limit
            , @ApiParam(value = "Where to start returning", defaultValue = "0") @Valid @RequestParam(value = "offset", required = false, defaultValue = "0") Long offset
            , ApiAuthInfo apiAuthInfo
    );

    @ApiOperation(value = "Returns a specific version of a project", nickname = "showVersion", notes = "Returns a specific version of a project. Requires the `view_public_info` permission in the project or owning organization.", response = Version.class, authorizations = {
            @Authorization(value = "Session")}, tags = "Versions")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok", response = Version.class),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")})
    @GetMapping(value = "/projects/{pluginId}/versions/{name:.*}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Version> showVersion(@ApiParam(value = "The plugin id of the project to return the version for", required = true) @PathVariable("pluginId") String pluginId
            , @ApiParam(value = "The name of the version to return", required = true) @PathVariable("name") String name,
                                        ApiAuthInfo apiAuthInfo);

    @ApiOperation(value = "Returns the stats for a version", nickname = "showVersionStats", notes = "Returns the stats(downloads) for a version per day for a certain date range. Requires the `is_subject_member` permission.", response = VersionStatsDay.class, responseContainer = "Map", authorizations = {
            @Authorization(value = "Session")}, tags = "Versions")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok", response = VersionStatsDay.class, responseContainer = "Map"),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")})
    @GetMapping(value = "/projects/{pluginId}/versions/{version}/stats",
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Map<String, VersionStatsDay>> showVersionStats(
            @ApiParam(value = "The plugin id of the version to return the stats for", required = true) @PathVariable("pluginId") String pluginId,
            @ApiParam(value = "The version to return the stats for", required = true) @PathVariable("version") String version,
            @ApiParam(value = "The first date to include in the result", required = true) @RequestParam(value = "fromDate") @NotNull @Valid String fromDate,
            @ApiParam(value = "The last date to include in the result", required = true) @RequestParam(value = "toDate") @NotNull @Valid String toDate
    );
}
