package io.papermc.hangar.service.internal.admin;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.HealthDAO;
import io.papermc.hangar.model.internal.admin.health.MissingFileCheck;
import io.papermc.hangar.model.internal.admin.health.UnhealthyProject;
import io.papermc.hangar.service.internal.file.FileService;
import io.papermc.hangar.service.internal.uploads.ProjectFiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HealthService extends HangarComponent {

    private final HealthDAO healthDAO;
    private final ProjectFiles projectFiles;
    private final FileService fileService;

    @Autowired
    public HealthService(HealthDAO healthDAO, ProjectFiles projectFiles, FileService fileService) {
        this.healthDAO = healthDAO;
        this.projectFiles = projectFiles;
        this.fileService = fileService;
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
            String path = projectFiles.getVersionDir(mfc.getNamespace().getOwner(), mfc.getName(), mfc.getVersionString(), mfc.getPlatform());
            return !fileService.exists(fileService.resolve(path, mfc.getFileName()));
        }).toList();
    }
}
