package me.minidigger.hangar.service.project;

import me.minidigger.hangar.model.generated.Project;
import me.minidigger.hangar.model.generated.ProjectNamespace;
import me.minidigger.hangar.model.generated.ProjectSettings;
import me.minidigger.hangar.model.generated.UserActions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.ProjectDao;
import me.minidigger.hangar.db.dao.UserDao;
import me.minidigger.hangar.db.model.ProjectPagesTable;
import me.minidigger.hangar.db.model.ProjectVersionsTable;
import me.minidigger.hangar.db.model.ProjectVisibilityChangesTable;
import me.minidigger.hangar.db.model.ProjectsTable;
import me.minidigger.hangar.db.model.UserProjectRolesTable;
import me.minidigger.hangar.db.model.UsersTable;
import me.minidigger.hangar.model.viewhelpers.ProjectData;

@Service
public class ProjectService {

    private final HangarDao<ProjectDao> projectDao;
    private final HangarDao<UserDao> userDao;

    @Autowired
    public ProjectService(HangarDao<ProjectDao> projectDao, HangarDao<UserDao> userDao) {
        this.projectDao = projectDao;
        this.userDao = userDao;
    }

    public ProjectData getProjectData(String author, String slug) {
        ProjectsTable projectsTable = projectDao.get().getBySlug(author, slug);
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
        Map<UsersTable, UserProjectRolesTable> members = new HashMap<>();
        List<Object> flags = new ArrayList<>();
        int noteCount = 0;
        ProjectVisibilityChangesTable lastVisibilityChange = null;
        String lastVisibilityChangeUser = null;
        ProjectVersionsTable recommendedVersion = null;
        String iconUrl = "";
        long starCount = 0;
        long watcherCount = 0;
        return new ProjectData(projectsTable, projectOwner, publicVersions, members, flags, noteCount, lastVisibilityChange, lastVisibilityChangeUser, recommendedVersion, iconUrl, starCount, watcherCount);
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
