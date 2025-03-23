package io.papermc.hangar.tasks;

import io.papermc.hangar.db.dao.internal.versions.HangarVersionsDAO;
import io.papermc.hangar.service.internal.admin.StatService;
import io.papermc.hangar.service.internal.projects.ProjectService;
import io.sentry.spring.jakarta.tracing.SentryTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DbUpdateTask {

    private final ProjectService projectService;
    private final StatService statService;
    private final HangarVersionsDAO hangarVersionsDAO;

    @Autowired
    public DbUpdateTask(final ProjectService projectService, final StatService statService, final HangarVersionsDAO hangarVersionsDAO) {
        this.projectService = projectService;
        this.statService = statService;
        this.hangarVersionsDAO = hangarVersionsDAO;
    }

    @SentryTransaction(operation = "task", name = "DbUpdateTask#refreshHomePage")
    @Scheduled(fixedRateString = "#{@hangarConfig.updateTasks.homepage.toMillis()}", initialDelay = 5_000)
    public void refreshHomePage() {
        this.projectService.refreshHomeProjects();
    }

    @SentryTransaction(operation = "task", name = "DbUpdateTask#refreshVersionView")
    @Scheduled(fixedRateString = "#{@hangarConfig.updateTasks.homepage.toMillis()}", initialDelay = 5_000)
    public void refreshVersionView() {
        this.hangarVersionsDAO.refreshVersionView();
    }

    @SentryTransaction(operation = "task", name = "DbUpdateTask#updateVersionDownloads")
    @Scheduled(fixedRateString = "#{@hangarConfig.updateTasks.versionDownloads.toMillis()}", initialDelay = 1000)
    public void updateVersionDownloads() {
        this.statService.processVersionDownloads();
    }

    @SentryTransaction(operation = "task", name = "DbUpdateTask#updateProjectViews")
    @Scheduled(fixedRateString = "#{@hangarConfig.updateTasks.projectViews.toMillis()}", initialDelay = 1000)
    public void updateProjectViews() {
        this.statService.processProjectViews();
    }
}
