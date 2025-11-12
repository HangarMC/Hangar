package io.papermc.hangar.components.health;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.components.jobs.JobService;
import io.papermc.hangar.components.jobs.db.JobTable;
import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.internal.admin.health.MissingFileCheck;
import io.papermc.hangar.model.internal.admin.health.UnhealthyProject;
import io.papermc.hangar.service.internal.file.FileService;
import io.papermc.hangar.service.internal.uploads.ProjectFiles;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

@Service
class HealthService extends HangarComponent {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(HealthService.class);

    private final HealthDAO healthDAO;
    private final ProjectFiles projectFiles;
    private final FileService fileService;
    private final TaskExecutor taskExecutor;
    private final JobService jobService;

    @Autowired
    HealthService(final HealthDAO healthDAO, final ProjectFiles projectFiles, final FileService fileService, final TaskExecutor taskExecutor, final JobService jobService) {
        this.healthDAO = healthDAO;
        this.projectFiles = projectFiles;
        this.fileService = fileService;
        this.taskExecutor = taskExecutor;
        this.jobService = jobService;
    }

    private List<UnhealthyProject> getStaleProjects() {
        return this.healthDAO.getStaleProjects("'" + this.config.projects().staleAge().toSeconds() + " SECONDS'");
    }

    private List<UnhealthyProject> getNonPublicProjects() {
        return this.healthDAO.getNonPublicProjects();
    }

    private List<MissingFileCheck> getVersionsWithMissingFiles(final HealthReportTable healthReport) {
        final List<MissingFileCheck> missingFileChecks = this.healthDAO.getVersionsForMissingFiles();
        final AtomicInteger counter = new AtomicInteger();
        return missingFileChecks.stream().filter(missingFileCheck -> {
            if (counter.getAndIncrement() % 10 == 0) {
                healthReport.setStatus("checking missing files (" + counter.get() + "/" + missingFileChecks.size() + ")");
                this.healthDAO.updateHealthReport(healthReport);
            }
            final List<Platform> platforms = missingFileCheck.platforms();
            final List<Platform> missingFilePlatforms = new ArrayList<>();
            final List<String> fileNames = missingFileCheck.fileNames();
            for (int i = 0; i < platforms.size(); i++) {
                final Platform platform = platforms.get(i);
                final String path = this.projectFiles.getVersionDir(missingFileCheck.namespace().getOwner(), missingFileCheck.namespace().getSlug(), missingFileCheck.versionString(), platform);
                if (!this.fileService.exists(this.fileService.resolve(path, fileNames.get(i)))) {
                    missingFilePlatforms.add(platform);
                }
            }

            if (!missingFilePlatforms.isEmpty()) {
                platforms.retainAll(missingFilePlatforms);
                return true;
            }
            return false;
        }).toList();
    }

    private void createNewReport(final HealthReportTable healthReport) {
        healthReport.setStatus("checking stale projects");
        this.healthDAO.updateHealthReport(healthReport);

        final List<UnhealthyProject> staleProjects = this.getStaleProjects();
        healthReport.setStatus("checking non public projects");
        this.healthDAO.updateHealthReport(healthReport);

        final List<UnhealthyProject> nonPublicProjects = this.getNonPublicProjects();
        healthReport.setStatus("checking missing files");
        this.healthDAO.updateHealthReport(healthReport);

        final List<MissingFileCheck> missingFiles = this.getVersionsWithMissingFiles(healthReport);
        healthReport.setFinishedAt(OffsetDateTime.now());
        healthReport.setReport(new JSONB(new HealthReport(staleProjects, missingFiles, nonPublicProjects, null, OffsetDateTime.now())));
        healthReport.setStatus("done");
        this.healthDAO.updateHealthReport(healthReport);
    }

    void queueNewReport() {
        final HealthReportTable healthReportTable = this.healthDAO.insertHealthReport(new HealthReportTable(this.getHangarPrincipal().getName()));
        this.taskExecutor.execute(() -> {
            try {
                this.createNewReport(healthReportTable);
            } catch (final Exception ex) {
                log.warn("Error while creating health report", ex);
            }
        });
    }

    HealthReport.@Nullable FinishedOrPendingHealthReport getReport() {
        final HealthReportTable healthReportTable = this.healthDAO.getHealthReport();
        if (healthReportTable == null) {
            return null;
        }

        if (healthReportTable.getFinishedAt() != null) {
            final HealthReport healthReport = healthReportTable.getReport().get(HealthReport.class);
            final List<JobTable> erroredJobs = this.jobService.getErroredJobs();
            healthReport.erroredJobs().addAll(erroredJobs);
            return new HealthReport.FinishedOrPendingHealthReport(healthReport, null);
        } else {
            final var pending = new HealthReport.PendingHealthReport(healthReportTable.getQueuedBy(), healthReportTable.getQueuedAt().toString(), healthReportTable.getStatus());
            return new HealthReport.FinishedOrPendingHealthReport(null, pending);
        }
    }
}
