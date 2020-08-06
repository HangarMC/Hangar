package me.minidigger.hangar.service.pluginupload;

import me.minidigger.hangar.config.CacheConfig;
import me.minidigger.hangar.service.VersionService;
import me.minidigger.hangar.service.plugindata.PluginFileWithData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import me.minidigger.hangar.config.hangar.HangarConfig;
import me.minidigger.hangar.db.model.ProjectChannelsTable;
import me.minidigger.hangar.db.model.ProjectsTable;
import me.minidigger.hangar.db.model.UsersTable;
import me.minidigger.hangar.service.project.ChannelService;
import me.minidigger.hangar.service.plugindata.PluginDataService;
import me.minidigger.hangar.service.plugindata.PluginFileData;
import me.minidigger.hangar.util.HangarException;
import me.minidigger.hangar.util.StringUtils;

@Service
public class PluginUploadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginUploadService.class);

    private final HangarConfig hangarConfig;
    private final ProjectFiles projectFiles;
    private final PluginDataService pluginDataService;
    private final ChannelService channelService;
    private final VersionService versionService;
    private final CacheManager cacheManager;
    private final HangarConfig config;

    @Autowired
    public PluginUploadService(HangarConfig hangarConfig, ProjectFiles projectFiles, PluginDataService pluginDataService, ChannelService channelService, VersionService versionService, CacheManager cacheManager, HangarConfig config) {
        this.hangarConfig = hangarConfig;
        this.projectFiles = projectFiles;
        this.pluginDataService = pluginDataService;
        this.channelService = channelService;
        this.versionService = versionService;
        this.cacheManager = cacheManager;
        this.config = config;
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
        // TODO not sure what to do w/plugin id, that isn't stored in the metadata for the file
//        if (!plugin.getData().getId().equals(project.getPluginId())) {
//            throw new HangarException("error.version.invalidPluginId");
//        }
        if (plugin.getData().getVersion() != null && plugin.getData().getVersion().contains("recommended")) {
            throw new HangarException("error.version.illegalVersion");
        }


        ProjectChannelsTable channel = channelService.getFirstChannel(project);

        PendingVersion pendingVersion = startVersion(plugin, project.getPluginId(), project.getId(), project.getForumSync(), channel.getName());

        boolean exists = versionService.exists(pendingVersion);
        if (exists && hangarConfig.projects.isFileValidate()) {
            throw new HangarException("error.version.duplicate");
        }
        cacheManager.getCache(CacheConfig.PENDING_VERSION_CACHE).put(project.getId() + "/" + pendingVersion.getVersionString(), pendingVersion);
        return pendingVersion;
    }

    private PendingVersion startVersion(PluginFileWithData plugin, String pluginId, long projectId, boolean forumSync, String channelName) {
        PluginFileData metaData = plugin.getData();
        // TODO same issue here w/plugin id, its not stored in metadata
//        if (!metaData.getId().equals(pluginId)) {
//            throw new HangarException("error.plugin.invalidPluginId");
//        }
        if (metaData.getVersion() == null || metaData.getVersion().isBlank()) {
            throw new HangarException("error.plugin.noVersion");
        }

        Path path = plugin.getPath();

        return new PendingVersion(
                StringUtils.slugify(metaData.getVersion()),
                metaData.getDependencies(),
                metaData.getDescription(),
                projectId,
                path.toFile().length(),
                plugin.getMd5(),
                path.getFileName().toString(),
                plugin.getUserId(),
                channelName,
                config.getChannels().getColorDefault(),
                plugin,
                forumSync
        );
    }
}
