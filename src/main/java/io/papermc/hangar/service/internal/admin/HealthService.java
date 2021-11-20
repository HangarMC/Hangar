package io.papermc.hangar.service.internal.admin;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.HealthDAO;
import io.papermc.hangar.model.internal.admin.health.MissingFileCheck;
import io.papermc.hangar.model.internal.admin.health.UnhealthyProject;
import io.papermc.hangar.service.internal.uploads.ProjectFiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HealthService extends HangarComponent {

    private final HealthDAO healthDAO;
    private final ProjectFiles projectFiles;

    @Autowired
    public HealthService(HealthDAO healthDAO, ProjectFiles projectFiles) {
        this.healthDAO = healthDAO;
        this.projectFiles = projectFiles;
    }

    public List<UnhealthyProject> getProjectsWithoutTopic() {
        return healthDAO.getProjectsWithoutTopic();
    }

    public List<UnhealthyProject> getStaleProjects() {
        return healthDAO.getStaleProjects("'" + config.projects.getStaleAge().toSeconds() + " SECONDS'");
    }

    public List<UnhealthyProject> getNonPublicProjects() {
        return healthDAO.getNonPublicProjects();
    }

    public List<MissingFileCheck> getVersionsWithMissingFiles() {
        List<MissingFileCheck> missingFileChecks = healthDAO.getVersionsForMissingFiles();
        return missingFileChecks.stream().filter(mfc -> {
            Path path = projectFiles.getVersionDir(mfc.getNamespace().getOwner(), mfc.getName(), mfc.getVersionString(), mfc.getPlatform());
            return Files.notExists(path.resolve(mfc.getFileName()));
        }).collect(Collectors.toList());
    }
}
