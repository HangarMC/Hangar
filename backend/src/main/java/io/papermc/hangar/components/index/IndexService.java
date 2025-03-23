package io.papermc.hangar.components.index;

import io.papermc.hangar.model.api.project.Project;
import io.papermc.hangar.model.api.project.version.Version;
import io.sentry.spring.jakarta.tracing.SentryTransaction;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class IndexService {

    private final IndexDAO indexDAO;
    private final MeiliService meiliService;

    public IndexService(final IndexDAO indexDAO, final MeiliService meiliService) {
        this.indexDAO = indexDAO;
        this.meiliService = meiliService;
    }

    @SentryTransaction(operation = "task", name = "IndexService#fullUpdateProjects")
    @Scheduled(fixedRateString = "#{@hangarConfig.updateTasks.homepage.toMillis()}", initialDelay = 5_000)
    public void fullUpdateProjects() {
        // TODO index switch instead of full update?
        List<Project> projects = this.indexDAO.getAllProjects();
        this.meiliService.sendProjects(projects);
    }

    @SentryTransaction(operation = "task", name = "IndexService#fullUpdateVersions")
    @Scheduled(fixedRateString = "#{@hangarConfig.updateTasks.homepage.toMillis()}", initialDelay = 5_000)
    public void fullUpdateVersions() {
        // TODO index switch instead of full update?
        List<Version> versions = this.indexDAO.getAllVersions();
        this.meiliService.sendVersions(versions);
    }
}
