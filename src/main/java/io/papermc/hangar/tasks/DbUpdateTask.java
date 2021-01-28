package io.papermc.hangar.tasks;

import io.papermc.hangar.serviceold.StatsService;
import io.papermc.hangar.serviceold.project.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DbUpdateTask {

    private final ProjectService projectService;
    private final StatsService statsService;

    @Autowired
    public DbUpdateTask(ProjectService projectService, StatsService statsService) {
        this.projectService = projectService;
        this.statsService = statsService;
    }

    @Scheduled(fixedRateString = "#{@hangarConfig.homepage.updateInterval.toMillis()}")
    public void refreshHomePage() {
        projectService.refreshHomePage();
    }

    @Scheduled(fixedRateString = "#{@hangarConfig.homepage.updateInterval.toMillis()}")
    public void updateStats() {
        statsService.processProjectViews();
        statsService.processVersionDownloads();
    }
}
