package io.papermc.hangar.service.internal.versions;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectsDAO;
import io.papermc.hangar.db.dao.internal.table.versions.ProjectVersionsDAO;
import io.papermc.hangar.db.dao.internal.table.versions.downloads.ProjectVersionDownloadWarningsDAO;
import io.papermc.hangar.db.dao.internal.table.versions.downloads.ProjectVersionUnsafeDownloadsDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.common.projects.ReviewState;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import io.papermc.hangar.model.db.versions.downloads.ProjectVersionDownloadWarningTable;
import io.papermc.hangar.model.db.versions.downloads.ProjectVersionUnsafeDownloadTable;
import io.papermc.hangar.service.internal.admin.StatService;
import io.papermc.hangar.service.internal.uploads.ProjectFiles;
import io.papermc.hangar.util.RequestUtil;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class DownloadService extends HangarComponent {

    private final StatService statService;
    private final ProjectFiles projectFiles;
    private final ProjectsDAO projectsDAO;
    private final ProjectVersionsDAO projectVersionsDAO;
    private final ProjectVersionUnsafeDownloadsDAO projectVersionUnsafeDownloadsDAO;
    private final ProjectVersionDownloadWarningsDAO projectVersionDownloadWarningsDAO;

    @Autowired
    public DownloadService(StatService statService, ProjectFiles projectFiles, HangarDao<ProjectsDAO> projectsDAO, HangarDao<ProjectVersionsDAO> projectVersionsDAO, HangarDao<ProjectVersionUnsafeDownloadsDAO> projectVersionUnsafeDownloadsDAO, HangarDao<ProjectVersionDownloadWarningsDAO> projectVersionDownloadWarningsDAO) {
        this.statService = statService;
        this.projectFiles = projectFiles;
        this.projectsDAO = projectsDAO.get();
        this.projectVersionsDAO = projectVersionsDAO.get();
        this.projectVersionUnsafeDownloadsDAO = projectVersionUnsafeDownloadsDAO.get();
        this.projectVersionDownloadWarningsDAO = projectVersionDownloadWarningsDAO.get();
    }

    public String createConfirmationToken(String author, String slug, String versionString, Platform platform) {
        ProjectVersionTable pvt = projectVersionsDAO.getProjectVersionTable(author, slug, versionString, platform);
        UUID token = UUID.randomUUID();
        OffsetDateTime expiresAt = OffsetDateTime.now().plus(config.projects.getUnsafeDownloadMaxAge().toMillis(), ChronoUnit.MILLIS);
        projectVersionDownloadWarningsDAO.insert(new ProjectVersionDownloadWarningTable(
                expiresAt,
                token,
                pvt.getId(),
                RequestUtil.getRemoteInetAddress(request)
        ));
        return token.toString();
    }

    public FileSystemResource getVersionFile(String author, String slug, String versionString, Platform platform) {
        ProjectVersionTable pvt = projectVersionsDAO.getProjectVersionTable(author, slug, versionString, platform);
        if (pvt == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        if (pvt.getFileName() == null) {
            throw new HangarApiException("Couldn't find a file for that version");
        }

        ProjectTable project = projectsDAO.getById(pvt.getProjectId());
        Path path = projectFiles.getVersionDir(project.getOwnerName(), project.getName(), versionString, platform);
        if (Files.notExists(path)) {
            throw new HangarApiException("Couldn't find a file for that version");
        }

        if (requiresConfirmation(pvt)) {
            projectVersionUnsafeDownloadsDAO.insert(new ProjectVersionUnsafeDownloadTable(getHangarUserId(), RequestUtil.getRemoteInetAddress(request)));
        }

        statService.addVersionDownload(pvt);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + pvt.getFileName() + "\"");
        return new FileSystemResource(path);
    }

    private boolean requiresConfirmation(ProjectVersionTable pvt) {
        return pvt.getReviewState() != ReviewState.REVIEWED && (pvt.getExternalUrl() == null || !config.security.checkSafe(pvt.getExternalUrl()));
    }

    private boolean isConfirmed(ProjectVersionTable pvt, @Nullable String token) {
        if (pvt.getReviewState() == ReviewState.REVIEWED || (pvt.getExternalUrl() != null && config.security.checkSafe(pvt.getExternalUrl()))) {
            return true;
        }
        if (token == null) {
            return false;
        }

        var downloadWarning = projectVersionDownloadWarningsDAO.findWarning(token, RequestUtil.getRemoteInetAddress(request), pvt.getId());
        if (downloadWarning == null) {
            return false;
        }
        var unsafeDownload = projectVersionUnsafeDownloadsDAO.insert(new ProjectVersionUnsafeDownloadTable(getHangarUserId(), RequestUtil.getRemoteInetAddress(request)));
        downloadWarning.setConfirmed(true);
        downloadWarning.setDownloadId(unsafeDownload.getId());
        return true;
    }
}
