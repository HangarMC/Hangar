package io.papermc.hangar.components.health;

import io.papermc.hangar.components.jobs.JobService;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import org.jspecify.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/health")
class HealthController {

    private final HealthService healthService;
    private final JobService jobService;

    HealthController(final HealthService healthService, final JobService jobService) {
        this.healthService = healthService;
        this.jobService = jobService;
    }

    @PermissionRequired(NamedPermission.VIEW_HEALTH)
    @PostMapping("/queue")
    void queue() {
        this.healthService.queueNewReport();
    }

    @PermissionRequired(NamedPermission.VIEW_HEALTH)
    @GetMapping("/")
    HealthReport.@Nullable FinishedOrPendingHealthReport get() {
        return this.healthService.getReport();
    }

    @PermissionRequired(NamedPermission.VIEW_HEALTH)
    @PostMapping(path = "/retry/{jobId}", produces = MediaType.APPLICATION_JSON_VALUE)
    void retryJob(@PathVariable final long jobId) {
        this.jobService.retryJob(jobId);
    }
}
