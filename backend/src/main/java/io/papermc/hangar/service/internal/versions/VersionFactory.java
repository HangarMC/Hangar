package io.papermc.hangar.service.internal.versions;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.controller.extras.pagination.filters.versions.VersionChannelFilter;
import io.papermc.hangar.controller.extras.pagination.filters.versions.VersionPlatformFilter;
import io.papermc.hangar.db.dao.internal.table.versions.ProjectVersionsDAO;
import io.papermc.hangar.db.dao.internal.table.versions.dependencies.ProjectVersionDependenciesDAO;
import io.papermc.hangar.db.dao.internal.table.versions.dependencies.ProjectVersionPlatformDependenciesDAO;
import io.papermc.hangar.db.dao.internal.table.versions.downloads.ProjectVersionDownloadsDAO;
import io.papermc.hangar.db.dao.v1.VersionsApiDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.project.version.FileInfo;
import io.papermc.hangar.model.api.project.version.PluginDependency;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.papermc.hangar.model.common.ChannelFlag;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.model.db.PlatformVersionTable;
import io.papermc.hangar.model.db.projects.ProjectChannelTable;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import io.papermc.hangar.model.db.versions.dependencies.ProjectVersionDependencyTable;
import io.papermc.hangar.model.db.versions.dependencies.ProjectVersionPlatformDependencyTable;
import io.papermc.hangar.model.db.versions.downloads.ProjectVersionDownloadTable;
import io.papermc.hangar.model.db.versions.downloads.ProjectVersionPlatformDownloadTable;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.VersionContext;
import io.papermc.hangar.model.internal.versions.LastDependencies;
import io.papermc.hangar.model.internal.versions.MultipartFileOrUrl;
import io.papermc.hangar.model.internal.versions.PendingVersion;
import io.papermc.hangar.model.internal.versions.PendingVersionFile;
import io.papermc.hangar.service.ValidationService;
import io.papermc.hangar.service.api.UsersApiService;
import io.papermc.hangar.service.internal.PlatformService;
import io.papermc.hangar.service.internal.file.FileService;
import io.papermc.hangar.service.internal.file.S3FileService;
import io.papermc.hangar.service.internal.projects.ChannelService;
import io.papermc.hangar.service.internal.projects.ProjectService;
import io.papermc.hangar.service.internal.uploads.ProjectFiles;
import io.papermc.hangar.service.internal.users.NotificationService;
import io.papermc.hangar.service.internal.versions.plugindata.PluginDataService;
import io.papermc.hangar.service.internal.versions.plugindata.PluginFileWithData;
import io.papermc.hangar.service.internal.visibility.ProjectVisibilityService;
import io.papermc.hangar.util.CryptoUtils;
import io.papermc.hangar.util.StringUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class VersionFactory extends HangarComponent {

    private final ProjectVersionPlatformDependenciesDAO projectVersionPlatformDependenciesDAO;
    private final ProjectVersionDependenciesDAO projectVersionDependenciesDAO;
    private final ProjectVersionsDAO projectVersionsDAO;
    private final ProjectFiles projectFiles;
    private final PluginDataService pluginDataService;
    private final ChannelService channelService;
    private final ProjectVisibilityService projectVisibilityService;
    private final ProjectService projectService;
    private final NotificationService notificationService;
    private final PlatformService platformService;
    private final UsersApiService usersApiService;
    private final ValidationService validationService;
    private final ProjectVersionDownloadsDAO downloadsDAO;
    private final VersionsApiDAO versionsApiDAO;
    private final FileService fileService;
    private final JarScanningService jarScanningService;
    private final ReviewService reviewService;

    @Autowired
    public VersionFactory(final ProjectVersionPlatformDependenciesDAO projectVersionPlatformDependencyDAO, final ProjectVersionDependenciesDAO projectVersionDependencyDAO, final ProjectVersionsDAO projectVersionDAO, final ProjectFiles projectFiles, final PluginDataService pluginDataService, final ChannelService channelService, final ProjectVisibilityService projectVisibilityService, final ProjectService projectService, final NotificationService notificationService, final PlatformService platformService, final UsersApiService usersApiService, final ValidationService validationService, final ProjectVersionDownloadsDAO downloadsDAO, final VersionsApiDAO versionsApiDAO, final FileService fileService, final JarScanningService jarScanningService, final ReviewService reviewService) {
        this.projectVersionPlatformDependenciesDAO = projectVersionPlatformDependencyDAO;
        this.projectVersionDependenciesDAO = projectVersionDependencyDAO;
        this.projectVersionsDAO = projectVersionDAO;
        this.projectFiles = projectFiles;
        this.pluginDataService = pluginDataService;
        this.channelService = channelService;
        this.projectVisibilityService = projectVisibilityService;
        this.projectService = projectService;
        this.notificationService = notificationService;
        this.platformService = platformService;
        this.usersApiService = usersApiService;
        this.validationService = validationService;
        this.downloadsDAO = downloadsDAO;
        this.versionsApiDAO = versionsApiDAO;
        this.fileService = fileService;
        this.jarScanningService = jarScanningService;
        this.reviewService = reviewService;
    }

    @Transactional
    public PendingVersion createPendingVersion(final long projectId, final List<MultipartFileOrUrl> data, final List<MultipartFile> files, final String channel, final boolean prefillDependencies) {
        final ProjectTable projectTable = this.projectService.getProjectTable(projectId);
        if (projectTable == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND, "Project not found");
        }

        final Map<Platform, Set<PluginDependency>> pluginDependencies = new EnumMap<>(Platform.class);
        final Map<Platform, SortedSet<String>> platformDependencies = new EnumMap<>(Platform.class);
        final List<PendingVersionFile> pendingFiles = new ArrayList<>();
        String versionString = null;
        final Set<Platform> processedPlatforms = EnumSet.noneOf(Platform.class);

        // Delete old temp data
        final String userTempDir = this.projectFiles.getTempDir(this.getHangarPrincipal().getName());
        this.fileService.deleteDirectory(userTempDir);

        for (final MultipartFileOrUrl fileOrUrl : data) {
            // Make sure each platform only has one corresponding file/url
            if (!processedPlatforms.addAll(fileOrUrl.platforms())) {
                this.fileService.deleteDirectory(userTempDir);
                throw new HangarApiException("Overlapping platforms defined");
            }

            if (fileOrUrl.isUrl()) {
                // Handle external url
                pendingFiles.add(new PendingVersionFile(fileOrUrl.platforms(), null, fileOrUrl.externalUrl()));
                if (prefillDependencies) {
                    this.createPendingUrl(channel, projectTable, pluginDependencies, platformDependencies, fileOrUrl);
                }
            } else {
                if (files == null || files.isEmpty()) {
                    throw new HangarApiException("Missing a file for platform(s): " + fileOrUrl.platforms());
                }

                final MultipartFile file = files.remove(0);
                versionString = this.createPendingFile(file, channel, projectTable, pluginDependencies, platformDependencies, pendingFiles, versionString, userTempDir, fileOrUrl, prefillDependencies);
            }
        }

        for (final Platform platform : processedPlatforms) {
            platformDependencies.putIfAbsent(platform, Collections.emptySortedSet());
            pluginDependencies.putIfAbsent(platform, Collections.emptySet());
        }
        return new PendingVersion(versionString, pluginDependencies, platformDependencies, pendingFiles);
    }

    private String createPendingFile(final MultipartFile file, final String channel, final ProjectTable projectTable, final Map<Platform, Set<PluginDependency>> pluginDependencies,
                                     final Map<Platform, SortedSet<String>> platformDependencies, final List<PendingVersionFile> pendingFiles, String versionString,
                                     final String userTempDir, final MultipartFileOrUrl fileOrUrl, final boolean prefillDependencies) {
        // check extension
        final String pluginFileName = file.getOriginalFilename();
        if (pluginFileName == null || (!pluginFileName.endsWith(".zip") && !pluginFileName.endsWith(".jar"))) {
            this.fileService.deleteDirectory(userTempDir);
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.fileExtension");
        }

        // move file into temp
        final PluginFileWithData pluginDataFile;
        try {
            final Platform platformToResolve = fileOrUrl.platforms().get(0); // Just save it by the first platform
            // read file
            final String tmpDir = this.fileService.resolve(userTempDir, platformToResolve.name());
            final String tmpPluginFile = this.fileService.resolve(tmpDir, pluginFileName);
            final byte[] bytes = file.getInputStream().readAllBytes();
            // write
            this.fileService.write(file.getInputStream(), tmpPluginFile, null);
            // load meta
            pluginDataFile = this.pluginDataService.loadMeta(pluginFileName, bytes, this.getHangarPrincipal().getUserId());
        } catch (final ConfigurateException configurateException) {
            this.fileService.deleteDirectory(userTempDir);
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.metaNotFound");
        } catch (final HangarApiException apiException) {
            this.fileService.deleteDirectory(userTempDir);
            throw apiException;
        } catch (final Exception e) {
            this.logger.error("Error while uploading {} for {}", pluginFileName, this.getHangarPrincipal().getName(), e);
            this.fileService.deleteDirectory(userTempDir);
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.unexpected");
        }

        if (versionString == null && pluginDataFile.data().getVersion() != null) {
            versionString = StringUtils.slugify(pluginDataFile.data().getVersion());
        }

        final FileInfo fileInfo = new FileInfo(pluginDataFile.fileName(), pluginDataFile.fileSize(), pluginDataFile.sha256());
        pendingFiles.add(new PendingVersionFile(fileOrUrl.platforms(), fileInfo, null));

        // setup dependencies
        if (prefillDependencies) {
            for (final Platform platform : fileOrUrl.platforms()) {
                final LastDependencies lastDependencies = this.getLastVersionDependencies(projectTable.getSlug(), channel, platform);
                if (lastDependencies != null) {
                    pluginDependencies.put(platform, lastDependencies.pluginDependencies());
                    platformDependencies.put(platform, lastDependencies.platformDependencies());
                    continue;
                }

                // If no previous version present, use uploaded version data
                final Set<PluginDependency> loadedPluginDependencies = pluginDataFile.data().getDependencies().get(platform);
                if (loadedPluginDependencies != null) {
                    pluginDependencies.put(platform, loadedPluginDependencies);
                }

                final SortedSet<String> loadedPlatformDependencies = pluginDataFile.data().getPlatformDependencies().get(platform);
                if (loadedPlatformDependencies != null) {
                    platformDependencies.put(platform, loadedPlatformDependencies);
                }
            }
        }
        return versionString;
    }

    private void createPendingUrl(final String channel, final ProjectTable projectTable, final Map<Platform, Set<PluginDependency>> pluginDependencies, final Map<Platform, SortedSet<String>> platformDependencies, final MultipartFileOrUrl fileOrUrl) {
        for (final Platform platform : fileOrUrl.platforms()) {
            final LastDependencies lastDependencies = this.getLastVersionDependencies(projectTable.getSlug(), channel, platform);
            if (lastDependencies != null) {
                pluginDependencies.put(platform, lastDependencies.pluginDependencies());
                platformDependencies.put(platform, lastDependencies.platformDependencies());
            }
        }
    }

    @Transactional
    public void publishPendingVersion(final long projectId, final PendingVersion pendingVersion) {
        // basic validation
        if (!this.validationService.isValidVersionName(pendingVersion.getVersionString())) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.invalidName");
        }
        if (pendingVersion.getPluginDependencies().values().stream().anyMatch(pluginDependencies -> pluginDependencies.size() > this.config.projects.maxDependencies())) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.tooManyDependencies");
        }
        if (this.exists(projectId, pendingVersion.getVersionString())) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.duplicateNameAndPlatform");
        }

        // get project
        final ProjectTable projectTable = this.projectService.getProjectTable(projectId);
        if (projectTable == null) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "Project not found");
        }

        // verify that all platform files exist and that platform dependencies are set up correctly
        final String userTempDir = this.projectFiles.getTempDir(this.getHangarPrincipal().getName());
        this.verifyPendingPlatforms(pendingVersion, userTempDir);

        ProjectVersionTable projectVersionTable = null;
        final String versionDir = this.projectFiles.getVersionDir(projectTable.getOwnerName(), projectTable.getSlug(), pendingVersion.getVersionString());
        try {
            // find channel
            ProjectChannelTable projectChannelTable = this.channelService.getProjectChannel(projectId, pendingVersion.getChannelName());
            if (projectChannelTable == null) {
                if (pendingVersion.getChannelColor() == null) {
                    throw new HangarApiException("version.new.error.channel.noColor");
                }

                projectChannelTable = this.channelService.createProjectChannel(pendingVersion.getChannelName(), pendingVersion.getChannelDescription(), pendingVersion.getChannelColor(), projectId, pendingVersion.getChannelFlags().stream().filter(ChannelFlag::isEditable).collect(Collectors.toSet()));
            }

            // insert version
            projectVersionTable = this.projectVersionsDAO.insert(new ProjectVersionTable(
                pendingVersion.getVersionString(),
                pendingVersion.getDescription(),
                projectId,
                projectChannelTable.getId(),
                this.getHangarPrincipal().getUserId()
            ));

            // insert dependencies
            this.processDependencies(pendingVersion, projectVersionTable.getId());

            // move over files
            final List<Pair<ProjectVersionDownloadTable, List<Platform>>> downloadsTables = new ArrayList<>();
            for (final PendingVersionFile pendingVersionFile : pendingVersion.getFiles()) {
                this.processPendingVersionFile(pendingVersion, userTempDir, projectVersionTable, versionDir, downloadsTables, pendingVersionFile);
            }

            // Insert download data
            final List<ProjectVersionDownloadTable> tables = this.downloadsDAO.insertDownloads(downloadsTables.stream().map(Pair::getLeft).toList());
            final List<ProjectVersionPlatformDownloadTable> platformDownloadsTables = new ArrayList<>();
            for (int i = 0; i < downloadsTables.size(); i++) {
                final ProjectVersionDownloadTable downloadTable = tables.get(i);
                for (final Platform platform : downloadsTables.get(i).getRight()) {
                    platformDownloadsTables.add(new ProjectVersionPlatformDownloadTable(projectVersionTable.getVersionId(), platform, downloadTable.getId()));
                }
            }
            this.downloadsDAO.insertPlatformDownloads(platformDownloadsTables);

            // send notifications
            if (projectChannelTable.getFlags().contains(ChannelFlag.SENDS_NOTIFICATIONS)) {
                this.notificationService.notifyUsersNewVersion(projectTable, projectVersionTable, this.projectService.getProjectWatchers(projectTable.getId()));
            }
            this.actionLogger.version(LogAction.VERSION_CREATED.create(VersionContext.of(projectId, projectVersionTable.getId()), "published", ""));

            if (projectTable.getVisibility() == Visibility.NEW) {
                this.projectVisibilityService.changeVisibility(projectTable, Visibility.PUBLIC, "First version");
            }

            // cache purging
            this.projectService.refreshHomeProjects();
            this.usersApiService.clearAuthorsCache();

            final List<Platform> platformsToScan = new ArrayList<>();
            boolean hasExternalLink = false;
            boolean safeLinks = true;
            for (final PendingVersionFile file : pendingVersion.getFiles()) {
                if (file.fileInfo() != null) {
                    platformsToScan.add(file.platforms().get(0));
                    continue;
                }

                hasExternalLink = true;
                safeLinks = safeLinks && this.config.security.checkSafe(file.externalUrl());
            }

            final boolean hasUnsafeLinks = hasExternalLink && !safeLinks;
            if (!platformsToScan.isEmpty()) {
                this.jarScanningService.scanAsync(projectVersionTable, platformsToScan, hasUnsafeLinks);
            } else if (!hasUnsafeLinks) {
                // External links will always show a secondary warning, even if from a safe website, so approving is ok
                this.reviewService.autoReviewLinks(projectVersionTable, this.jarScanningService.getJarScannerUser().getUserId());
            }
        } catch (final HangarApiException e) {
            throw e;
        } catch (final Exception e) {
            this.logger.error("Unable to create version {} for {}", pendingVersion.getVersionString(), this.getHangarPrincipal().getName(), e);
            if (projectVersionTable != null) {
                this.projectVersionsDAO.delete(projectVersionTable);
            }
            this.fileService.deleteDirectory(versionDir);
            if (e instanceof IOException) {
                throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.fileIOError");
            } else {
                throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.unknown");
            }
        }
    }

    private void processDependencies(final PendingVersion pendingVersion, final long versionId) {
        // platform deps
        final List<ProjectVersionPlatformDependencyTable> platformDependencyTables = new ArrayList<>();
        for (final Map.Entry<Platform, SortedSet<String>> entry : pendingVersion.getPlatformDependencies().entrySet()) {
            for (final String version : entry.getValue()) {
                final PlatformVersionTable platformVersionTable = this.platformService.getByPlatformAndVersion(entry.getKey(), version);
                platformDependencyTables.add(new ProjectVersionPlatformDependencyTable(versionId, platformVersionTable.getId()));
            }
        }
        this.projectVersionPlatformDependenciesDAO.insertAll(platformDependencyTables);

        // plugin deps
        final List<ProjectVersionDependencyTable> pluginDependencyTables = new ArrayList<>();
        for (final Map.Entry<Platform, Set<PluginDependency>> platformListEntry : pendingVersion.getPluginDependencies().entrySet()) {
            for (final PluginDependency pluginDependency : platformListEntry.getValue()) {
                Long depProjectId = null;
                if (pluginDependency.getExternalUrl() == null) {
                    // TODO only get id
                    // Hangar project dependency
                    final ProjectTable depProjectTable = this.projectService.getProjectTable(pluginDependency.getName());
                    if (depProjectTable == null) {
                        throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.invalidPluginDependencyNamespace", pluginDependency.getName());
                    }
                    depProjectId = depProjectTable.getProjectId();
                }

                pluginDependencyTables.add(new ProjectVersionDependencyTable(versionId, platformListEntry.getKey(), pluginDependency.getName(), pluginDependency.isRequired(), depProjectId, pluginDependency.getExternalUrl()));
            }
        }
        this.projectVersionDependenciesDAO.insertAll(pluginDependencyTables);
    }

    private void processPendingVersionFile(final PendingVersion pendingVersion, final String userTempDir, final ProjectVersionTable projectVersionTable, final String versionDir, final List<Pair<ProjectVersionDownloadTable, List<Platform>>> downloadsTables, final PendingVersionFile pendingVersionFile) throws IOException {
        final FileInfo fileInfo = pendingVersionFile.fileInfo();
        if (fileInfo == null) {
            final ProjectVersionDownloadTable table = new ProjectVersionDownloadTable(projectVersionTable.getVersionId(), null, null, null, pendingVersionFile.externalUrl());
            downloadsTables.add(ImmutablePair.of(table, pendingVersionFile.platforms()));
            return;
        }

        // Move file for first platform
        final Platform platformToResolve = pendingVersionFile.platforms().get(0);
        final String tmpVersionJar = this.fileService.resolve(this.fileService.resolve(userTempDir, platformToResolve.name()), fileInfo.getName());

        final String newVersionJarPath = this.fileService.resolve(this.fileService.resolve(versionDir, platformToResolve.name()), fileInfo.getName());
        this.fileService.moveFile(tmpVersionJar, newVersionJarPath);

        // Create links for the other platforms
        for (int i = 1; i < pendingVersionFile.platforms().size(); i++) {
            final Platform platform = pendingVersionFile.platforms().get(i);
            if (pendingVersion.getPlatformDependencies().get(platform).isEmpty()) {
                continue;
            }

            final String platformPath = this.fileService.resolve(versionDir, platform.name());
            final String platformJarPath = this.fileService.resolve(platformPath, fileInfo.getName());
            if (this.fileService instanceof S3FileService) {
                // this isn't nice, but we cant link, so what am I supposed to do?
                // fileService.move(tmpVersionJar.toString(), platformJarPath);
                // actually, lets do nothing here, in frontend only the primary platform is used for downloading anyways
            } else {
                this.fileService.link(newVersionJarPath, platformJarPath);
            }
        }

        final ProjectVersionDownloadTable table = new ProjectVersionDownloadTable(projectVersionTable.getVersionId(), fileInfo.sizeBytes(), fileInfo.sha256Hash(), fileInfo.getName(), null);
        downloadsTables.add(ImmutablePair.of(table, pendingVersionFile.platforms()));
    }

    private void verifyPendingPlatforms(final PendingVersion pendingVersion, final String userTempDir) {
        final Set<Platform> processedPlatforms = EnumSet.noneOf(Platform.class);
        for (final PendingVersionFile pendingVersionFile : pendingVersion.getFiles()) {
            if (!processedPlatforms.addAll(pendingVersionFile.platforms())) {
                throw new IllegalArgumentException("Overlapping platforms in pending version");
            }

            final FileInfo fileInfo = pendingVersionFile.fileInfo();
            if (fileInfo != null) { // verify file
                final Platform platform = pendingVersionFile.platforms().get(0); // Use the first platform to resolve
                final String tmpVersionJar = this.fileService.resolve(this.fileService.resolve(userTempDir, platform.name()), fileInfo.getName());
                try {
                    if (!this.fileService.exists(tmpVersionJar)) {
                        throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.noFile");
                    }

                    final byte[] bytes = this.fileService.bytes(tmpVersionJar);
                    if (bytes.length != fileInfo.sizeBytes()) {
                        throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.mismatchedFileSize");
                    } else if (!Objects.equals(CryptoUtils.sha256ToHex(bytes), fileInfo.sha256Hash())) {
                        throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.hashMismatch");
                    }
                } catch (final IOException e) {
                    this.logger.error("Could not publish version for {}", this.getHangarPrincipal().getName(), e);
                }
            }

            // Check if the platform versions are valid
            for (final Map.Entry<Platform, SortedSet<String>> entry : pendingVersion.getPlatformDependencies().entrySet()) {
                final Platform platform = entry.getKey();
                final Set<String> versionsForPlatform = new HashSet<>(this.platformService.getFullVersionsForPlatform(platform));
                if (!versionsForPlatform.containsAll(entry.getValue())) {
                    throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.invalidPlatformVersion");
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public @Nullable LastDependencies getLastVersionDependencies(final String slug, final @Nullable String channel, final Platform platform) {
        // TODO Optimize with specific query
        final RequestPagination pagination = new RequestPagination(1L, 0L);
        pagination.getFilters().put("platform", new VersionPlatformFilter.VersionPlatformFilterInstance(new Platform[]{platform}));
        if (channel != null) {
            // Find the last version with the specified channel
            pagination.getFilters().put("channel", new VersionChannelFilter.VersionChannelFilterInstance(new String[]{channel}));
        }

        final Long versionId = this.versionsApiDAO.getVersions(slug, false, this.getHangarUserId(), pagination).keySet().stream().findAny().orElse(null);
        if (versionId != null) {
            final SortedSet<String> platformDependency = this.versionsApiDAO.getPlatformDependencies(versionId).get(platform);
            if (platformDependency != null) {
                return new LastDependencies(platformDependency, this.versionsApiDAO.getPluginDependencies(versionId, platform));
            }
            return null;
        }

        // Try again with any channel, else empty
        return channel != null ? this.getLastVersionDependencies(slug, null, platform) : null;
    }

    private boolean exists(final long projectId, final String versionString) {
        return this.projectVersionsDAO.getProjectVersion(projectId, versionString) != null;
    }
}
