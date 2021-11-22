package io.papermc.hangar.tasks;

import io.papermc.hangar.service.internal.admin.StatService;
import io.papermc.hangar.service.internal.projects.HomeProjectService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DbUpdateTask {

    private final HomeProjectService homeProjectService;
    private final StatService statService;

    @Autowired
    public DbUpdateTask(HomeProjectService projectService, StatService statService) {
        this.homeProjectService = projectService;
        this.statService = statService;
    }

    @Scheduled(fixedRateString = "#{@hangarConfig.homepage.updateInterval.toMillis()}")
    public void refreshHomePage() {
        homeProjectService.refreshHomeProjects();
    }

    @Scheduled(fixedRateString = "#{@hangarConfig.homepage.updateInterval.toMillis()}", initialDelay = 1000)
    public void updateStats() {
        statService.processProjectViews();
        statService.processVersionDownloads();
    }
}
