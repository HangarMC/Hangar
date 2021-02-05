package io.papermc.hangar.controllerold.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.modelold.ApiAuthInfo;
import io.papermc.hangar.modelold.Platform;
import io.papermc.hangar.modelold.api.PlatformInfo;
import io.papermc.hangar.modelold.generated.DeployVersionInfo;
import io.papermc.hangar.modelold.generated.TagColor;
import io.papermc.hangar.modelold.generated.Version;
import io.papermc.hangar.serviceold.apiold.VersionApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class VersionsApiController implements VersionsApi {


    private static final Logger log = LoggerFactory.getLogger(VersionsApiController.class);

    private final HangarConfig hangarConfig;
    private final ObjectMapper objectMapper;
    private final ApiAuthInfo apiAuthInfo;
    private final VersionApiService versionApiService;

    @Autowired
    public VersionsApiController(HangarConfig hangarConfig, ObjectMapper objectMapper, ApiAuthInfo apiAuthInfo, VersionApiService versionApiService) {
        this.hangarConfig = hangarConfig;
        this.objectMapper = objectMapper;
        this.apiAuthInfo = apiAuthInfo;
        this.versionApiService = versionApiService;
    }

    @Override
    public ResponseEntity<Version> deployVersion(DeployVersionInfo pluginInfo, MultipartFile pluginFile, String author, String slug) {
        try {
            return new ResponseEntity<>(objectMapper.readValue("{\n  \"visibility\" : \"public\",\n  \"stats\" : {\n    \"downloads\" : 0\n  },\n  \"author\" : \"author\",\n  \"file_info\" : {\n    \"size_bytes\" : 6,\n    \"md_5_hash\" : \"md_5_hash\",\n    \"name\" : \"name\"\n  },\n  \"name\" : \"name\",\n  \"created_at\" : \"2000-01-23T04:56:07.000+00:00\",\n  \"description\" : \"description\",\n  \"dependencies\" : [ {\n    \"plugin_id\" : \"plugin_id\",\n    \"version\" : \"version\"\n  }, {\n    \"plugin_id\" : \"plugin_id\",\n    \"version\" : \"version\"\n  } ],\n  \"review_state\" : \"unreviewed\",\n  \"tags\" : [ {\n    \"data\" : \"data\",\n    \"color\" : {\n      \"background\" : \"background\",\n      \"foreground\" : \"foreground\"\n    },\n    \"name\" : \"name\"\n  }, {\n    \"data\" : \"data\",\n    \"color\" : {\n      \"background\" : \"background\",\n      \"foreground\" : \"foreground\"\n    },\n    \"name\" : \"name\"\n  } ]\n}", Version.class), HttpStatus.OK); // TODO Implement me
        } catch (IOException e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @PreAuthorize("@authenticationService.handleApiRequest(T(io.papermc.hangar.model.common.Permission).None, T(io.papermc.hangar.controller.extras.ApiScope).ofGlobal())")
    public ResponseEntity<List<PlatformInfo>> showPlatforms() {
        List<PlatformInfo> platformInfoList = new ArrayList<>();
        for (Platform platform : Platform.getValues()) {
            platformInfoList.add(new PlatformInfo(platform.getName(),
                    platform.getUrl(),
                    platform.getPlatformCategory(),
                    platform.getPossibleVersions(),
                    new TagColor().background(platform.getTagColor().getBackground()).foreground(platform.getTagColor().getForeground())));
        }
        return ResponseEntity.ok(platformInfoList);
    }

    // moved from versions controller

    @Override
    @PreAuthorize("@authenticationService.authApiRequest(T(io.papermc.hangar.model.common.Permission).ViewPublicInfo, T(io.papermc.hangar.controller.extras.ApiScope).ofProject(#author, #slug))")
    public Object download(String author, String slug, String name, String token) {
//        ProjectsTable project = projectsTable.get();
//        ProjectVersionsTable pvt = projectVersionsTable.get();
//        if (pvt.isExternal()) {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No jar for this version found");
//        }
//        if (token.isPresent()) {
//            confirmDownload0(DownloadType.JAR_FILE, token);
//            return sendJar(project, pvt, token.get(), true);
//        } else {
//            return sendJar(project, pvt, token.orElse(null), true);
//        }
        throw new ResponseStatusException(HttpStatus.I_AM_A_TEAPOT, "Not implemented");
    }

    @Override
    @PreAuthorize("@authenticationService.authApiRequest(T(io.papermc.hangar.model.common.Permission).ViewPublicInfo, T(io.papermc.hangar.controller.extras.ApiScope).ofProject(#author, #slug))")
    public Object downloadRecommended(String author, String slug, String token) {
//        ProjectsTable project = projectsTable.get();
//        ProjectVersionsTable recommendedVersion = versionService.getRecommendedVersion(project);
//        if (recommendedVersion == null) {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
//        } else {
//            return sendJar(project, recommendedVersion, token, true);
//        }
        throw new ResponseStatusException(HttpStatus.I_AM_A_TEAPOT, "Not implemented");
    }
}
