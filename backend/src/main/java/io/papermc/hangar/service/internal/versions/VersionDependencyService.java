package io.papermc.hangar.service.internal.versions;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.config.CacheConfig;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectsDAO;
import io.papermc.hangar.db.dao.internal.table.versions.dependencies.ProjectVersionDependenciesDAO;
import io.papermc.hangar.db.dao.internal.table.versions.dependencies.ProjectVersionPlatformDependenciesDAO;
import io.papermc.hangar.db.dao.v1.VersionsApiDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.project.version.PlatformVersionDownload;
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
import io.papermc.hangar.model.internal.projects.HangarProject;
import io.papermc.hangar.service.internal.PlatformService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;
import io.papermc.hangar.util.VersionFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VersionDependencyService extends HangarComponent {

    private final ProjectVersionDependenciesDAO projectVersionDependenciesDAO;
    private final VersionsApiDAO versionsApiDAO;
    private final ProjectsDAO projectsDAO;
    private final ProjectVersionPlatformDependenciesDAO projectVersionPlatformDependenciesDAO;
    private final PlatformService platformService;
    private final DownloadService downloadService;

    @Autowired
    public VersionDependencyService(final ProjectVersionDependenciesDAO projectVersionDependencyDAO, final VersionsApiDAO versionsApiDAO, final ProjectsDAO projectsDAO, final ProjectVersionPlatformDependenciesDAO projectVersionPlatformDependencyDAO, final PlatformService platformService, final DownloadService downloadService) {
        this.projectVersionDependenciesDAO = projectVersionDependencyDAO;
        this.versionsApiDAO = versionsApiDAO;
        this.projectsDAO = projectsDAO;
        this.projectVersionPlatformDependenciesDAO = projectVersionPlatformDependencyDAO;
        this.platformService = platformService;
        this.downloadService = downloadService;
    }

    @Cacheable(value = CacheConfig.VERSION_DEPENDENCIES, key = "#p2") // versionId is key
    public DownloadsAndDependencies addDownloadsAndDependencies(final String project, final String versionName, final long versionId) {
        final ProjectTable projectTable = this.projectsDAO.getBySlug(project);
        if (projectTable == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }

        return this.addDownloadsAndDependencies(projectTable.getOwnerName(), project, versionName, versionId);
    }

    @Cacheable(value = CacheConfig.VERSION_DEPENDENCIES, key = "#p3") // versionId is key
    public DownloadsAndDependencies addDownloadsAndDependencies(final String user, final String project, final String versionName, final long versionId) {
        //TODO All of this is dumb and needs to be redone into as little queries as possible
        final Map<Platform, SortedSet<String>> platformDependencies = this.versionsApiDAO.getPlatformDependencies(versionId);
        final Map<Platform, String> platformDependenciesFormatted = new EnumMap<>(Platform.class);
        platformDependencies.entrySet().parallelStream().forEach(entry -> {
            final List<String> fullVersionsForPlatform = this.platformService.getFullVersionsForPlatform(entry.getKey());
            final String formattedVersionRange = VersionFormatter.formatVersionRange(new ArrayList<>(entry.getValue()), fullVersionsForPlatform);
            platformDependenciesFormatted.put(entry.getKey(), formattedVersionRange);
        });

        final Map<Platform, Set<PluginDependency>> pluginDependencies = this.versionsApiDAO.getPluginDependencies(versionId).stream()
                .collect(Collectors.groupingBy(PluginDependency::getPlatform, Collectors.toSet()));

        final Map<Platform, PlatformVersionDownload> downloads = this.downloadService.getDownloads(user, project, versionName, versionId);
        return new DownloadsAndDependencies(pluginDependencies, platformDependencies, platformDependenciesFormatted, downloads);
    }

    public record DownloadsAndDependencies(Map<Platform, Set<PluginDependency>> pluginDependencies,
                                    Map<Platform, SortedSet<String>> platformDependencies,
                                    Map<Platform, String> platformDependenciesFormatted,
                                    Map<Platform, PlatformVersionDownload> downloads
    ) {
        public <T extends Version> T applyTo(final T version) {
            version.getPluginDependencies().putAll(this.pluginDependencies);
            version.getPlatformDependencies().putAll(this.platformDependencies);
            version.getPlatformDependenciesFormatted().putAll(this.platformDependenciesFormatted);
            version.getDownloads().putAll(this.downloads);
            return version;
        }

        public HangarProject.PinnedVersion applyTo(final HangarProject.PinnedVersion version) {
            version.getPlatformDependenciesFormatted().putAll(this.platformDependenciesFormatted);
            version.getDownloads().putAll(this.downloads);
            return version;
        }
    }

    @Transactional
    @CacheEvict(value = CacheConfig.VERSION_DEPENDENCIES, key = "#p1") // versionId is key
    public void updateVersionPlatformVersions(final long projectId, final long versionId, final UpdatePlatformVersions form) {
        if (form.versions().size() > this.platformService.getFullVersionsForPlatform(form.platform()).size()) {
            throw new HangarApiException("Too many platform versions");
        }

        final Map<String, ProjectVersionPlatformDependencyTable> platformDependencyTables = this.projectVersionPlatformDependenciesDAO.getPlatformVersions(versionId, form.platform());
        final Map<String, ProjectVersionPlatformDependencyTable> toBeRemoved = new HashMap<>();
        final Map<String, ProjectVersionPlatformDependencyTable> toBeAdded = new HashMap<>();
        for (final Map.Entry<String, ProjectVersionPlatformDependencyTable> entry : platformDependencyTables.entrySet()) {
            final String key = entry.getKey();
            if (!form.versions().contains(key)) {
                toBeRemoved.put(key, entry.getValue());
            }
        }
        for (final String version : form.versions()) {
            if (!platformDependencyTables.containsKey(version)) {
                final PlatformVersionTable platformVersionTable = this.platformService.getByPlatformAndVersion(form.platform(), version);
                if (platformVersionTable == null) {
                    throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.edit.error.invalidVersionForPlatform", version, form.platform().getName());
                }
                toBeAdded.put(version, new ProjectVersionPlatformDependencyTable(versionId, platformVersionTable.getId()));
            }
        }

        if (!toBeAdded.isEmpty()) {
            this.projectVersionPlatformDependenciesDAO.insertAll(toBeAdded.values());
            this.actionLogger.version(LogAction.VERSION_PLATFORM_DEPENDENCIES_ADDED.create(VersionContext.of(projectId, versionId), "Added: " + String.join(", ", toBeAdded.keySet()), String.join(", ", platformDependencyTables.keySet())));
        }
        if (!toBeRemoved.isEmpty()) {
            this.projectVersionPlatformDependenciesDAO.deleteAll(toBeRemoved.values());
            this.actionLogger.version(LogAction.VERSION_PLATFORM_DEPENDENCIES_REMOVED.create(VersionContext.of(projectId, versionId), "Removed: " + String.join(", ", toBeRemoved.keySet()), String.join(", ", platformDependencyTables.keySet())));
        }
    }

    @Transactional
    @CacheEvict(value = CacheConfig.VERSION_DEPENDENCIES, key = "#p1") // versionId is key
    public void updateVersionPluginDependencies(final long projectId, final long versionId, final UpdatePluginDependencies form) {
        if (form.getPluginDependencies().size() > this.config.projects.maxDependencies()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.tooManyDependencies");
        }

        final Map<String, ProjectVersionDependencyTable> projectVersionDependencies = this.projectVersionDependenciesDAO.getForVersionAndPlatform(versionId, form.getPlatform());
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
            } else if (dependency.getExternalUrl() == null) {
                final ProjectTable projectTable = this.projectsDAO.getBySlug(dependency.getName());
                if (projectTable == null) {
                    throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.edit.error.invalidProjectNamespace", dependency.getName());
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
            if (dependency.getExternalUrl() == null) {
                final ProjectTable projectTable = this.projectsDAO.getBySlug(dependency.getName());
                if (projectTable == null) {
                    throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.edit.error.invalidProjectNamespace", dependency.getName());
                }

                pdProjectId = projectTable.getId();
            }

            toBeAdded.add(new ProjectVersionDependencyTable(versionId, form.getPlatform(), entry.getKey(), dependency.isRequired(), pdProjectId, dependency.getExternalUrl()));
        }

        if (!toBeRemoved.isEmpty()) {
            this.projectVersionDependenciesDAO.deleteAll(toBeRemoved);
            this.actionLogger.version(LogAction.VERSION_PLUGIN_DEPENDENCIES_REMOVED.create(VersionContext.of(projectId, versionId), "Removed: " + String.join(", ", toBeRemoved.stream().map(ProjectVersionDependencyTable::toLogString).collect(Collectors.toSet())), ""));
        }
        if (!toBeUpdated.isEmpty()) {
            this.projectVersionDependenciesDAO.updateAll(toBeUpdated);
            this.actionLogger.version(LogAction.VERSION_PLUGIN_DEPENDENCIES_EDITED.create(VersionContext.of(projectId, versionId), String.join(", ", toBeUpdated.stream().map(ProjectVersionDependencyTable::toLogString).collect(Collectors.toSet())), String.join(", ", toBeUpdated.stream().map(ProjectVersionDependencyTable::getName).map(projectVersionDependencies::get).map(ProjectVersionDependencyTable::toLogString).collect(Collectors.toSet()))));
        }
        if (!toBeAdded.isEmpty()) {
            this.projectVersionDependenciesDAO.insertAll(toBeAdded);
            this.actionLogger.version(LogAction.VERSION_PLUGIN_DEPENDENCIES_ADDED.create(VersionContext.of(projectId, versionId), "Added: " + String.join(", ", toBeAdded.stream().map(ProjectVersionDependencyTable::toLogString).collect(Collectors.toSet())), ""));
        }
    }
}
