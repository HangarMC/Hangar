package io.papermc.hangar.service.internal.versions;

import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectsDAO;
import io.papermc.hangar.db.dao.internal.table.versions.ProjectVersionsDAO;
import io.papermc.hangar.db.dao.internal.table.versions.downloads.ProjectVersionDownloadsDAO;
import io.papermc.hangar.db.dao.internal.versions.JarScanResultDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.project.version.VersionToScan;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.versions.JarScanResultTable;
import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import io.papermc.hangar.model.db.versions.downloads.ProjectVersionDownloadTable;
import io.papermc.hangar.model.db.versions.downloads.ProjectVersionPlatformDownloadTable;
import io.papermc.hangar.scanner.HangarJarScanner;
import io.papermc.hangar.scanner.check.Check;
import io.papermc.hangar.scanner.model.ScanResult;
import io.papermc.hangar.scanner.model.Severity;
import io.papermc.hangar.service.internal.file.FileService;
import io.papermc.hangar.service.internal.uploads.ProjectFiles;
import io.papermc.hangar.service.internal.users.UserService;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.utils.ThreadFactoryBuilder;

@Service
public class JarScanningService {

    public static final UUID JAR_SCANNER_USER = new UUID(952332837L, -376012533328L);
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool(new ThreadFactoryBuilder().threadNamePrefix("Scanner").build());
    private final HangarJarScanner scanner = new HangarJarScanner();
    private final JarScanResultDAO dao;
    private final ProjectVersionsDAO projectVersionsDAO;
    private final ProjectVersionDownloadsDAO downloadsDAO;
    private final ProjectsDAO projectsDAO;
    private final ProjectFiles projectFiles;
    private final FileService fileService;
    private final ReviewService reviewService;
    private final UserService userService;
    private UserTable jarScannerUser;

    public JarScanningService(final JarScanResultDAO dao, final ProjectVersionsDAO projectVersionsDAO, final ProjectVersionDownloadsDAO downloadsDAO, final ProjectsDAO projectsDAO, final ProjectFiles projectFiles, final FileService fileService, final ReviewService reviewService, final UserService userService) {
        this.dao = dao;
        this.projectVersionsDAO = projectVersionsDAO;
        this.downloadsDAO = downloadsDAO;
        this.projectsDAO = projectsDAO;
        this.projectFiles = projectFiles;
        this.fileService = fileService;
        this.reviewService = reviewService;
        this.userService = userService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        this.jarScannerUser = this.createUser();
        this.rescanProjectVersions();
    }

    @Transactional
    public void rescanProjectVersions() {
        for (final VersionToScan version : this.dao.versionsRequiringScans(this.scanner.version())) {
            this.scan(version, false); // TODO partial parameter
        }
    }

    private UserTable createUser() {
        final UserTable jarScannerUser = this.userService.getUserTable(JAR_SCANNER_USER);
        if (jarScannerUser != null) {
            return jarScannerUser;
        }

        final UserTable userTable = new UserTable(
            -1,
            JAR_SCANNER_USER,
            "JarScanner",
            "automated@test.test",
            List.of(),
            false,
            Locale.ENGLISH.toLanguageTag(),
            "white"
        );
        return this.userService.insertUser(userTable);
    }

    public void scanAsync(final long versionId, final List<Platform> platforms, final boolean partial) {
        EXECUTOR_SERVICE.execute(() -> {
            final ProjectVersionTable pvt = this.projectVersionsDAO.getProjectVersionTable(versionId);
            if (pvt == null) {
                return;
            }

            this.scan(new VersionToScan(pvt.getVersionId(), pvt.getProjectId(), pvt.getReviewState(), pvt.getVersionString(), platforms), partial);
        });
    }

    private void scan(final VersionToScan versionToScan, final boolean partial) {
        Severity highestSeverity = Severity.UNKNOWN;
        for (final Platform platform : versionToScan.platforms()) {
            Severity severity = Severity.HIGH;
            try {
                severity = this.scanPlatform(versionToScan, platform);
            } catch (final IOException ignored) {
            }

            if (severity.compareTo(highestSeverity) < 0) {
                highestSeverity = severity;
            }
        }

        this.applyReview(highestSeverity, versionToScan, partial);
    }

    @Transactional
    void applyReview(final Severity severity, final VersionToScan version, final boolean partial) {
        if (Severity.HIGH.compareTo(severity) < 0) {
            this.reviewService.autoReview(version, partial, this.jarScannerUser.getUserId());
        } else if (severity == Severity.HIGHEST) {
            // TODO: state for requires manual review
        }
    }

    public Severity scanPlatform(final long versionId, final Platform platform) throws IOException {
        final ProjectVersionTable pvt = this.projectVersionsDAO.getProjectVersionTable(versionId);
        if (pvt == null) {
            throw new HangarApiException("Version not found");
        }

        return this.scanPlatform(new VersionToScan(pvt.getVersionId(), pvt.getProjectId(), pvt.getReviewState(), pvt.getVersionString(), List.of(platform)), platform);
    }

    public Severity scanPlatform(final VersionToScan versionToScan, final Platform platform) throws IOException {
        final NamedResource resource = this.getFile(versionToScan, platform);

        final List<ScanResult> scanResults;
        try (final InputStream inputStream = resource.resource().getInputStream()) {
            scanResults = this.scanner.scanJar(inputStream, resource.name());
        }

        // find the highest severity
        Severity highestSeverity = Severity.UNKNOWN;
        for (final ScanResult scanResult : scanResults) {
            for (final Check.CheckResult result : scanResult.results()) {
                if (result.severity().compareTo(highestSeverity) < 0) {
                    highestSeverity = result.severity();
                }
            }
        }

        // this is dum, but I can't be bothered to adjust the model in the scanner to serialize to json properly
        final List<List<String>> formattedResults = new ArrayList<>();
        for (final ScanResult scanResult : scanResults) {
            final List<String> formattedChecks = scanResult.results().stream().map(Check.CheckResult::format).toList();
            formattedResults.add(formattedChecks);
        }

        this.dao.save(new JarScanResultTable(versionToScan.versionId(), this.scanner.version(), platform, highestSeverity.name(), new JSONB(formattedResults)));
        return highestSeverity;
    }

    private NamedResource getFile(final VersionToScan version, final Platform platform) {
        final long versionId = version.versionId();
        final ProjectVersionPlatformDownloadTable platformDownload = this.downloadsDAO.getPlatformDownload(versionId, platform);
        final ProjectVersionDownloadTable download = this.downloadsDAO.getDownload(versionId, platformDownload.getDownloadId());
        if (download.getFileName() == null) {
            throw new HangarApiException("Couldn't find a file for version " + version + " in download " + download);
        }

        final ProjectTable project = this.projectsDAO.getById(version.projectId());
        final String path = this.projectFiles.getVersionDir(project.getOwnerName(), project.getSlug(), version.versionString(), platform, download.getFileName());
        if (!this.fileService.exists(path)) {
            throw new HangarApiException("Couldn't find a file for version " + version.versionString());
        }
        return new NamedResource(this.fileService.getResource(path), download.getFileName());
    }

    public JarScanResultTable getLastResult(final long versionId, final Platform platform) {
        return this.dao.getLastResult(versionId, platform);
    }

    private record NamedResource(Resource resource, String name) {
    }
}
