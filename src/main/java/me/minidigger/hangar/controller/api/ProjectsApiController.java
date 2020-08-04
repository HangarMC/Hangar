package me.minidigger.hangar.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.minidigger.hangar.config.hangar.HangarConfig;
import me.minidigger.hangar.db.model.UsersTable;
import me.minidigger.hangar.model.Category;
import me.minidigger.hangar.model.Permission;
import me.minidigger.hangar.model.generated.PaginatedProjectResult;
import me.minidigger.hangar.model.generated.Pagination;
import me.minidigger.hangar.model.generated.Project;
import me.minidigger.hangar.model.generated.ProjectMember;
import me.minidigger.hangar.model.generated.ProjectSortingStrategy;
import me.minidigger.hangar.model.generated.ProjectStatsDay;
import me.minidigger.hangar.model.generated.Tag;
import me.minidigger.hangar.service.PermissionService;
import me.minidigger.hangar.service.UserService;
import me.minidigger.hangar.service.project.ProjectService;

import static me.minidigger.hangar.util.ApiUtil.limitOrDefault;
import static me.minidigger.hangar.util.ApiUtil.offsetOrZero;

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
    public ResponseEntity<PaginatedProjectResult> listProjects(String q, List<Category> categories, List<String> tags, String owner, ProjectSortingStrategy sort, boolean relevance, Long inLimit, Long inOffset) {
        // handle input
        long limit = limitOrDefault(inLimit, hangarConfig.getProjects().getInitLoad());
        long offset = offsetOrZero(inOffset);

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
        boolean seeHidden = permissionService.getGlobalPermissions(currentUser.getId()).has(Permission.SeeHidden);

        List<Project> projects = projectService.getProjects(
                null,
                categories,
                parsedTags,
                q,
                owner,
                seeHidden,
                currentUser.getId(),
                sort,
                relevance,
                limit,
                offset
        );

        long count = projectService.countProjects(
                null,
                categories,
                parsedTags,
                q,
                owner,
                seeHidden,
                currentUser.getId()
        );

        PaginatedProjectResult result = new PaginatedProjectResult();
        result.setPagination(new Pagination().limit(limit).offset(offset).count(count));
        result.setResult(projects);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ProjectMember> showMembers(String pluginId, Long inLimit, Long inOffset) {
        long limit = limitOrDefault(inLimit, hangarConfig.getProjects().getInitLoad());
        long offset = offsetOrZero(inOffset);

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
        return new ResponseEntity<>(projectService.getProjectApi(pluginId), HttpStatus.OK);
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
