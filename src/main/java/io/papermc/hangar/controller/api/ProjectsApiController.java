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
    public ResponseEntity<PaginatedProjectResult> listProjects(String q, List<Category> categories, List<String> tags, String owner, ProjectSortingStrategy sort, boolean relevance, Long inLimit, Long inOffset, ApiAuthInfo apiAuthInfo) {
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

        seeHidden = true; //TODO for testing
        String pluginId = "";
        if(categories == null){
            categories = List.of();
        }

        owner = "";
        String trimmedQuery = "";
        if(q != null && !q.isBlank()) {
            trimmedQuery = q.trim(); //Ore#APIV2Queries line 169
        }
        String sqlQuery = projectSelectFrag(pluginId, categories, tags, q, owner, seeHidden, requesterId);
        List<Project> projects = projectService.getProjects(
                sqlQuery,
                pluginId,
                categories,
                parsedTags,
                trimmedQuery,
                owner,
                seeHidden,
                requesterId,
                sort,
                relevance,
                limit,
                offset
        );

        long count = projectService.countProjects(
                sqlQuery,
                pluginId,
                categories,
                parsedTags,
                trimmedQuery,
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

    //Same name as in 'APIV2Queries' from ore
    public String projectSelectFrag(String pluginId, List<Category> categories, List<String> tags, String query, String owner, boolean canSeeHidden, Long currentUserId){

        String base = "SELECT p.created_at," +
                "       p.plugin_id," +
                "       p.name," +
                "       p.owner_name," +
                "       p.slug," +
                "       p.promoted_versions," +
                "       p.views," +
                "       p.downloads," +
                "       p.recent_views," +
                "       p.recent_downloads," +
                "       p.stars," +
                "       p.watchers," +
                "       p.category," +
                "       p.description," +
                "       COALESCE(p.last_updated, p.created_at) AS last_updated," +
                "       p.visibility, " +
                        getSQLStatementForStarsAndWatchers(currentUserId) +
                "       ps.homepage," +
                "       ps.issues," +
                "       ps.source," +
                "       ps.support," +
                "       ps.license_name," +
                "       ps.license_url," +
                "       ps.forum_sync" +
                "  FROM home_projects p" +
                "         JOIN projects ps ON p.id = ps.id ";


        String visibilityFrag = "";
        boolean notAllowedToSeeAllProjects = !canSeeHidden;
        if(notAllowedToSeeAllProjects) {
            visibilityFrag = " (p.visibility = 1 OR (:currentUserId = ANY(p.project_members) AND p.visibility != 5)) ";
        }

        String pluginIdCondition = "";
        if(pluginId != null && !pluginId.isBlank()){
            pluginIdCondition = " (p.plugin_id = :pluginId) ";
        }

        String categoryCondition = "";
        if(categories.size() > 0){
            categoryCondition = " (p.category in (<categories>)) ";
        }

        String tagsCondition = "";
        if(tags != null){
            String jsSelect = getSqlStatementForTags(tags);
        }

        String ownerCondition = "";
        if(owner != null && !owner.isBlank()){
            ownerCondition = " p.owner_name = :owner ";
        }

        String queryCondition = "";
        if(query != null && !query.isBlank()){
            if(query.endsWith(" ")) {
                queryCondition = "p.search_words @@ websearch_to_tsquery('english', :query)";
            } else {
                queryCondition = "p.search_words @@ websearch_to_tsquery_postfix('english', :query)";
            }
        }


        String filters = whereAndOpt(visibilityFrag, pluginIdCondition, categoryCondition, tagsCondition, ownerCondition, queryCondition);
        return base + filters;
    }

    private String getSQLStatementForStarsAndWatchers(Long currentUserId){
        if(currentUserId == null){
            return "FALSE, FALSE, ";
        }
        return "EXISTS(SELECT * FROM project_stars s WHERE s.project_id = p.id AND s.user_id = :currentUserId) AS user_stared, " +
            "EXISTS(SELECT * FROM project_watchers s WHERE s.project_id = p.id AND s.user_id = :currentUserId) AS user_watching, ";
    }

    //TODO: Tags seems to exist in ore as an combination of <tag_name, tag_version>, we have only a single string here not
    // clear yet how this string looks like and or we need to preprocess it.
    private String getSqlStatementForTags(List<String> tags){
        String baseStatement = " SELECT pv.tag_name FROM jsonb_to_recordset(p.promoted_versions) AS pv(tag_name TEXT, tag_version TEXT) ";
//        String whereWithData = " (pv.tag_name, pv.tag_version) in (<tags>) "; //TODO add data option to tags?
        String whereWithoutData = " (pv.tag_name) in (<tags>) ";
        String condition = baseStatement + whereAndOpt(whereWithoutData); //whereAndOpt(whereWithData, whereWithoutData);

        return " EXISTS ( " + condition + " ) ";
    }


    private String whereAndOpt(String ... optionalWhereStatements){
        StringBuilder sqlStatement = new StringBuilder();
        List<String> whereConditions = Arrays.stream(optionalWhereStatements)
                .filter((text) -> !text.isEmpty())
                .collect(Collectors.toList());

        if(whereConditions.size() > 0) {
            sqlStatement.append(" WHERE ");
            for (int i = 0; i < whereConditions.size(); i++) {
                sqlStatement.append(whereConditions.get(i));
                if((whereConditions.size()-1) > i ) { //As long as the size of it -1, is large than the i. It needs to add " AND "
                    sqlStatement.append(" AND ");
                }
            }
        }
        return sqlStatement.toString();
    }

}
