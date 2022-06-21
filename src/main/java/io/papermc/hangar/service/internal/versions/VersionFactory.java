package io.papermc.hangar.service.internal.versions;

import org.spongepowered.configurate.ConfigurateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.table.PlatformVersionDAO;
import io.papermc.hangar.db.dao.internal.table.versions.ProjectVersionsDAO;
import io.papermc.hangar.db.dao.internal.table.versions.dependencies.ProjectVersionDependenciesDAO;
import io.papermc.hangar.db.dao.internal.table.versions.dependencies.ProjectVersionPlatformDependenciesDAO;
import io.papermc.hangar.db.dao.v1.VersionsApiDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.project.version.FileInfo;
import io.papermc.hangar.model.api.project.version.PluginDependency;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.model.db.PlatformVersionTable;
import io.papermc.hangar.model.db.projects.ProjectChannelTable;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import io.papermc.hangar.model.db.versions.ProjectVersionTagTable;
import io.papermc.hangar.model.db.versions.dependencies.ProjectVersionDependencyTable;
import io.papermc.hangar.model.db.versions.dependencies.ProjectVersionPlatformDependencyTable;
import io.papermc.hangar.model.internal.job.UpdateDiscourseProjectTopicJob;
import io.papermc.hangar.model.internal.job.UpdateDiscourseVersionPostJob;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.VersionContext;
import io.papermc.hangar.model.internal.versions.PendingVersion;
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
import io.papermc.hangar.util.StringUtils;

@Service
public class VersionFactory extends HangarComponent {

    private final ProjectVersionPlatformDependenciesDAO projectVersionPlatformDependenciesDAO;
    private final ProjectVersionDependenciesDAO projectVersionDependenciesDAO;
    private final PlatformVersionDAO platformVersionDAO;
    private final ProjectVersionsDAO projectVersionsDAO;
    private final VersionsApiDAO versionsApiDAO;
    private final ProjectFiles projectFiles;
    private final PluginDataService pluginDataService;
    private final ChannelService channelService;
    private final ProjectVisibilityService projectVisibilityService;
    private final RecommendedVersionService recommendedVersionService;
    private final ProjectService projectService;
    private final NotificationService notificationService;
    private final VersionTagService versionTagService;
    private final PlatformService platformService;
    private final UsersApiService usersApiService;
    private final JobService jobService;
    private final ValidationService validationService;

    @Autowired
    public VersionFactory(ProjectVersionPlatformDependenciesDAO projectVersionPlatformDependencyDAO, ProjectVersionDependenciesDAO projectVersionDependencyDAO, PlatformVersionDAO platformVersionDAO, ProjectVersionsDAO projectVersionDAO, VersionsApiDAO versionsApiDAO, ProjectFiles projectFiles, PluginDataService pluginDataService, ChannelService channelService, ProjectVisibilityService projectVisibilityService, RecommendedVersionService recommendedVersionService, ProjectService projectService, NotificationService notificationService, VersionTagService versionTagService, PlatformService platformService, UsersApiService usersApiService, JobService jobService, ValidationService validationService) {
        this.projectVersionPlatformDependenciesDAO = projectVersionPlatformDependencyDAO;
        this.projectVersionDependenciesDAO = projectVersionDependencyDAO;
        this.platformVersionDAO = platformVersionDAO;
        this.projectVersionsDAO = projectVersionDAO;
        this.versionsApiDAO = versionsApiDAO;
        this.projectFiles = projectFiles;
        this.pluginDataService = pluginDataService;
        this.channelService = channelService;
        this.projectVisibilityService = projectVisibilityService;
        this.recommendedVersionService = recommendedVersionService;
        this.projectService = projectService;
        this.notificationService = notificationService;
        this.versionTagService = versionTagService;
        this.platformService = platformService;
        this.usersApiService = usersApiService;
        this.jobService = jobService;
        this.validationService = validationService;
    }

    public PendingVersion createPendingVersion(long projectId, MultipartFile file) {
        ProjectTable projectTable = projectService.getProjectTable(projectId);
        assert projectTable != null;
        String pluginFileName = file.getOriginalFilename();
        if (pluginFileName == null || (!pluginFileName.endsWith(".zip") && !pluginFileName.endsWith(".jar"))) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.fileExtension");
        }

        PluginFileWithData pluginDataFile;
        try {
            Path tmpDir = projectFiles.getTempDir(getHangarPrincipal().getName());
            if (!Files.isDirectory(tmpDir)) {
                Files.createDirectories(tmpDir);
            }

            Path tmpPluginFile = tmpDir.resolve(pluginFileName);
            file.transferTo(tmpPluginFile);
            pluginDataFile = pluginDataService.loadMeta(tmpPluginFile, getHangarPrincipal().getUserId());
        } catch (ConfigurateException configurateException) {
            logger.error("Error while reading file metadata while uploading {} for {}", pluginFileName, getHangarPrincipal().getName(), configurateException);
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.metaNotFound");
        } catch (IOException e) {
            logger.error("Error while uploading {} for {}", pluginFileName, getHangarPrincipal().getName(), e);
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.unexpected");
        }

        String versionString = StringUtils.slugify(pluginDataFile.getData().getVersion());
        if (!config.projects.getVersionNameMatcher().test(versionString)) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.invalidVersionString");
        }

        if (exists(projectId, versionString, pluginDataFile.getData().getPlatformDependencies().keySet())) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.duplicateNameAndPlatform");
        }

        ProjectChannelTable projectChannelTable = channelService.getFirstChannel(projectId);
        PendingVersion pendingVersion = new PendingVersion(
                StringUtils.slugify(pluginDataFile.getData().getVersion()),
                pluginDataFile.getData().getDependencies(),
                pluginDataFile.getData().getPlatformDependencies(),
                pluginDataFile.getData().getDescription(),
                new FileInfo(pluginDataFile.getPath().getFileName().toString(), pluginDataFile.getPath().toFile().length(), pluginDataFile.getMd5()),
                projectChannelTable,
                projectTable.isForumSync()
        );

        if (!validationService.isValidVersionName(pendingVersion.getVersionString())) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.invalidName");
        }

        if (projectVersionsDAO.getProjectVersionTableFromHashAndName(projectId, pluginDataFile.getMd5(), pendingVersion.getVersionString()) != null && config.projects.isFileValidate()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.duplicate");
        }
        return pendingVersion;
    }

    public PendingVersion createPendingVersion(long projectId, String url) {
        ProjectTable projectTable = projectService.getProjectTable(projectId);
        assert projectTable != null;
        ProjectChannelTable projectChannelTable = channelService.getFirstChannel(projectId);
        ProjectVersionTable lastVersionOnChannel = projectVersionsDAO.getProjectVersionTable(projectChannelTable.getId());
        Map<Platform, Set<PluginDependency>> pluginDependencies = new EnumMap<>(Platform.class);
        Map<Platform, SortedSet<String>> platformDependencies;
        if (lastVersionOnChannel == null) {
            platformDependencies = new EnumMap<>(Platform.class);
        } else {
            for (Platform platform : Platform.getValues()) {
                pluginDependencies.put(platform, versionsApiDAO.getPluginDependencies(lastVersionOnChannel.getId(), platform));
            }
            platformDependencies = versionsApiDAO.getPlatformDependencies(lastVersionOnChannel.getId());
        }
        return new PendingVersion(pluginDependencies, platformDependencies, url, projectChannelTable, projectTable.isForumSync());
    }

    public void publishPendingVersion(long projectId, final PendingVersion pendingVersion) {
        if (!validationService.isValidVersionName(pendingVersion.getVersionString())) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.invalidName");
        }
        if (pendingVersion.getPluginDependencies().values().stream().anyMatch(pluginDependencies -> pluginDependencies.size() > config.projects.getMaxDependencies())) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.tooManyDependencies");
        }

        final ProjectTable projectTable = projectService.getProjectTable(projectId);
        assert projectTable != null;
        Path tmpVersionJar = null;
        if (pendingVersion.isFile()) { // verify file
            tmpVersionJar = projectFiles.getTempDir(getHangarPrincipal().getName()).resolve(pendingVersion.getFileInfo().getName());
            try {
                if (Files.notExists(tmpVersionJar)) {
                    throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.noFile");
                } else if (tmpVersionJar.toFile().length() != pendingVersion.getFileInfo().getSizeBytes()) {
                    throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.mismatchedFileSize");
                } else if (!Objects.equals(CryptoUtils.md5ToHex(Files.readAllBytes(tmpVersionJar)), pendingVersion.getFileInfo().getMd5Hash())) {
                    throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.hashMismatch");
                }
            } catch (IOException e) {
                logger.error("Could not publish version for {}", getHangarPrincipal().getName(), e);
            }
        } else if (exists(projectId, pendingVersion.getVersionString(), pendingVersion.getPlatformDependencies().keySet())) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.duplicateNameAndPlatform");
        }

        if (pendingVersion.getPlatformDependencies().entrySet().stream().anyMatch(entry -> !platformService.getVersionsForPlatform(entry.getKey()).containsAll(entry.getValue()))) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.invalidPlatformVersion");
        }

        ProjectVersionTable projectVersionTable = null;
        try {
            ProjectChannelTable projectChannelTable = channelService.getProjectChannel(projectId, pendingVersion.getChannelName(), pendingVersion.getChannelColor());
            if (projectChannelTable == null) {
                projectChannelTable = this.channelService.createProjectChannel(pendingVersion.getChannelName(), pendingVersion.getChannelColor(), projectId, pendingVersion.getChannelFlags());
            }

            Long fileSize = null;
            String fileHash = null;
            String fileName = null;
            if (pendingVersion.getFileInfo() != null) {
                fileSize = pendingVersion.getFileInfo().getSizeBytes();
                fileHash = pendingVersion.getFileInfo().getMd5Hash();
                fileName = pendingVersion.getFileInfo().getName();
            }
            //TODO automatic checks for malicious code or files => set visibility to NEEDSAPPROVAL
            projectVersionTable = projectVersionsDAO.insert(new ProjectVersionTable(
                    pendingVersion.getVersionString(),
                    pendingVersion.getDescription(),
                    projectId,
                    projectChannelTable.getId(),
                    fileSize,
                    fileHash,
                    fileName,
                    getHangarPrincipal().getUserId(),
                    pendingVersion.isForumSync(),
                    pendingVersion.getExternalUrl()
            ));

            List<ProjectVersionTagTable> projectVersionTagTables = new ArrayList<>();
            List<ProjectVersionPlatformDependencyTable> platformDependencyTables = new ArrayList<>();
            for (Map.Entry<Platform, SortedSet<String>> entry : pendingVersion.getPlatformDependencies().entrySet()) {
                projectVersionTagTables.add(new ProjectVersionTagTable(projectVersionTable.getId(), entry.getKey(), entry.getValue()));
                for (String version : entry.getValue()) {
                    PlatformVersionTable platformVersionTable = platformVersionDAO.getByPlatformAndVersion(entry.getKey(), version);
                    platformDependencyTables.add(new ProjectVersionPlatformDependencyTable(projectVersionTable.getId(), platformVersionTable.getId()));
                }
            }
            versionTagService.addTags(projectVersionTagTables);
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

            if (tmpVersionJar != null) {
                for (Platform platform : pendingVersion.getPlatformDependencies().keySet()) {
                    if (pendingVersion.getPlatformDependencies().get(platform).isEmpty()) continue;
                    Path newVersionJarPath = projectFiles.getVersionDir(projectTable.getOwnerName(), projectTable.getName(), pendingVersion.getVersionString(), platform).resolve(tmpVersionJar.getFileName());
                    if (Files.notExists(newVersionJarPath)) {
                        Files.createDirectories(newVersionJarPath.getParent());
                    }

                    Files.copy(tmpVersionJar, newVersionJarPath, StandardCopyOption.REPLACE_EXISTING);
                    if (Files.notExists(newVersionJarPath)) {
                        throw new IOException("Didn't successfully move the jar");
                    }
                }
                Files.deleteIfExists(tmpVersionJar);
            }

            if (projectTable.getVisibility() == Visibility.NEW) {
                projectVisibilityService.changeVisibility(projectTable, Visibility.PUBLIC, "First version");
                jobService.save(new UpdateDiscourseProjectTopicJob(projectId));
            }

            if (pendingVersion.isRecommended()) {
                for (Platform platform : pendingVersion.getPlatformDependencies().keySet()) {
                    recommendedVersionService.setRecommendedVersion(projectId, projectVersionTable.getId(), platform);
                }
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
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.fileIOError");
        } catch (Exception throwable) {
            logger.error("Unable to create version {} for {}", pendingVersion.getVersionString(), getHangarPrincipal().getName(), throwable);
            if (projectVersionTable != null) {
                projectVersionsDAO.delete(projectVersionTable);
            }
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.unknown");
        }
    }

    private boolean exists(long projectId, String versionString, Collection<Platform> platforms) {
        List<PlatformVersionTable> platformTables = platformVersionDAO.getPlatformsForVersionString(projectId, versionString);
        return platformTables.stream().anyMatch(pt -> platforms.contains(pt.getPlatform()));
    }
}
