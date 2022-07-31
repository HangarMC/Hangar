package io.papermc.hangar.service.internal.versions;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.table.PlatformVersionDAO;
import io.papermc.hangar.db.dao.internal.table.versions.ProjectVersionsDAO;
import io.papermc.hangar.db.dao.internal.table.versions.dependencies.ProjectVersionDependenciesDAO;
import io.papermc.hangar.db.dao.internal.table.versions.dependencies.ProjectVersionPlatformDependenciesDAO;
import io.papermc.hangar.db.dao.internal.table.versions.downloads.ProjectVersionDownloadsDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.project.version.FileInfo;
import io.papermc.hangar.model.api.project.version.PluginDependency;
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
import io.papermc.hangar.model.internal.job.UpdateDiscourseProjectTopicJob;
import io.papermc.hangar.model.internal.job.UpdateDiscourseVersionPostJob;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.VersionContext;
import io.papermc.hangar.model.internal.versions.MultipartFileOrUrl;
import io.papermc.hangar.model.internal.versions.PendingVersion;
import io.papermc.hangar.model.internal.versions.PendingVersionFile;
import io.papermc.hangar.service.ValidationService;
import io.papermc.hangar.service.api.UsersApiService;
import io.papermc.hangar.service.internal.JobService;
import io.papermc.hangar.service.internal.PlatformService;
import io.papermc.hangar.service.internal.projects.ChannelService;
import io.papermc.hangar.service.internal.projects.ProjectService;
import io.papermc.hangar.service.internal.uploads.ProjectFiles;
import io.papermc.hangar.service.internal.users.NotificationService;
import io.papermc.hangar.service.internal.versions.plugindata.PluginDataService;
import io.papermc.hangar.service.internal.versions.plugindata.PluginFileWithData;
import io.papermc.hangar.service.internal.visibility.ProjectVisibilityService;
import io.papermc.hangar.util.CryptoUtils;
import io.papermc.hangar.util.FileUtils;
import io.papermc.hangar.util.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;

@Service
public class VersionFactory extends HangarComponent {

    private static final Object DUMMY = new Object();
    private final Cache<Path, Object> tempDirCache = Caffeine.newBuilder().expireAfterAccess(Duration.ofMinutes(30)).evictionListener((@Nullable Path path, @Nullable Object o, @NonNull RemovalCause cause) -> {
        // Clean up temp user dirs
        if (path != null && Files.exists(path)) {
            FileUtils.deleteDirectory(path);
        }
    }).build();
    private final ProjectVersionPlatformDependenciesDAO projectVersionPlatformDependenciesDAO;
    private final ProjectVersionDependenciesDAO projectVersionDependenciesDAO;
    private final PlatformVersionDAO platformVersionDAO;
    private final ProjectVersionsDAO projectVersionsDAO;
    private final ProjectFiles projectFiles;
    private final PluginDataService pluginDataService;
    private final ChannelService channelService;
    private final ProjectVisibilityService projectVisibilityService;
    private final ProjectService projectService;
    private final NotificationService notificationService;
    private final PlatformService platformService;
    private final UsersApiService usersApiService;
    private final JobService jobService;
    private final ValidationService validationService;
    private final ProjectVersionDownloadsDAO downloadsDAO;

    @Autowired
    public VersionFactory(ProjectVersionPlatformDependenciesDAO projectVersionPlatformDependencyDAO, ProjectVersionDependenciesDAO projectVersionDependencyDAO, PlatformVersionDAO platformVersionDAO, ProjectVersionsDAO projectVersionDAO, ProjectFiles projectFiles, PluginDataService pluginDataService, ChannelService channelService, ProjectVisibilityService projectVisibilityService, ProjectService projectService, NotificationService notificationService, PlatformService platformService, UsersApiService usersApiService, JobService jobService, ValidationService validationService, final ProjectVersionDownloadsDAO downloadsDAO) {
        this.projectVersionPlatformDependenciesDAO = projectVersionPlatformDependencyDAO;
        this.projectVersionDependenciesDAO = projectVersionDependencyDAO;
        this.platformVersionDAO = platformVersionDAO;
        this.projectVersionsDAO = projectVersionDAO;
        this.projectFiles = projectFiles;
        this.pluginDataService = pluginDataService;
        this.channelService = channelService;
        this.projectVisibilityService = projectVisibilityService;
        this.projectService = projectService;
        this.notificationService = notificationService;
        this.platformService = platformService;
        this.usersApiService = usersApiService;
        this.jobService = jobService;
        this.validationService = validationService;
        this.downloadsDAO = downloadsDAO;
    }

    public PendingVersion createPendingVersion(final long projectId, final List<MultipartFileOrUrl> data, final List<MultipartFile> files) {
        final ProjectChannelTable projectChannelTable = channelService.getFirstChannel(projectId);
        final Map<Platform, Set<PluginDependency>> pluginDependencies = new EnumMap<>(Platform.class);
        final Map<Platform, SortedSet<String>> platformDependencies = new EnumMap<>(Platform.class);
        final List<PendingVersionFile> pendingFiles = new ArrayList<>();
        String versionString = null;
        final Set<Platform> processedPlatforms = EnumSet.noneOf(Platform.class);

        // Delete old temp data
        final Path userTempDir = projectFiles.getTempDir(getHangarPrincipal().getName());
        if (Files.exists(userTempDir)) {
            FileUtils.deleteDirectory(userTempDir);
        }

        for (final MultipartFileOrUrl fileOrUrl : data) {
            // Make sure each platform only has one corresponding file/url
            if (!processedPlatforms.addAll(fileOrUrl.platforms())) {
                FileUtils.deleteDirectory(userTempDir);
                throw new IllegalArgumentException();
            }

            if (fileOrUrl.isUrl()) {
                // Handle external url
                pendingFiles.add(new PendingVersionFile(fileOrUrl.platforms(), null, fileOrUrl.externalUrl()));
                continue;
            }

            final MultipartFile file = files.remove(0);
            final String pluginFileName = file.getOriginalFilename();
            if (pluginFileName == null || (!pluginFileName.endsWith(".zip") && !pluginFileName.endsWith(".jar"))) {
                FileUtils.deleteDirectory(userTempDir);
                throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.fileExtension");
            }

            final PluginFileWithData pluginDataFile;
            final Platform platformToResolve = fileOrUrl.platforms().get(0); // Just save it by the first platform
            final Path tmpDir = userTempDir.resolve(platformToResolve.name());
            try {
                if (!Files.isDirectory(tmpDir)) {
                    Files.createDirectories(tmpDir);
                }

                final Path tmpPluginFile = tmpDir.resolve(pluginFileName);
                file.transferTo(tmpPluginFile);
                pluginDataFile = pluginDataService.loadMeta(tmpPluginFile, getHangarPrincipal().getUserId());
            } catch (final ConfigurateException configurateException) {
                logger.error("Error while reading file metadata while uploading {} for {}", pluginFileName, getHangarPrincipal().getName(), configurateException);
                FileUtils.deleteDirectory(userTempDir);
                throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.metaNotFound");
            } catch (final IOException e) {
                logger.error("Error while uploading {} for {}", pluginFileName, getHangarPrincipal().getName(), e);
                FileUtils.deleteDirectory(userTempDir);
                throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.unexpected");
            }

            if (versionString == null && pluginDataFile.getData().getVersion() != null) {
                versionString = StringUtils.slugify(pluginDataFile.getData().getVersion());
            }

            final FileInfo fileInfo = new FileInfo(pluginDataFile.getPath().getFileName().toString(), pluginDataFile.getPath().toFile().length(), pluginDataFile.getMd5());
            pendingFiles.add(new PendingVersionFile(fileOrUrl.platforms(), fileInfo, null));
            for (final Platform platform : fileOrUrl.platforms()) {
                //TODO add last version data if present, if not present the file data
                final Set<PluginDependency> loadedPluginDependencies = pluginDataFile.getData().getDependencies().get(platform);
                if (loadedPluginDependencies != null) {
                    pluginDependencies.put(platform, loadedPluginDependencies);
                }

                final SortedSet<String> loadedPlatformDependencies = pluginDataFile.getData().getPlatformDependencies().get(platform);
                if (loadedPlatformDependencies != null) {
                    platformDependencies.put(platform, loadedPlatformDependencies);
                }
            }
        }

        tempDirCache.put(userTempDir, DUMMY);

        final ProjectTable projectTable = projectService.getProjectTable(projectId);
        if (projectTable == null) {
            throw new IllegalArgumentException();
        }

        for (final Platform platform : processedPlatforms) {
            platformDependencies.putIfAbsent(platform, Collections.emptySortedSet());
            pluginDependencies.putIfAbsent(platform, Collections.emptySet());
        }
        return new PendingVersion(versionString, pluginDependencies, platformDependencies, pendingFiles, projectChannelTable, projectTable.isForumSync());
    }

    @Transactional
    public void publishPendingVersion(long projectId, final PendingVersion pendingVersion) {
        if (!validationService.isValidVersionName(pendingVersion.getVersionString())) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.invalidName");
        }
        if (pendingVersion.getPluginDependencies().values().stream().anyMatch(pluginDependencies -> pluginDependencies.size() > config.projects.getMaxDependencies())) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.tooManyDependencies");
        }
        if (exists(projectId, pendingVersion.getVersionString())) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.duplicateNameAndPlatform");
        }

        final ProjectTable projectTable = projectService.getProjectTable(projectId);
        assert projectTable != null;

        final Set<Platform> processedPlatforms = EnumSet.noneOf(Platform.class);
        final Path userTempDir = projectFiles.getTempDir(getHangarPrincipal().getName());
        for (final PendingVersionFile pendingVersionFile : pendingVersion.getFiles()) {
            if (!processedPlatforms.addAll(pendingVersionFile.platforms())) {
                throw new IllegalArgumentException();
            }

            final FileInfo fileInfo = pendingVersionFile.fileInfo();
            if (fileInfo != null) { // verify file
                final Platform platform = pendingVersionFile.platforms().get(0); // Use the first platform to resolve
                final Path tmpVersionJar = userTempDir.resolve(platform.name()).resolve(fileInfo.getName());
                try {
                    if (Files.notExists(tmpVersionJar)) {
                        throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.noFile");
                    } else if (tmpVersionJar.toFile().length() != fileInfo.getSizeBytes()) {
                        throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.mismatchedFileSize");
                    } else if (!Objects.equals(CryptoUtils.md5ToHex(Files.readAllBytes(tmpVersionJar)), fileInfo.getMd5Hash())) {
                        throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.hashMismatch");
                    }
                } catch (final IOException e) {
                    logger.error("Could not publish version for {}", getHangarPrincipal().getName(), e);
                }
            }

            if (pendingVersion.getPlatformDependencies().entrySet().stream().anyMatch(en -> !new HashSet<>(platformService.getVersionsForPlatform(en.getKey())).containsAll(en.getValue()))) {
                throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.invalidPlatformVersion");
            }
        }

        ProjectVersionTable projectVersionTable = null;
        final Path versionDir = projectFiles.getVersionDir(projectTable.getOwnerName(), projectTable.getSlug(), pendingVersion.getVersionString());
        try {
            ProjectChannelTable projectChannelTable = channelService.getProjectChannel(projectId, pendingVersion.getChannelName(), pendingVersion.getChannelColor());
            if (projectChannelTable == null) {
                projectChannelTable = this.channelService.createProjectChannel(pendingVersion.getChannelName(), pendingVersion.getChannelColor(), projectId, pendingVersion.getChannelFlags().stream().filter(ChannelFlag::isEditable).collect(Collectors.toSet()));
            }

            //TODO automatic checks for malicious code or files => set visibility to NEEDSAPPROVAL
            projectVersionTable = projectVersionsDAO.insert(new ProjectVersionTable(
                pendingVersion.getVersionString(),
                pendingVersion.getDescription(),
                projectId,
                projectChannelTable.getId(),
                getHangarPrincipal().getUserId(),
                pendingVersion.isForumSync()
            ));

            List<ProjectVersionPlatformDependencyTable> platformDependencyTables = new ArrayList<>();
            for (Map.Entry<Platform, SortedSet<String>> entry : pendingVersion.getPlatformDependencies().entrySet()) {
                for (String version : entry.getValue()) {
                    PlatformVersionTable platformVersionTable = platformVersionDAO.getByPlatformAndVersion(entry.getKey(), version);
                    platformDependencyTables.add(new ProjectVersionPlatformDependencyTable(projectVersionTable.getId(), platformVersionTable.getId()));
                }
            }
            projectVersionPlatformDependenciesDAO.insertAll(platformDependencyTables);

            List<ProjectVersionDependencyTable> pluginDependencyTables = new ArrayList<>();
            for (Map.Entry<Platform, Set<PluginDependency>> platformListEntry : pendingVersion.getPluginDependencies().entrySet()) {
                for (PluginDependency pluginDependency : platformListEntry.getValue()) {
                    Long depProjectId = null;
                    if (pluginDependency.getNamespace() != null) {
                        Optional<ProjectTable> depProjectTable = Optional.ofNullable(projectService.getProjectTable(pluginDependency.getNamespace().getOwner(), pluginDependency.getNamespace().getSlug()));
                        if (depProjectTable.isEmpty()) {
                            throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.invalidPluginDependencyNamespace");
                        }
                        depProjectId = depProjectTable.get().getProjectId();
                    }
                    pluginDependencyTables.add(new ProjectVersionDependencyTable(projectVersionTable.getId(), platformListEntry.getKey(), pluginDependency.getName(), pluginDependency.isRequired(), depProjectId, pluginDependency.getExternalUrl()));
                }
            }
            projectVersionDependenciesDAO.insertAll(pluginDependencyTables);

            notificationService.notifyUsersNewVersion(projectTable, projectVersionTable, projectService.getProjectWatchers(projectTable.getId()));

            final List<Pair<ProjectVersionDownloadTable, List<Platform>>> downloadsTables = new ArrayList<>();
            for (final PendingVersionFile pendingVersionFile : pendingVersion.getFiles()) {
                final FileInfo fileInfo = pendingVersionFile.fileInfo();
                if (fileInfo == null) {
                    final ProjectVersionDownloadTable table = new ProjectVersionDownloadTable(projectVersionTable.getVersionId(), null, null, null, pendingVersionFile.externalUrl());
                    downloadsTables.add(ImmutablePair.of(table, pendingVersionFile.platforms()));
                    continue;
                }

                // Move file for first platform
                final Platform platformToResolve = pendingVersionFile.platforms().get(0);
                final Path tmpVersionJar = userTempDir.resolve(platformToResolve.name()).resolve(fileInfo.getName());

                final Path newVersionJarPath = versionDir.resolve(platformToResolve.name()).resolve(tmpVersionJar.getFileName());
                if (Files.notExists(newVersionJarPath)) {
                    Files.createDirectories(newVersionJarPath.getParent());
                }

                Files.move(tmpVersionJar, newVersionJarPath, StandardCopyOption.REPLACE_EXISTING);
                if (Files.notExists(newVersionJarPath)) {
                    throw new IOException("Didn't successfully move the jar");
                }

                // Create symbolic links for the other platforms
                for (int i = 1; i < pendingVersionFile.platforms().size(); i++) {
                    final Platform platform = pendingVersionFile.platforms().get(i);
                    if (pendingVersion.getPlatformDependencies().get(platform).isEmpty()) {
                        continue;
                    }

                    final Path platformPath = versionDir.resolve(platform.name());
                    if (Files.notExists(platformPath)) {
                        Files.createDirectories(platformPath);
                    }
                    final Path platformJarPath = platformPath.resolve(tmpVersionJar.getFileName());
                    Files.createSymbolicLink(platformJarPath, newVersionJarPath);
                }

                final ProjectVersionDownloadTable table = new ProjectVersionDownloadTable(projectVersionTable.getVersionId(), fileInfo.getSizeBytes(), fileInfo.getMd5Hash(), fileInfo.getName(), null);
                downloadsTables.add(ImmutablePair.of(table, pendingVersionFile.platforms()));

            }

            // Insert download data
            final List<ProjectVersionDownloadTable> tables = downloadsDAO.insertDownloads(downloadsTables.stream().map(Pair::getLeft).collect(Collectors.toList()));
            final List<ProjectVersionPlatformDownloadTable> platformDownloadsTables = new ArrayList<>();
            for (int i = 0; i < downloadsTables.size(); i++) {
                final ProjectVersionDownloadTable downloadTable = tables.get(i);
                for (final Platform platform : downloadsTables.get(i).getRight()) {
                    platformDownloadsTables.add(new ProjectVersionPlatformDownloadTable(projectVersionTable.getVersionId(), platform, downloadTable.getId()));
                }
            }
            downloadsDAO.insertPlatformDownloads(platformDownloadsTables);

            if (projectTable.getVisibility() == Visibility.NEW) {
                projectVisibilityService.changeVisibility(projectTable, Visibility.PUBLIC, "First version");
                jobService.save(new UpdateDiscourseProjectTopicJob(projectId));
            }

            actionLogger.version(LogAction.VERSION_CREATED.create(VersionContext.of(projectId, projectVersionTable.getId()), "published", ""));

            if (pendingVersion.isForumSync()) {
                jobService.save(new UpdateDiscourseVersionPostJob(projectVersionTable.getId()));
            }

            projectService.refreshHomeProjects();
            usersApiService.clearAuthorsCache();
        } catch (IOException e) {
            logger.error("Unable to create version {} for {}", pendingVersion.getVersionString(), getHangarPrincipal().getName(), e);
            projectVersionsDAO.delete(projectVersionTable);
            if (Files.exists(versionDir)) {
                FileUtils.deleteDirectory(versionDir);
            }
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.fileIOError");
        } catch (Exception throwable) {
            logger.error("Unable to create version {} for {}", pendingVersion.getVersionString(), getHangarPrincipal().getName(), throwable);
            if (projectVersionTable != null) {
                projectVersionsDAO.delete(projectVersionTable);
            }
            if (Files.exists(versionDir)) {
                FileUtils.deleteDirectory(versionDir);
            }
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.unknown");
        }
    }

    private boolean exists(long projectId, String versionString) {
        return projectVersionsDAO.getProjectVersion(projectId, versionString) != null;
    }
}
