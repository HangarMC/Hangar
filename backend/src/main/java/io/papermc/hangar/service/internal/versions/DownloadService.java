package io.papermc.hangar.service.internal.versions;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.table.versions.downloads.ProjectVersionDownloadsDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.project.version.FileInfo;
import io.papermc.hangar.model.api.project.version.PlatformVersionDownload;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.versions.downloads.ProjectVersionDownloadTable;
import io.papermc.hangar.model.db.versions.downloads.ProjectVersionPlatformDownloadTable;
import io.papermc.hangar.service.internal.file.FileService;
import io.papermc.hangar.service.internal.file.S3FileService;
import io.papermc.hangar.service.internal.projects.ProjectService;
import io.papermc.hangar.service.internal.uploads.ProjectFiles;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class DownloadService extends HangarComponent {

    private final ProjectFiles projectFiles;
    private final ProjectVersionDownloadsDAO downloadsDAO;
    private final FileService fileService;
    private final ProjectService projectService;

    @Autowired
    public DownloadService(final ProjectFiles projectFiles, final ProjectVersionDownloadsDAO downloadsDAO, final FileService fileService, final ProjectService projectService) {
        this.projectFiles = projectFiles;
        this.downloadsDAO = downloadsDAO;
        this.fileService = fileService;
        this.projectService = projectService;
    }

    public Map<Platform, PlatformVersionDownload> getDownloads(final String user, final String project, final String version, final long versionId) {
        final Map<Platform, PlatformVersionDownload> versionDownloadsMap = new EnumMap<>(Platform.class);
        final List<Pair<ProjectVersionPlatformDownloadTable, ProjectVersionDownloadTable>> platformDownloadsFull = this.downloadsDAO.getPlatformDownloadsFull(versionId);
        final Map<Long, PlatformVersionDownload> downloads = new HashMap<>();
        for (final Pair<ProjectVersionPlatformDownloadTable, ProjectVersionDownloadTable> platformDownloadPair : platformDownloadsFull) {
            final ProjectVersionPlatformDownloadTable platformDownload = platformDownloadPair.getLeft();
            PlatformVersionDownload download = downloads.get(platformDownload.getDownloadId());
            if (download != null) {
                versionDownloadsMap.put(platformDownload.getPlatform(), download);
                continue;
            }

            final ProjectVersionDownloadTable downloadTable = platformDownloadPair.getRight();
            final FileInfo fileInfo = downloadTable.getFileName() != null ? new FileInfo(downloadTable.getFileName(), downloadTable.getFileSize(), downloadTable.getHash()) : null;
            final String downloadUrl = fileInfo != null ? this.fileService.getVersionDownloadUrl(user, project, version, platformDownload.getPlatform(), fileInfo.getName()) : null;
            download = new PlatformVersionDownload(fileInfo, downloadTable.getExternalUrl(), downloadUrl);
            downloads.put(platformDownload.getDownloadId(), download);
            versionDownloadsMap.put(platformDownload.getPlatform(), download);
        }
        return versionDownloadsMap;
    }

    public ResponseEntity<?> downloadVersion(final String project, final String versionString, final Platform platform) {
        final ProjectTable projectTable = this.projectService.getProjectTable(project);
        if (projectTable == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }

        final ProjectVersionDownloadTable download = this.downloadsDAO.getDownloadByPlatform(projectTable.getProjectId(), versionString, platform);
        if (download == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }

        final String ownerName = projectTable.getOwnerName(); // TODO Move away from owner name dirs
        if (StringUtils.hasText(download.getExternalUrl())) {
            return ResponseEntity.status(301).header("Location", download.getExternalUrl()).build();
        } else if (this.fileService instanceof S3FileService){
            return ResponseEntity.status(301).header("Location", this.fileService.getVersionDownloadUrl(ownerName, project, versionString, platform, download.getFileName())).build();
        } else {
            final String path = this.projectFiles.getVersionDir(ownerName, project, versionString, platform, download.getFileName());
            if (!this.fileService.exists(path)) {
                throw new HangarApiException("Couldn't find a file for version " + versionString);
            }
            return ResponseEntity.status(200).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + download.getFileName() + "\"").body(this.fileService.getResource(path));
        }
    }
}
