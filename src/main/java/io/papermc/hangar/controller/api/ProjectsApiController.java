package io.papermc.hangar.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.db.model.UsersTable;
import io.papermc.hangar.model.ApiAuthInfo;
import io.papermc.hangar.service.UserService;
import io.papermc.hangar.util.ApiUtil;
import io.papermc.hangar.model.Category;
import io.papermc.hangar.model.Permission;
import io.papermc.hangar.model.generated.PaginatedProjectResult;
import io.papermc.hangar.model.generated.Pagination;
import io.papermc.hangar.model.generated.Project;
import io.papermc.hangar.model.generated.ProjectMember;
import io.papermc.hangar.model.generated.ProjectSortingStrategy;
import io.papermc.hangar.model.generated.ProjectStatsDay;
import io.papermc.hangar.model.generated.Tag;
import io.papermc.hangar.service.PermissionService;
import io.papermc.hangar.service.project.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ProjectsApiController implements ProjectsApi {

    private static final Logger log = LoggerFactory.getLogger(ProjectsApiController.class);

    private final ObjectMapper objectMapper;
    private final ProjectService projectService;
    private final HangarConfig hangarConfig;
    private final UserService userService;
    private final PermissionService permissionService;

    @Autowired
    public ProjectsApiController(ObjectMapper objectMapper, ProjectService projectService, HangarConfig hangarConfig, UserService userService, PermissionService permissionService) {
        this.objectMapper = objectMapper;
        this.projectService = projectService;
        this.hangarConfig = hangarConfig;
        this.userService = userService;
        this.permissionService = permissionService;
    }

    @Override
    @PreAuthorize("@authenticationService.authApiRequest(T(io.papermc.hangar.model.Permission).ViewPublicInfo, T(io.papermc.hangar.controller.util.ApiScope).forGlobal())")
    public ResponseEntity<PaginatedProjectResult> listProjects(String q, List<Category> categories, List<String> tags, String owner, ProjectSortingStrategy sort, boolean orderWithRelevance, Long inLimit, Long inOffset, ApiAuthInfo apiAuthInfo) {
        // handle input
        long limit = ApiUtil.limitOrDefault(inLimit, hangarConfig.getProjects().getInitLoad());
        long offset = ApiUtil.offsetOrZero(inOffset);

        // parse tags
        List<Tag> parsedTags = new ArrayList<>();
        if (tags == null) {
            tags = new ArrayList<>();
        }
        for (String tag : tags) {
            String[] split = tag.split(":", 2);
            parsedTags.add(new Tag().name(split[0]).data(split.length > 1 ? split[1] : null));
        }

        UsersTable currentUser = userService.getCurrentUser();
        // TODO this is really meh, we want to save global permissions somewhere
        boolean seeHidden = currentUser != null && permissionService.getGlobalPermissions(currentUser.getId()).has(Permission.SeeHidden);
        Long requesterId = currentUser == null ? null : currentUser.getId();

        String pluginId = null;

        List<Project> projects = projectService.getProjects(
                pluginId,
                categories,
                parsedTags,
                q,
                owner,
                seeHidden,
                requesterId,
                sort,
                orderWithRelevance,
                limit,
                offset
        );

        long count = projectService.countProjects(
                pluginId,
                categories,
                parsedTags,
                q,
                owner,
                seeHidden,
                requesterId
        );

        PaginatedProjectResult result = new PaginatedProjectResult();
        result.setPagination(new Pagination().limit(limit).offset(offset).count(count));
        result.setResult(projects);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ProjectMember> showMembers(String pluginId, Long inLimit, Long inOffset) {
        long limit = ApiUtil.limitOrDefault(inLimit, hangarConfig.getProjects().getInitLoad());
        long offset = ApiUtil.offsetOrZero(inOffset);

        try {
            return new ResponseEntity<>(objectMapper.readValue("{\n  \"roles\" : [ {\n    \"color\" : \"color\",\n    \"name\" : \"name\",\n    \"title\" : \"title\"\n  }, {\n    \"color\" : \"color\",\n    \"name\" : \"name\",\n    \"title\" : \"title\"\n  } ],\n  \"user\" : \"user\"\n}", ProjectMember.class), HttpStatus.OK); // TODO Implement me
        } catch (IOException e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Override
    public ResponseEntity<Project> showProject(String pluginId) {
        Project project = projectService.getProjectApi(pluginId);
        if (project == null) {
            log.error("Couldn't find a project for that pluginId");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(project, HttpStatus.OK);
    }


    @Override
    public ResponseEntity<Map<String, ProjectStatsDay>> showProjectStats(String pluginId, LocalDate fromDate, LocalDate toDate) {
        try {
            return new ResponseEntity<Map<String, ProjectStatsDay>>(objectMapper.readValue("{\n  \"key\" : {\n    \"downloads\" : 0,\n    \"views\" : 6\n  }\n}", Map.class), HttpStatus.OK); // TODO Implement me
        } catch (IOException e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
