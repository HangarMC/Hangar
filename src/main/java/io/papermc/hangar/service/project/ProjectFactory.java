package io.papermc.hangar.service.project;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.db.dao.GeneralDao;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.ProjectChannelDao;
import io.papermc.hangar.db.dao.ProjectDao;
import io.papermc.hangar.db.model.ProjectChannelsTable;
import io.papermc.hangar.db.model.ProjectPagesTable;
import io.papermc.hangar.db.model.ProjectVersionsTable;
import io.papermc.hangar.db.model.ProjectsTable;
import io.papermc.hangar.db.model.UsersTable;
import io.papermc.hangar.model.NotificationType;
import io.papermc.hangar.model.Platform;
import io.papermc.hangar.model.Visibility;
import io.papermc.hangar.service.NotificationService;
import io.papermc.hangar.service.UserService;
import io.papermc.hangar.service.plugindata.PluginFileWithData;
import io.papermc.hangar.util.StringUtils;
import io.papermc.hangar.db.customtypes.LoggedActionType;
import io.papermc.hangar.db.customtypes.LoggedActionType.ProjectContext;
import io.papermc.hangar.db.dao.ProjectPageDao;
import io.papermc.hangar.db.dao.ProjectVersionDao;
import io.papermc.hangar.model.generated.Dependency;
import io.papermc.hangar.model.viewhelpers.ProjectPage;
import io.papermc.hangar.model.viewhelpers.VersionData;
import io.papermc.hangar.service.UserActionLogService;
import io.papermc.hangar.service.VersionService;
import io.papermc.hangar.service.pluginupload.ProjectFiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import io.papermc.hangar.model.Category;
import io.papermc.hangar.model.Role;
import io.papermc.hangar.model.viewhelpers.ProjectData;
import io.papermc.hangar.service.RoleService;
import io.papermc.hangar.service.pluginupload.PendingVersion;
import io.papermc.hangar.util.HangarException;

import org.springframework.lang.Nullable;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private final ChannelService channelService;
    private final VersionService versionService;
    private final NotificationService notificationService;
    private final UserActionLogService userActionLogService;
    private final ProjectFiles projectFiles;
    private final HangarDao<GeneralDao> generalDao;

    @Autowired
    public ProjectFactory(HangarConfig hangarConfig, HangarDao<ProjectChannelDao> projectChannelDao, HangarDao<ProjectDao> projectDao, HangarDao<ProjectPageDao> projectPagesDao, HangarDao<ProjectVersionDao> projectVersionDao, RoleService roleService, UserService userService, ProjectService projectService, ChannelService channelService, VersionService versionService, NotificationService notificationService, UserActionLogService userActionLogService, ProjectFiles projectFiles, HangarDao<GeneralDao> generalDao) {
        this.hangarConfig = hangarConfig;
        this.projectChannelDao = projectChannelDao;
        this.projectDao = projectDao;
        this.projectVersionDao = projectVersionDao;
        this.roleService = roleService;
        this.userService = userService;
        this.projectPagesDao = projectPagesDao;
        this.projectService = projectService;
        this.channelService = channelService;
        this.versionService = versionService;
        this.notificationService = notificationService;
        this.userActionLogService = userActionLogService;
        this.projectFiles = projectFiles;
        this.generalDao = generalDao;
    }

    public String getUploadError(UsersTable user) {
        if (user.isLocked()) {
            return "error.user.locked";
        } else {
            return null;
        }
    }

    public ProjectsTable createProject(UsersTable ownerUser, String name, String pluginId, Category category, String description) throws HangarException {
        String slug = StringUtils.slugify(name);
        ProjectsTable projectsTable = new ProjectsTable(pluginId, name, slug, ownerUser.getName(), ownerUser.getId(), category, description, Visibility.NEW);

        ProjectChannelsTable channelsTable = new ProjectChannelsTable(hangarConfig.channels.getNameDefault(), hangarConfig.channels.getColorDefault(), -1);

        String content = "# " + name + "\n\n" + hangarConfig.pages.home.getMessage();
        ProjectPagesTable pagesTable = new ProjectPage( -1, hangarConfig.pages.home.getName(), StringUtils.slugify(hangarConfig.pages.home.getName()), content, false, null);

        checkProjectAvailability(ownerUser, name, pluginId);

        projectsTable = projectDao.get().insert(projectsTable);
        channelsTable.setProjectId(projectsTable.getId());
        projectChannelDao.get().insert(channelsTable);

        pagesTable.setProjectId(projectsTable.getId());
        projectPagesDao.get().insert(pagesTable);

        roleService.addRole(projectsTable, ownerUser.getId(), Role.PROJECT_OWNER, true);

        userService.clearAuthorsCache();
        generalDao.get().refreshHomeProjects();

        return projectsTable;
    }

    public void softDeleteProject(ProjectData projectData, String comment) {
        ProjectsTable project = projectData.getProject();
        if (project.getVisibility() == Visibility.NEW) {
            hardDeleteProject(projectData);
            return;
        }
        projectService.changeVisibility(project, Visibility.SOFTDELETE, comment);
    }

    public void hardDeleteProject(ProjectData projectData) {
        projectDao.get().delete(projectData.getProject());
    }

    public void checkProjectAvailability(UsersTable author, String page) throws HangarException {
        checkProjectAvailability(author, page, null);
    }

    public void checkProjectAvailability(UsersTable author, String page, @Nullable String pluginId) throws HangarException {
        InvalidProjectReason invalidProjectReason;
        if (!hangarConfig.isValidProjectName(page)) invalidProjectReason = InvalidProjectReason.INVALID_NAME;
        else if (pluginId != null) invalidProjectReason = projectDao.get().checkValidProject(author.getId(), pluginId, page, StringUtils.slugify(page));
        else invalidProjectReason = projectDao.get().checkNamespace(author.getId(), page, StringUtils.slugify(page));
        if (invalidProjectReason != null) throw new HangarException(invalidProjectReason.key);
    }

    public ProjectVersionsTable createVersion(HttpServletRequest request, ProjectData project, PendingVersion pendingVersion) {

        ProjectChannelsTable channel = projectChannelDao.get().getProjectChannel(project.getProject().getId(), pendingVersion.getChannelName(), null);
        if (channel == null) {
            channel = channelService.addProjectChannel(project.getProject().getId(), pendingVersion.getChannelName(), pendingVersion.getChannelColor());
        }

        if (versionService.exists(pendingVersion) && hangarConfig.projects.isFileValidate()) {
            throw new HangarException("error.version.duplicate");
        }

        ProjectVersionsTable version = projectVersionDao.get().insert(new ProjectVersionsTable(
                pendingVersion.getVersionString(),
                pendingVersion.getDependencies().stream().map(d -> {
                    if (d.getVersion() == null || d.getVersion().isBlank()) {
                        return d.getPluginId();
                    } else {
                        return d.getPluginId() + ":" + d.getVersion();
                    }
                }).collect(Collectors.toList()),
                pendingVersion.getDescription(),
                pendingVersion.getProjectId(),
                channel.getId(),
                pendingVersion.getFileSize(),
                pendingVersion.getHash(),
                pendingVersion.getFileName(),
                pendingVersion.getAuthorId(),
                pendingVersion.isCreateForumPost()
        ));

        pendingVersion.getPlugin().getData().createTags(version.getId(), versionService); // TODO not sure what this is for
        Platform.createPlatformTags(versionService, version.getId(), Dependency.from(version.getDependencies()));

        List<UsersTable> watchers = projectService.getProjectWatchers(project.getProject().getId(), 0, null);
        // TODO bulk notif insert
        watchers.forEach(watcher -> notificationService.sendNotification(
                watcher.getId(),
                project.getProject().getOwnerId(),
                NotificationType.NEW_PROJECT_VERSION,
                new String[]{"notification.project.newVersion", project.getProject().getName(), version.getVersionString()},
                project.getNamespace() + "/versions/" + version.getVersionString()
                ));
        try {
            uploadPlugin(project, pendingVersion.getPlugin(), version);
        } catch (IOException e) {
            versionService.deleteVersion(version.getId());
            throw new HangarException("error.version.fileIOError");
        }


        // first project upload
        if (project.getVisibility() == Visibility.NEW) {
            projectService.changeVisibility(project.getProject(), Visibility.PUBLIC, "First upload");
            userActionLogService.project(request, LoggedActionType.PROJECT_VISIBILITY_CHANGE.with(ProjectContext.of(project.getProject().getId())), Visibility.PUBLIC.getName(), Visibility.NEW.getName());
            // TODO Add forum job

        }

        generalDao.get().refreshHomeProjects();
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
        Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING); // TODO maybe remove the replace_existing in prod?
        Files.deleteIfExists(oldPath);

        if (Files.notExists(newPath)) {
            throw new HangarException("error.plugin.fileName");
        }
    }

    public void prepareDeleteVersion(VersionData versionData) {
        if (versionData.getP().getVisibility() == Visibility.SOFTDELETE) return;
        List<ProjectVersionsTable> projectVersions = projectVersionDao.get().getProjectVersions(versionData.getP().getProject().getId());
        if (projectVersions.stream().filter(p -> p.getVisibility() == Visibility.PUBLIC).count() <= 1) {
            throw new HangarException("error.version.onlyOnePublic");
        }
        if (versionData.getV().getId() == versionData.getP().getRecommendedVersion().getId()) { // pick new recommended
            Optional<ProjectVersionsTable> tableOptional = projectVersions.stream().filter(v -> v.getId() != versionData.getV().getId() && v.getVisibility() != Visibility.SOFTDELETE).findFirst();
            tableOptional.ifPresent(projectVersionsTable -> {
                versionData.getP().getProject().setRecommendedVersionId(projectVersionsTable.getId());
                projectDao.get().update(versionData.getP().getProject());
            });
        }
    }

    public enum InvalidProjectReason {
        PLUGIN_ID("error.project.invalidPluginId"),
        OWNER_NAME("error.project.nameExists"),
        OWNER_SLUG("error.project.slugExists"),
        INVALID_NAME("error.project.invalidName");

        final String key;

        InvalidProjectReason(String key) {
            this.key = key;
        }
    }
}
