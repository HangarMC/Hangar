package me.minidigger.hangar.service.project;

import me.minidigger.hangar.db.dao.ProjectPageDao;
import me.minidigger.hangar.db.model.ProjectPagesTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import me.minidigger.hangar.config.HangarConfig;
import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.ProjectChannelDao;
import me.minidigger.hangar.db.dao.ProjectDao;
import me.minidigger.hangar.db.dao.VisibilityDao;
import me.minidigger.hangar.db.model.ProjectChannelsTable;
import me.minidigger.hangar.db.model.ProjectVisibilityChangesTable;
import me.minidigger.hangar.db.model.ProjectsTable;
import me.minidigger.hangar.db.model.UsersTable;
import me.minidigger.hangar.model.Category;
import me.minidigger.hangar.model.Role;
import me.minidigger.hangar.model.Visibility;
import me.minidigger.hangar.model.generated.Project;
import me.minidigger.hangar.model.viewhelpers.ProjectData;
import me.minidigger.hangar.service.RoleService;
import me.minidigger.hangar.service.UserService;
import me.minidigger.hangar.service.pluginupload.PendingVersion;
import me.minidigger.hangar.util.HangarException;
import me.minidigger.hangar.util.StringUtils;
import org.springframework.lang.Nullable;

import java.time.OffsetDateTime;

@Component
public class ProjectFactory {

    private final HangarConfig hangarConfig;
    private final HangarDao<ProjectChannelDao> projectChannelDao;
    private final HangarDao<ProjectDao> projectDao;
    private final HangarDao<ProjectPageDao> projectPagesDao;
    private final HangarDao<VisibilityDao> visibilityDao;
    private final RoleService roleService;
    private final UserService userService;
    private final ProjectService projectService;

    @Autowired
    public ProjectFactory(HangarConfig hangarConfig, HangarDao<ProjectChannelDao> projectChannelDao, HangarDao<ProjectDao> projectDao, HangarDao<ProjectPageDao> projectPagesDao, HangarDao<VisibilityDao> visibilityDao, RoleService roleService, UserService userService, ProjectService projectService) {
        this.hangarConfig = hangarConfig;
        this.projectChannelDao = projectChannelDao;
        this.projectDao = projectDao;
        this.visibilityDao = visibilityDao;
        this.roleService = roleService;
        this.userService = userService;
        this.projectPagesDao = projectPagesDao;
        this.projectService = projectService;
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

        ProjectChannelsTable channelsTable = new ProjectChannelsTable(hangarConfig.channels.getNameDefault(), hangarConfig.channels.getColorDefault().getValue(), -1);

        String content = "# " + name + "\n\n" + hangarConfig.pages.home.getMessage();
        ProjectPagesTable pagesTable = new ProjectPagesTable(-1, OffsetDateTime.now(), -1, hangarConfig.pages.home.getName(), StringUtils.slugify(hangarConfig.pages.home.getName()), content, false, null);

        checkProjectAvailability(ownerUser, name, pluginId);

        projectsTable = projectDao.get().insert(projectsTable);
        channelsTable.setProjectId(projectsTable.getId());
        projectChannelDao.get().insert(channelsTable);

        pagesTable.setProjectId(projectsTable.getId());
        projectPagesDao.get().insert(pagesTable);

        roleService.addRole(projectsTable, ownerUser.getId(), Role.PROJECT_OWNER, true);

        userService.clearAuthorsCache();

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
        // TODO UAC
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

    public void createVersion(Project project, PendingVersion pendingVersion) {
        // TODO createVersion
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
