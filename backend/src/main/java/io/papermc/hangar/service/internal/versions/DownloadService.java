package io.papermc.hangar.service.internal.versions;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.table.versions.downloads.ProjectVersionDownloadsDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import io.papermc.hangar.model.db.versions.downloads.ProjectVersionDownloadTable;
import io.papermc.hangar.service.internal.file.FileService;
import io.papermc.hangar.service.internal.file.S3FileService;
import io.papermc.hangar.service.internal.projects.ProjectService;
import io.papermc.hangar.service.internal.uploads.ProjectFiles;
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

    public ResponseEntity<?> downloadVersion(final ProjectTable project, final ProjectVersionTable version, final Platform platform) {
        final ProjectVersionDownloadTable download = this.downloadsDAO.getDownloadByPlatform(version.getVersionId(), platform);
        if (download == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }

        final String ownerName = project.getOwnerName(); // TODO Move away from owner name dirs
        if (StringUtils.hasText(download.getExternalUrl())) {
            return ResponseEntity.status(301).header("Location", download.getExternalUrl()).build();
        } else if (this.fileService instanceof S3FileService){
            return ResponseEntity.status(301).header("Location", this.fileService.getVersionDownloadUrl(ownerName, project.getName(), version.getName(), download.getDownloadPlatform(), download.getFileName())).build();
        } else {
            final String path = this.projectFiles.getVersionDir(ownerName, project.getName(), version.getName(), platform, download.getFileName());
            if (!this.fileService.exists(path)) {
                throw new HangarApiException("Couldn't find a file for version " + version.getName());
            }
            return ResponseEntity.status(200).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + download.getFileName() + "\"").body(this.fileService.getResource(path));
        }
    }

    public ResponseEntity<?> downloadVersion(final ProjectVersionTable version, final Platform platform) {
        final ProjectTable project = this.projectService.getProjectTable(version.getProjectId());
        if (project == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }

        return this.downloadVersion(project, version, platform);
    }
}
