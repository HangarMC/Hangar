package io.papermc.hangar.model.internal.api.responses;

import io.papermc.hangar.model.db.JobTable;
import io.papermc.hangar.model.internal.admin.health.MissingFileCheck;
import io.papermc.hangar.model.internal.admin.health.UnhealthyProject;
import java.util.List;

public record HealthReport(List<UnhealthyProject> staleProjects,
                           List<MissingFileCheck> missingFiles,
                           List<JobTable> erroredJobs) {
}
