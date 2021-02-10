package io.papermc.hangar.service.internal.versions;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.versions.ProjectVersionDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.project.version.FileInfo;
import io.papermc.hangar.model.db.projects.ProjectChannelTable;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.internal.versions.PendingVersion;
import io.papermc.hangar.service.HangarService;
import io.papermc.hangar.service.internal.projects.ChannelService;
import io.papermc.hangar.service.internal.projects.ProjectService;
import io.papermc.hangar.service.internal.uploads.ProjectFiles;
import io.papermc.hangar.service.internal.versions.plugindata.PluginFileWithData;
import io.papermc.hangar.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class VersionFactory extends HangarService {

    private final ProjectVersionDAO projectVersionDAO;
    private final ProjectFiles projectFiles;
    private final PluginDataService pluginDataService;
    private final ChannelService channelService;
    private final ProjectService projectService;

    @Autowired
    public VersionFactory(HangarDao<ProjectVersionDAO> projectVersionDAO, ProjectFiles projectFiles, PluginDataService pluginDataService, ChannelService channelService, ProjectService projectService) {
        this.projectVersionDAO = projectVersionDAO.get();
        this.projectFiles = projectFiles;
        this.pluginDataService = pluginDataService;
        this.channelService = channelService;
        this.projectService = projectService;
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
        } catch (IOException e) {
            logger.error("Error while uploading {} for {}", pluginFileName, getHangarPrincipal().getName(), e);
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.unexpected");
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

        if (projectVersionDAO.getProjectVersionTable(projectId, pluginDataFile.getMd5(), pendingVersion.getVersionString()) != null && hangarConfig.projects.isFileValidate()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.duplicate");
        }
        return pendingVersion;
    }

    public PendingVersion createPendingVersion(long projectId, String url) {
        ProjectTable projectTable = projectService.getProjectTable(projectId);
        assert projectTable != null;
        ProjectChannelTable projectChannelTable = channelService.getFirstChannel(projectId);
        return new PendingVersion(url, projectChannelTable, projectTable.isForumSync());
    }
}
