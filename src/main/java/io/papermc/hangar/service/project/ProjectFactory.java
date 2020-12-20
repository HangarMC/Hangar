package io.papermc.hangar.service.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.controller.forms.NewProjectForm;
import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.db.customtypes.LoggedActionType;
import io.papermc.hangar.db.customtypes.LoggedActionType.ProjectContext;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.ProjectChannelDao;
import io.papermc.hangar.db.dao.ProjectDao;
import io.papermc.hangar.db.dao.ProjectPageDao;
import io.papermc.hangar.db.dao.ProjectVersionDao;
import io.papermc.hangar.db.model.ProjectChannelsTable;
import io.papermc.hangar.db.model.ProjectOwner;
import io.papermc.hangar.db.model.ProjectPagesTable;
import io.papermc.hangar.db.model.ProjectVersionsTable;
import io.papermc.hangar.db.model.ProjectsTable;
import io.papermc.hangar.db.model.UsersTable;
import io.papermc.hangar.exceptions.HangarException;
import io.papermc.hangar.model.Category;
import io.papermc.hangar.model.NotificationType;
import io.papermc.hangar.model.Platform;
import io.papermc.hangar.model.Role;
import io.papermc.hangar.model.Visibility;
import io.papermc.hangar.model.viewhelpers.ProjectData;
import io.papermc.hangar.model.viewhelpers.ProjectPage;
import io.papermc.hangar.model.viewhelpers.VersionData;
import io.papermc.hangar.service.NotificationService;
import io.papermc.hangar.service.RoleService;
import io.papermc.hangar.service.UserActionLogService;
import io.papermc.hangar.service.UserService;
import io.papermc.hangar.service.VersionService;
import io.papermc.hangar.service.plugindata.PluginFileWithData;
import io.papermc.hangar.service.pluginupload.PendingVersion;
import io.papermc.hangar.service.pluginupload.ProjectFiles;
import io.papermc.hangar.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

@Component
public class ProjectFactory {

    private final HangarConfig hangarConfig;
    private final HangarDao<ProjectChannelDao> projectChannelDao;
    private final HangarDao<ProjectDao> projectDao;
    private final HangarDao<ProjectPageDao> projectPagesDao;
    private final HangarDao<ProjectVersionDao> projectVersionDao;
    private final RoleService roleService;
    private final UserService userService;
    private final ProjectService projectService;
    private final VersionService versionService;
    private final NotificationService notificationService;
    private final UserActionLogService userActionLogService;
    private final ProjectFiles projectFiles;
    private final ObjectMapper mapper;

    @Autowired
    public ProjectFactory(HangarConfig hangarConfig, HangarDao<ProjectChannelDao> projectChannelDao, HangarDao<ProjectDao> projectDao, HangarDao<ProjectPageDao> projectPagesDao, HangarDao<ProjectVersionDao> projectVersionDao, RoleService roleService, UserService userService, ProjectService projectService, ChannelService channelService, VersionService versionService, NotificationService notificationService, UserActionLogService userActionLogService, ProjectFiles projectFiles, ObjectMapper mapper) {
        this.hangarConfig = hangarConfig;
        this.projectChannelDao = projectChannelDao;
        this.projectDao = projectDao;
        this.projectVersionDao = projectVersionDao;
        this.roleService = roleService;
        this.userService = userService;
        this.projectPagesDao = projectPagesDao;
        this.projectService = projectService;
        this.versionService = versionService;
        this.notificationService = notificationService;
        this.userActionLogService = userActionLogService;
        this.projectFiles = projectFiles;
        this.mapper = mapper;
    }

    public String getUploadError(UsersTable user) {
        if (user.isLocked()) {
            return "error.user.locked";
        } else {
            return null;
        }
    }

    public ProjectsTable createProject(ProjectOwner ownerUser, Category category, NewProjectForm newProjectForm) {
        ProjectsTable projectsTable = new ProjectsTable(ownerUser, category, newProjectForm);

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

        userService.clearAuthorsCache();
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
                pendingVersion.getDependencies(),
                pendingVersion.getDescription(),
                pendingVersion.getProjectId(),
                channel.getId(),
                pendingVersion.getFileSize(),
                pendingVersion.getHash(),
                pendingVersion.getFileName(),
                pendingVersion.getAuthorId(),
                pendingVersion.isCreateForumPost(),
                pendingVersion.getExternalUrl(),
                pendingVersion.getPlatforms()
        ), new JSONB(mapper.valueToTree(pendingVersion.getDependencies())), new JSONB(mapper.valueToTree(pendingVersion.getPlatforms())));

        if (pendingVersion.getPlugin() != null) {
            pendingVersion.getPlugin().getData().createTags(version.getId(), versionService); // TODO not sure what this is for
        }

        Platform.createPlatformTags(versionService, version.getId(), version.getPlatforms());

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
        userService.clearAuthorsCache();

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
