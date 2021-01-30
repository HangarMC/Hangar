package io.papermc.hangar.serviceold.pluginupload;

import io.papermc.hangar.config.CacheConfig;
import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.db.modelold.ProjectChannelsTable;
import io.papermc.hangar.db.modelold.ProjectsTable;
import io.papermc.hangar.db.modelold.UsersTable;
import io.papermc.hangar.exceptions.HangarException;
import io.papermc.hangar.serviceold.VersionService;
import io.papermc.hangar.serviceold.plugindata.PluginDataService;
import io.papermc.hangar.serviceold.plugindata.PluginFileData;
import io.papermc.hangar.serviceold.plugindata.PluginFileWithData;
import io.papermc.hangar.serviceold.project.ChannelService;
import io.papermc.hangar.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

@Service
public class PluginUploadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginUploadService.class);

    private final HangarConfig hangarConfig;
    private final ProjectFiles projectFiles;
    private final PluginDataService pluginDataService;
    private final ChannelService channelService;
    private final VersionService versionService;
    private final CacheManager cacheManager;

    private final Supplier<ProjectsTable> projectsTable;

    @Autowired
    public PluginUploadService(HangarConfig hangarConfig, ProjectFiles projectFiles, PluginDataService pluginDataService, ChannelService channelService, VersionService versionService, CacheManager cacheManager, Supplier<ProjectsTable> projectsTable) {
        this.hangarConfig = hangarConfig;
        this.projectFiles = projectFiles;
        this.pluginDataService = pluginDataService;
        this.channelService = channelService;
        this.versionService = versionService;
        this.cacheManager = cacheManager;
        this.projectsTable = projectsTable;
    }

    public PluginFileWithData processPluginUpload(MultipartFile file, UsersTable owner) {
        String pluginFileName = file.getOriginalFilename();

        if (!pluginFileName.endsWith(".zip") && !pluginFileName.endsWith(".jar")) {
            throw new HangarException("error.plugin.fileExtension");
        }

        Path tmpDir = projectFiles.getTempDir(owner.getName());
        if (!Files.isDirectory(tmpDir)) {
            try {
                Files.createDirectories(tmpDir);
            } catch (IOException e) {
                LOGGER.error("Error while uploading {} for {}: could not create dir", pluginFileName, owner.getName(), e);
                throw new HangarException("error.plugin.unexpected");
            }
        }

        Path tmpPluginFile = tmpDir.resolve(pluginFileName);
        try {
            file.transferTo(tmpPluginFile);
        } catch (IOException e) {
            LOGGER.error("Error while uploading {} for {}: could not save tmp file", pluginFileName, owner.getName(), e);
            throw new HangarException("error.plugin.unexpected");
        }

        try {
            return pluginDataService.loadMeta(tmpPluginFile, owner.getId());
        } catch (IOException e) {
            LOGGER.error("Error while uploading {} for {}", pluginFileName, owner.getName(), e);
            throw new HangarException("error.plugin.unexpected");
        }
    }


    public PendingVersion processSubsequentPluginUpload(MultipartFile file, UsersTable owner, ProjectsTable project) throws HangarException {
        PluginFileWithData plugin = processPluginUpload(file, owner);
        if (plugin.getData().getVersion() != null && plugin.getData().getVersion().contains("recommended")) {
            throw new HangarException("error.version.illegalVersion");
        }

        ProjectChannelsTable channel = channelService.getFirstChannel(project);
        PendingVersion pendingVersion = startVersion(plugin, project.getId(), project.getForumSync(), channel);

        boolean exists = versionService.exists(pendingVersion);
        if (exists && hangarConfig.projects.isFileValidate()) {
            throw new HangarException("error.version.duplicate");
        }

        cacheManager.getCache(CacheConfig.PENDING_VERSION_CACHE).put(project.getId() + "/" + pendingVersion.getVersionString(), pendingVersion);
        return pendingVersion;
    }

    private PendingVersion startVersion(PluginFileWithData plugin, long projectId, boolean forumSync, ProjectChannelsTable channel) {
        PluginFileData metaData = plugin.getData();
        if (metaData.getVersion() == null || metaData.getVersion().isBlank()) {
            throw new HangarException("error.plugin.noVersion");
        }

        Path path = plugin.getPath();

        return new PendingVersion(
                StringUtils.slugify(metaData.getVersion()),
                metaData.getDependencies(),
                metaData.getPlatformDependencies(),
                metaData.getDescription(),
                projectId,
                path.toFile().length(),
                plugin.getMd5(),
                path.getFileName().toString(),
                plugin.getUserId(),
                channel.getName(),
                channel.getColor(),
                plugin,
                null,
                forumSync,
                versionService.getMostRelevantVersion(projectsTable.get()));
    }
}
