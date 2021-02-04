package io.papermc.hangar.serviceold.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.controllerold.forms.NewProjectForm;
import io.papermc.hangar.db.customtypes.LoggedActionType;
import io.papermc.hangar.db.customtypes.LoggedActionType.ProjectContext;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.ProjectVersionDependencyDAO;
import io.papermc.hangar.db.dao.internal.table.ProjectVersionPlatformDependencyDAO;
import io.papermc.hangar.db.daoold.PlatformVersionsDao;
import io.papermc.hangar.db.daoold.ProjectChannelDao;
import io.papermc.hangar.db.daoold.ProjectDao;
import io.papermc.hangar.db.daoold.ProjectPageDao;
import io.papermc.hangar.db.daoold.ProjectVersionDao;
import io.papermc.hangar.db.modelold.PlatformVersionsTable;
import io.papermc.hangar.db.modelold.ProjectChannelsTable;
import io.papermc.hangar.db.modelold.ProjectOwner;
import io.papermc.hangar.db.modelold.ProjectPagesTable;
import io.papermc.hangar.db.modelold.ProjectVersionsTable;
import io.papermc.hangar.db.modelold.ProjectsTable;
import io.papermc.hangar.db.modelold.UsersTable;
import io.papermc.hangar.exceptions.HangarException;
import io.papermc.hangar.model.Category;
import io.papermc.hangar.model.Visibility;
import io.papermc.hangar.model.db.versions.ProjectVersionDependencyTable;
import io.papermc.hangar.model.db.versions.ProjectVersionPlatformDependencyTable;
import io.papermc.hangar.model.internal.user.notifications.NotificationType;
import io.papermc.hangar.modelold.Platform;
import io.papermc.hangar.modelold.Role;
import io.papermc.hangar.modelold.generated.Dependency;
import io.papermc.hangar.modelold.generated.PlatformDependency;
import io.papermc.hangar.modelold.viewhelpers.ProjectData;
import io.papermc.hangar.modelold.viewhelpers.ProjectPage;
import io.papermc.hangar.modelold.viewhelpers.VersionData;
import io.papermc.hangar.service.api.UsersApiService;
import io.papermc.hangar.serviceold.NotificationService;
import io.papermc.hangar.serviceold.RoleService;
import io.papermc.hangar.serviceold.UserActionLogService;
import io.papermc.hangar.serviceold.VersionService;
import io.papermc.hangar.serviceold.plugindata.PluginFileWithData;
import io.papermc.hangar.serviceold.pluginupload.PendingVersion;
import io.papermc.hangar.serviceold.pluginupload.ProjectFiles;
import io.papermc.hangar.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ProjectFactory {

    private final HangarConfig hangarConfig;
    private final HangarDao<PlatformVersionsDao> platformVersionsDao;
    private final HangarDao<ProjectVersionDependencyDAO> projectVersionDependencyDAO;
    private final HangarDao<ProjectVersionPlatformDependencyDAO> projectVersionPlatformDependencyDAO;
    private final HangarDao<ProjectChannelDao> projectChannelDao;
    private final HangarDao<ProjectDao> projectDao;
    private final HangarDao<ProjectPageDao> projectPagesDao;
    private final HangarDao<ProjectVersionDao> projectVersionDao;
    private final RoleService roleService;
    private final ProjectService projectService;
    private final VersionService versionService;
    private final NotificationService notificationService;
    private final UserActionLogService userActionLogService;
    private final ProjectFiles projectFiles;
    private final ObjectMapper mapper;
    private final UsersApiService usersApiService;

    @Autowired
    public ProjectFactory(HangarConfig hangarConfig, HangarDao<PlatformVersionsDao> platformVersionsDao, HangarDao<ProjectVersionDependencyDAO> projectVersionDependencyDAO, HangarDao<ProjectVersionPlatformDependencyDAO> projectVersionPlatformDependencyDAO, HangarDao<ProjectChannelDao> projectChannelDao, HangarDao<ProjectDao> projectDao, HangarDao<ProjectPageDao> projectPagesDao, HangarDao<ProjectVersionDao> projectVersionDao, RoleService roleService, ProjectService projectService, VersionService versionService, NotificationService notificationService, UserActionLogService userActionLogService, ProjectFiles projectFiles, ObjectMapper mapper, UsersApiService usersApiService) {
        this.hangarConfig = hangarConfig;
        this.platformVersionsDao = platformVersionsDao;
        this.projectVersionDependencyDAO = projectVersionDependencyDAO;
        this.projectVersionPlatformDependencyDAO = projectVersionPlatformDependencyDAO;
        this.projectChannelDao = projectChannelDao;
        this.projectDao = projectDao;
        this.projectVersionDao = projectVersionDao;
        this.roleService = roleService;
        this.projectPagesDao = projectPagesDao;
        this.projectService = projectService;
        this.versionService = versionService;
        this.notificationService = notificationService;
        this.userActionLogService = userActionLogService;
        this.projectFiles = projectFiles;
        this.mapper = mapper;
        this.usersApiService = usersApiService;
    }

    public String getUploadError(UsersTable user) {
        if (user.isLocked()) {
            return "error.user.locked";
        } else {
            return null;
        }
    }

    public ProjectsTable createProject(ProjectOwner ownerUser, Category category, NewProjectForm newProjectForm) {
        Collection<String> keywords;
        try {
            keywords = StringUtils.parseKeywords(newProjectForm.getKeywords());
            if (keywords.size() > hangarConfig.projects.getMaxKeywords()) {
                throw new HangarException("error.project.maxKeywords", String.valueOf(hangarConfig.projects.getMaxKeywords()));
            }
        } catch (IllegalArgumentException e) {
            throw new HangarException("error.project.duplicateKeyword");
        }
        ProjectsTable projectsTable = new ProjectsTable(ownerUser, category, newProjectForm, keywords);

        ProjectChannelsTable channelsTable = new ProjectChannelsTable(hangarConfig.channels.getNameDefault(), hangarConfig.channels.getColorDefault(), -1, false);

        String newPageContent = StringUtils.stringOrNull(newProjectForm.getPageContent());
        if (newPageContent == null) {
            newPageContent = "# " + projectsTable.getName() + "\n\n" + hangarConfig.pages.home.getMessage();
        }
        ProjectPagesTable pagesTable = new ProjectPage(-1, hangarConfig.pages.home.getName(), StringUtils.slugify(hangarConfig.pages.home.getName()), newPageContent, false, null);

        checkProjectAvailability(ownerUser, projectsTable.getName());

        projectsTable = projectDao.get().insert(projectsTable);
        channelsTable.setProjectId(projectsTable.getId());
        projectChannelDao.get().insert(channelsTable);

        pagesTable.setProjectId(projectsTable.getId());
        projectPagesDao.get().insert(pagesTable);

        roleService.addRole(projectsTable, ownerUser.getUserId(), Role.PROJECT_OWNER, true);

        usersApiService.clearAuthorsCache();
        projectService.refreshHomePage();

        return projectsTable;
    }

    public void softDeleteProject(ProjectsTable projectsTable, String comment) {
        if (projectsTable.getVisibility() == Visibility.NEW) {
            hardDeleteProject(projectsTable);
            return;
        }
        projectService.changeVisibility(projectsTable, Visibility.SOFTDELETE, comment);
    }

    public void hardDeleteProject(ProjectsTable projectsTable) {
        projectDao.get().delete(projectsTable);
    }

    public void checkProjectAvailability(ProjectOwner author, String page) {
        InvalidProjectReason invalidProjectReason;
        if (!hangarConfig.isValidProjectName(page)) {
            invalidProjectReason = InvalidProjectReason.INVALID_NAME;
        } else {
            invalidProjectReason = projectDao.get().checkNamespace(author.getUserId(), page, StringUtils.slugify(page));
        }

        if (invalidProjectReason != null) {
            throw new HangarException(invalidProjectReason.key);
        }
    }

    public ProjectVersionsTable createVersion(HttpServletRequest request, ProjectData project, PendingVersion pendingVersion) {
        ProjectChannelsTable channel = projectChannelDao.get().getProjectChannel(project.getProject().getId(), pendingVersion.getChannelName(), null);
        if (versionService.exists(pendingVersion) && hangarConfig.projects.isFileValidate()) {
            throw new HangarException("error.version.duplicate");
        }

        if (!hangarConfig.projects.getVersionNameMatcher().test(pendingVersion.getVersionString())) {
            throw new HangarException("error.project.version.invalidName");
        }

        ProjectVersionsTable version = projectVersionDao.get().insert(new ProjectVersionsTable(
                pendingVersion.getVersionString(),
//                pendingVersion.getDependencies(),
                pendingVersion.getDescription(),
                pendingVersion.getProjectId(),
                channel.getId(),
                pendingVersion.getFileSize(),
                pendingVersion.getHash(),
                pendingVersion.getFileName(),
                pendingVersion.getAuthorId(),
                pendingVersion.isCreateForumPost(),
                pendingVersion.getExternalUrl()/*,*/
//                pendingVersion.getPlatforms()
        ));

        if (pendingVersion.getPlugin() != null) {
            pendingVersion.getPlugin().getData().createTags(version.getId(), versionService); // TODO not sure what this is for
        }

        Platform.createPlatformTags(versionService, version.getId(), pendingVersion.getPlatforms());

        List<ProjectVersionPlatformDependencyTable> platformDependencyTables = new ArrayList<>();
        for (PlatformDependency platformDependency : pendingVersion.getPlatforms()) {
            if (platformDependency.getVersions().isEmpty()) continue;
            Map<String, Long> platformVersionTableIds = platformVersionsDao.get().getVersionsForPlatform(platformDependency.getPlatform()).stream().collect(Collectors.toMap(PlatformVersionsTable::getVersion, PlatformVersionsTable::getId));
            for (String versionString : platformDependency.getVersions()) {
                platformDependencyTables.add(new ProjectVersionPlatformDependencyTable(version.getId(), platformVersionTableIds.get(versionString)));
            }
        }
        projectVersionPlatformDependencyDAO.get().insertAll(platformDependencyTables);

        List<ProjectVersionDependencyTable> projectVersionDependencyTables = new ArrayList<>();
        for (Map.Entry<Platform, List<Dependency>> entry : pendingVersion.getDependencies().entrySet()) {
            for (Dependency dependency : entry.getValue()) {
                projectVersionDependencyTables.add(new ProjectVersionDependencyTable(version.getId(), io.papermc.hangar.model.Platform.valueOf(entry.getKey().name()), dependency.getName(), dependency.isRequired(), dependency.getNamespace() != null ? projectService.getProjectsTable(dependency.getNamespace().getOwner(), dependency.getNamespace().getSlug()).getId() : null, dependency.getExternalUrl()));
            }
        }
        projectVersionDependencyDAO.get().insertAll(projectVersionDependencyTables);

        List<UsersTable> watchers = projectService.getProjectWatchers(project.getProject().getId(), 0, null);
        // TODO bulk notif insert
        watchers.forEach(watcher -> notificationService.sendNotification(
                watcher.getId(),
                project.getProject().getOwnerId(),
                NotificationType.NEW_PROJECT_VERSION,
                new String[]{"notification.project.newVersion", project.getProject().getName(), version.getVersionString()},
                project.getNamespace() + "/versions/" + version.getVersionString()
        ));

        if (pendingVersion.getPlugin() != null) {
            try {
                uploadPlugin(project, pendingVersion.getPlugin(), version);
            } catch (IOException e) {
                versionService.deleteVersion(version.getId());
                throw new HangarException("error.version.fileIOError");
            }
        }

        // first project upload
        if (project.getVisibility() == Visibility.NEW) {
            projectService.changeVisibility(project.getProject(), Visibility.PUBLIC, "First upload");
            userActionLogService.project(request, LoggedActionType.PROJECT_VISIBILITY_CHANGE.with(ProjectContext.of(project.getProject().getId())), Visibility.PUBLIC.getName(), Visibility.NEW.getName());
            // TODO Add forum job
        }

        projectService.refreshHomePage();
        usersApiService.clearAuthorsCache();

        return version;
    }

    private void uploadPlugin(ProjectData project, PluginFileWithData plugin, ProjectVersionsTable version) throws IOException {
        Path oldPath = plugin.getPath();
        Path versionDir = projectFiles.getVersionDir(project.getProjectOwner().getName(), project.getProject().getName(), version.getVersionString());
        Path newPath = versionDir.resolve(oldPath.getFileName());
        if (Files.notExists(newPath)) {
            Files.createDirectories(newPath.getParent());
        }

        Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
        Files.deleteIfExists(oldPath);
        if (Files.notExists(newPath)) {
            throw new HangarException("error.plugin.fileName");
        }
    }

    public void prepareDeleteVersion(VersionData versionData) {
        if (versionData.getP().getVisibility() == Visibility.SOFTDELETE) return;
        List<ProjectVersionsTable> projectVersions = projectVersionDao.get().getProjectVersions(versionData.getP().getProject().getId());
        if (projectVersions.stream().filter(p -> p.getVisibility() == Visibility.PUBLIC).count() <= 1 && versionData.getV().getVisibility() == Visibility.PUBLIC) {
            throw new HangarException("error.version.onlyOnePublic");
        }

        ProjectVersionsTable recommended = versionData.getP().getRecommendedVersion();
        if (recommended != null && versionData.getV().getId() == recommended.getId()) { // pick new recommended
            Optional<ProjectVersionsTable> tableOptional = projectVersions.stream().filter(v -> v.getId() != versionData.getV().getId() && v.getVisibility() != Visibility.SOFTDELETE).findFirst();
            tableOptional.ifPresent(projectVersionsTable -> {
                versionData.getP().getProject().setRecommendedVersionId(projectVersionsTable.getId());
                projectDao.get().update(versionData.getP().getProject());
            });
        }
    }

    public enum InvalidProjectReason {
        OWNER_NAME("error.project.nameExists"),
        OWNER_SLUG("error.project.slugExists"),
        INVALID_NAME("error.project.invalidName");

        final String key;

        InvalidProjectReason(String key) {
            this.key = key;
        }
    }
}
