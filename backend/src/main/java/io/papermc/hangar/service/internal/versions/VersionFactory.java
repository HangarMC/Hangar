package io.papermc.hangar.service.internal.versions;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.components.images.service.AvatarService;
import io.papermc.hangar.components.index.IndexService;
import io.papermc.hangar.components.webhook.model.event.ProjectPublishedEvent;
import io.papermc.hangar.components.webhook.model.event.VersionPublishedEvent;
import io.papermc.hangar.components.webhook.service.WebhookService;
import io.papermc.hangar.db.dao.internal.table.versions.ProjectVersionsDAO;
import io.papermc.hangar.db.dao.internal.table.versions.dependencies.ProjectVersionDependenciesDAO;
import io.papermc.hangar.db.dao.internal.table.versions.downloads.ProjectVersionDownloadsDAO;
import io.papermc.hangar.db.dao.v1.VersionsApiDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.project.version.FileInfo;
import io.papermc.hangar.model.api.project.version.PluginDependency;
import io.papermc.hangar.model.common.ChannelFlag;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.model.db.projects.ProjectChannelTable;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import io.papermc.hangar.model.db.versions.dependencies.ProjectVersionDependencyTable;
import io.papermc.hangar.model.db.versions.downloads.ProjectVersionDownloadTable;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.VersionContext;
import io.papermc.hangar.model.internal.versions.LastDependencies;
import io.papermc.hangar.model.internal.versions.MultipartFileOrUrl;
import io.papermc.hangar.model.internal.versions.PendingVersion;
import io.papermc.hangar.model.internal.versions.PendingVersionFile;
import io.papermc.hangar.service.ValidationService;
import io.papermc.hangar.service.api.UsersApiService;
import io.papermc.hangar.service.api.VersionsApiService;
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
import java.io.InputStream;
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
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class VersionFactory extends HangarComponent {

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
    private final WebhookService webhookService;
    private final AvatarService avatarService;
    private final VersionsApiService versionApiService;
    private final IndexService indexService;
    private final ConcurrentTaskExecutor taskExecutor;

    @Autowired
    public VersionFactory(final ProjectVersionDependenciesDAO projectVersionDependencyDAO, final ProjectVersionsDAO projectVersionDAO, final ProjectFiles projectFiles, final PluginDataService pluginDataService, final ChannelService channelService, final ProjectVisibilityService projectVisibilityService, final ProjectService projectService, final NotificationService notificationService, final PlatformService platformService, final UsersApiService usersApiService, final ValidationService validationService, final ProjectVersionDownloadsDAO downloadsDAO, final VersionsApiDAO versionsApiDAO, final FileService fileService, final JarScanningService jarScanningService, final ReviewService reviewService, final WebhookService webhookService, final AvatarService avatarService, final @Lazy VersionsApiService versionApiService, final IndexService indexService, ConcurrentTaskExecutor taskExecutor) {
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
        this.webhookService = webhookService;
        this.avatarService = avatarService;
        this.versionApiService = versionApiService;
        this.indexService = indexService;
        this.taskExecutor = taskExecutor;
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

                final MultipartFile file = files.removeFirst();
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
            final Platform platformToResolve = fileOrUrl.platforms().getFirst(); // Just save it by the first platform
            // read file
            final String tmpDir = this.fileService.resolve(userTempDir, platformToResolve.name());
            final String tmpPluginFile = this.fileService.resolve(tmpDir, pluginFileName);
            final byte[] bytes;
            try (final InputStream in = file.getInputStream()) {
                bytes = in.readAllBytes();
            }
            this.fileService.write(file, bytes, tmpPluginFile, null);

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
                final LastDependencies lastDependencies = this.getLastVersionDependencies(projectTable, channel, platform);
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
                if (loadedPlatformDependencies != null && !loadedPlatformDependencies.isEmpty()) {
                    // Make sure we don't add invalid versions
                    final Set<String> versionsForPlatform = new HashSet<>(this.platformService.getFullVersionsForPlatform(platform));
                    loadedPlatformDependencies.retainAll(versionsForPlatform);

                    platformDependencies.put(platform, loadedPlatformDependencies);
                }
            }
        }
        return versionString;
    }

    private void createPendingUrl(final String channel, final ProjectTable projectTable, final Map<Platform, Set<PluginDependency>> pluginDependencies, final Map<Platform, SortedSet<String>> platformDependencies, final MultipartFileOrUrl fileOrUrl) {
        for (final Platform platform : fileOrUrl.platforms()) {
            final LastDependencies lastDependencies = this.getLastVersionDependencies(projectTable, channel, platform);
            if (lastDependencies != null) {
                pluginDependencies.put(platform, lastDependencies.pluginDependencies());
                platformDependencies.put(platform, lastDependencies.platformDependencies());
            }
        }
    }

    @Transactional
    public ProjectVersionTable publishPendingVersion(final long projectId, final PendingVersion pendingVersion) {
        // basic validation
        if (!this.validationService.isValidVersionName(pendingVersion.getVersionString())) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.invalidName");
        }
        if (pendingVersion.getPluginDependencies().values().stream().anyMatch(pluginDependencies -> pluginDependencies.size() > this.config.projects().maxDependencies())) {
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
                this.getHangarPrincipal().getUserId(),
                pendingVersion.getPlatformDependencies()
            ));

            // insert dependencies
            this.processDependencies(pendingVersion, projectVersionTable.getId());

            // move over files
            final List<ProjectVersionDownloadTable> downloadsTables = new ArrayList<>();
            for (final PendingVersionFile pendingVersionFile : pendingVersion.getFiles()) {
                this.processPendingVersionFile(pendingVersion, userTempDir, projectVersionTable, versionDir, downloadsTables, pendingVersionFile);
            }

            // Insert download data
            this.downloadsDAO.insertDownloads(downloadsTables);

            this.actionLogger.version(LogAction.VERSION_CREATED.create(VersionContext.of(projectId, projectVersionTable.getId()), "published", ""));

            // change visibility
            final boolean firstPublish = projectTable.getVisibility() == Visibility.NEW;
            if (firstPublish) {
                this.projectVisibilityService.changeVisibility(projectTable, Visibility.PUBLIC, "First version");
            }

            // do slow stuff later
            final ProjectChannelTable finalProjectChannelTable = projectChannelTable;
            final ProjectVersionTable finalProjectVersionTable = projectVersionTable;
            CompletableFuture.runAsync(() -> {
                try {
                    postPublish(pendingVersion, projectTable, finalProjectChannelTable, finalProjectVersionTable, firstPublish);
                } catch (Throwable ex) {
                    logger.warn("Error in post publish for version {} of project {}", pendingVersion.getVersionString(), projectTable.getName(), ex);
                }
            }, taskExecutor);

            return projectVersionTable;
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

    private void postPublish(PendingVersion pendingVersion, ProjectTable projectTable, ProjectChannelTable projectChannelTable, ProjectVersionTable projectVersionTable, boolean firstPublish) {
        // send notifications
        if (projectChannelTable.getFlags().contains(ChannelFlag.SENDS_NOTIFICATIONS)) {
            this.notificationService.notifyUsersNewVersion(projectTable, projectVersionTable, this.projectService.getProjectWatchers(projectTable.getId()));
        }

        // send webhooks
        final List<String> platformString = pendingVersion.getPlatformDependencies().keySet().stream().map(Platform::getName).toList();
        // TODO rewrite avatar fetching (for move this code to an async method)
        final String avatarUrl = this.avatarService.getProjectAvatarUrl(projectTable.getProjectId(), projectTable.getOwnerName());
        final String url = this.config.baseUrl() + "/" + projectTable.getOwnerName() + "/" + projectTable.getSlug();
        if (firstPublish) {
            this.webhookService.handleEvent(new ProjectPublishedEvent(projectTable.getOwnerName(), projectTable.getName(), avatarUrl, url, projectTable.getDescription(), platformString));
        }
        this.webhookService.handleEvent(new VersionPublishedEvent(projectTable.getOwnerName(), projectTable.getName(), avatarUrl, url + "/versions/" + projectVersionTable.getVersionString(), projectVersionTable.getVersionString(), projectVersionTable.getDescription(), platformString));

        // cache purging
        this.indexService.updateProject(projectTable.getId());
        this.indexService.updateVersion(projectVersionTable.getId());
        this.usersApiService.clearAuthorsCache();
        this.versionApiService.evictLatestRelease(projectTable.getId());
        this.versionApiService.evictLatest(projectTable.getId(), projectChannelTable.getName());

        // jar scanning
        final List<Platform> platformsToScan = new ArrayList<>();
        boolean hasExternalLink = false;
        boolean safeLinks = true;
        for (final PendingVersionFile file : pendingVersion.getFiles()) {
            if (file.fileInfo() != null) {
                platformsToScan.add(file.platforms().getFirst());
                continue;
            }

            hasExternalLink = true;
            safeLinks = safeLinks && this.config.security().checkSafe(file.externalUrl());
        }

        final boolean hasUnsafeLinks = hasExternalLink && !safeLinks;
        if (!platformsToScan.isEmpty()) {
            this.jarScanningService.scanAsync(projectVersionTable, platformsToScan, hasUnsafeLinks);
        } else if (!hasUnsafeLinks) {
            // External links will always show a secondary warning, even if from a safe website, so approving is ok
            this.reviewService.autoReviewLinks(projectVersionTable, this.jarScanningService.getJarScannerUser().getUserId());
        }
    }

    private void processDependencies(final PendingVersion pendingVersion, final long versionId) {
        final List<ProjectVersionDependencyTable> pluginDependencyTables = new ArrayList<>();
        for (final Map.Entry<Platform, Set<PluginDependency>> platformListEntry : pendingVersion.getPluginDependencies().entrySet()) {
            for (final PluginDependency pluginDependency : platformListEntry.getValue()) {
                Long depProjectId = null;
                if (pluginDependency.getExternalUrl() == null) {
                    depProjectId = this.projectService.getProjectId(pluginDependency.getName());
                    if (depProjectId == null) {
                        throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.invalidPluginDependencyNamespace", pluginDependency.getName());
                    }
                }

                pluginDependencyTables.add(new ProjectVersionDependencyTable(versionId, platformListEntry.getKey(), pluginDependency.getName(), pluginDependency.isRequired(), depProjectId, pluginDependency.getExternalUrl()));
            }
        }
        this.projectVersionDependenciesDAO.insertAll(pluginDependencyTables);
    }

    private void processPendingVersionFile(final PendingVersion pendingVersion, final String userTempDir, final ProjectVersionTable projectVersionTable, final String versionDir, final List<ProjectVersionDownloadTable> downloadsTables, final PendingVersionFile pendingVersionFile) throws IOException {
        final Platform downloadPlatform = pendingVersionFile.platforms().getFirst();
        final FileInfo fileInfo = pendingVersionFile.fileInfo();
        if (fileInfo == null) {
            final ProjectVersionDownloadTable table = new ProjectVersionDownloadTable(projectVersionTable.getVersionId(), null, null, null, pendingVersionFile.externalUrl(), pendingVersionFile.platforms().toArray(new Platform[0]), downloadPlatform);
            downloadsTables.add(table);
            return;
        }

        // Move file for first platform
        final String tmpVersionJar = this.fileService.resolve(this.fileService.resolve(userTempDir, downloadPlatform.name()), fileInfo.getName());

        final String newVersionJarPath = this.fileService.resolve(this.fileService.resolve(versionDir, downloadPlatform.name()), fileInfo.getName());
        this.fileService.moveFile(tmpVersionJar, newVersionJarPath);

        // Create links for the other platforms
        for (int i = 1; i < pendingVersionFile.platforms().size(); i++) {
            final Platform platform = pendingVersionFile.platforms().get(i);
            if (pendingVersion.getPlatformDependencies().get(platform).isEmpty()) {
                continue;
            }

            final String platformPath = this.fileService.resolve(versionDir, platform.name());
            final String platformJarPath = this.fileService.resolve(platformPath, fileInfo.getName());
            // noinspection StatementWithEmptyBody
            if (this.fileService instanceof S3FileService) {
                // this isn't nice, but we cant link, so what am I supposed to do?
                // fileService.move(tmpVersionJar.toString(), platformJarPath);
                // actually, lets do nothing here, in frontend only the primary platform is used for downloading anyways
            } else {
                this.fileService.link(newVersionJarPath, platformJarPath);
            }
        }

        final ProjectVersionDownloadTable table = new ProjectVersionDownloadTable(projectVersionTable.getVersionId(), fileInfo.sizeBytes(), fileInfo.sha256Hash(), fileInfo.getName(), null, pendingVersionFile.platforms().toArray(new Platform[0]), downloadPlatform);
        downloadsTables.add(table);
    }

    private void verifyPendingPlatforms(final PendingVersion pendingVersion, final String userTempDir) {
        final Set<Platform> processedPlatforms = EnumSet.noneOf(Platform.class);
        for (final PendingVersionFile pendingVersionFile : pendingVersion.getFiles()) {
            if (!processedPlatforms.addAll(pendingVersionFile.platforms())) {
                throw new IllegalArgumentException("Overlapping platforms in pending version");
            }

            final FileInfo fileInfo = pendingVersionFile.fileInfo();
            if (fileInfo != null) { // verify file
                final Platform platform = pendingVersionFile.platforms().getFirst(); // Use the first platform to resolve
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
                    throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.invalidPlatformVersionList", entry.getValue());
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public @Nullable LastDependencies getLastVersionDependencies(final ProjectTable project, final @Nullable String channel, final Platform platform) {
        final Long lastVersion = this.versionsApiDAO.getLatestVersionId(project.getProjectId(), channel == null ? this.config.channels().nameDefault() : channel, platform);
        if (lastVersion != null) {
            final ProjectVersionTable projectVersionTable = this.projectVersionsDAO.getProjectVersionTable(lastVersion);
            if (projectVersionTable != null) {
                return new LastDependencies(projectVersionTable.getPlatformsAsMap().get(platform), this.versionsApiDAO.getPluginDependencies(lastVersion, platform));
            }
        }
        return null;
    }

    private boolean exists(final long projectId, final String versionString) {
        return this.projectVersionsDAO.getProjectVersion(projectId, versionString) != null;
    }
}
