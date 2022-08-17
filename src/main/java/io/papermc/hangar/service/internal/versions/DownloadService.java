package io.papermc.hangar.service.internal.versions;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectsDAO;
import io.papermc.hangar.db.dao.internal.table.versions.ProjectVersionsDAO;
import io.papermc.hangar.db.dao.internal.table.versions.downloads.ProjectVersionDownloadWarningsDAO;
import io.papermc.hangar.db.dao.internal.table.versions.downloads.ProjectVersionDownloadsDAO;
import io.papermc.hangar.db.dao.internal.table.versions.downloads.ProjectVersionUnsafeDownloadsDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.project.version.FileInfo;
import io.papermc.hangar.model.api.project.version.PlatformVersionDownload;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.common.projects.ReviewState;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import io.papermc.hangar.model.db.versions.downloads.ProjectVersionDownloadTable;
import io.papermc.hangar.model.db.versions.downloads.ProjectVersionDownloadWarningTable;
import io.papermc.hangar.model.db.versions.downloads.ProjectVersionPlatformDownloadTable;
import io.papermc.hangar.model.db.versions.downloads.ProjectVersionUnsafeDownloadTable;
import io.papermc.hangar.service.internal.admin.StatService;
import io.papermc.hangar.service.internal.file.FileService;
import io.papermc.hangar.service.internal.uploads.ProjectFiles;
import io.papermc.hangar.util.RequestUtil;
import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.servlet.http.Cookie;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.WebUtils;

@Service
public class DownloadService extends HangarComponent {

    private final StatService statService;
    private final ProjectFiles projectFiles;
    private final ProjectsDAO projectsDAO;
    private final ProjectVersionsDAO projectVersionsDAO;
    private final ProjectVersionUnsafeDownloadsDAO projectVersionUnsafeDownloadsDAO;
    private final ProjectVersionDownloadWarningsDAO projectVersionDownloadWarningsDAO;
    private final ProjectVersionDownloadsDAO downloadsDAO;
    private final FileService fileService;

    @Autowired
    public DownloadService(StatService statService, ProjectFiles projectFiles, ProjectsDAO projectsDAO, ProjectVersionsDAO projectVersionsDAO, ProjectVersionUnsafeDownloadsDAO projectVersionUnsafeDownloadsDAO, ProjectVersionDownloadWarningsDAO projectVersionDownloadWarningsDAO, final ProjectVersionDownloadsDAO downloadsDAO, FileService fileService) {
        this.statService = statService;
        this.projectFiles = projectFiles;
        this.projectsDAO = projectsDAO;
        this.projectVersionsDAO = projectVersionsDAO;
        this.projectVersionUnsafeDownloadsDAO = projectVersionUnsafeDownloadsDAO;
        this.projectVersionDownloadWarningsDAO = projectVersionDownloadWarningsDAO;
        this.downloadsDAO = downloadsDAO;
        this.fileService = fileService;
    }

    @Transactional
    public String createConfirmationToken(String author, String slug, String versionString) {
        ProjectVersionTable pvt = projectVersionsDAO.getProjectVersionTable(author, slug, versionString);
        InetAddress remoteInetAddress = RequestUtil.getRemoteInetAddress(request);
        // check for existing token
        ProjectVersionDownloadWarningTable warning = projectVersionDownloadWarningsDAO.findWarning(remoteInetAddress, pvt.getId());
        if (warning != null) {
            if (warning.isConfirmed()) {
                return null;
            }
            return warning.getToken().toString();
        }

        // create new token
        UUID token = UUID.randomUUID();
        OffsetDateTime expiresAt = OffsetDateTime.now().plus(config.projects.getUnsafeDownloadMaxAge().toMillis(), ChronoUnit.MILLIS);
        projectVersionDownloadWarningsDAO.insert(new ProjectVersionDownloadWarningTable(
            expiresAt,
            token,
            pvt.getId(),
            remoteInetAddress
        ));
        return token.toString();
    }

    @Transactional
    public Resource getVersionFile(String author, String slug, String versionString, Platform platform, boolean checkConfirmation, @Nullable String token) {
        ProjectVersionTable pvt = projectVersionsDAO.getProjectVersionTable(author, slug, versionString);
        if (pvt == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }

        //TODO as one query
        final ProjectVersionPlatformDownloadTable platformDownload = downloadsDAO.getPlatformDownload(pvt.getVersionId(), platform);
        final ProjectVersionDownloadTable download = downloadsDAO.getDownload(platformDownload.getVersionId(), platformDownload.getDownloadId());
        if (download.getFileName() == null) {
            throw new HangarApiException("Couldn't find a file for version " + versionString);
        }

        ProjectTable project = projectsDAO.getById(pvt.getProjectId());
        String path = projectFiles.getVersionDir(project.getOwnerName(), project.getSlug(), versionString, platform, download.getFileName());
        if (!fileService.exists(path)) {
            throw new HangarApiException("Couldn't find a file for version " + versionString);
        }

        if (requiresConfirmation(pvt, download)) {
            if (checkConfirmation) {
                // find cookie
                Optional<Cookie> cookie = Optional.ofNullable(WebUtils.getCookie(request, ProjectVersionDownloadWarningTable.cookieKey(pvt.getId())));
                boolean hasSessionConfirm = "confirmed".equals(cookie.map(Cookie::getValue).orElse(""));
                if (!hasSessionConfirm && !isConfirmed(pvt, download, token)) {
                    throw new HangarApiException(HttpStatus.PRECONDITION_FAILED, "Needs confirmation. Please try again");
                }
            } else {
                projectVersionUnsafeDownloadsDAO.insert(new ProjectVersionUnsafeDownloadTable(getHangarUserId(), RequestUtil.getRemoteInetAddress(request)));
            }
        }

        statService.addVersionDownload(pvt);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + download.getFileName() + "\"");
        return fileService.getResource(path);
    }

    public boolean requiresConfirmation(String author, String slug, String versionString, Platform platform) {
        ProjectVersionTable pvt = projectVersionsDAO.getProjectVersionTable(author, slug, versionString);
        if (pvt == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }

        final ProjectVersionPlatformDownloadTable platformDownload = downloadsDAO.getPlatformDownload(pvt.getVersionId(), platform);
        return requiresConfirmation(pvt, downloadsDAO.getDownload(platformDownload.getVersionId(), platformDownload.getDownloadId()));
    }

    private boolean requiresConfirmation(ProjectVersionTable pvt, ProjectVersionDownloadTable downloadTable) {
        if (pvt.getVisibility() == Visibility.PUBLIC && !config.projects.showUnreviewedDownloadWarning()) {
            return false;
        }
        return pvt.getReviewState() != ReviewState.REVIEWED && (downloadTable.getExternalUrl() == null || !config.security.checkSafe(downloadTable.getExternalUrl()));
    }

    private boolean isConfirmed(ProjectVersionTable pvt, ProjectVersionDownloadTable downloadTable, @Nullable String token) {
        if (pvt.getReviewState() == ReviewState.REVIEWED || (downloadTable.getExternalUrl() != null && config.security.checkSafe(downloadTable.getExternalUrl()))) {
            return true;
        }
        InetAddress remoteInetAddress = RequestUtil.getRemoteInetAddress(request);
        if (token == null || token.equals("null")) {
            ProjectVersionDownloadWarningTable warning = projectVersionDownloadWarningsDAO.findWarning(remoteInetAddress, pvt.getId());
            if (warning != null && warning.isConfirmed()) {
                projectVersionUnsafeDownloadsDAO.insert(new ProjectVersionUnsafeDownloadTable(getHangarUserId(), remoteInetAddress));
                return true;
            } else {
                return false;
            }
        }

        var downloadWarning = projectVersionDownloadWarningsDAO.findWarning(token, remoteInetAddress, pvt.getId());
        if (downloadWarning == null) {
            return false;
        }
        if (downloadWarning.hasExpired()) {
            projectVersionDownloadWarningsDAO.delete(downloadWarning.getId());
            return false;
        }
        var unsafeDownload = projectVersionUnsafeDownloadsDAO.insert(new ProjectVersionUnsafeDownloadTable(getHangarUserId(), remoteInetAddress));
        downloadWarning.setConfirmed(true);
        downloadWarning.setDownloadId(unsafeDownload.getId());
        projectVersionDownloadWarningsDAO.update(downloadWarning);
        return true;
    }

    public void addDownloads(String user, String project, String version, long versionId, Map<Platform, PlatformVersionDownload> versionDownloadsMap) {
        // TODO into one query
        final List<ProjectVersionDownloadTable> versionDownloads = downloadsDAO.getDownloads(versionId);
        final List<ProjectVersionPlatformDownloadTable> platformDownloads = downloadsDAO.getPlatformDownloads(versionId);
        final Map<Long, PlatformVersionDownload> downloads = new HashMap<>();
        for (final ProjectVersionPlatformDownloadTable platformDownload : platformDownloads) {
            PlatformVersionDownload download = downloads.get(platformDownload.getDownloadId());
            if (download != null) {
                versionDownloadsMap.put(platformDownload.getPlatform(), download);
                continue;
            }

            final ProjectVersionDownloadTable downloadTable = versionDownloads.stream().filter(table -> table.getId() == platformDownload.getDownloadId()).findAny().orElseThrow(NullPointerException::new);
            final FileInfo fileInfo = downloadTable.getFileName() != null ? new FileInfo(downloadTable.getFileName(), downloadTable.getFileSize(), downloadTable.getHash()) : null;
            download = new PlatformVersionDownload(fileInfo, downloadTable.getExternalUrl(), fileService.getDownloadUrl(user, project, version, platformDownload.getPlatform(), fileInfo != null ? fileInfo.getName() : null));
            downloads.put(platformDownload.getDownloadId(), download);
            versionDownloadsMap.put(platformDownload.getPlatform(), download);
        }
    }
}
