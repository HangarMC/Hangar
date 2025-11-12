package io.papermc.hangar.components.health;

import io.papermc.hangar.components.jobs.db.JobTable;
import io.papermc.hangar.model.internal.admin.health.MissingFileCheck;
import io.papermc.hangar.model.internal.admin.health.UnhealthyProject;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

record HealthReport(List<UnhealthyProject> staleProjects,
                    List<MissingFileCheck> missingFiles,
                    List<UnhealthyProject> nonPublicProjects,
                    List<JobTable> erroredJobs,
                    OffsetDateTime generatedAt) {

    HealthReport(final List<UnhealthyProject> staleProjects,
                 final List<MissingFileCheck> missingFiles,
                 final List<UnhealthyProject> nonPublicProjects,
                 final List<JobTable> erroredJobs,
                 final OffsetDateTime generatedAt) {
        this.generatedAt = generatedAt;
        this.staleProjects = staleProjects;
        this.missingFiles = missingFiles;
        this.nonPublicProjects = nonPublicProjects;
        this.erroredJobs = Objects.requireNonNullElseGet(erroredJobs, ArrayList::new);
    }

    record PendingHealthReport(String queuedBy, String queuedAt, String status) {
    }


    record FinishedOrPendingHealthReport(@Nullable HealthReport finished, @Nullable PendingHealthReport pending) {}
}
