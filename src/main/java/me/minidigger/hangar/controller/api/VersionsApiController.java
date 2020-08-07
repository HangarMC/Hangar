package me.minidigger.hangar.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.api.ApiVersionsDao;
import me.minidigger.hangar.model.generated.DeployVersionInfo;
import me.minidigger.hangar.model.generated.PaginatedVersionResult;
import me.minidigger.hangar.model.generated.Pagination;
import me.minidigger.hangar.model.generated.Version;
import me.minidigger.hangar.model.generated.VersionStatsDay;
import me.minidigger.hangar.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
public class VersionsApiController implements VersionsApi {


    private static final Logger log = LoggerFactory.getLogger(VersionsApiController.class);

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    private final HangarDao<ApiVersionsDao> apiVersionsDao;
    private final UserService userService;

    @Autowired
    public VersionsApiController(ObjectMapper objectMapper, HttpServletRequest request, HangarDao<ApiVersionsDao> apiVersionsDao, UserService userService) {
        this.objectMapper = objectMapper;
        this.request = request;
        this.apiVersionsDao = apiVersionsDao;
        this.userService = userService;
    }

    @Override
    public ResponseEntity<Version> deployVersion(DeployVersionInfo pluginInfo, MultipartFile pluginFile, String pluginId) {
        try {
            return new ResponseEntity<>(objectMapper.readValue("{\n  \"visibility\" : \"public\",\n  \"stats\" : {\n    \"downloads\" : 0\n  },\n  \"author\" : \"author\",\n  \"file_info\" : {\n    \"size_bytes\" : 6,\n    \"md_5_hash\" : \"md_5_hash\",\n    \"name\" : \"name\"\n  },\n  \"name\" : \"name\",\n  \"created_at\" : \"2000-01-23T04:56:07.000+00:00\",\n  \"description\" : \"description\",\n  \"dependencies\" : [ {\n    \"plugin_id\" : \"plugin_id\",\n    \"version\" : \"version\"\n  }, {\n    \"plugin_id\" : \"plugin_id\",\n    \"version\" : \"version\"\n  } ],\n  \"review_state\" : \"unreviewed\",\n  \"tags\" : [ {\n    \"data\" : \"data\",\n    \"color\" : {\n      \"background\" : \"background\",\n      \"foreground\" : \"foreground\"\n    },\n    \"name\" : \"name\"\n  }, {\n    \"data\" : \"data\",\n    \"color\" : {\n      \"background\" : \"background\",\n      \"foreground\" : \"foreground\"\n    },\n    \"name\" : \"name\"\n  } ]\n}", Version.class), HttpStatus.OK); // TODO Implement me
        } catch (IOException e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Override
    public ResponseEntity<PaginatedVersionResult> listVersions(String pluginId, List<String> tags, Long limit, Long offset) {
        // TODO handling users
        List<Version> versions = apiVersionsDao.get().listVersions(pluginId, null, tags, false, limit, offset, null);
        int count = apiVersionsDao.get().listVersions(pluginId, null,tags, false, limit, offset, null).size();
        return new ResponseEntity<>(new PaginatedVersionResult().result(versions).pagination(new Pagination().limit(limit).offset(offset).count((long) count)), HttpStatus.OK);
    }


    @Override
    public ResponseEntity<Version> showVersion(String pluginId, String name) {
        // TODO handling users
        Version version = apiVersionsDao.get().listVersions(pluginId, name, null, false, 1L, 0, null).stream().findFirst().orElse(null);
        if (version == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(version, HttpStatus.OK);
        }
    }


    @Override
    public ResponseEntity<Map<String, VersionStatsDay>> showVersionStats(String pluginId, String version, LocalDate fromDate, LocalDate toDate) {
        try {
            return new ResponseEntity<Map<String, VersionStatsDay>>(objectMapper.readValue("{\n  \"key\" : {\n    \"downloads\" : 0\n  }\n}", Map.class), HttpStatus.OK); // TODO Implement me
        } catch (IOException e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
