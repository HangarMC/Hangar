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
import me.minidigger.hangar.model.Visibility;
import me.minidigger.hangar.util.StringUtils;

@Component
public class ProjectFactory {

    private final HangarConfig hangarConfig;
    private final HangarDao<ProjectChannelDao> projectChannelDao;
    private final HangarDao<ProjectDao> projectDao;

    @Autowired
    public ProjectFactory(HangarConfig hangarConfig, HangarDao<ProjectChannelDao> projectChannelDao, HangarDao<ProjectDao> projectDao) {
        this.hangarConfig = hangarConfig;
        this.projectChannelDao = projectChannelDao;
        this.projectDao = projectDao;
    }

    public String getUploadError(UsersTable user) {
        if (user.getIsLocked()) {
            return "error.user.locked";
        } else {
            return null;
        }
    }

    public ProjectsTable createProject(UsersTable ownerUser, String name, String pluginId, Category category, String description) {
        String slug = StringUtils.slugify(name);
        ProjectsTable projectsTable = new ProjectsTable(pluginId, name, slug, ownerUser.getName(), ownerUser.getId(), category.getValue(), description, Visibility.NEW.getValue());

        ProjectChannelsTable channelsTable = new ProjectChannelsTable(hangarConfig.getDefaultChannelName(), hangarConfig.getDefaultChannelColor().getValue(), -1);

        // TODO check plugin id "project with that plugin id already exists"
        // TODO check owner, name combo "project with that name already exists"
        // TODO check owner, slug combo "slug not available"
        // TODO check if project name is valid "invalid name"

        projectsTable = projectDao.get().insert(projectsTable);
        channelsTable.setProjectId(projectsTable.getId());
        projectChannelDao.get().insert(channelsTable);

        // TODO role stuff

        return projectsTable;
    }
}
