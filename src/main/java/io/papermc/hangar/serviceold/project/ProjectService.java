package io.papermc.hangar.serviceold.project;

import com.fasterxml.jackson.databind.node.ArrayNode;
import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.daoold.GeneralDao;
import io.papermc.hangar.db.daoold.ProjectDao;
import io.papermc.hangar.db.daoold.UserDao;
import io.papermc.hangar.db.daoold.VisibilityDao;
import io.papermc.hangar.db.modelold.ProjectVersionsTable;
import io.papermc.hangar.db.modelold.ProjectVisibilityChangesTable;
import io.papermc.hangar.db.modelold.ProjectsTable;
import io.papermc.hangar.db.modelold.UserProjectRolesTable;
import io.papermc.hangar.db.modelold.UsersTable;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.modelold.viewhelpers.ProjectData;
import io.papermc.hangar.modelold.viewhelpers.ProjectFlag;
import io.papermc.hangar.modelold.viewhelpers.ProjectMissingFile;
import io.papermc.hangar.modelold.viewhelpers.ProjectViewSettings;
import io.papermc.hangar.modelold.viewhelpers.ScopedProjectData;
import io.papermc.hangar.modelold.viewhelpers.UnhealthyProject;
import io.papermc.hangar.modelold.viewhelpers.UserRole;
import io.papermc.hangar.service.PermissionService;
import io.papermc.hangar.service.internal.uploads.ProjectFiles;
import io.papermc.hangar.serviceold.HangarService;
import io.papermc.hangar.util.RequestUtil;
import io.papermc.hangar.util.Routes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service("oldProjectService")
public class ProjectService extends HangarService {

    private final HangarConfig hangarConfig;
    private final HangarDao<ProjectDao> projectDao;
    private final HangarDao<UserDao> userDao;
    private final HangarDao<VisibilityDao> visibilityDao;
    private final HangarDao<GeneralDao> generalDao;
    private final PermissionService permissionService;
    private final ProjectFiles projectFiles;

    private final HttpServletRequest request;

    @Autowired
    public ProjectService(HangarConfig hangarConfig, HangarDao<ProjectDao> projectDao, HangarDao<UserDao> userDao, HangarDao<VisibilityDao> visibilityDao, HangarDao<GeneralDao> generalDao, ProjectFiles projectFiles, PermissionService permissionService, HttpServletRequest request) {
        this.hangarConfig = hangarConfig;
        this.projectDao = projectDao;
        this.userDao = userDao;
        this.visibilityDao = visibilityDao;
        this.generalDao = generalDao;
        this.projectFiles = projectFiles;
        this.permissionService = permissionService;
        this.request = request;
    }

    @Bean
    @RequestScope
    public Supplier<ProjectsTable> projectsTable() {
        Map<String, String> pathParams = RequestUtil.getPathParams(request);
        ProjectsTable pt;
        if (pathParams.keySet().containsAll(Set.of("author", "slug"))) {
            pt = this.getProjectsTable(pathParams.get("author"), pathParams.get("slug"));
        } else {
            return () -> null;
        }
        if (pt == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return () -> pt;
    }

    @Bean
    @RequestScope
    public Supplier<ProjectData> projectData() {
        //noinspection SpringConfigurationProxyMethods
        return () -> this.getProjectData(projectsTable().get());
    }

    public void refreshHomePage() {
        // TODO logging
        generalDao.get().refreshHomeProjects();
    }

    public ProjectData getProjectData(ProjectsTable projectsTable) {
        UsersTable projectOwner = userDao.get().getById(projectsTable.getOwnerId());
        if (projectOwner == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        int publicVersions = 0;
        Map<UserProjectRolesTable, UsersTable> projectMembers = projectDao.get().getProjectMembers(projectsTable.getId());
        List<ProjectFlag> flags = /*flagService.getProjectFlags(projectsTable.getId());*/ new ArrayList<>();
        ArrayNode messages = (ArrayNode) projectsTable.getNotes().getJson().get("messages");
        int noteCount;
        if (messages == null) {
            noteCount = 0;
        } else {
            noteCount = messages.size();
        }
        Map.Entry<String, ProjectVisibilityChangesTable> latestProjectVisibilityChangeWithUser = visibilityDao.get().getLatestProjectVisibilityChange(projectsTable.getId());
//        ProjectVersionsTable recommendedVersion = visibilityService.checkVisibility(recommendedVersionService.getRecommendedVersion(projectsTable), ProjectVersionsTable::getProjectId);
        ProjectVersionsTable recommendedVersion = null;
        String iconUrl = Routes.PROJECTS_SHOW_ICON.getRouteUrl(projectsTable.getOwnerName(), projectsTable.getSlug());
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
        Optional<UsersTable> curUser = currentUser.get();
        if (curUser.isEmpty()) {
            return new ScopedProjectData();
        } else {
            ScopedProjectData sp = projectDao.get().getScopedProjectData(projectId, curUser.get().getId());
            if (sp == null) {
                return new ScopedProjectData();
            }
            sp.setPermissions(permissionService.getProjectPermissions(curUser.get().getId(), projectId));
            return sp;
        }
    }

    public ProjectsTable getProjectsTable(long projectId) {
        return null;
//        return visibilityService.checkVisibility(projectDao.get().getById(projectId), ProjectsTable::getId);
    }

    public ProjectsTable getProjectsTable(String author, String name) {
        return null;
//        return visibilityService.checkVisibility(projectDao.get().getBySlug(author, name), ProjectsTable::getId);
    }

    public void changeVisibility(ProjectsTable project, Visibility newVisibility, String comment) {
        if (project.getVisibility() == newVisibility) return; // No change

        visibilityDao.get().updateLatestProjectChange(getCurrentUser().getId(), project.getId());

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

    public List<UnhealthyProject> getUnhealthyProjects() {
        return projectDao.get().getUnhealthyProjects(hangarConfig.projects.getStaleAge().toMillis());
    }

    public List<ProjectMissingFile> getPluginsWithMissingFiles() {
        List<ProjectMissingFile> projectMissingFiles = projectDao.get().allProjectsForMissingFiles();
        return projectMissingFiles.stream()
                .filter(project -> {
                    Path path = projectFiles.getVersionDir(project.getOwner(), project.getName(), project.getVersionString());
                    return (project.getVersion().getFileName() != null && project.getVersion().getExternalUrl() != null) && !path.resolve(project.getFileName()).toFile().exists();
                }).collect(Collectors.toList());
    }

}
