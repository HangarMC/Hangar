package io.papermc.hangar.service.internal.versions;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.table.PlatformVersionDAO;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectsDAO;
import io.papermc.hangar.db.dao.internal.table.versions.dependencies.ProjectVersionDependenciesDAO;
import io.papermc.hangar.db.dao.internal.table.versions.dependencies.ProjectVersionPlatformDependenciesDAO;
import io.papermc.hangar.db.dao.v1.VersionsApiDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.project.version.PluginDependency;
import io.papermc.hangar.model.api.project.version.Version;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.db.PlatformVersionTable;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.versions.dependencies.ProjectVersionDependencyTable;
import io.papermc.hangar.model.db.versions.dependencies.ProjectVersionPlatformDependencyTable;
import io.papermc.hangar.model.internal.api.requests.versions.UpdatePlatformVersions;
import io.papermc.hangar.model.internal.api.requests.versions.UpdatePluginDependencies;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.VersionContext;
import io.papermc.hangar.service.internal.projects.ProjectService;
import io.papermc.hangar.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;

@Service
public class VersionDependencyService extends HangarComponent {

    private final ProjectVersionDependenciesDAO projectVersionDependenciesDAO;
    private final VersionsApiDAO versionsApiDAO;
    private final ProjectsDAO projectsDAO;
    private final ProjectVersionPlatformDependenciesDAO projectVersionPlatformDependenciesDAO;
    private final PlatformVersionDAO platformVersionDAO;
    private final ProjectService projectService;
    private final DownloadService downloadService;

    @Autowired
    public VersionDependencyService(ProjectVersionDependenciesDAO projectVersionDependencyDAO, VersionsApiDAO versionsApiDAO, ProjectsDAO projectsDAO, ProjectVersionPlatformDependenciesDAO projectVersionPlatformDependencyDAO, PlatformVersionDAO platformVersionDAO, ProjectService projectService, final DownloadService downloadService) {
        this.projectVersionDependenciesDAO = projectVersionDependencyDAO;
        this.versionsApiDAO = versionsApiDAO;
        this.projectsDAO = projectsDAO;
        this.projectVersionPlatformDependenciesDAO = projectVersionPlatformDependencyDAO;
        this.platformVersionDAO = platformVersionDAO;
        this.projectService = projectService;
        this.downloadService = downloadService;
    }

    public List<ProjectVersionDependencyTable> getProjectVersionDependencyTables(long versionId) {
        return projectVersionDependenciesDAO.getForVersion(versionId);
    }

    public List<ProjectVersionPlatformDependencyTable> getProjectVersionPlatformDependencyTable(long versionId) {
        return projectVersionPlatformDependenciesDAO.getForVersion(versionId);
    }

    public <T extends Version> T addDownloadsAndDependencies(final String user, final String project, final String versionName, final long versionId, final T version) {
        final Map<Platform, SortedSet<String>> platformDependencies = versionsApiDAO.getPlatformDependencies(versionId);
        version.getPlatformDependencies().putAll(platformDependencies);
        for (final Map.Entry<Platform, SortedSet<String>> entry : platformDependencies.entrySet()) {
            version.getPlatformDependenciesFormatted().put(entry.getKey(), StringUtils.formatVersionNumbers(new ArrayList<>(entry.getValue())));
        }

        //TODO into one query
        for (final Platform platform : Platform.getValues()) {
            final Set<PluginDependency> pluginDependencySet = versionsApiDAO.getPluginDependencies(versionId, platform);
            if (!pluginDependencySet.isEmpty()) {
                version.getPluginDependencies().put(platform, pluginDependencySet);
            }
        }

        downloadService.addDownloads(user, project, versionName, versionId, version.getDownloads());
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
            actionLogger.version(LogAction.VERSION_PLATFORM_DEPENDENCIES_ADDED.create(VersionContext.of(projectId, versionId), "Added: " + String.join(", ", toBeAdded.keySet()), String.join(", ", platformDependencyTables.keySet())));
        }
        if (!toBeRemoved.isEmpty()) {
            projectVersionPlatformDependenciesDAO.deleteAll(toBeRemoved.values());
            actionLogger.version(LogAction.VERSION_PLATFORM_DEPENDENCIES_REMOVED.create(VersionContext.of(projectId, versionId), "Removed: " + String.join(", ", toBeRemoved.keySet()), String.join(", ", platformDependencyTables.keySet())));
        }

        projectService.refreshHomeProjects();
    }

    @Transactional
    public void updateVersionPluginDependencies(long projectId, long versionId, UpdatePluginDependencies form) {
        if (form.getPluginDependencies().size() > config.projects.maxDependencies()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.tooManyDependencies");
        }

        final Map<String, ProjectVersionDependencyTable> projectVersionDependencies = projectVersionDependenciesDAO.getForVersionAndPlatform(versionId, form.getPlatform());
        final List<ProjectVersionDependencyTable> toBeRemoved = new ArrayList<>();
        final List<ProjectVersionDependencyTable> toBeUpdated = new ArrayList<>();
        final List<ProjectVersionDependencyTable> toBeAdded = new ArrayList<>();

        // Update/remove existing dependencies
        for (final Map.Entry<String, ProjectVersionDependencyTable> entry : projectVersionDependencies.entrySet()) {
            final ProjectVersionDependencyTable dependencyTable = entry.getValue();
            final PluginDependency dependency = form.getPluginDependencies().get(entry.getKey());
            if (dependency == null) {
                toBeRemoved.add(dependencyTable);
                continue;
            }

            boolean updated = false;
            if (dependencyTable.isRequired() != dependency.isRequired()) {
                dependencyTable.setRequired(dependency.isRequired());
                updated = true;
            }

            if (dependency.getExternalUrl() != null && !dependency.getExternalUrl().equals(dependencyTable.getExternalUrl())) {
                dependencyTable.setExternalUrl(dependency.getExternalUrl());
                dependencyTable.setProjectId(null);
                updated = true;
            } else if (dependency.getNamespace() != null) {
                final ProjectTable projectTable = projectsDAO.getBySlug(dependency.getNamespace().getOwner(), dependency.getNamespace().getSlug());
                if (projectTable == null) {
                    throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.edit.error.invalidProjectNamespace", dependency.getNamespace().getOwner() + "/" + dependency.getNamespace().getSlug());
                }

                if (dependencyTable.getExternalUrl() != null || dependencyTable.getProjectId() != projectTable.getId()) {
                    dependencyTable.setExternalUrl(null);
                    dependencyTable.setProjectId(projectTable.getId());
                    updated = true;
                }
            }
            if (updated) {
                toBeUpdated.add(dependencyTable);
            }
            form.getPluginDependencies().remove(entry.getKey());
        }

        // Add remaining new dependencies
        for (final Map.Entry<String, PluginDependency> entry : form.getPluginDependencies().entrySet()) {
            final PluginDependency dependency = entry.getValue();
            Long pdProjectId = null;
            if (dependency.getNamespace() != null) {
                final ProjectTable projectTable = projectsDAO.getBySlug(dependency.getNamespace().getOwner(), dependency.getNamespace().getSlug());
                if (projectTable == null) {
                    throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.edit.error.invalidProjectNamespace", dependency.getNamespace().getOwner() + "/" + dependency.getNamespace().getSlug());
                }

                pdProjectId = projectTable.getId();
            }

            toBeAdded.add(new ProjectVersionDependencyTable(versionId, form.getPlatform(), entry.getKey(), dependency.isRequired(), pdProjectId, dependency.getExternalUrl()));
        }

        if (!toBeRemoved.isEmpty()) {
            projectVersionDependenciesDAO.deleteAll(toBeRemoved);
            actionLogger.version(LogAction.VERSION_PLUGIN_DEPENDENCIES_REMOVED.create(VersionContext.of(projectId, versionId), "Removed: " + String.join(", ", toBeRemoved.stream().map(ProjectVersionDependencyTable::toLogString).collect(Collectors.toSet())), ""));
        }
        if (!toBeUpdated.isEmpty()) {
            projectVersionDependenciesDAO.updateAll(toBeUpdated);
            actionLogger.version(LogAction.VERSION_PLUGIN_DEPENDENCIES_EDITED.create(VersionContext.of(projectId, versionId), String.join(", ", toBeUpdated.stream().map(ProjectVersionDependencyTable::toLogString).collect(Collectors.toSet())), String.join(", ", toBeUpdated.stream().map(ProjectVersionDependencyTable::getName).map(projectVersionDependencies::get).map(ProjectVersionDependencyTable::toLogString).collect(Collectors.toSet()))));
        }
        if (!toBeAdded.isEmpty()) {
            projectVersionDependenciesDAO.insertAll(toBeAdded);
            actionLogger.version(LogAction.VERSION_PLUGIN_DEPENDENCIES_ADDED.create(VersionContext.of(projectId, versionId), "Added: " + String.join(", ", toBeAdded.stream().map(ProjectVersionDependencyTable::toLogString).collect(Collectors.toSet())), ""));
        }
    }
}
