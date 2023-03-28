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
import io.papermc.hangar.service.internal.file.FileService;
import io.papermc.hangar.service.internal.uploads.ProjectFiles;
import io.papermc.hangar.util.RequestUtil;
import jakarta.servlet.http.Cookie;
import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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

    private final ProjectFiles projectFiles;
    private final ProjectsDAO projectsDAO;
    private final ProjectVersionsDAO projectVersionsDAO;
    private final ProjectVersionUnsafeDownloadsDAO projectVersionUnsafeDownloadsDAO;
    private final ProjectVersionDownloadWarningsDAO projectVersionDownloadWarningsDAO;
    private final ProjectVersionDownloadsDAO downloadsDAO;
    private final FileService fileService;

    @Autowired
    public DownloadService(final ProjectFiles projectFiles, final ProjectsDAO projectsDAO, final ProjectVersionsDAO projectVersionsDAO, final ProjectVersionUnsafeDownloadsDAO projectVersionUnsafeDownloadsDAO, final ProjectVersionDownloadWarningsDAO projectVersionDownloadWarningsDAO, final ProjectVersionDownloadsDAO downloadsDAO, final FileService fileService) {
        this.projectFiles = projectFiles;
        this.projectsDAO = projectsDAO;
        this.projectVersionsDAO = projectVersionsDAO;
        this.projectVersionUnsafeDownloadsDAO = projectVersionUnsafeDownloadsDAO;
        this.projectVersionDownloadWarningsDAO = projectVersionDownloadWarningsDAO;
        this.downloadsDAO = downloadsDAO;
        this.fileService = fileService;
    }

    @Transactional
    public String createConfirmationToken(final String author, final String slug, final String versionString) {
        final ProjectVersionTable pvt = this.projectVersionsDAO.getProjectVersionTable(author, slug, versionString);
        final InetAddress remoteInetAddress = RequestUtil.getRemoteInetAddress(this.request);
        // check for existing token
        final ProjectVersionDownloadWarningTable warning = this.projectVersionDownloadWarningsDAO.findWarning(remoteInetAddress, pvt.getId());
        if (warning != null) {
            if (warning.isConfirmed()) {
                return null;
            }
            return warning.getToken().toString();
        }

        // create new token
        final UUID token = UUID.randomUUID();
        final OffsetDateTime expiresAt = OffsetDateTime.now().plus(this.config.projects.unsafeDownloadMaxAge().toMillis(), ChronoUnit.MILLIS);
        this.projectVersionDownloadWarningsDAO.insert(new ProjectVersionDownloadWarningTable(
            expiresAt,
            token,
            pvt.getId(),
            remoteInetAddress
        ));
        return token.toString();
    }

    @Transactional
    public Resource getVersionFile(final String author, final String slug, final String versionString, final Platform platform, final boolean checkConfirmation, final @Nullable String token) {
        final ProjectVersionTable pvt = this.projectVersionsDAO.getProjectVersionTable(author, slug, versionString);
        if (pvt == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }

        // TODO as one query
        final ProjectVersionPlatformDownloadTable platformDownload = this.downloadsDAO.getPlatformDownload(pvt.getVersionId(), platform);
        final ProjectVersionDownloadTable download = this.downloadsDAO.getDownload(platformDownload.getVersionId(), platformDownload.getDownloadId());
        if (download.getFileName() == null) {
            throw new HangarApiException("Couldn't find a file for version " + versionString);
        }

        final ProjectTable project = this.projectsDAO.getById(pvt.getProjectId());
        final String path = this.projectFiles.getVersionDir(project.getOwnerName(), project.getSlug(), versionString, platform, download.getFileName());
        if (!this.fileService.exists(path)) {
            throw new HangarApiException("Couldn't find a file for version " + versionString);
        }

        if (this.requiresConfirmation(pvt, download)) {
            if (checkConfirmation) {
                // find cookie
                final Optional<Cookie> cookie = Optional.ofNullable(WebUtils.getCookie(this.request, ProjectVersionDownloadWarningTable.cookieKey(pvt.getId())));
                final boolean hasSessionConfirm = "confirmed".equals(cookie.map(Cookie::getValue).orElse(""));
                if (!hasSessionConfirm && !this.isConfirmed(pvt, download, token)) {
                    throw new HangarApiException(HttpStatus.PRECONDITION_FAILED, "Needs confirmation. Please try again");
                }
            } else {
                this.projectVersionUnsafeDownloadsDAO.insert(new ProjectVersionUnsafeDownloadTable(this.getHangarUserId(), RequestUtil.getRemoteInetAddress(this.request)));
            }
        }

        this.response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + download.getFileName() + "\"");
        return this.fileService.getResource(path);
    }

    public boolean requiresConfirmation(final String author, final String slug, final String versionString, final Platform platform) {
        final ProjectVersionTable pvt = this.projectVersionsDAO.getProjectVersionTable(author, slug, versionString);
        if (pvt == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }

        final ProjectVersionPlatformDownloadTable platformDownload = this.downloadsDAO.getPlatformDownload(pvt.getVersionId(), platform);
        return this.requiresConfirmation(pvt, this.downloadsDAO.getDownload(platformDownload.getVersionId(), platformDownload.getDownloadId()));
    }

    private boolean requiresConfirmation(final ProjectVersionTable pvt, final ProjectVersionDownloadTable downloadTable) {
        if (pvt.getVisibility() == Visibility.PUBLIC && !this.config.projects.showUnreviewedDownloadWarning()) {
            return false;
        }
        return pvt.getReviewState() != ReviewState.REVIEWED && (downloadTable.getExternalUrl() == null || !this.config.security.checkSafe(downloadTable.getExternalUrl()));
    }

    private boolean isConfirmed(final ProjectVersionTable pvt, final ProjectVersionDownloadTable downloadTable, final @Nullable String token) {
        if (pvt.getReviewState() == ReviewState.REVIEWED || (downloadTable.getExternalUrl() != null && this.config.security.checkSafe(downloadTable.getExternalUrl()))) {
            return true;
        }
        final InetAddress remoteInetAddress = RequestUtil.getRemoteInetAddress(this.request);
        if (token == null || token.equals("null")) {
            final ProjectVersionDownloadWarningTable warning = this.projectVersionDownloadWarningsDAO.findWarning(remoteInetAddress, pvt.getId());
            if (warning != null && warning.isConfirmed()) {
                this.projectVersionUnsafeDownloadsDAO.insert(new ProjectVersionUnsafeDownloadTable(this.getHangarUserId(), remoteInetAddress));
                return true;
            } else {
                return false;
            }
        }

        final var downloadWarning = this.projectVersionDownloadWarningsDAO.findWarning(token, remoteInetAddress, pvt.getId());
        if (downloadWarning == null) {
            return false;
        }
        if (downloadWarning.hasExpired()) {
            this.projectVersionDownloadWarningsDAO.delete(downloadWarning.getId());
            return false;
        }
        final var unsafeDownload = this.projectVersionUnsafeDownloadsDAO.insert(new ProjectVersionUnsafeDownloadTable(this.getHangarUserId(), remoteInetAddress));
        downloadWarning.setConfirmed(true);
        downloadWarning.setDownloadId(unsafeDownload.getId());
        this.projectVersionDownloadWarningsDAO.update(downloadWarning);
        return true;
    }

    public Map<Platform, PlatformVersionDownload> getDownloads(final String user, final String project, final String version, final long versionId) {
        final Map<Platform, PlatformVersionDownload> versionDownloadsMap = new EnumMap<>(Platform.class);
        // TODO into one query
        final List<ProjectVersionDownloadTable> versionDownloads = this.downloadsDAO.getDownloads(versionId);
        final List<ProjectVersionPlatformDownloadTable> platformDownloads = this.downloadsDAO.getPlatformDownloads(versionId);
        final Map<Long, PlatformVersionDownload> downloads = new HashMap<>();
        for (final ProjectVersionPlatformDownloadTable platformDownload : platformDownloads) {
            PlatformVersionDownload download = downloads.get(platformDownload.getDownloadId());
            if (download != null) {
                versionDownloadsMap.put(platformDownload.getPlatform(), download);
                continue;
            }

            final ProjectVersionDownloadTable downloadTable = versionDownloads.stream().filter(table -> table.getId() == platformDownload.getDownloadId()).findAny().orElseThrow(NullPointerException::new);
            final FileInfo fileInfo = downloadTable.getFileName() != null ? new FileInfo(downloadTable.getFileName(), downloadTable.getFileSize(), downloadTable.getHash()) : null;
            final String downloadUrl = fileInfo != null ? this.fileService.getDownloadUrl(user, project, version, platformDownload.getPlatform(), fileInfo.getName()) : null;
            download = new PlatformVersionDownload(fileInfo, downloadTable.getExternalUrl(), downloadUrl);
            downloads.put(platformDownload.getDownloadId(), download);
            versionDownloadsMap.put(platformDownload.getPlatform(), download);
        }
        return versionDownloadsMap;
    }
}
