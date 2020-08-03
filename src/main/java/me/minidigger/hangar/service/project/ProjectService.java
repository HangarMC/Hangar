package me.minidigger.hangar.service.project;

import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.ProjectDao;
import me.minidigger.hangar.db.dao.UserDao;
import me.minidigger.hangar.db.dao.VisibilityDao;
import me.minidigger.hangar.db.model.ProjectVersionsTable;
import me.minidigger.hangar.db.model.ProjectVisibilityChangesTable;
import me.minidigger.hangar.db.model.ProjectsTable;
import me.minidigger.hangar.db.model.UserProjectRolesTable;
import me.minidigger.hangar.db.model.UsersTable;
import me.minidigger.hangar.model.Visibility;
import me.minidigger.hangar.model.generated.Project;
import me.minidigger.hangar.model.generated.ProjectNamespace;
import me.minidigger.hangar.model.generated.ProjectSettings;
import me.minidigger.hangar.model.generated.UserActions;
import me.minidigger.hangar.model.viewhelpers.ProjectApprovalData;
import me.minidigger.hangar.model.viewhelpers.ProjectData;
import me.minidigger.hangar.model.viewhelpers.ProjectFlag;
import me.minidigger.hangar.model.viewhelpers.ProjectMember;
import me.minidigger.hangar.model.viewhelpers.ProjectViewSettings;
import me.minidigger.hangar.model.viewhelpers.ScopedProjectData;
import me.minidigger.hangar.model.viewhelpers.UserRole;
import me.minidigger.hangar.service.UserService;
import me.minidigger.hangar.util.StringUtils;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final HangarDao<ProjectDao> projectDao;
    private final HangarDao<UserDao> userDao;
    private final HangarDao<VisibilityDao> visibilityDao;
    private final UserService userService;
    private final FlagService flagService;

    @Autowired
    public ProjectService(HangarDao<ProjectDao> projectDao, HangarDao<UserDao> userDao, HangarDao<VisibilityDao> visibilityDao, UserService userService, FlagService flagService) {
        this.projectDao = projectDao;
        this.userDao = userDao;
        this.visibilityDao = visibilityDao;
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
        Map<ProjectMember, UsersTable> projectMembers = projectDao.get().getProjectMembers(projectsTable.getId());
        projectMembers.forEach(ProjectMember::setUser); // I don't know why the SQL query isn't doing this automatically...
        List<ProjectFlag> flags = flagService.getProjectFlags(projectsTable.getId());
        int noteCount = 0; // TODO a whole lot
        ProjectVisibilityChangesTable lastVisibilityChange = null;
        String lastVisibilityChangeUser = null;
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
                lastVisibilityChange,
                lastVisibilityChangeUser,
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
            return projectDao.get().getScopedProjectData(projectId, currentUser.getId());
        }
    }

    @Secured("ROLE_USER")
    public void changeVisibility(ProjectsTable project, Visibility newVisibility, String comment) {
        Preconditions.checkArgument(project != null && newVisibility != null, "project and visibility cannot be null");
        if (project.getVisibility() == newVisibility) return; // No change

        visibilityDao.get().updateLatestChange(userService.getCurrentUser().getId(), project.getId());

        visibilityDao.get().insert(new ProjectVisibilityChangesTable(project.getOwnerId(), project.getId(), comment, null, null, newVisibility.getValue()));

        project.setVisibility(newVisibility);
        projectDao.get().update(project);
        // TODO user action log
    }

    public List<UsersTable> getProjectWatchers(long projectId, int offset, int limit) {
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
        Project project = new Project();
        project.setCreatedAt(projectsTable.getCreatedAt());
        project.setPluginId(projectsTable.getPluginId());
        project.setName(projectsTable.getName());
        ProjectNamespace projectNamespace = new ProjectNamespace();
        projectNamespace.setOwner(userDao.get().getById(projectsTable.getOwnerId()).getName());
        projectNamespace.setSlug(projectsTable.getSlug());
        project.setNamespace(projectNamespace);

        project.setPromotedVersions(new ArrayList<>()); // TODO implement
        project.setStats(projectDao.get().getProjectStats(projectsTable.getId()));
        project.setCategory(projectsTable.getCategory());
        project.setDescription(projectsTable.getDescription());
        project.setLastUpdated(OffsetDateTime.now()); // TODO implement
        project.setVisibility(projectsTable.getVisibility());
        project.setUserActions(new UserActions()); // TODO implement
        project.setSettings(new ProjectSettings()); // TODO implement
        project.setIconUrl(""); // TODO implement
        return project;
    }

    public long getIdByPluginId(String pluginId) {
        return projectDao.get().getByPluginId(pluginId).getId();
    }

    public List<ProjectApprovalData> getProjectsNeedingApproval() {
        return projectDao.get().getVisibilityNeedsApproval();
    }

    public List<ProjectApprovalData> getProjectsWaitingForChanges() {
        return projectDao.get().getVisibilityWaitingProject();
    }
}
