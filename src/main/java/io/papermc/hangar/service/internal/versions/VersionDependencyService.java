package io.papermc.hangar.service.internal.versions;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.PlatformVersionDAO;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectsDAO;
import io.papermc.hangar.db.dao.internal.table.versions.ProjectVersionDependenciesDAO;
import io.papermc.hangar.db.dao.internal.table.versions.ProjectVersionPlatformDependenciesDAO;
import io.papermc.hangar.db.dao.internal.table.versions.ProjectVersionsDAO;
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
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.VersionContext;
import io.papermc.hangar.service.HangarService;
import io.papermc.hangar.service.internal.projects.ChannelService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final ProjectVersionsDAO projectVersionsDAO;

    public VersionDependencyService(HangarDao<ProjectVersionDependenciesDAO> projectVersionDependencyDAO, HangarDao<VersionsApiDAO> versionsApiDAO, HangarDao<ProjectsDAO> projectsDAO, HangarDao<ProjectVersionPlatformDependenciesDAO> projectVersionPlatformDependencyDAO, HangarDao<PlatformVersionDAO> platformVersionDAO, ChannelService channelService, VersionTagService versionTagService, HangarDao<ProjectVersionsDAO> projectVersionsDAO) {
        this.projectVersionDependenciesDAO = projectVersionDependencyDAO.get();
        this.versionsApiDAO = versionsApiDAO.get();
        this.projectsDAO = projectsDAO.get();
        this.projectVersionPlatformDependenciesDAO = projectVersionPlatformDependencyDAO.get();
        this.platformVersionDAO = platformVersionDAO.get();
        this.channelService = channelService;
        this.versionTagService = versionTagService;
        this.projectVersionsDAO = projectVersionsDAO.get();
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

    @Transactional
    public void updateVersionPlatformVersions(long projectId, long versionId, UpdatePlatformVersions form) {
        Map<String, ProjectVersionPlatformDependencyTable> platformDependencyTables = projectVersionPlatformDependenciesDAO.getPlatformVersions(versionId, form.getPlatform());
        final Map<String, ProjectVersionPlatformDependencyTable> toBeRemoved = new HashMap<>();
        final Map<String, ProjectVersionPlatformDependencyTable> toBeAdded = new HashMap<>();
        platformDependencyTables.forEach((version, table) -> {
            if (!form.getVersions().contains(version)) {
                toBeRemoved.put(version, table);
            }
        });
        form.getVersions().forEach(version -> {
            if (!platformDependencyTables.containsKey(version)) {
                PlatformVersionTable platformVersionTable = platformVersionDAO.getByPlatformAndVersion(form.getPlatform(), version);
                if (platformVersionTable == null) {
                    throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.edit.error.invalidVersionForPlatform", version, form.getPlatform().getName());
                }
                toBeAdded.put(version, new ProjectVersionPlatformDependencyTable(versionId, platformVersionTable.getId()));
            }
        });

        if (!toBeAdded.isEmpty()) {
            projectVersionPlatformDependenciesDAO.insertAll(toBeAdded.values());
            userActionLogService.version(LogAction.VERSION_PLATFORM_DEPENDENCIES_ADDED.create(VersionContext.of(projectId, versionId), "Added: " + String.join(", ", toBeAdded.keySet()), String.join(", ", platformDependencyTables.keySet())));
        }
        if (!toBeRemoved.isEmpty()) {
            projectVersionPlatformDependenciesDAO.deleteAll(toBeRemoved.values());
            userActionLogService.version(LogAction.VERSION_PLATFORM_DEPENDENCIES_REMOVED.create(VersionContext.of(projectId, versionId), "Removed: " + String.join(", ", toBeRemoved.keySet()), String.join(", ", platformDependencyTables.keySet())));
        }

        ProjectVersionTagTable projectVersionTagTable = versionTagService.getTag(versionId, form.getPlatform().getName());
        if (projectVersionTagTable == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        projectVersionTagTable.setData(form.getVersions());
        versionTagService.updateTag(projectVersionTagTable);
    }

    public void updateVersionPluginDependencies(long projectId, long versionId, UpdatePluginDependencies form) {
        Map<String, ProjectVersionDependencyTable> projectVersionDependencies = projectVersionDependenciesDAO.getForVersionAndPlatform(versionId, form.getPlatform());
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
            Long pdProjectId = null;
            if (dependency.getNamespace() != null) {
                ProjectTable projectTable = projectsDAO.getBySlug(dependency.getNamespace().getOwner(), dependency.getNamespace().getSlug());
                if (projectTable == null) {
                    throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.edit.error.invalidProjectNamespace", dependency.getNamespace().getOwner() + "/" + dependency.getNamespace().getSlug());
                }
                pdProjectId = projectTable.getId();
            }
            toBeAdded.add(new ProjectVersionDependencyTable(versionId, form.getPlatform(), name, dependency.isRequired(), pdProjectId, dependency.getExternalUrl()));
        });
        if (!toBeRemoved.isEmpty()) {
            projectVersionDependenciesDAO.deleteAll(toBeRemoved);
            userActionLogService.version(LogAction.VERSION_PLUGIN_DEPENDENCIES_REMOVED.create(VersionContext.of(projectId, versionId), "Removed: " + String.join(", ", toBeRemoved.stream().map(ProjectVersionDependencyTable::toLogString).collect(Collectors.toSet())), ""));
        }
        if (!toBeUpdated.isEmpty()) {
            projectVersionDependenciesDAO.updateAll(toBeUpdated);
            userActionLogService.version(LogAction.VERSION_PLUGIN_DEPENDENCIES_EDITED.create(VersionContext.of(projectId, versionId), String.join(", ", toBeUpdated.stream().map(ProjectVersionDependencyTable::toLogString).collect(Collectors.toSet())), String.join(", ", toBeUpdated.stream().map(ProjectVersionDependencyTable::getName).map(projectVersionDependencies::get).map(ProjectVersionDependencyTable::toLogString).collect(Collectors.toSet()))));
        }
        if (!toBeAdded.isEmpty()) {
            projectVersionDependenciesDAO.insertAll(toBeAdded);
            userActionLogService.version(LogAction.VERSION_PLUGIN_DEPENDENCIES_ADDED.create(VersionContext.of(projectId, versionId), "Added: " + String.join(", ", toBeAdded.stream().map(ProjectVersionDependencyTable::toLogString).collect(Collectors.toSet())), ""));
        }
    }
}
