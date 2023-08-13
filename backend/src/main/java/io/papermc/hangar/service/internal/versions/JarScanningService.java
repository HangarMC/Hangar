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
import io.papermc.hangar.model.db.versions.JarScanResultEntryTable;
import io.papermc.hangar.model.db.versions.JarScanResultTable;
import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import io.papermc.hangar.model.db.versions.downloads.ProjectVersionDownloadTable;
import io.papermc.hangar.model.db.versions.downloads.ProjectVersionPlatformDownloadTable;
import io.papermc.hangar.model.internal.versions.JarScanResult;
import io.papermc.hangar.scanner.HangarJarScanner;
import io.papermc.hangar.scanner.check.Check;
import io.papermc.hangar.scanner.model.ScanResult;
import io.papermc.hangar.scanner.model.Severity;
import io.papermc.hangar.service.internal.file.FileService;
import io.papermc.hangar.service.internal.uploads.ProjectFiles;
import io.papermc.hangar.service.internal.users.UserService;
import io.papermc.hangar.util.ThreadFactory;
import io.sentry.Sentry;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JarScanningService {

    public static final UUID JAR_SCANNER_USER = new UUID(952332837L, -376012533328L);
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool(new ThreadFactory("Scanner", true));
    private static final Logger LOGGER = LoggerFactory.getLogger(JarScanningService.class);
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
        EXECUTOR_SERVICE.execute(this::scanRemainingProjectVersions);
    }

    @Transactional
    public void scanRemainingProjectVersions() {
        // TODO Pass this.scanner.version()
        final List<VersionToScan> versionToScans = this.dao.versionsRequiringScans();
        LOGGER.info("Rescanning " + versionToScans.size() + " versions");
        for (final VersionToScan version : versionToScans) {
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
            "white",
            true,
            new JSONB(Map.of())
        );
        return this.userService.insertUser(userTable);
    }

    public void scanAsync(final ProjectVersionTable pvt, final List<Platform> platforms, final boolean partial) {
        EXECUTOR_SERVICE.execute(() -> this.scan(new VersionToScan(pvt.getVersionId(), pvt.getProjectId(), pvt.getReviewState(), pvt.getVersionString(), platforms), partial));
    }

    private void scan(final VersionToScan versionToScan, final boolean partial) {
        Severity highestSeverity = Severity.UNKNOWN;
        for (final Platform platform : versionToScan.platforms()) {
            Severity severity = Severity.HIGH;
            try {
                severity = this.scanPlatform(versionToScan, platform);
            } catch (final Exception e) {
                e.printStackTrace();
                Sentry.captureException(e);
            }

            if (severity.compareTo(highestSeverity) < 0) {
                highestSeverity = severity;
            }
        }

        this.applyReview(highestSeverity, versionToScan, partial);
    }

    private void applyReview(final Severity severity, final VersionToScan version, final boolean partial) {
        if (Severity.HIGH.compareTo(severity) < 0) {
            this.reviewService.autoReviewFiles(version, partial, this.jarScannerUser.getUserId());
        } else if (severity == Severity.HIGHEST) {
            // TODO: state for requires manual review
        }
    }

    @Transactional
    public void scanPlatform(final long versionId, final Platform platform) throws IOException {
        final ProjectVersionTable pvt = this.projectVersionsDAO.getProjectVersionTable(versionId);
        if (pvt == null) {
            throw new HangarApiException("Version not found");
        }

        this.scanPlatform(new VersionToScan(pvt.getVersionId(), pvt.getProjectId(), pvt.getReviewState(), pvt.getVersionString(), List.of(platform)), platform);
    }

    public Severity scanPlatform(final VersionToScan versionToScan, final Platform platform) throws IOException {
        final NamedResource resource = this.getFile(versionToScan, platform);

        final ScanResult scanResult;
        try (final InputStream inputStream = resource.resource().getInputStream()) {
            scanResult = this.scanner.scanJar(inputStream, resource.name());
        }

        final JarScanResultTable table = this.dao.save(new JarScanResultTable(versionToScan.versionId(), this.scanner.version(), platform, scanResult.highestSeverity().name()));
        for (final Check.CheckResult result : scanResult.results()) {
            this.dao.save(new JarScanResultEntryTable(table.getId(), result.location(), result.message(), result.severity().name()));
        }
        return scanResult.highestSeverity();
    }

    private NamedResource getFile(final VersionToScan version, final Platform platform) {
        final long versionId = version.versionId();
        final ProjectVersionPlatformDownloadTable platformDownload = this.downloadsDAO.getPlatformDownload(versionId, platform);
        // TODO Why can platformDownload be null at this stage
        if (platformDownload == null) {
            throw new RuntimeException("Couldn't find a download for version " + version + " in platform " + platform);
        }

        final ProjectVersionDownloadTable download = this.downloadsDAO.getDownload(versionId, platformDownload.getDownloadId());
        if (download.getFileName() == null) {
            throw new RuntimeException("Couldn't find a file for version " + version + " in download " + download);
        }

        final ProjectTable project = this.projectsDAO.getById(version.projectId());
        final String path = this.projectFiles.getVersionDir(project.getOwnerName(), project.getSlug(), version.versionString(), platform, download.getFileName());
        if (!this.fileService.exists(path)) {
            throw new RuntimeException("Couldn't find a file for version " + version + " in path " + path);
        }
        return new NamedResource(this.fileService.getResource(path), download.getFileName());
    }

    public @Nullable JarScanResult getLastResult(final long versionId, final Platform platform) {
        final JarScanResultTable lastResult = this.dao.getLastResult(versionId, platform);
        if (lastResult == null) {
            return null;
        }

        final List<String> entries = this.dao.getEntries(lastResult.getId()).stream().sorted(Comparator.comparing(s -> Severity.valueOf(s.getSeverity()))).map(this::format).toList();
        return new JarScanResult(lastResult.getId(), platform, lastResult.getCreatedAt(), lastResult.getHighestSeverity(), entries);
    }

    private String format(final JarScanResultEntryTable entry) {
        return "[" + entry.getSeverity() + "]: " + entry.getMessage() + " at " + entry.getLocation();
    }

    public UserTable getJarScannerUser() {
        return this.jarScannerUser;
    }

    private record NamedResource(Resource resource, String name) {
    }
}
