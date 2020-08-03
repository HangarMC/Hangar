package me.minidigger.hangar.service.pluginupload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import me.minidigger.hangar.config.hangar.HangarConfig;
import me.minidigger.hangar.db.model.ProjectChannelsTable;
import me.minidigger.hangar.db.model.ProjectsTable;
import me.minidigger.hangar.db.model.UsersTable;
import me.minidigger.hangar.service.ChannelService;
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
    private final HangarConfig config;

    private Map<String, PendingVersion> cache = new HashMap<>();

    @Autowired
    public PluginUploadService(ProjectFiles projectFiles, PluginDataService pluginDataService, ChannelService channelService, HangarConfig config) {
        this.projectFiles = projectFiles;
        this.pluginDataService = pluginDataService;
        this.channelService = channelService;
        this.config = config;
    }

    public PluginFileData processPluginUpload(MultipartFile file, UsersTable owner) {
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
            return pluginDataService.loadMeta(tmpPluginFile);
        } catch (IOException e) {
            LOGGER.error("Error while uploading {} for {}", pluginFileName, owner.getName(), e);
            throw new HangarException("error.plugin.unexpected");
        }
    }

    public PendingVersion processSubsequentPluginUpload(MultipartFile file, UsersTable owner, ProjectsTable project) {
        PluginFileData pluginFileData = processPluginUpload(file, owner);

        ProjectChannelsTable channel = channelService.getFirstChannel(project);
//        String channelName = channel.getName(); // TODO fix this
        String channelName = "dumChannel";

        PendingVersion pendingVersion = startVersion(pluginFileData, project.getPluginId(), project.getId(), project.getForumSync(), channelName);

        // todo duplicate checking and shit

        return pendingVersion;
    }

    private PendingVersion startVersion(PluginFileData pluginFileData, String pluginId, long projectId, boolean forumSync, String channelName) {
        if (pluginFileData.getVersion() == null || pluginFileData.getVersion().isEmpty()) {
            throw new HangarException("error.plugin.noVersion");
        }

        // TODO finish startVersion
        String hash = "";
        String fileName = "";
        long authorId = -1;
        long fileSize = -1;

        return new PendingVersion(
                StringUtils.slugify(pluginFileData.getVersion()),
                pluginFileData.getDependencies(),
                pluginFileData.getDescription(),
                projectId,
                fileSize,
                hash,
                fileName,
                authorId,
                channelName,
                config.getChannels().getColorDefault(),
                pluginFileData,
                forumSync
                );
    }

    public PendingVersion getPendingVersion(ProjectsTable projectsTable, String versionName) {
        return cache.get(projectsTable.getId() + "/" + versionName);
    }
}
