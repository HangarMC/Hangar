package me.minidigger.hangar.service.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import me.minidigger.hangar.config.HangarConfig;
import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.ProjectChannelDao;
import me.minidigger.hangar.db.dao.ProjectDao;
import me.minidigger.hangar.db.model.ProjectChannelsTable;
import me.minidigger.hangar.db.model.ProjectsTable;
import me.minidigger.hangar.db.model.UsersTable;
import me.minidigger.hangar.model.Category;
import me.minidigger.hangar.model.Role;
import me.minidigger.hangar.model.Visibility;
import me.minidigger.hangar.service.RoleService;
import me.minidigger.hangar.service.UserService;
import me.minidigger.hangar.util.HangarException;
import me.minidigger.hangar.util.StringUtils;

@Component
public class ProjectFactory {

    private final HangarConfig hangarConfig;
    private final HangarDao<ProjectChannelDao> projectChannelDao;
    private final HangarDao<ProjectDao> projectDao;
    private final RoleService roleService;
    private final UserService userService;

    @Autowired
    public ProjectFactory(HangarConfig hangarConfig, HangarDao<ProjectChannelDao> projectChannelDao, HangarDao<ProjectDao> projectDao, RoleService roleService, UserService userService) {
        this.hangarConfig = hangarConfig;
        this.projectChannelDao = projectChannelDao;
        this.projectDao = projectDao;
        this.roleService = roleService;
        this.userService = userService;
    }

    public String getUploadError(UsersTable user) {
        if (user.isLocked()) {
            return "error.user.locked";
        } else {
            return null;
        }
    }

    public ProjectsTable createProject(UsersTable ownerUser, String name, String pluginId, Category category, String description) {
        String slug = StringUtils.slugify(name);
        ProjectsTable projectsTable = new ProjectsTable(pluginId, name, slug, ownerUser.getName(), ownerUser.getId(), category, description, Visibility.NEW.getValue());

        ProjectChannelsTable channelsTable = new ProjectChannelsTable(hangarConfig.channels.getNameDefault(), hangarConfig.channels.getColorDefault().getValue(), -1);

        InvalidProjectReason invalidProjectReason;
        if (!hangarConfig.isValidProjectName(name)) {
            invalidProjectReason = InvalidProjectReason.INVALID_NAME;
        } else {
            invalidProjectReason = projectDao.get().checkValidProject(ownerUser.getId(), pluginId, name, slug);
        }
        if (invalidProjectReason != null) {
            throw new HangarException(invalidProjectReason.key);
        }

        projectsTable = projectDao.get().insert(projectsTable);
        channelsTable.setProjectId(projectsTable.getId());
        projectChannelDao.get().insert(channelsTable);

        roleService.addRole(projectsTable, ownerUser.getId(), Role.PROJECT_OWNER, true);

        userService.clearAuthorsCache();

        return projectsTable;
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
