package me.minidigger.hangar.service.pluginupload;

import me.minidigger.hangar.config.CacheConfig;
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

    private final ProjectFiles projectFiles;
    private final PluginDataService pluginDataService;
    private final ChannelService channelService;
    private final CacheManager cacheManager;
    private final HangarConfig config;

    @Autowired
    public PluginUploadService(ProjectFiles projectFiles, PluginDataService pluginDataService, ChannelService channelService, CacheManager cacheManager, HangarConfig config) {
        this.projectFiles = projectFiles;
        this.pluginDataService = pluginDataService;
        this.channelService = channelService;
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
//        if (!plugin.getData().getId().equals(project.getPluginId())) { // TODO pluginId
//            throw new HangarException("error.version.invalidPluginId");
//        } else if (plugin.getData().getVersion().contains("recommended")) {
//            throw new HangarException("error.version.illegalVersion");
//        }


        ProjectChannelsTable channel = channelService.getFirstChannel(project);

        PendingVersion pendingVersion = startVersion(plugin, project.getPluginId(), project.getId(), project.getForumSync(), channel.getName());

        // todo duplicate checking and shit
        cacheManager.getCache(CacheConfig.PENDING_VERSION_CACHE).put(project.getId() + "/" + pendingVersion.getVersionString(), pendingVersion);
        return pendingVersion;
    }

    private PendingVersion startVersion(PluginFileWithData plugin, String pluginId, long projectId, boolean forumSync, String channelName) throws HangarException {
        PluginFileData metaData = plugin.getData();
        if (metaData.getVersion() == null || metaData.getVersion().isEmpty()) {
            throw new HangarException("error.plugin.noVersion");
        }
//        if (!metaData.getId().equals(pluginId)) { // TODO pluginId
//            throw new HangarException("error.plugin.invalidPluginId");
//        } else if (metaData.getVersion() == null || metaData.getVersion().isBlank()) {
//            throw new HangarException("error.plugin.noVersion");
//        }

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
