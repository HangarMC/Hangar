package io.papermc.hangar.controllerold.api;

import io.papermc.hangar.modelold.api.PlatformInfo;
import io.papermc.hangar.modelold.generated.DeployVersionInfo;
import io.papermc.hangar.modelold.generated.Version;
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
import java.util.List;

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
