package io.papermc.hangar.service.internal.versions;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.versions.ProjectVersionDependenciesDAO;
import io.papermc.hangar.db.dao.internal.table.versions.ProjectVersionPlatformDependenciesDAO;
import io.papermc.hangar.db.dao.v1.VersionsApiDAO;
import io.papermc.hangar.model.api.color.TagColor;
import io.papermc.hangar.model.api.project.version.PluginDependency;
import io.papermc.hangar.model.api.project.version.Tag;
import io.papermc.hangar.model.api.project.version.Version;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.db.projects.ProjectChannelTable;
import io.papermc.hangar.model.db.versions.ProjectVersionDependencyTable;
import io.papermc.hangar.model.db.versions.ProjectVersionPlatformDependencyTable;
import io.papermc.hangar.service.HangarService;
import io.papermc.hangar.service.internal.projects.ChannelService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class VersionDependencyService extends HangarService {

    private final ProjectVersionDependenciesDAO projectVersionDependenciesDAO;
    private final VersionsApiDAO versionsApiDAO;
    private final ProjectVersionPlatformDependenciesDAO projectVersionPlatformDependenciesDAO;
    private final ChannelService channelService;

    public VersionDependencyService(HangarDao<ProjectVersionDependenciesDAO> projectVersionDependencyDAO, HangarDao<VersionsApiDAO> versionsApiDAO, HangarDao<ProjectVersionPlatformDependenciesDAO> projectVersionPlatformDependencyDAO, ChannelService channelService) {
        this.projectVersionDependenciesDAO = projectVersionDependencyDAO.get();
        this.versionsApiDAO = versionsApiDAO.get();
        this.projectVersionPlatformDependenciesDAO = projectVersionPlatformDependencyDAO.get();
        this.channelService = channelService;
    }

    public List<ProjectVersionDependencyTable> getProjectVersionDependencyTables(long versionId) {
        return projectVersionDependenciesDAO.getForVersion(versionId);
    }

    public List<ProjectVersionPlatformDependencyTable> getProjectVersionPlatformDependencyTable(long versionId) {
        return projectVersionPlatformDependenciesDAO.getForVersion(versionId);
    }

    public <T extends Version> T addDependenciesAndTags(Long versionId, T version) {
        version.getPlatformDependencies().putAll(versionsApiDAO.getPlatformDependencies(versionId));
        for (Platform platform : Platform.getValues()) {
            Set<PluginDependency> pluginDependencySet = versionsApiDAO.getPluginDependencies(versionId, platform);
            if (!pluginDependencySet.isEmpty()) {
                version.getPluginDependencies().put(platform, pluginDependencySet);
            }
        }
        version.getTags().addAll(versionsApiDAO.getVersionTags(versionId));
        ProjectChannelTable projectChannelTable = channelService.getProjectChannelForVersion(versionId);
        version.getTags().add(new Tag("Channel", projectChannelTable.getName(), new TagColor(null, projectChannelTable.getColor().getHex())));
        return version;
    }

}
