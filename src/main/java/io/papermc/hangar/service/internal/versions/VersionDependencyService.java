package io.papermc.hangar.service.internal.versions;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.PlatformVersionDAO;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectsDAO;
import io.papermc.hangar.db.dao.internal.table.versions.ProjectVersionDependenciesDAO;
import io.papermc.hangar.db.dao.internal.table.versions.ProjectVersionPlatformDependenciesDAO;
import io.papermc.hangar.db.dao.v1.VersionsApiDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.color.TagColor;
import io.papermc.hangar.model.api.project.version.PluginDependency;
import io.papermc.hangar.model.api.project.version.Tag;
import io.papermc.hangar.model.api.project.version.Version;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.db.PlatformVersionTable;
import io.papermc.hangar.model.db.projects.ProjectChannelTable;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.versions.ProjectVersionDependencyTable;
import io.papermc.hangar.model.db.versions.ProjectVersionPlatformDependencyTable;
import io.papermc.hangar.model.db.versions.ProjectVersionTagTable;
import io.papermc.hangar.model.internal.api.requests.versions.UpdatePlatformVersions;
import io.papermc.hangar.model.internal.api.requests.versions.UpdatePluginDependencies;
import io.papermc.hangar.service.HangarService;
import io.papermc.hangar.service.internal.projects.ChannelService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class VersionDependencyService extends HangarService {

    private final ProjectVersionDependenciesDAO projectVersionDependenciesDAO;
    private final VersionsApiDAO versionsApiDAO;
    private final ProjectsDAO projectsDAO;
    private final ProjectVersionPlatformDependenciesDAO projectVersionPlatformDependenciesDAO;
    private final PlatformVersionDAO platformVersionDAO;
    private final ChannelService channelService;
    private final VersionTagService versionTagService;

    public VersionDependencyService(HangarDao<ProjectVersionDependenciesDAO> projectVersionDependencyDAO, HangarDao<VersionsApiDAO> versionsApiDAO, HangarDao<ProjectsDAO> projectsDAO, HangarDao<ProjectVersionPlatformDependenciesDAO> projectVersionPlatformDependencyDAO, HangarDao<PlatformVersionDAO> platformVersionDAO, ChannelService channelService, VersionTagService versionTagService) {
        this.projectVersionDependenciesDAO = projectVersionDependencyDAO.get();
        this.versionsApiDAO = versionsApiDAO.get();
        this.projectsDAO = projectsDAO.get();
        this.projectVersionPlatformDependenciesDAO = projectVersionPlatformDependencyDAO.get();
        this.platformVersionDAO = platformVersionDAO.get();
        this.channelService = channelService;
        this.versionTagService = versionTagService;
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

    public void updateVersionPlatformVersions(long versionId, UpdatePlatformVersions form) {
        Map<String, ProjectVersionPlatformDependencyTable> platformDependencyTables = projectVersionPlatformDependenciesDAO.getPlatformVersions(versionId, form.getPlatform());
        Set<String> oldVersions = new HashSet<>(platformDependencyTables.keySet()); // copy set because we dont want changes to it to be reflected in the map
        oldVersions.retainAll(form.getVersions());
        Set<String> newVersions = new HashSet<>(oldVersions);
        newVersions.addAll(form.getVersions());
        Set<ProjectVersionPlatformDependencyTable> newVersionTables = new HashSet<>();
        for (String newVersion : newVersions) {
            ProjectVersionPlatformDependencyTable table = platformDependencyTables.remove(newVersion);
            if (table == null) {
                PlatformVersionTable platformVersionTable = platformVersionDAO.getByPlatformAndVersion(form.getPlatform(), newVersion);
                if (platformVersionTable == null) {
                    throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.edit.error.invalidVersionForPlatform", newVersion, form.getPlatform().getName());
                }
                newVersionTables.add(new ProjectVersionPlatformDependencyTable(versionId, platformVersionTable.getId()));
            }
        }
        // At this point `platformDependencyTables` contains rows that should be removed
        projectVersionPlatformDependenciesDAO.insertAll(newVersionTables);
        projectVersionPlatformDependenciesDAO.deleteAll(platformDependencyTables.values());

        ProjectVersionTagTable projectVersionTagTable = versionTagService.getTag(versionId, form.getPlatform().getName());
        if (projectVersionTagTable == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        projectVersionTagTable.setData(form.getVersions());
        versionTagService.updateTag(projectVersionTagTable);
        // TODO action logging here
    }

    public void updateVersionPluginDependencies(long versionId, UpdatePluginDependencies form) {
        Map<String, ProjectVersionDependencyTable> projectVersionDependencies = projectVersionDependenciesDAO.getForVersionAndPlatform(versionId, form.getPlatform()).stream().collect(Collectors.toMap(ProjectVersionDependencyTable::getName, Function.identity()));
        final Set<ProjectVersionDependencyTable> toBeRemoved = new HashSet<>();
        final Set<ProjectVersionDependencyTable> toBeUpdated = new HashSet<>();
        final Set<ProjectVersionDependencyTable> toBeAdded = new HashSet<>();
        projectVersionDependencies.forEach((name, dependency) -> {
            PluginDependency otherDep = form.getPluginDependencies().get(name);
            if (otherDep == null) {
                toBeRemoved.add(dependency);
            } else {
                boolean updated = false;
                if (dependency.isRequired() != otherDep.isRequired()) {
                    dependency.setRequired(otherDep.isRequired());
                    updated = true;
                }

                if (otherDep.getExternalUrl() != null && !dependency.getExternalUrl().equals(otherDep.getExternalUrl())) {
                    dependency.setExternalUrl(otherDep.getExternalUrl());
                    dependency.setProjectId(null);
                    updated = true;
                } else if (otherDep.getNamespace() != null) {
                    dependency.setExternalUrl(null);
                    ProjectTable projectTable = projectsDAO.getBySlug(otherDep.getNamespace().getOwner(), otherDep.getNamespace().getSlug());
                    if (projectTable == null) {
                        throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.edit.error.invalidProjectNamespace", otherDep.getNamespace().getOwner() + "/" + otherDep.getNamespace().getSlug());
                    }
                    if (dependency.getProjectId() != projectTable.getId()) {
                        dependency.setProjectId(projectTable.getId());
                        updated = true;
                    }
                }
                if (updated) {
                    toBeUpdated.add(dependency);
                }
                form.getPluginDependencies().remove(name);
            }
        });
        form.getPluginDependencies().forEach((name, dependency) -> {
            Long projectId = null;
            if (dependency.getNamespace() != null) {
                ProjectTable projectTable = projectsDAO.getBySlug(dependency.getNamespace().getOwner(), dependency.getNamespace().getSlug());
                if (projectTable == null) {
                    throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.edit.error.invalidProjectNamespace", dependency.getNamespace().getOwner() + "/" + dependency.getNamespace().getSlug());
                }
                projectId = projectTable.getId();
            }
            toBeAdded.add(new ProjectVersionDependencyTable(versionId, form.getPlatform(), name, dependency.isRequired(), projectId, dependency.getExternalUrl()));
        });
        projectVersionDependenciesDAO.deleteAll(toBeRemoved);
        projectVersionDependenciesDAO.updateAll(toBeUpdated);
        projectVersionDependenciesDAO.insertAll(toBeAdded);
        // TODO action logging here
    }
}
