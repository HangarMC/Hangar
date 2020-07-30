package me.minidigger.hangar.service.project;

import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.ProjectDao;
import me.minidigger.hangar.db.dao.UserDao;
import me.minidigger.hangar.db.model.ProjectPagesTable;
import me.minidigger.hangar.db.model.ProjectVersionsTable;
import me.minidigger.hangar.db.model.ProjectVisibilityChangesTable;
import me.minidigger.hangar.db.model.ProjectsTable;
import me.minidigger.hangar.db.model.UsersTable;
import me.minidigger.hangar.model.generated.Project;
import me.minidigger.hangar.model.generated.ProjectNamespace;
import me.minidigger.hangar.model.generated.ProjectSettings;
import me.minidigger.hangar.model.generated.UserActions;
import me.minidigger.hangar.model.viewhelpers.ProjectData;
import me.minidigger.hangar.model.viewhelpers.ProjectMember;
import me.minidigger.hangar.model.viewhelpers.ProjectViewSettings;
import me.minidigger.hangar.model.viewhelpers.ScopedProjectData;
import me.minidigger.hangar.service.UserService;
import me.minidigger.hangar.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final HangarDao<ProjectDao> projectDao;
    private final HangarDao<UserDao> userDao;
    private final UserService userService;

    @Autowired
    public ProjectService(HangarDao<ProjectDao> projectDao, HangarDao<UserDao> userDao, UserService userService) {
        this.projectDao = projectDao;
        this.userDao = userDao;
        this.userService = userService;
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
//        System.out.println(projectMembers.keySet().stream().findFirst().get().getRole().getPermissions().toNamed()); TODO REMOVE
        List<Object> flags = new ArrayList<>();
        int noteCount = 0;
        ProjectVisibilityChangesTable lastVisibilityChange = null;
        String lastVisibilityChangeUser = null;
        ProjectVersionsTable recommendedVersion = null;
        String iconUrl = "";
        long starCount = 0;
        long watcherCount = 0;
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

    public ProjectPagesTable getPage(long projectId, String slug) {
        // TODO get project page
        return new ProjectPagesTable(1, OffsetDateTime.now(), projectId, "Home", slug, "# Test\n This is a test", false, null);
    }

    public Project getProjectApi(String pluginId) {
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
}
