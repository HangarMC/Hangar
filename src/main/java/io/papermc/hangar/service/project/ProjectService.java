package io.papermc.hangar.service.project;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.ProjectDao;
import io.papermc.hangar.db.dao.UserDao;
import io.papermc.hangar.db.dao.ProjectViewDao;
import io.papermc.hangar.db.dao.api.ProjectApiDao;
import io.papermc.hangar.db.model.ProjectVersionsTable;
import io.papermc.hangar.db.model.ProjectVisibilityChangesTable;
import io.papermc.hangar.db.model.ProjectsTable;
import io.papermc.hangar.db.model.UserProjectRolesTable;
import io.papermc.hangar.db.model.UsersTable;
import io.papermc.hangar.model.Role;
import io.papermc.hangar.model.Visibility;
import io.papermc.hangar.model.generated.*;
import io.papermc.hangar.model.viewhelpers.ProjectData;
import io.papermc.hangar.model.viewhelpers.ProjectFlag;
import io.papermc.hangar.service.UserService;
import io.papermc.hangar.util.StringUtils;
import io.papermc.hangar.db.dao.VisibilityDao;
import io.papermc.hangar.model.Category;
import io.papermc.hangar.model.viewhelpers.ProjectApprovalData;
import io.papermc.hangar.model.viewhelpers.ProjectViewSettings;
import io.papermc.hangar.model.viewhelpers.ScopedProjectData;
import io.papermc.hangar.model.viewhelpers.UnhealthyProject;
import io.papermc.hangar.model.viewhelpers.UserRole;

import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final HangarConfig hangarConfig;
    private final HangarDao<ProjectDao> projectDao;
    private final HangarDao<UserDao> userDao;
    private final HangarDao<VisibilityDao> visibilityDao;
    private final HangarDao<ProjectApiDao> projectApiDao;
    private final HangarDao<ProjectViewDao> projectViewDao;
    private final UserService userService;
    private final FlagService flagService;

    @Autowired
    public ProjectService(HangarConfig hangarConfig, HangarDao<ProjectDao> projectDao, HangarDao<UserDao> userDao, HangarDao<VisibilityDao> visibilityDao, HangarDao<ProjectApiDao> projectApiDao, HangarDao<ProjectViewDao> projectViewDao, UserService userService, FlagService flagService) {
        this.hangarConfig = hangarConfig;
        this.projectDao = projectDao;
        this.userDao = userDao;
        this.visibilityDao = visibilityDao;
        this.projectApiDao = projectApiDao;
        this.projectViewDao = projectViewDao;
        this.userService = userService;
        this.flagService = flagService;
    }

    public ProjectData getProjectData(String author, String slug) {
        ProjectsTable projectsTable = projectDao.get().getBySlug(author, StringUtils.slugify(slug));
        if (projectsTable == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return getProjectData(projectsTable);

    }

    public List<ProjectData> getProjectsData(long id) {
        List<ProjectsTable> projectsTables = projectDao.get().getProjectsByUserId(id);
        return projectsTables.stream().map(this::getProjectData).collect(Collectors.toList());
    }

    public ProjectData getProjectData(ProjectsTable projectsTable) {
        UsersTable projectOwner = userDao.get().getById(projectsTable.getOwnerId());
        if (projectOwner == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        int publicVersions = 0;
        Map<UserProjectRolesTable, UsersTable> projectMembers = projectDao.get().getProjectMembers(projectsTable.getId());
        List<ProjectFlag> flags = flagService.getProjectFlags(projectsTable.getId());
        int noteCount = 0; // TODO a whole lot
        Map.Entry<String, ProjectVisibilityChangesTable> latestProjectVisibilityChangeWithUser = visibilityDao.get().getLatestProjectVisibilityChange(projectsTable.getId());
        ProjectVersionsTable recommendedVersion = null;
        String iconUrl = "";
        long starCount = userDao.get().getProjectStargazers(projectsTable.getId(), 0, null).size();
        long watcherCount = userDao.get().getProjectWatchers(projectsTable.getId(), 0, null).size();
        ProjectViewSettings settings = new ProjectViewSettings(
                projectsTable.getKeywords(),
                projectsTable.getHomepage(),
                projectsTable.getIssues(),
                projectsTable.getSource(),
                projectsTable.getSupport(),
                projectsTable.getLicenseName(),
                projectsTable.getLicenseUrl(),
                projectsTable.getForumSync()
        );
        return new ProjectData(projectsTable,
                projectOwner,
                publicVersions,
                projectMembers,
                flags,
                noteCount,
                latestProjectVisibilityChangeWithUser != null ? latestProjectVisibilityChangeWithUser.getValue() : null,
                latestProjectVisibilityChangeWithUser != null ? latestProjectVisibilityChangeWithUser.getKey() : null,
                recommendedVersion,
                iconUrl,
                starCount,
                watcherCount,
                settings
        );
    }

    public ScopedProjectData getScopedProjectData(long projectId) {
        UsersTable currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return new ScopedProjectData();
        } else {
            ScopedProjectData sp = projectDao.get().getScopedProjectData(projectId, currentUser.getId());
            return sp == null ? new ScopedProjectData() : sp;
        }
    }

    @Secured("ROLE_USER")
    public void changeVisibility(ProjectsTable project, Visibility newVisibility, String comment) {
        Preconditions.checkNotNull(project, "project");
        Preconditions.checkNotNull(newVisibility, "newVisibility");
        if (project.getVisibility() == newVisibility) return; // No change

        visibilityDao.get().updateLatestProjectChange(userService.getCurrentUser().getId(), project.getId());

        visibilityDao.get().insert(new ProjectVisibilityChangesTable(project.getOwnerId(), project.getId(), comment, null, null, newVisibility));

        project.setVisibility(newVisibility);
        projectDao.get().update(project);
    }

    public List<UsersTable> getProjectWatchers(long projectId, int offset, Integer limit) {
        return userDao.get().getProjectWatchers(projectId, offset, limit);
    }

    public List<UsersTable> getProjectStargazers(long projectId, int offset, int limit) {
        return userDao.get().getProjectStargazers(projectId, offset, limit);
    }

    public Map<ProjectData, UserRole<UserProjectRolesTable>> getProjectsAndRoles(long userId) {
        Map<ProjectsTable, UserProjectRolesTable> dbMap = projectDao.get().getProjectsAndRoles(userId);
        Map<ProjectData, UserRole<UserProjectRolesTable>> map = new HashMap<>();
        dbMap.forEach((projectsTable, role) -> map.put(getProjectData(projectsTable), new UserRole<>(role)));
        return map;
    }

    public Project getProjectApi(String pluginId) { // TODO still probably have to work out a standard for how to handle the api models
        ProjectsTable projectsTable = projectDao.get().getByPluginId(pluginId);
        if (projectsTable == null) return null;
        //Increasing the view count by one for this project
        projectViewDao.get().increaseView(projectsTable.getId()); //Refresh home_project view?

        Project project = new Project();
        project.setCreatedAt(projectsTable.getCreatedAt());
        project.setPluginId(projectsTable.getPluginId());
        project.setName(projectsTable.getName());
        ProjectNamespace projectNamespace = new ProjectNamespace();
        projectNamespace.setOwner(userDao.get().getById(projectsTable.getOwnerId()).getName());
        projectNamespace.setSlug(projectsTable.getSlug());
        project.setNamespace(projectNamespace);

//        project.setPromotedVersions(new ArrayList<>()); // TODO implement
        project.setStats(projectDao.get().getProjectStats(projectsTable.getId()));
        project.setCategory(projectsTable.getCategory());
        project.setDescription(projectsTable.getDescription());
        project.setLastUpdated(OffsetDateTime.now()); // TODO implement
        project.setVisibility(projectsTable.getVisibility());
        project.setUserActions(new UserActions()); // TODO implement
        project.setIconUrl(""); // TODO implement

        ProjectSettings projectSettings = new ProjectSettings();
        projectSettings.setHomepage(projectsTable.getHomepage());
        projectSettings.setIssues(projectsTable.getIssues());
        projectSettings.setSources(projectsTable.getSource());
        projectSettings.setSupport(projectsTable.getSupport());
        projectSettings.setForumSync(projectsTable.getForumSync());

        ProjectLicense projectLicense = new ProjectLicense();
        projectLicense.setName(projectsTable.getLicenseName());
        projectLicense.setUrl(projectsTable.getLicenseUrl());
        projectSettings.setLicense(projectLicense);
        project.setSettings(projectSettings);
        return project;
    }

    public List<ProjectApprovalData> getProjectsNeedingApproval() {
        return projectDao.get().getVisibilityNeedsApproval();
    }

    public List<ProjectApprovalData> getProjectsWaitingForChanges() {
        return projectDao.get().getVisibilityWaitingProject();
    }

    public List<UnhealthyProject> getUnhealthyProjects() {
        return projectDao.get().getUnhealthyProjects(hangarConfig.projects.getStaleAge().toMillis());
    }

    // TODO move to API daos
    public List<Project> getProjects(String pluginId, List<Category> categories, List<Tag> tags, String query, String owner, boolean seeHidden, Long requesterId, ProjectSortingStrategy sort, boolean orderWithRelevance, long limit, long offset) {
        String ordering = sort.getSql();
        if (orderWithRelevance && query != null && !query.isEmpty()) {
            String relevance = "ts_rank(p.search_words, websearch_to_tsquery_postfix('english', :query)) DESC";
            if(query.endsWith(" ")) {
                relevance = "ts_rank(p.search_words, websearch_to_tsquery('english', :query)) DESC";
            }
            String orderingFirstHalf;
            // 1483056000 is the Ore epoch
            // 86400 seconds to days
            // 604800‬ seconds to weeks
            switch(sort){
                case STARS: orderingFirstHalf = "p.starts * "; break;
                case DOWNLOADS: orderingFirstHalf ="(p.downloads / 100) * "; break;
                case VIEWS: orderingFirstHalf ="(p.views / 200) *"; break;
                case NEWEST: orderingFirstHalf ="((EXTRACT(EPOCH FROM p.created_at) - 1483056000) / 86400) *"; break;
                case UPDATED: orderingFirstHalf ="((EXTRACT(EPOCH FROM p.last_updated) - 1483056000) / 604800) *"; break;
                case ONLY_RELEVANCE: orderingFirstHalf =""; break;
                case RECENT_DOWNLOADS : orderingFirstHalf ="p.recent_views *"; break;
                case RECENT_VIEWS: orderingFirstHalf ="p.recent_downloads*"; break;
                default:
                    orderingFirstHalf = " "; //Just in case and so that the ide doesnt complain
            }
            ordering = orderingFirstHalf + relevance;
        }
        return projectApiDao.get().listProjects(pluginId, owner, seeHidden, requesterId, ordering, getCategoryNumbers(categories), getTagsNames(tags), trimQuery(query), getQueryStatement(query), limit, offset);
    }

    public long countProjects(String pluginId, List<Category> categories, List<Tag> tags, String query, String owner, boolean seeHidden, Long requesterId) {
        return projectApiDao.get().countProjects(pluginId, owner, seeHidden, requesterId, getCategoryNumbers(categories), getTagsNames(tags), trimQuery(query), getQueryStatement(query));
    }

    private String trimQuery(String query){
        String trimmedQuery = null;
        if(query != null && !query.isBlank()) {
            trimmedQuery = query.trim(); //Ore#APIV2Queries line 169 && 200
        }
        return trimmedQuery;
    }

    private String getQueryStatement(String query){
        String queryStatement = null;
        if(query != null && !query.isBlank()){
            if(query.endsWith(" ")) {
                queryStatement = "p.search_words @@ websearch_to_tsquery('english', :query)";
            } else {
                queryStatement =  "p.search_words @@ websearch_to_tsquery_postfix('english', :query)";
            }
        }
        return queryStatement;
    }

    private List<Integer> getCategoryNumbers(List<Category> categories){
        return categories == null ? null : categories.stream().map(Category::getValue).collect(Collectors.toList());
    }

    private List<String> getTagsNames(List<Tag> tags){
        return tags == null ? null : tags.stream().filter(tag -> tag.getData() == null).map(Tag::getName).collect(Collectors.toList());
    }

    private List<String> getTagsNamesAndVersion(List<Tag> tags){
        return tags == null ? null :  tags.stream().filter(tag -> tag.getData() != null).map(tag -> " (" + tag.getName() + "," +  tag.getData() + ") ").collect(Collectors.toList());
    }
}
