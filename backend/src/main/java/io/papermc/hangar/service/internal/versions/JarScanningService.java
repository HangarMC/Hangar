package io.papermc.hangar.service.internal.versions;

import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectsDAO;
import io.papermc.hangar.db.dao.internal.table.versions.ProjectVersionsDAO;
import io.papermc.hangar.db.dao.internal.table.versions.downloads.ProjectVersionDownloadsDAO;
import io.papermc.hangar.db.dao.internal.versions.JarScanResultDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.Platform;
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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
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

    public JarScanningService(final JarScanResultDAO dao, final ProjectVersionsDAO projectVersionsDAO, final ProjectVersionDownloadsDAO downloadsDAO, final ProjectsDAO projectsDAO, final ProjectFiles projectFiles, final FileService fileService, final ReviewService reviewService) {
        this.dao = dao;
        this.projectVersionsDAO = projectVersionsDAO;
        this.downloadsDAO = downloadsDAO;
        this.projectsDAO = projectsDAO;
        this.projectFiles = projectFiles;
        this.fileService = fileService;
        this.reviewService = reviewService;
    }

    public void scanAsync(final long versionId, final Collection<Platform> platforms) {
        EXECUTOR_SERVICE.execute(() -> {
            Severity highestSeverity = Severity.UNKNOWN;
            for (final Platform platform : platforms) {
                final Severity severity = this.scan(versionId, platform);
                if (highestSeverity.compareTo(severity) < 0) {
                    highestSeverity = severity;
                }
            }

            this.applyReview(highestSeverity, versionId);
        });
    }

    @Transactional
    void applyReview(final Severity severity, final long versionId) {
        if (Severity.HIGH.compareTo(severity) < 0) {
            this.reviewService.autoReview(versionId);
        } else if (severity == Severity.HIGHEST) {
            // TODO: state for requires changes or requires manual review
        }
    }

    public Severity scan(final long versionId, final Platform platform) {
        final Resource resource = this.getFile(versionId, platform);

        final List<ScanResult> scanResults;
        try (final InputStream inputStream = resource.getInputStream()) {
            scanResults = this.scanner.scanJar(inputStream, resource.getFilename());
        } catch (final IOException e) {
            return Severity.UNKNOWN;
        }

        // find the highest severity
        Severity highestSeverity = Severity.UNKNOWN;
        for (final ScanResult scanResult : scanResults) {
            for (final Check.CheckResult result : scanResult.results()) {
                if (highestSeverity.compareTo(result.severity()) < 0) {
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

        this.dao.save(new JarScanResultTable(versionId, platform, highestSeverity.name(), new JSONB(formattedResults)));
        return highestSeverity;
    }

    private Resource getFile(final long versionId, final Platform platform) {
        final ProjectVersionTable pvt = this.projectVersionsDAO.getProjectVersionTable(versionId);
        if (pvt == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }

        final ProjectVersionPlatformDownloadTable platformDownload = this.downloadsDAO.getPlatformDownload(pvt.getVersionId(), platform);
        final ProjectVersionDownloadTable download = this.downloadsDAO.getDownload(platformDownload.getVersionId(), platformDownload.getDownloadId());
        if (download.getFileName() == null) {
            throw new HangarApiException("Couldn't find a file for version " + versionId);
        }

        final ProjectTable project = this.projectsDAO.getById(pvt.getProjectId());
        final String path = this.projectFiles.getVersionDir(project.getOwnerName(), project.getSlug(), pvt.getVersionString(), platform, download.getFileName());
        if (!this.fileService.exists(path)) {
            throw new HangarApiException("Couldn't find a file for version " + versionId);
        }
        return this.fileService.getResource(path);
    }

    public JarScanResultTable getLastResult(final long versionId, final Platform platform) {
        return this.dao.getLastResult(versionId, platform);
    }
}
