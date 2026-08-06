package io.papermc.hangar.components.scheduler;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.components.auth.service.AccountDeletionService;
import io.papermc.hangar.components.index.IndexService;
import io.papermc.hangar.components.jobs.JobService;
import io.papermc.hangar.components.jobs.model.ScheduledTaskJob;
import io.papermc.hangar.components.stats.StatService;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

@Service
public class SchedulerService extends HangarComponent implements ApplicationListener<ContextRefreshedEvent> {

    private final SchedulerDAO schedulerDAO;
    private final StatService statService;
    private final IndexService indexService;
    private final JobService jobService;
    private final AccountDeletionService accountDeletionService;

    public SchedulerService(final SchedulerDAO schedulerDAO, final StatService statService, final IndexService indexService, final JobService jobService, final AccountDeletionService accountDeletionService) {
        this.schedulerDAO = schedulerDAO;
        this.statService = statService;
        this.indexService = indexService;
        this.jobService = jobService;
        this.accountDeletionService = accountDeletionService;
    }

    public void runScheduledTask(String taskName) {
        switch (taskName) {
            case "updateVersionDownloads" -> {
                this.statService.processVersionDownloads();
                this.schedulerDAO.refreshVersionStatsView();
            }
            case "updateProjectViews" -> {
                this.statService.processProjectViews();
                this.schedulerDAO.refreshProjectStatsView();
            }
            case "updateProjectIndex" -> this.indexService.fullUpdateProjects();
            case "updateVersionIndex" -> this.indexService.fullUpdateVersions();
            case "refreshHomePage" -> this.schedulerDAO.refreshHomeProjects();
            case "deleteAccounts" -> this.accountDeletionService.deleteDueAccounts();
            default -> throw new RuntimeException("Unknown scheduled task name: " + taskName);
        }
    }

    @Deprecated
    @SuppressWarnings("DeprecatedIsStillUsed")
    public void runAllJobs() {
        this.runScheduledTask("updateVersionDownloads");
        this.runScheduledTask("updateProjectViews");
        this.runScheduledTask("updateProjectIndex");
        this.runScheduledTask("updateVersionIndex");
        this.runScheduledTask("refreshHomePage");
        this.runScheduledTask("deleteAccounts");
    }

    @Override
    public void onApplicationEvent(@NotNull ContextRefreshedEvent event) {
        this.jobService.scheduleIfNotExists(new ScheduledTaskJob("updateVersionDownloads", this.config.updateTasks().versionDownloads().toMillis()));
        this.jobService.scheduleIfNotExists(new ScheduledTaskJob("updateProjectViews", this.config.updateTasks().projectViews().toMillis()));
        this.jobService.scheduleIfNotExists(new ScheduledTaskJob("updateProjectIndex", this.config.updateTasks().projectIndex().toMillis()));
        this.jobService.scheduleIfNotExists(new ScheduledTaskJob("updateVersionIndex", this.config.updateTasks().versionIndex().toMillis()));
        this.jobService.scheduleIfNotExists(new ScheduledTaskJob("refreshHomePage", this.config.updateTasks().homepage().toMillis()));
        this.jobService.scheduleIfNotExists(new ScheduledTaskJob("deleteAccounts", this.config.updateTasks().accountDeletion().toMillis()));
    }
}
