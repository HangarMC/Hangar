package io.papermc.hangar.tasks;

import io.papermc.hangar.service.internal.admin.StatService;
import io.papermc.hangar.service.internal.projects.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DbUpdateTask {

    private final ProjectService projectService;
    private final StatService statService;

    @Autowired
    public DbUpdateTask(ProjectService projectService, StatService statService) {
        this.projectService = projectService;
        this.statService = statService;
    }

    @Scheduled(fixedRateString = "${hangar.homepage.update-interval}")
    public void refreshHomePage() {
        projectService.refreshHomeProjects();
    }

    @Scheduled(fixedRateString = "${hangar.homepage.update-interval}", initialDelay = 1000)
    public void updateStats() {
        statService.processProjectViews();
        statService.processVersionDownloads();
    }
}
