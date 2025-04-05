package io.papermc.hangar.service.internal.versions;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectsDAO;
import io.papermc.hangar.db.dao.internal.table.versions.ProjectVersionsDAO;
import io.papermc.hangar.db.dao.internal.table.versions.dependencies.ProjectVersionDependenciesDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.project.version.PluginDependency;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import io.papermc.hangar.model.db.versions.dependencies.ProjectVersionDependencyTable;
import io.papermc.hangar.model.internal.api.requests.versions.UpdatePlatformVersions;
import io.papermc.hangar.model.internal.api.requests.versions.UpdatePluginDependencies;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.VersionContext;
import io.papermc.hangar.service.internal.PlatformService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VersionDependencyService extends HangarComponent {

    private final ProjectVersionDependenciesDAO projectVersionDependenciesDAO;
    private final ProjectsDAO projectsDAO;
    private final PlatformService platformService;
    private final ProjectVersionsDAO projectVersionsDAO;

    @Autowired
    public VersionDependencyService(final ProjectVersionDependenciesDAO projectVersionDependencyDAO, final ProjectsDAO projectsDAO, final PlatformService platformService, final ProjectVersionsDAO projectVersionsDAO) {
        this.projectVersionDependenciesDAO = projectVersionDependencyDAO;
        this.projectsDAO = projectsDAO;
        this.platformService = platformService;
        this.projectVersionsDAO = projectVersionsDAO;
    }

    @Transactional
    public void updateVersionPlatformVersions(final long projectId, final long versionId, final UpdatePlatformVersions form) {
        if (form.versions().size() > this.platformService.getFullVersionsForPlatform(form.platform()).size()) {
            throw new HangarApiException("Too many platform versions");
        }

        final ProjectVersionTable version = this.projectVersionsDAO.getProjectVersionTable(versionId);
        if (version == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }

        final Set<String> toBeRemoved = new HashSet<>();
        final Set<String> toBeAdded = new HashSet<>();

        final Map<Platform, SortedSet<String>> platforms = version.getPlatformsAsMap();
        final SortedSet<String> oldVersions = platforms.get(form.platform());

        for (final String platformVersion : form.versions()) {
            if (!oldVersions.contains(platformVersion)) {
                toBeAdded.add(platformVersion);
            }
        }

        for (final String platformVersion : oldVersions) {
            if (!form.versions().contains(platformVersion)) {
                toBeRemoved.add(platformVersion);
            }
        }

        platforms.put(form.platform(), form.versions());

        if (!toBeAdded.isEmpty()) {
            this.actionLogger.version(LogAction.VERSION_PLATFORM_DEPENDENCIES_ADDED.create(VersionContext.of(projectId, versionId), "Added: " + String.join(", ", toBeAdded), String.join(", ", oldVersions)));
        }
        if (!toBeRemoved.isEmpty()) {
            this.actionLogger.version(LogAction.VERSION_PLATFORM_DEPENDENCIES_REMOVED.create(VersionContext.of(projectId, versionId), "Removed: " + String.join(", ", toBeRemoved), String.join(", ", oldVersions)));
        }
    }

    @Transactional
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
